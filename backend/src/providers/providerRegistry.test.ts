import { describe, expect, test } from "bun:test";

import { getEnabledPollenProviders, pollenProviders } from "./providerRegistry";

describe("getEnabledPollenProviders", () => {
  test("keeps the normalized provider metadata available to development tools", () => {
    expect(pollenProviders.map((provider) => provider.id)).toEqual([
      "weatherdt",
      "nearby",
      "beijing-pollen",
    ]);
    expect(pollenProviders.map((provider) => provider.capabilities)).toEqual([
      ["TOTAL_CURRENT", "TOTAL_FORECAST", "HISTORY"],
      ["TOTAL_ESTIMATE"],
      ["GENUS_FORECAST"],
    ]);
    expect(pollenProviders[2]?.supportedTaxa).toEqual(["ARTEMISIA"]);
  });

  test("does not include external source providers when their gates are disabled", () => {
    const providerIds = getEnabledPollenProviders({ weatherDt: false, beijingPollen: false }).map((provider) => provider.id);

    expect(providerIds).not.toContain("weatherdt");
    expect(providerIds).not.toContain("beijing-pollen");
  });
});
