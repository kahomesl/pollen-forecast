export interface SqlExecutor {
  (strings: TemplateStringsArray, ...values: readonly unknown[]): Promise<readonly unknown[]>;
}

export async function initializePollenObservationSchema(sql: SqlExecutor): Promise<void> {
  await sql`
    CREATE TABLE IF NOT EXISTS pollen_observations (
      id TEXT PRIMARY KEY,
      location_id TEXT NOT NULL,
      station_id TEXT NULL,
      taxon_code TEXT NULL,
      taxon_name_cn TEXT NULL,
      taxon_name_en TEXT NULL,
      scope TEXT NOT NULL CHECK (scope IN ('TOTAL', 'CATEGORY', 'FAMILY', 'GENUS', 'SPECIES')),
      measurement_type TEXT NOT NULL CHECK (measurement_type IN ('OBSERVATION', 'CURRENT', 'FORECAST', 'ESTIMATE')),
      value DOUBLE PRECISION NULL,
      min_value DOUBLE PRECISION NULL,
      max_value DOUBLE PRECISION NULL,
      unit TEXT NOT NULL CHECK (unit IN ('grains/m3', 'grains/1000mm2', 'index', 'level', 'unknown')),
      risk_level DOUBLE PRECISION NULL,
      risk_label TEXT NULL,
      provider TEXT NOT NULL,
      source_name TEXT NOT NULL,
      source_url TEXT NULL,
      confidence SMALLINT NOT NULL CHECK (confidence >= 1 AND confidence <= 5),
      observed_at TIMESTAMPTZ NULL,
      valid_from TIMESTAMPTZ NULL,
      valid_to TIMESTAMPTZ NULL,
      created_at TIMESTAMPTZ NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL,
      stored_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    )
  `;

  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_observations_location_taxon
    ON pollen_observations (location_id, taxon_code)
  `;
  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_observations_location_measurement
    ON pollen_observations (location_id, measurement_type)
  `;
  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_observations_provider
    ON pollen_observations (provider)
  `;
  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_observations_valid_from
    ON pollen_observations (valid_from)
  `;
  await sql`
    CREATE INDEX IF NOT EXISTS idx_pollen_observations_observed_at
    ON pollen_observations (observed_at)
  `;
}
