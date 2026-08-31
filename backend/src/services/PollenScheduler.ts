import type { PollenSyncService } from "./PollenSyncService";

const DEFAULT_INTERVAL_MINUTES = 60;
const MINIMUM_INTERVAL_MINUTES = 15;

export interface PollenSchedulerOptions {
  readonly syncService: Pick<PollenSyncService, "runPollenSync">;
  readonly enabled?: boolean;
  readonly intervalMinutes?: number;
  readonly setIntervalImpl?: (callback: () => void, milliseconds: number) => ReturnType<typeof setInterval>;
  readonly clearIntervalImpl?: (timer: ReturnType<typeof setInterval>) => void;
  readonly logger?: Pick<Console, "error">;
}

export class PollenScheduler {
  private readonly enabled: boolean;
  private readonly intervalMinutes: number;
  private readonly setIntervalImpl: (callback: () => void, milliseconds: number) => ReturnType<typeof setInterval>;
  private readonly clearIntervalImpl: (timer: ReturnType<typeof setInterval>) => void;
  private readonly logger: Pick<Console, "error">;
  private timer: ReturnType<typeof setInterval> | undefined;
  private isRunning = false;

  constructor(options: PollenSchedulerOptions) {
    this.enabled = options.enabled ?? isBackgroundSyncEnabled();
    this.intervalMinutes = options.intervalMinutes === undefined
      ? getPollenSyncIntervalMinutes()
      : normalizeIntervalMinutes(options.intervalMinutes);
    this.setIntervalImpl = options.setIntervalImpl ?? setInterval;
    this.clearIntervalImpl = options.clearIntervalImpl ?? clearInterval;
    this.logger = options.logger ?? console;
    this.syncService = options.syncService;
  }

  private readonly syncService: Pick<PollenSyncService, "runPollenSync">;

  start(): boolean {
    if (!this.enabled || this.timer !== undefined) return false;

    this.timer = this.setIntervalImpl(() => {
      void this.runIfIdle();
    }, this.intervalMinutes * 60_000);
    return true;
  }

  stop(): void {
    if (this.timer === undefined) return;
    this.clearIntervalImpl(this.timer);
    this.timer = undefined;
  }

  async runIfIdle(): Promise<boolean> {
    if (this.isRunning) return false;

    this.isRunning = true;
    try {
      await this.syncService.runPollenSync("SCHEDULED");
      return true;
    } catch {
      this.logger.error("Pollen scheduled sync failed.");
      return false;
    } finally {
      this.isRunning = false;
    }
  }
}

export function isBackgroundSyncEnabled(value = process.env.POLLEN_BACKGROUND_SYNC_ENABLED): boolean {
  return value === "true";
}

export function getPollenSyncIntervalMinutes(value = process.env.POLLEN_SYNC_INTERVAL_MINUTES): number {
  if (typeof value !== "string" || !/^\d+$/.test(value)) return DEFAULT_INTERVAL_MINUTES;
  const minutes = Number(value);
  if (!Number.isInteger(minutes) || minutes < 1) return DEFAULT_INTERVAL_MINUTES;
  return Math.max(minutes, MINIMUM_INTERVAL_MINUTES);
}

function normalizeIntervalMinutes(minutes: number): number {
  return Number.isInteger(minutes) && minutes >= 1
    ? Math.max(minutes, MINIMUM_INTERVAL_MINUTES)
    : DEFAULT_INTERVAL_MINUTES;
}
