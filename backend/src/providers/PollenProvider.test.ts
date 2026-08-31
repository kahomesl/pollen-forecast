import { describe, expect, test } from "bun:test";

import { ARTEMISIA } from "../domain/taxon";
import {
  type PollenProvider,
  type PollenProviderCapability,
} from "./PollenProvider";

// @ts-expect-error Unsupported capabilities must not be accepted.
const invalidCapability: PollenProviderCapability = "SPECIES_OBSERVATION";
void invalidCapability;

describe("PollenProvider", () => {
  test("expresses provider capabilities and supported taxa", async () => {
    const capabilities: PollenProviderCapability[] = [
      "TOTAL_OBSERVATION",
      "TOTAL_FORECAST",
      "CATEGORY_FORECAST",
      "GENUS_OBSERVATION",
      "GENUS_FORECAST",
      "HISTORY",
    ];

    const provider: PollenProvider = {
      id: "example",
      name: "Example provider",
      capabilities,
      supportedTaxa: [ARTEMISIA.code],
      fetchCurrent: async () => ({ raw: "current" }),
      fetchHistory: async () => ({ raw: "history" }),
      fetchForecast: async () => ({ raw: "forecast" }),
    };

    expect(provider.id).toBe("example");
    expect(provider.capabilities).toEqual(capabilities);
    expect(provider.supportedTaxa).toEqual(["ARTEMISIA"]);
    expect(await provider.fetchCurrent?.({ locationId: "beijing" })).toEqual({ raw: "current" });
    expect(await provider.fetchHistory?.({ locationId: "beijing" })).toEqual({ raw: "history" });
    expect(await provider.fetchForecast?.({ locationId: "beijing", taxonCode: ARTEMISIA.code }))
      .toEqual({ raw: "forecast" });
  });
});
