import { describe, expect, test } from "bun:test";

import { checkHealth } from "./health";

describe("checkHealth", () => {
  test("reports a database-backed healthy response without running a scrape", async () => {
    let probes = 0;
    const response = await checkHealth({
      checkDatabase: async () => { probes += 1; },
      now: () => new Date("2026-09-01T00:00:00.000Z"),
    });

    expect(probes).toBe(1);
    expect(response).toEqual({ status: "ok", timestamp: "2026-09-01T00:00:00.000Z", database: true });
  });

  test("reports a degraded response when the database probe fails", async () => {
    const response = await checkHealth({
      checkDatabase: async () => { throw new Error("unavailable"); },
      now: () => new Date("2026-09-01T00:00:00.000Z"),
    });

    expect(response).toEqual({ status: "degraded", timestamp: "2026-09-01T00:00:00.000Z", database: false });
  });
});
