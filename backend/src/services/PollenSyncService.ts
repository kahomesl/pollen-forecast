import {
  type CompletePollenSyncRunInput,
  type PollenSyncRun,
  type PollenSyncRunStatus,
  type PollenSyncTrigger,
} from "../domain/pollenSyncRun";
import { LOCATION_DEFINITIONS, type LocationDefinition, type LocationId } from "../domain/location";
import type { PollenObservation } from "../domain/pollenObservation";
import type { PollenProvider, PollenProviderCapability } from "../providers/PollenProvider";
import { ProviderRequestError, type ProviderRequestErrorType } from "../providers/providerRequest";
import type { SyncRunRepository } from "../repositories/SyncRunRepository";
import type { ObservationStore } from "./ObservationStore";

const DEFAULT_CONCURRENCY = 3;
const MAX_CONCURRENCY = 10;

export type PollenSyncErrorType = ProviderRequestErrorType | "UNKNOWN";

export interface PollenSyncProviderError {
  readonly providerId: string;
  readonly locationId: LocationId;
  readonly errorType: PollenSyncErrorType;
}

export interface PollenSyncResult {
  readonly runId: number;
  readonly status: Exclude<PollenSyncRunStatus, "RUNNING">;
  readonly startedAt: Date;
  readonly finishedAt: Date;
  readonly locationsAttempted: number;
  readonly locationsSucceeded: number;
  readonly locationsFailed: number;
  readonly observationsReceived: number;
  readonly observationsPersisted: number;
  readonly providerErrors: readonly PollenSyncProviderError[];
}

export interface PollenSyncServiceOptions {
  readonly providers: readonly PollenProvider[];
  readonly locations?: readonly LocationDefinition[];
  readonly observationStore: Pick<ObservationStore, "persistAndCount">;
  readonly syncRunRepository: Pick<SyncRunRepository, "startRun" | "completeRun">;
  readonly concurrency?: number;
  readonly now?: () => Date;
  readonly delay?: (milliseconds: number) => Promise<void>;
  readonly logger?: Pick<SyncLogger, "info">;
}

export interface SyncLogger {
  info(event: Record<string, string | number | boolean>): void;
}

interface SyncTask {
  readonly provider: PollenProvider;
  readonly location: LocationDefinition;
  readonly fetches: readonly (() => Promise<PollenObservation[]>)[];
}

interface SyncTaskResult {
  readonly observationsReceived: number;
  readonly observationsPersisted: number;
  readonly errors: readonly PollenSyncProviderError[];
}

export class PollenSyncService {
  private readonly locations: readonly LocationDefinition[];
  private readonly concurrency: number;
  private readonly now: () => Date;
  private readonly delay: (milliseconds: number) => Promise<void>;
  private readonly logger?: Pick<SyncLogger, "info">;

  constructor(private readonly options: PollenSyncServiceOptions) {
    this.locations = options.locations ?? LOCATION_DEFINITIONS;
    this.concurrency = normalizeConcurrency(options.concurrency ?? getPollenSyncConcurrency());
    this.now = options.now ?? (() => new Date());
    this.delay = options.delay ?? ((milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds)));
    this.logger = options.logger;
  }

  async runPollenSync(trigger: PollenSyncTrigger = "MANUAL"): Promise<PollenSyncResult> {
    const startedAt = this.now();
    const run = await this.options.syncRunRepository.startRun(trigger, startedAt);
    const tasks = this.createTasks();
    const results = await runWithConcurrency(tasks, this.concurrency, (task) => this.syncTask(task));
    const finishedAt = this.now();
    const providerErrors = results.flatMap((result) => result.errors);
    const locationsFailed = results.filter((result) => result.errors.length > 0).length;
    const locationsSucceeded = results.length - locationsFailed;
    const status = determineStatus(locationsSucceeded, locationsFailed);
    const completion: CompletePollenSyncRunInput = {
      status,
      finishedAt,
      locationsAttempted: results.length,
      locationsSucceeded,
      locationsFailed,
      observationsReceived: results.reduce((total, result) => total + result.observationsReceived, 0),
      observationsPersisted: results.reduce((total, result) => total + result.observationsPersisted, 0),
    };
    await this.options.syncRunRepository.completeRun(run.id, completion);

    return {
      runId: run.id,
      startedAt: run.startedAt,
      ...completion,
      providerErrors,
    };
  }

  private createTasks(): SyncTask[] {
    return this.options.providers.flatMap((provider) => this.locations.flatMap((location) => {
      if (!provider.supportsLocation(location.id)) return [];

      const fetches = createProviderFetches(provider, location.id);
      return fetches.length > 0 ? [{ provider, location, fetches }] : [];
    }));
  }

  private async syncTask(task: SyncTask): Promise<SyncTaskResult> {
    const startedAt = this.now();
    const observations: PollenObservation[] = [];
    const errors: PollenSyncProviderError[] = [];

    for (const fetchObservations of task.fetches) {
      const result = await this.fetchWithRetry(fetchObservations);
      if ("errorType" in result) {
        errors.push({ providerId: task.provider.id, locationId: task.location.id, errorType: result.errorType });
      } else {
        observations.push(...result.observations);
      }
    }

    const observationsPersisted = observations.length > 0
      ? await this.options.observationStore.persistAndCount(observations)
      : 0;
    if (observations.length > 0 && observationsPersisted !== observations.length) {
      errors.push({ providerId: task.provider.id, locationId: task.location.id, errorType: "UNKNOWN" });
    }

    this.logger?.info({
      event: "provider_sync_completed",
      providerId: task.provider.id,
      locationId: task.location.id,
      status: errors.length === 0 ? "SUCCESS" : "ERROR",
      durationMs: Math.max(0, this.now().getTime() - startedAt.getTime()),
      timeout: errors.some((error) => error.errorType === "TIMEOUT"),
      observationsReceived: observations.length,
      observationsPersisted,
    });

    return {
      observationsReceived: observations.length,
      observationsPersisted,
      errors,
    };
  }

  private async fetchWithRetry(
    fetchObservations: () => Promise<PollenObservation[]>,
  ): Promise<{ observations: readonly PollenObservation[] } | { errorType: PollenSyncErrorType }> {
    for (let attempt = 0; attempt < 2; attempt += 1) {
      try {
        return { observations: await fetchObservations() };
      } catch (error) {
        const errorType = classifyError(error);
        if (attempt === 0 && errorType !== "UNKNOWN") {
          await this.delay(100);
          continue;
        }
        return { errorType };
      }
    }

    return { errorType: "UNKNOWN" };
  }
}

