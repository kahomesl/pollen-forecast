import { describe, expect, test } from "bun:test";

import type { PollenObservation } from "../domain/pollenObservation";
import { ARTEMISIA } from "../domain/taxon";
import {
  PollenObservationRepository,
  rowToPollenObservation,
  type PollenObservationRow,
  type PollenObservationSql,
} from "./PollenObservationRepository";

const artemisiaForecast: PollenObservation = {
  id: "beijing-pollen:cn-beijing-chaoyang:JKHS:202608300900",
  locationId: "cn-beijing-chaoyang",
  taxonCode: ARTEMISIA.code,
  taxonNameCn: ARTEMISIA.nameCn,
  taxonNameEn: ARTEMISIA.nameEn,
  scope: "GENUS",
  measurementType: "FORECAST",
  minValue: 12,
  maxValue: 33,
  unit: "index",
  riskLevel: 2,
  provider: "beijing-pollen",
  sourceName: "北京花粉监测",
  confidence: 4,
  createdAt: new Date("2026-08-31T00:00:00.000Z"),
  updatedAt: new Date("2026-08-31T00:00:00.000Z"),
};

const totalCurrent: PollenObservation = {
  id: "weatherdt:cn-city-beijing:2026-08-31:4:CURRENT",
  locationId: "cn-city-beijing",
  scope: "TOTAL",
  measurementType: "CURRENT",
  value: 4,
  unit: "level",
  riskLevel: 4,
  riskLabel: "高",
  provider: "weatherdt",
  sourceName: "WeatherDT",
  confidence: 3,
  createdAt: new Date("2026-08-31T00:00:00.000Z"),
  updatedAt: new Date("2026-08-31T00:00:00.000Z"),
};

function rowFrom(observation: PollenObservation): PollenObservationRow {
  return {
    id: observation.id,
    location_id: observation.locationId,
    station_id: observation.stationId ?? null,
    taxon_code: observation.taxonCode ?? null,
    taxon_name_cn: observation.taxonNameCn ?? null,
    taxon_name_en: observation.taxonNameEn ?? null,
    scope: observation.scope,
    measurement_type: observation.measurementType,
    value: observation.value ?? null,
    min_value: observation.minValue ?? null,
    max_value: observation.maxValue ?? null,
    unit: observation.unit,
    risk_level: observation.riskLevel ?? null,
    risk_label: observation.riskLabel ?? null,
    provider: observation.provider,
    source_name: observation.sourceName,
    source_url: observation.sourceUrl ?? null,
    confidence: observation.confidence,
    observed_at: observation.observedAt ?? null,
    valid_from: observation.validFrom ?? null,
    valid_to: observation.validTo ?? null,
    created_at: observation.createdAt,
    updated_at: observation.updatedAt,
  };
}

describe("PollenObservationRepository", () => {
  test("round-trips Artemisia forecasts and preserves nullable validity times", () => {
    expect(rowToPollenObservation(rowFrom(artemisiaForecast))).toEqual(artemisiaForecast);
  });

  test("round-trips total CURRENT values without a taxon", () => {
    const result = rowToPollenObservation(rowFrom(totalCurrent));

    expect(result).toEqual(totalCurrent);
    expect(result.taxonCode).toBeUndefined();
    expect(result.validFrom).toBeUndefined();
    expect(result.validTo).toBeUndefined();
  });

  test("upserts by id and updates normalized values without duplicate insert semantics", async () => {
    const statements: string[] = [];
    const sql: PollenObservationSql = async (strings) => {
      statements.push(strings.join("?"));
      return [rowFrom(totalCurrent)];
    };
    const repository = new PollenObservationRepository(sql);

    await expect(repository.save(totalCurrent)).resolves.toEqual(totalCurrent);
    expect(statements[0]).toContain("INSERT INTO pollen_observations");
    expect(statements[0]).toContain("ON CONFLICT (id) DO UPDATE SET");
    expect(statements[0]).toContain("stored_at = NOW()");
  });
});
