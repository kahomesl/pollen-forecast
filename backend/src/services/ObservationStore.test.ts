import { describe, expect, test } from "bun:test";

import type { PollenObservation } from "../domain/pollenObservation";
import { ObservationStore } from "./ObservationStore";

const currentObservation: PollenObservation = {
  id: "weatherdt:cn-city-beijing:2026-08-31:4:CURRENT",
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

describe("ObservationStore", () => {
  test("persists successful normalized observations", async () => {
    const saved: Array<readonly PollenObservation[]> = [];
    const store = new ObservationStore({
      saveMany: async (observations) => {
        saved.push(observations);
        return [...observations];
      },
    });

    await store.persist([currentObservation]);

    expect(saved).toEqual([[currentObservation]]);
  });

  test("does not throw or expose persistence failures to the query flow", async () => {
    const errors: string[] = [];
    const store = new ObservationStore(
      { saveMany: async () => { throw new Error("database password=secret"); } },
      { error: (message) => errors.push(message) },
    );

    await expect(store.persist([currentObservation])).resolves.toBeUndefined();
    expect(errors).toEqual(["Failed to persist normalized pollen observations."]);
  });
});
