import { expect, test } from "bun:test";

import { pollenProviders } from "./providerRegistry";

test("provider registry exposes only normalized provider metadata", () => {
  expect(pollenProviders.map((provider) => provider.id)).toEqual([
    "weatherdt",
    "nearby",
    "beijing-pollen",
  ]);
  expect(pollenProviders.map((provider) => provider.capabilities)).toEqual([
    ["TOTAL_OBSERVATION", "TOTAL_FORECAST", "HISTORY"],
    ["TOTAL_ESTIMATE"],
    ["GENUS_FORECAST"],
  ]);
  expect(pollenProviders[2]?.supportedTaxa).toEqual(["ARTEMISIA"]);
});
