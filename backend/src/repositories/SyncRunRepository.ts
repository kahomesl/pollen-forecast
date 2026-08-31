import type {
  CompletePollenSyncRunInput,
  PollenSyncRun,
  PollenSyncRunStatus,
  PollenSyncTrigger,
} from "../domain/pollenSyncRun";

export interface PollenSyncRunRow {
  readonly id: number | string;
  readonly started_at: Date | string;
  readonly finished_at: Date | string | null;
  readonly status: string;
  readonly trigger: string;
  readonly locations_attempted: number;
  readonly locations_succeeded: number;
  readonly locations_failed: number;
  readonly observations_received: number;
  readonly observations_persisted: number;
  readonly created_at: Date | string;
}

export interface PollenSyncRunSql {
  (strings: TemplateStringsArray, ...values: readonly unknown[]): Promise<readonly PollenSyncRunRow[]>;
}

export class SyncRunRepository {
  constructor(private readonly sql: PollenSyncRunSql) {}

  async startRun(trigger: PollenSyncTrigger, startedAt: Date): Promise<PollenSyncRun> {
    const rows = await this.sql`
      INSERT INTO pollen_sync_runs (started_at, status, trigger)
      VALUES (${startedAt}, 'RUNNING', ${trigger})
      RETURNING *
    `;
    return requireRun(rows[0], "Sync run insert returned no row");
  }

  async completeRun(id: number, input: CompletePollenSyncRunInput): Promise<PollenSyncRun> {
    const rows = await this.sql`
      UPDATE pollen_sync_runs
      SET finished_at = ${input.finishedAt},
          status = ${input.status},
          locations_attempted = ${input.locationsAttempted},
          locations_succeeded = ${input.locationsSucceeded},
          locations_failed = ${input.locationsFailed},
          observations_received = ${input.observationsReceived},
          observations_persisted = ${input.observationsPersisted}
      WHERE id = ${id}
      RETURNING *
    `;
    return requireRun(rows[0], "Sync run completion returned no row");
  }

  async getLatestRun(): Promise<PollenSyncRun | null> {
    const rows = await this.sql`
      SELECT * FROM pollen_sync_runs
      ORDER BY started_at DESC
      LIMIT 1
    `;
    return rows[0] ? rowToPollenSyncRun(rows[0]) : null;
  }
}

export function rowToPollenSyncRun(row: PollenSyncRunRow): PollenSyncRun {
  return {
    id: Number(row.id),
    startedAt: toDate(row.started_at),
    ...(row.finished_at ? { finishedAt: toDate(row.finished_at) } : {}),
    status: row.status as PollenSyncRunStatus,
    trigger: row.trigger as PollenSyncTrigger,
    locationsAttempted: row.locations_attempted,
    locationsSucceeded: row.locations_succeeded,
    locationsFailed: row.locations_failed,
    observationsReceived: row.observations_received,
    observationsPersisted: row.observations_persisted,
    createdAt: toDate(row.created_at),
  };
}

function requireRun(row: PollenSyncRunRow | undefined, message: string): PollenSyncRun {
  if (!row) throw new Error(message);
  return rowToPollenSyncRun(row);
}

function toDate(value: Date | string): Date {
  return value instanceof Date ? value : new Date(value);
}
