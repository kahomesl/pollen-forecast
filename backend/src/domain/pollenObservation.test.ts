import { describe, expect, test } from "bun:test";

import { ARTEMISIA } from "./taxon";
import {
  CONFIDENCE_LEVELS,
  MEASUREMENT_TYPES,
  POLLEN_UNITS,
  type ConfidenceLevel,
  type MeasurementType,
  type PollenObservation,
  type PollenUnit,
} from "./pollenObservation";

// @ts-expect-error "MODEL" is not a valid MeasurementType.
const invalidMeasurementType: MeasurementType = "MODEL";
void invalidMeasurementType;

// @ts-expect-error ConfidenceLevel is limited to 1 through 5.
const invalidConfidenceLevel: ConfidenceLevel = 6;
void invalidConfidenceLevel;

// @ts-expect-error "ppm" is not a supported PollenUnit.
const invalidPollenUnit: PollenUnit = "ppm";
void invalidPollenUnit;

describe("PollenObservation", () => {
  test("accepts only the defined measurement types and pollen units", () => {
    const measurementTypes: MeasurementType[] = [...MEASUREMENT_TYPES];
    const units: PollenUnit[] = [...POLLEN_UNITS];

    expect(measurementTypes).toEqual(["OBSERVATION", "FORECAST", "ESTIMATE"]);
    expect(units).toHaveLength(5);
  });

  test("limits confidence to levels one through five", () => {
    const confidenceLevels: ConfidenceLevel[] = [...CONFIDENCE_LEVELS];

    expect(confidenceLevels).toEqual([1, 2, 3, 4, 5]);
  });

  test("represents an Artemisia observation with Date metadata", () => {
    const observedAt = new Date("2026-08-31T08:00:00.000Z");
    const createdAt = new Date("2026-08-31T08:05:00.000Z");
    const updatedAt = new Date("2026-08-31T08:10:00.000Z");
    const observation: PollenObservation = {
      id: "beijing-artemisia-20260831-0800",
      locationId: "beijing-chaoyang",
      stationId: "beijing-chaoyang-01",
      taxonCode: ARTEMISIA.code,
      taxonNameCn: ARTEMISIA.nameCn,
      taxonNameEn: ARTEMISIA.nameEn,
      scope: "GENUS",
      measurementType: "OBSERVATION",
      value: 17,
      unit: "grains/m3",
      riskLevel: 3,
      riskLabel: "中",
      provider: "beijing-pollen",
      sourceName: "北京花粉监测",
      confidence: 5,
      observedAt,
      createdAt,
      updatedAt,
    };

    expect(observation.taxonCode).toBe("ARTEMISIA");
    expect(observation.scope).toBe("GENUS");
    expect(observation.measurementType).toBe("OBSERVATION");
    expect(observation.observedAt).toBe(observedAt);
    expect(observation.createdAt).toBe(createdAt);
    expect(observation.updatedAt).toBe(updatedAt);
  });

  test("represents a total-pollen estimate without a taxon code", () => {
    const estimate: PollenObservation = {
      id: "lasa-total-20260831",
      locationId: "lasa",
      scope: "TOTAL",
      measurementType: "ESTIMATE",
      value: 4,
      unit: "level",
      riskLevel: 4,
      riskLabel: "较高",
      provider: "nearby",
      sourceName: "Nearby interpolation",
      confidence: 2,
      createdAt: new Date("2026-08-31T09:00:00.000Z"),
      updatedAt: new Date("2026-08-31T09:00:00.000Z"),
    };

    expect(estimate.scope).toBe("TOTAL");
    expect(estimate.taxonCode).toBeUndefined();
    expect(estimate.measurementType).toBe("ESTIMATE");
  });
});
