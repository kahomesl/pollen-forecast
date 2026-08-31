import { describe, expect, test } from "bun:test";

import {
  createNearbyPollenEstimate,
  nearbyPollenProvider,
} from "./NearbyPollenEstimate";

describe("nearby pollen estimate", () => {
  test("declares only total-pollen estimate capability", () => {
    expect(nearbyPollenProvider).toMatchObject({
      id: "nearby",
      capabilities: ["TOTAL_ESTIMATE"],
      supportedTaxa: [],
    });
  });

  test("normalizes interpolation output as a total-pollen estimate", () => {
    const validFrom = new Date("2026-08-31T16:00:00.000Z");
    const createdAt = new Date("2026-08-31T17:00:00.000Z");
    const estimate = createNearbyPollenEstimate({
      locationId: "lasa",
      value: 4,
      riskLabel: "较高",
      validFrom,
      createdAt,
    });

    expect(estimate).toMatchObject({
      locationId: "lasa",
      scope: "TOTAL",
      measurementType: "ESTIMATE",
      value: 4,
      unit: "level",
      riskLevel: 4,
      riskLabel: "较高",
      provider: "nearby",
      confidence: 2,
      validFrom,
      createdAt,
      updatedAt: createdAt,
    });
    expect(estimate.taxonCode).toBeUndefined();
  });
});
