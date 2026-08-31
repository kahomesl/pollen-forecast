import { describe, expect, test } from "bun:test";

import { ARTEMISIA } from "../domain/taxon";
import type { PollenObservation } from "../domain/pollenObservation";
import {
  type PollenProvider,
  type PollenProviderCapability,
} from "./PollenProvider";

// @ts-expect-error Unsupported capabilities must not be accepted.
const invalidCapability: PollenProviderCapability = "SPECIES_OBSERVATION";
void invalidCapability;

const normalizedForecast: PollenObservation = {
  id: "example-artemisia-forecast",
  locationId: "beijing",
  taxonCode: ARTEMISIA.code,
  taxonNameCn: ARTEMISIA.nameCn,
  taxonNameEn: ARTEMISIA.nameEn,
  scope: ARTEMISIA.scope,
  measurementType: "FORECAST",
  unit: "level",
  provider: "example",
  sourceName: "Example provider",
  confidence: 4,
  validFrom: new Date("2026-09-01T00:00:00.000Z"),
  validTo: new Date("2026-09-02T00:00:00.000Z"),
  createdAt: new Date("2026-08-31T00:00:00.000Z"),
  updatedAt: new Date("2026-08-31T00:00:00.000Z"),
};

const invalidRawProvider: PollenProvider = {
  id: "raw-example",
  name: "Raw example provider",
  capabilities: ["GENUS_FORECAST"],
  supportedTaxa: [ARTEMISIA.code],
  supportsLocation: () => false,
  // @ts-expect-error Provider methods must return normalized observations.
  fetchCurrent: async () => ({ raw: "current" }),
};
void invalidRawProvider;

describe("PollenProvider", () => {
  test("expresses provider capabilities and supported taxa", async () => {
    const capabilities: PollenProviderCapability[] = [
      "TOTAL_CURRENT",
      "TOTAL_OBSERVATION",
      "TOTAL_FORECAST",
      "CATEGORY_FORECAST",
      "GENUS_OBSERVATION",
      "GENUS_FORECAST",
      "TOTAL_ESTIMATE",
      "HISTORY",
    ];

    const provider: PollenProvider = {
      id: "example",
      name: "Example provider",
      capabilities,
      supportedTaxa: [ARTEMISIA.code],
      supportsLocation: (locationId) => locationId === "cn-city-beijing",
      fetchCurrent: async () => [normalizedForecast],
      fetchHistory: async () => [normalizedForecast],
      fetchForecast: async () => [normalizedForecast],
    };

    expect(provider.id).toBe("example");
    expect(provider.capabilities).toEqual(capabilities);
    expect(provider.supportedTaxa).toEqual(["ARTEMISIA"]);
    expect(provider.supportsLocation("cn-city-beijing")).toBe(true);
    expect(await provider.fetchCurrent?.({ locationId: "cn-city-beijing" })).toEqual([normalizedForecast]);
    expect(await provider.fetchHistory?.({ locationId: "cn-city-beijing" })).toEqual([normalizedForecast]);
    expect(await provider.fetchForecast?.({ locationId: "cn-city-beijing", taxonCode: ARTEMISIA.code }))
      .toEqual([normalizedForecast]);
  });
});
