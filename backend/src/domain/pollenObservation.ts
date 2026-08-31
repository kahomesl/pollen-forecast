import type { TaxonCode, TaxonScope } from "./taxon";

export const MEASUREMENT_TYPES = ["OBSERVATION", "FORECAST", "ESTIMATE"] as const;
export type MeasurementType = (typeof MEASUREMENT_TYPES)[number];

export const POLLEN_UNITS = [
  "grains/m3",
  "grains/1000mm2",
  "index",
  "level",
  "unknown",
] as const;
export type PollenUnit = (typeof POLLEN_UNITS)[number];

export const CONFIDENCE_LEVELS = [1, 2, 3, 4, 5] as const;
export type ConfidenceLevel = (typeof CONFIDENCE_LEVELS)[number];

export interface PollenObservation {
  readonly id: string;
  readonly locationId: string;
  readonly stationId?: string;

  readonly taxonCode?: TaxonCode;
  readonly taxonNameCn?: string;
  readonly taxonNameEn?: string;
  readonly scope: TaxonScope;

  readonly measurementType: MeasurementType;

  readonly value?: number;
  readonly minValue?: number;
  readonly maxValue?: number;
  readonly unit: PollenUnit;

  readonly riskLevel?: number;
  readonly riskLabel?: string;

  readonly provider: string;
  readonly sourceName: string;
  readonly sourceUrl?: string;

  readonly confidence: ConfidenceLevel;

  readonly observedAt?: Date;
  readonly validFrom?: Date;
  readonly validTo?: Date;
  readonly createdAt: Date;
  readonly updatedAt: Date;
}
