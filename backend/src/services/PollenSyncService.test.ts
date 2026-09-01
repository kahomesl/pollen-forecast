import { describe, expect, test } from "bun:test";

import type { LocationDefinition } from "../domain/location";
import type { PollenObservation } from "../domain/pollenObservation";
import type { CompletePollenSyncRunInput } from "../domain/pollenSyncRun";
import type { PollenProvider } from "../providers/PollenProvider";
import {
  PollenSyncService,
  getPollenSyncConcurrency,
} from "./PollenSyncService";

const city: LocationDefinition = { id: "cn-city-beijing", nameCn: "北京", scope: "CITY", weatherDtCityCode: "beijing" };
const district: LocationDefinition = { id: "cn-beijing-chaoyang", nameCn: "朝阳区", scope: "DISTRICT", beijingAreaCode: "110105" };

function observation(id: string, locationId: string, measurementType: "CURRENT" | "FORECAST"): PollenObservation {
  return {
    id,
    locationId,
    scope: "TOTAL",
    measurementType,
    unit: "level",
    provider: "weatherdt",
    sourceName: "WeatherDT",
    confidence: 3,
    createdAt: new Date("2026-08-31T00:00:00.000Z"),
    updatedAt: new Date("2026-08-31T00:00:00.000Z"),
  };
}

function createRunRepository() {
  const completed: unknown[] = [];
  return {
    completed,
    startRun: async (trigger: "MANUAL" | "SCHEDULED" | "STARTUP", startedAt: Date) => ({
      id: 1,
      startedAt,
      status: "RUNNING" as const,
      trigger,
      locationsAttempted: 0,
      locationsSucceeded: 0,
      locationsFailed: 0,
      observationsReceived: 0,
      observationsPersisted: 0,
      createdAt: startedAt,
    }),
    completeRun: async (_id: number, input: CompletePollenSyncRunInput) => {
      completed.push(input);
      return { id: 1, startedAt: new Date(), createdAt: new Date(), trigger: "MANUAL" as const, ...input };
    },
  };
}

describe("PollenSyncService", () => {
  test("syncs only provider-supported canonical locations and persists normalized results", async () => {
    const persisted: Array<readonly PollenObservation[]> = [];
    const runs = createRunRepository();
    const weather: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT", "TOTAL_FORECAST"],
      supportedTaxa: [],
      supportsLocation: (locationId) => locationId === city.id,
      fetchCurrent: async ({ locationId }) => [observation("weather-current", locationId!, "CURRENT")],
      fetchForecast: async ({ locationId }) => [observation("weather-forecast", locationId!, "FORECAST")],
    };
    const beijing: PollenProvider = {
      id: "beijing-pollen",
      name: "北京花粉监测",
      capabilities: ["GENUS_FORECAST"],
      supportedTaxa: ["ARTEMISIA"],
      supportsLocation: (locationId) => locationId === district.id,
      fetchForecast: async () => [],
    };

    const service = new PollenSyncService({
      providers: [weather, beijing],
      locations: [city, district],
      observationStore: {
        persistAndCount: async (observations) => {
          persisted.push(observations);
          return observations.length;
        },
      },
      syncRunRepository: runs,
      concurrency: 3,
    });

    const result = await service.runPollenSync("MANUAL");

    expect(result).toMatchObject({
      runId: 1,
      status: "SUCCESS",
      locationsAttempted: 2,
      locationsSucceeded: 2,
      locationsFailed: 0,
      observationsReceived: 2,
      observationsPersisted: 2,
      providerErrors: [],
    });
    expect(persisted).toEqual([[expect.objectContaining({ id: "weather-current" }), expect.objectContaining({ id: "weather-forecast" })]]);
    expect(runs.completed).toHaveLength(1);
  });

  test("retries one retryable provider failure and keeps the final result free of error details", async () => {
    let calls = 0;
    const runs = createRunRepository();
    const provider: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: () => true,
      fetchCurrent: async ({ locationId }) => {
        calls += 1;
        if (calls === 1) throw new TypeError("network unavailable");
        return [observation("weather-current", locationId!, "CURRENT")];
      },
    };
    const service = new PollenSyncService({
      providers: [provider],
      locations: [city],
      observationStore: { persistAndCount: async (observations) => observations.length },
      syncRunRepository: runs,
      delay: async () => undefined,
    });

    const result = await service.runPollenSync();

    expect(calls).toBe(2);
    expect(result.status).toBe("SUCCESS");
    expect(result.providerErrors).toEqual([]);
    expect(JSON.stringify(result)).not.toContain("network unavailable");
  });

  test("returns a partial status and a classified error when one provider-location task fails", async () => {
    const runs = createRunRepository();
    const provider: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: () => true,
      fetchCurrent: async () => { throw new Error("unexpected"); },
    };
    const service = new PollenSyncService({
      providers: [provider],
      locations: [city, district],
      observationStore: { persistAndCount: async () => 0 },
      syncRunRepository: runs,
    });

    const result = await service.runPollenSync();

    expect(result.status).toBe("FAILED");
    expect(result.locationsFailed).toBe(2);
    expect(result.providerErrors).toEqual([
      { providerId: "weatherdt", locationId: city.id, errorType: "UNKNOWN" },
      { providerId: "weatherdt", locationId: district.id, errorType: "UNKNOWN" },
    ]);
  });

  test("uses a safe default and clamps configured concurrency", () => {
    expect(getPollenSyncConcurrency(undefined)).toBe(3);
    expect(getPollenSyncConcurrency("invalid")).toBe(3);
    expect(getPollenSyncConcurrency("0")).toBe(3);
    expect(getPollenSyncConcurrency("-1")).toBe(3);
    expect(getPollenSyncConcurrency("4")).toBe(4);
    expect(getPollenSyncConcurrency("999")).toBe(10);
  });

  test("records provider outcomes without raw provider errors", async () => {
    const events: unknown[] = [];
    const runs = createRunRepository();
    const provider: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: () => true,
      fetchCurrent: async () => { throw new Error("sensitive provider response"); },
    };
    const service = new PollenSyncService({
      providers: [provider],
      locations: [city],
      observationStore: { persistAndCount: async () => 0 },
      syncRunRepository: runs,
      logger: { info: (event) => events.push(event) },
    });

    await service.runPollenSync();

    expect(events).toEqual([expect.objectContaining({
      event: "provider_sync_completed",
      providerId: "weatherdt",
      status: "ERROR",
      observationsReceived: 0,
      observationsPersisted: 0,
    })]);
    expect(JSON.stringify(events)).not.toContain("sensitive provider response");
  });
});
