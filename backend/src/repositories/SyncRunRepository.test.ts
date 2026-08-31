import { describe, expect, test } from "bun:test";

import {
  SyncRunRepository,
  rowToPollenSyncRun,
  type PollenSyncRunRow,
  type PollenSyncRunSql,
} from "./SyncRunRepository";

const runningRow: PollenSyncRunRow = {
  id: 42,
  started_at: new Date("2026-08-31T00:00:00.000Z"),
  finished_at: null,
  status: "RUNNING",
  trigger: "MANUAL",
  locations_attempted: 0,
  locations_succeeded: 0,
  locations_failed: 0,
  observations_received: 0,
  observations_persisted: 0,
  created_at: new Date("2026-08-31T00:00:00.000Z"),
};

describe("SyncRunRepository", () => {
  test("maps nullable run completion fields to a typed running sync run", () => {
    expect(rowToPollenSyncRun(runningRow)).toEqual({
      id: 42,
      startedAt: new Date("2026-08-31T00:00:00.000Z"),
      status: "RUNNING",
      trigger: "MANUAL",
      locationsAttempted: 0,
      locationsSucceeded: 0,
      locationsFailed: 0,
      observationsReceived: 0,
      observationsPersisted: 0,
      createdAt: new Date("2026-08-31T00:00:00.000Z"),
    });
  });

  test("starts, completes, and reads the latest run without storing error text", async () => {
    const statements: string[] = [];
    const sql: PollenSyncRunSql = async (strings) => {
      const statement = strings.join("?");
      statements.push(statement);
      if (statement.includes("UPDATE pollen_sync_runs")) {
        return [{
          ...runningRow,
          status: "SUCCESS",
          finished_at: new Date("2026-08-31T00:00:05.000Z"),
          locations_attempted: 58,
          locations_succeeded: 58,
          observations_received: 42,
          observations_persisted: 42,
        }];
      }
      return [runningRow];
    };
    const repository = new SyncRunRepository(sql);

    const started = await repository.startRun("MANUAL", new Date("2026-08-31T00:00:00.000Z"));
    const completed = await repository.completeRun(started.id, {
      status: "SUCCESS",
      finishedAt: new Date("2026-08-31T00:00:05.000Z"),
      locationsAttempted: 58,
      locationsSucceeded: 58,
      locationsFailed: 0,
      observationsReceived: 42,
      observationsPersisted: 42,
    });
    const latest = await repository.getLatestRun();

    expect(completed.status).toBe("SUCCESS");
    expect(latest?.id).toBe(42);
    const sqlText = statements.join("\n");
    expect(sqlText).toContain("INSERT INTO pollen_sync_runs");
    expect(sqlText).toContain("UPDATE pollen_sync_runs");
    expect(sqlText).toContain("ORDER BY started_at DESC");
    expect(sqlText).not.toContain("error_message");
    expect(sqlText).not.toContain("stack");
  });
});