export function getPollenSyncConcurrency(value = process.env.POLLEN_SYNC_CONCURRENCY): number {
  if (typeof value !== "string" || !/^\d+$/.test(value)) return DEFAULT_CONCURRENCY;
  const concurrency = Number(value);
  return Number.isInteger(concurrency) && concurrency >= 1
    ? Math.min(concurrency, MAX_CONCURRENCY)
    : DEFAULT_CONCURRENCY;
}

function createProviderFetches(provider: PollenProvider, locationId: LocationId): Array<() => Promise<PollenObservation[]>> {
  const fetches: Array<() => Promise<PollenObservation[]>> = [];
  if (hasAnyCapability(provider, ["TOTAL_CURRENT", "TOTAL_OBSERVATION"]) && provider.fetchCurrent) {
    fetches.push(() => provider.fetchCurrent!({ locationId }));
  }
  if (hasAnyCapability(provider, ["TOTAL_FORECAST", "CATEGORY_FORECAST"]) && provider.fetchForecast) {
    fetches.push(() => provider.fetchForecast!({ locationId }));
  }
  for (const taxonCode of provider.supportedTaxa) {
    if (hasAnyCapability(provider, ["GENUS_CURRENT", "GENUS_OBSERVATION"]) && provider.fetchCurrent) {
      fetches.push(() => provider.fetchCurrent!({ locationId, taxonCode }));
    }
    if (hasAnyCapability(provider, ["GENUS_FORECAST"]) && provider.fetchForecast) {
      fetches.push(() => provider.fetchForecast!({ locationId, taxonCode }));
    }
  }
  return fetches;
}

function hasAnyCapability(provider: PollenProvider, capabilities: readonly PollenProviderCapability[]): boolean {
  return capabilities.some((capability) => provider.capabilities.includes(capability));
}

function classifyError(error: unknown): PollenSyncErrorType {
  if (error instanceof ProviderRequestError) return error.errorType;
  return error instanceof TypeError ? "NETWORK" : "UNKNOWN";
}

function determineStatus(locationsSucceeded: number, locationsFailed: number): Exclude<PollenSyncRunStatus, "RUNNING"> {
  if (locationsFailed === 0) return "SUCCESS";
  return locationsSucceeded === 0 ? "FAILED" : "PARTIAL";
}

function normalizeConcurrency(concurrency: number): number {
  return Number.isInteger(concurrency) && concurrency >= 1
    ? Math.min(concurrency, MAX_CONCURRENCY)
    : DEFAULT_CONCURRENCY;
}

async function runWithConcurrency<T, TResult>(
  values: readonly T[],
  concurrency: number,
  run: (value: T) => Promise<TResult>,
): Promise<TResult[]> {
  const results: TResult[] = new Array(values.length);
  let nextIndex = 0;
  const workers = Array.from({ length: Math.min(concurrency, values.length) }, async () => {
    while (nextIndex < values.length) {
      const index = nextIndex;
      nextIndex += 1;
      results[index] = await run(values[index]!);
    }
  });
  await Promise.all(workers);
  return results;
}
