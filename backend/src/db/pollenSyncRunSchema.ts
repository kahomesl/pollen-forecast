export interface SyncRunSqlExecutor {
  (strings: TemplateStringsArray, ...values: readonly unknown[]): Promise<readonly unknown[]>;
}

export async function initializePollenSyncRunSchema(sql: SyncRunSqlExecutor): Promise<void> {
  await sql`
    CREATE TABLE IF NOT EXISTS pollen_sync_runs (
      id BIGSERIAL PRIMARY KEY,
      started_at TIMESTAMPTZ NOT NULL,
      finished_at TIMESTAMPTZ NULL,
      status TEXT NOT NULL CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL', 'FAILED')),
      trigger TEXT NOT NULL CHECK (trigger IN ('MANUAL', 'SCHEDULED', 'STARTUP')),
      locations_attempted INTEGER NOT NULL DEFAULT 0,
      locations_succeeded INTEGER NOT NULL DEFAULT 0,
      locations_failed INTEGER NOT NULL DEFAULT 0,
      observations_received INTEGER NOT NULL DEFAULT 0,
      observations_persisted INTEGER NOT NULL DEFAULT 0,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `;

  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_sync_runs_started_at
    ON pollen_sync_runs (started_at DESC)
  `;
}
