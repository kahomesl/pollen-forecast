import { describe, expect, test } from "bun:test";

import { PollenScheduler, getPollenSyncIntervalMinutes, isBackgroundSyncEnabled } from "./PollenScheduler";

describe("PollenScheduler", () => {
  test("is disabled by default and clamps the minimum configured interval", () => {
    expect(isBackgroundSyncEnabled(undefined)).toBe(false);
    expect(isBackgroundSyncEnabled("false")).toBe(false);
    expect(isBackgroundSyncEnabled("true")).toBe(true);
    expect(getPollenSyncIntervalMinutes(undefined)).toBe(60);
    expect(getPollenSyncIntervalMinutes("invalid")).toBe(60);
    expect(getPollenSyncIntervalMinutes("5")).toBe(15);
    expect(getPollenSyncIntervalMinutes("30")).toBe(30);
  });

  test("does not execute immediately and skips an overlapping scheduled run", async () => {
    let scheduledCallback: (() => void) | undefined;
    let resolveRun: (() => void) | undefined;
    let calls = 0;
    const scheduler = new PollenScheduler({
      syncService: {
        runPollenSync: async () => {
          calls += 1;
          await new Promise<void>((resolve) => { resolveRun = resolve; });
          return {} as never;
        },
      },
      enabled: true,
      intervalMinutes: 60,
      setIntervalImpl: (callback) => {
        scheduledCallback = callback;
        return 1 as unknown as ReturnType<typeof setInterval>;
      },
      clearIntervalImpl: () => undefined,
    });

    expect(scheduler.start()).toBe(true);
    expect(calls).toBe(0);
    expect(scheduledCallback).toBeDefined();

    const firstRun = scheduler.runIfIdle();
    expect(await scheduler.runIfIdle()).toBe(false);
    expect(calls).toBe(1);
    resolveRun?.();
    expect(await firstRun).toBe(true);
    scheduler.stop();
  });
});
