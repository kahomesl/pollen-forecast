import { describe, expect, test } from "bun:test";

import type { PollenProvider } from "../providers/PollenProvider";
import type { PollenObservation } from "../domain/pollenObservation";
import type { PollenObservationRepository } from "../repositories/PollenObservationRepository";
import type { ObservationStore } from "../services/ObservationStore";
import { createAllergenV1Api } from "./allergenV1";

async function responseFor(
  path: string,
  providers: readonly PollenProvider[] = [],
  options: {
    observationStore?: Pick<ObservationStore, "persist">;
    observationRepository?: Pick<PollenObservationRepository, "findByLocation" | "findByLocationAndTaxon">;
  } = {},
) {
  const response = await createAllergenV1Api({ providers, ...options }).handle(new Request(`http://localhost${path}`));
  return { response, body: await response.json() };
}

describe("allergen v1 API", () => {
  test("lists public canonical location metadata without upstream provider codes", async () => {
    const { response, body } = await responseFor("/api/v1/locations");
    const chaoyang = body.find((location: { id: string }) => location.id === "cn-beijing-chaoyang");

    expect(response.status).toBe(200);
    expect(body).toHaveLength(58);
    expect(chaoyang).toEqual({
      id: "cn-beijing-chaoyang",
      nameCn: "朝阳区",
      scope: "DISTRICT",
      latitude: 39.9215,
      longitude: 116.4864,
    });
  });

  test("returns explicit 404 errors for unknown locations and taxa", async () => {
    const unknownLocation = await responseFor("/api/v1/locations/no-such-place/allergens");
    const unknownTaxon = await responseFor("/api/v1/locations/cn-city-beijing/allergens/no-such-taxon");

    expect(unknownLocation.response.status).toBe(404);
    expect(unknownLocation.body).toEqual({ error: { code: "LOCATION_NOT_FOUND", message: "Unknown location" } });
    expect(unknownTaxon.response.status).toBe(404);
    expect(unknownTaxon.body).toEqual({ error: { code: "TAXON_NOT_FOUND", message: "Unknown allergen" } });
  });

  test("returns an empty Artemisia collection when the Beijing provider has no valid forecast", async () => {
    const emptyBeijingProvider: PollenProvider = {
      id: "beijing-pollen",
      name: "北京花粉监测",
      capabilities: ["GENUS_FORECAST"],
      supportedTaxa: ["ARTEMISIA"],
      supportsLocation: (locationId) => locationId === "cn-beijing-chaoyang",
      fetchForecast: async () => [],
    };

    const { response, body } = await responseFor(
      "/api/v1/locations/cn-beijing-chaoyang/allergens/artemisia",
      [emptyBeijingProvider],
    );

    expect(response.status).toBe(200);
    expect(body.observations).toEqual([]);
    expect(body.providersWithErrors).toEqual([]);
  });

  test("serializes normalized observations with ISO timestamps and source metadata", async () => {
    const totalProvider: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: (locationId) => locationId === "cn-city-beijing",
      fetchCurrent: async () => [{
        id: "weatherdt:beijing",
        locationId: "cn-city-beijing",
        scope: "TOTAL",
        measurementType: "CURRENT",
        value: 4,
        unit: "level",
        riskLevel: 4,
        provider: "weatherdt",
        sourceName: "WeatherDT",
        sourceUrl: "https://example.test/weatherdt",
        confidence: 3,
        createdAt: new Date("2026-08-31T00:00:00.000Z"),
        updatedAt: new Date("2026-08-31T00:00:00.000Z"),
      }],
    };

    const { body } = await responseFor("/api/v1/locations/cn-city-beijing/allergens", [totalProvider]);

    expect(body.observations).toEqual([{
      id: "weatherdt:beijing",
      locationId: "cn-city-beijing",
      scope: "TOTAL",
      measurementType: "CURRENT",
      value: 4,
      unit: "level",
      risk: { level: 4 },
      provider: "weatherdt",
      source: { name: "WeatherDT", url: "https://example.test/weatherdt" },
      confidence: 3,
      time: {
        createdAt: "2026-08-31T00:00:00.000Z",
        updatedAt: "2026-08-31T00:00:00.000Z",
      },
    }]);
  });

  test("isolates a failing provider without exposing its exception", async () => {
    const successful: PollenProvider = {
      id: "successful",
      name: "Successful",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: (locationId) => locationId === "cn-city-beijing",
      fetchCurrent: async () => [{
        id: "successful-current",
        locationId: "cn-city-beijing",
        scope: "TOTAL",
        measurementType: "CURRENT",
        unit: "level",
        provider: "successful",
        sourceName: "Successful",
        confidence: 3,
        createdAt: new Date("2026-08-31T00:00:00.000Z"),
        updatedAt: new Date("2026-08-31T00:00:00.000Z"),
      }],
    };
    const failed: PollenProvider = {
      id: "failed",
      name: "Failed",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: (locationId) => locationId === "cn-city-beijing",
      fetchCurrent: async () => { throw new Error("upstream password=secret"); },
    };

    const { response, body } = await responseFor(
      "/api/v1/locations/cn-city-beijing/allergens",
      [successful, failed],
    );

    expect(response.status).toBe(200);
    expect(body.observations).toHaveLength(1);
    expect(body.providersWithErrors).toEqual(["failed"]);
    expect(JSON.stringify(body)).not.toContain("password");
  });

  test("persists successful live observations without changing the API response", async () => {
    const observation: PollenObservation = {
      id: "weatherdt:beijing",
      locationId: "cn-city-beijing",
      scope: "TOTAL",
      measurementType: "CURRENT",
      value: 4,
      unit: "level",
      provider: "weatherdt",
      sourceName: "WeatherDT",
      confidence: 3,
      createdAt: new Date("2026-08-31T00:00:00.000Z"),
      updatedAt: new Date("2026-08-31T00:00:00.000Z"),
    };
    const saved: Array<readonly PollenObservation[]> = [];
    const provider: PollenProvider = {
      id: "weatherdt",
      name: "WeatherDT",
      capabilities: ["TOTAL_CURRENT"],
      supportedTaxa: [],
      supportsLocation: (locationId) => locationId === "cn-city-beijing",
      fetchCurrent: async () => [observation],
    };

    const { response, body } = await responseFor(
      "/api/v1/locations/cn-city-beijing/allergens",
      [provider],
      { observationStore: { persist: async (observations) => { saved.push(observations); } } },
    );

    expect(response.status).toBe(200);
    expect(body.observations).toHaveLength(1);
    expect(saved).toEqual([[observation]]);
  });

  test("reads explicitly requested history without using it as a current-data fallback", async () => {
    const historyObservation: PollenObservation = {
      id: "weatherdt:stored",
      locationId: "cn-city-beijing",
      scope: "TOTAL",
      measurementType: "CURRENT",
      value: 3,
      unit: "level",
      provider: "weatherdt",
      sourceName: "WeatherDT",
      confidence: 3,
      createdAt: new Date("2026-08-30T00:00:00.000Z"),
      updatedAt: new Date("2026-08-30T00:00:00.000Z"),
    };
    const calls: unknown[][] = [];
    const repository = {
      findByLocation: async (...args: unknown[]) => {
        calls.push(args);
        return [historyObservation];
      },
      findByLocationAndTaxon: async () => [],
    } as unknown as Pick<PollenObservationRepository, "findByLocation" | "findByLocationAndTaxon">;

    const { response, body } = await responseFor(
      "/api/v1/locations/cn-city-beijing/history?measurementType=CURRENT&limit=1",
      [],
      { observationRepository: repository },
    );

    expect(response.status).toBe(200);
    expect(body.observations).toHaveLength(1);
    expect(calls).toEqual([["cn-city-beijing", { measurementType: "CURRENT", limit: 1 }]]);
  });
});
