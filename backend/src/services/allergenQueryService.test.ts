import { describe, expect, test } from "bun:test";

import type { PollenObservation } from "../domain/pollenObservation";
import { ARTEMISIA } from "../domain/taxon";
import type { PollenProvider } from "../providers/PollenProvider";
import { queryLocationAllergens } from "./allergenQueryService";

const totalCurrent: PollenObservation = {
  id: "weatherdt-current",
  locationId: "cn-city-beijing",
  scope: "TOTAL",
  measurementType: "CURRENT",
  value: 3,
  unit: "level",
  provider: "weatherdt",
  sourceName: "WeatherDT",
  confidence: 3,
  createdAt: new Date("2026-08-31T00:00:00.000Z"),
  updatedAt: new Date("2026-08-31T00:00:00.000Z"),
};

const artemisiaForecast: PollenObservation = {
  id: "beijing-artemisia-forecast",
  locationId: "cn-beijing-chaoyang",
  taxonCode: ARTEMISIA.code,
  taxonNameCn: ARTEMISIA.nameCn,
  taxonNameEn: ARTEMISIA.nameEn,
  scope: "GENUS",
  measurementType: "FORECAST",
  minValue: 12,
  maxValue: 33,
  unit: "index",
  provider: "beijing-pollen",
  sourceName: "北京花粉监测",
  confidence: 4,
  createdAt: new Date("2026-08-31T00:00:00.000Z"),
  updatedAt: new Date("2026-08-31T00:00:00.000Z"),
};

function provider(overrides: Partial<PollenProvider> = {}): PollenProvider {
  return {
    id: "test-provider",
    name: "Test provider",
    capabilities: [],
    supportedTaxa: [],
    supportsLocation: () => true,
    ...overrides,
  };
}

describe("queryLocationAllergens", () => {
  test("keeps successful provider data when another supported provider fails", async () => {
    const successful = provider({ id: "successful", capabilities: ["TOTAL_CURRENT"], fetchCurrent: async () => [totalCurrent] });
    const failed = provider({ id: "failed", capabilities: ["TOTAL_CURRENT"], fetchCurrent: async () => { throw new Error("timeout"); } });

    const result = await queryLocationAllergens({
      locationId: "cn-city-beijing",
      providers: [successful, failed],
    });

    expect(result.observations).toEqual([totalCurrent]);
    expect(result.providersWithErrors).toEqual(["failed"]);
  });

  test("keeps a provider's current data when its later forecast call fails", async () => {
    const partiallySuccessful = provider({
      id: "partially-successful",
      capabilities: ["TOTAL_CURRENT", "TOTAL_FORECAST"],
      fetchCurrent: async () => [totalCurrent],
      fetchForecast: async () => { throw new Error("forecast timeout"); },
    });

    const result = await queryLocationAllergens({
      locationId: "cn-city-beijing",
      providers: [partiallySuccessful],
    });

    expect(result.observations).toEqual([totalCurrent]);
    expect(result.providersWithErrors).toEqual(["partially-successful"]);
  });

  test("returns an empty collection when supported providers return no data", async () => {
    const noData = provider({ capabilities: ["TOTAL_CURRENT"], fetchCurrent: async () => [] });

    await expect(queryLocationAllergens({
      locationId: "cn-city-beijing",
      providers: [noData],
    })).resolves.toEqual({ observations: [], providersWithErrors: [] });
  });

  test("preserves a class provider method receiver while invoking it", async () => {
    const receiverAware = provider({
      id: "receiver-aware",
      capabilities: ["TOTAL_CURRENT"],
      fetchCurrent: async function (this: PollenProvider) {
        if (this.id !== "receiver-aware") throw new Error("provider receiver lost");
        return [totalCurrent];
      },
    });

    await expect(queryLocationAllergens({
      locationId: "cn-city-beijing",
      providers: [receiverAware],
    })).resolves.toEqual({ observations: [totalCurrent], providersWithErrors: [] });
  });

  test("does not call total providers for a requested Artemisia taxon", async () => {
    const total = provider({
      id: "total",
      capabilities: ["TOTAL_CURRENT"],
      fetchCurrent: async () => [totalCurrent],
    });
    const artemisia = provider({
      id: "artemisia",
      capabilities: ["GENUS_FORECAST"],
      supportedTaxa: [ARTEMISIA.code],
      fetchForecast: async () => [artemisiaForecast],
    });

    const result = await queryLocationAllergens({
      locationId: "cn-beijing-chaoyang",
      taxonCode: ARTEMISIA.code,
      providers: [total, artemisia],
    });

    expect(result.observations).toEqual([artemisiaForecast]);
    expect(result.providersWithErrors).toEqual([]);
  });

  test("returns supported Artemisia current and forecast values without querying history", async () => {
    const artemisiaCurrent: PollenObservation = {
      ...artemisiaForecast,
      id: "beijing-artemisia-current",
      measurementType: "CURRENT",
    };
    const artemisia = provider({
      id: "artemisia",
      capabilities: ["GENUS_CURRENT", "GENUS_FORECAST"],
      supportedTaxa: [ARTEMISIA.code],
      fetchCurrent: async () => [artemisiaCurrent],
      fetchForecast: async () => [artemisiaForecast],
      fetchHistory: async () => { throw new Error("history must not be queried"); },
    });

    const result = await queryLocationAllergens({
      locationId: "cn-beijing-chaoyang",
      taxonCode: ARTEMISIA.code,
      providers: [artemisia],
    });

    expect(result.observations).toEqual([artemisiaCurrent, artemisiaForecast]);
    expect(result.providersWithErrors).toEqual([]);
  });

  test("keeps a taxon's current value when that provider's forecast fails", async () => {
    const artemisiaCurrent: PollenObservation = {
      ...artemisiaForecast,
      id: "beijing-artemisia-current",
      measurementType: "CURRENT",
    };
    const artemisia = provider({
      id: "artemisia",
      capabilities: ["GENUS_CURRENT", "GENUS_FORECAST"],
      supportedTaxa: [ARTEMISIA.code],
      fetchCurrent: async () => [artemisiaCurrent],
      fetchForecast: async () => { throw new Error("forecast timeout"); },
    });

    const result = await queryLocationAllergens({
      locationId: "cn-beijing-chaoyang",
      taxonCode: ARTEMISIA.code,
      providers: [artemisia],
    });

    expect(result.observations).toEqual([artemisiaCurrent]);
    expect(result.providersWithErrors).toEqual(["artemisia"]);
  });
});
