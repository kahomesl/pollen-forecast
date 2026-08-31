import { expect, test } from "bun:test";

import { initializePollenSyncRunSchema, type SyncRunSqlExecutor } from "./pollenSyncRunSchema";

test("creates additive sync-run storage without sensitive error columns", async () => {
  const statements: string[] = [];
  const sql: SyncRunSqlExecutor = async (strings) => {
    statements.push(strings.join("?"));
    return [];
  };

  await initializePollenSyncRunSchema(sql);

  const schema = statements.join("\n");
  expect(schema).toContain("CREATE TABLE IF NOT EXISTS pollen_sync_runs");
  expect(schema).toContain("id BIGSERIAL PRIMARY KEY");
  expect(schema).toContain("CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL', 'FAILED'))");
  expect(schema).toContain("CHECK (trigger IN ('MANUAL', 'SCHEDULED', 'STARTUP'))");
  expect(schema).toContain("idx_pollen_sync_runs_started_at");
  expect(schema).not.toContain("error_message");
  expect(schema).not.toContain("stack");
});
