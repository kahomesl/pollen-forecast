import { expect, test } from "bun:test";

import { initializePollenObservationSchema, type SqlExecutor } from "./pollenObservationSchema";

test("creates additive pollen observation schema with semantic checks and query indexes", async () => {
  const statements: string[] = [];
  const sql: SqlExecutor = async (strings) => {
    statements.push(strings.join("?"));
    return [];
  };

  await initializePollenObservationSchema(sql);

  const schema = statements.join("\n");
  expect(schema).toContain("CREATE TABLE IF NOT EXISTS pollen_observations");
  expect(schema).toContain("CHECK (scope IN ('TOTAL', 'CATEGORY', 'FAMILY', 'GENUS', 'SPECIES'))");
  expect(schema).toContain("CHECK (measurement_type IN ('OBSERVATION', 'CURRENT', 'FORECAST', 'ESTIMATE'))");
  expect(schema).toContain("CHECK (unit IN ('grains/m3', 'grains/1000mm2', 'index', 'level', 'unknown'))");
  expect(schema).toContain("CHECK (confidence >= 1 AND confidence <= 5)");
  expect(schema).toContain("idx_pollen_observations_location_taxon");
  expect(schema).toContain("idx_pollen_observations_location_measurement");
  expect(schema).toContain("idx_pollen_observations_provider");
  expect(schema).toContain("idx_pollen_observations_valid_from");
  expect(schema).toContain("idx_pollen_observations_observed_at");
});
