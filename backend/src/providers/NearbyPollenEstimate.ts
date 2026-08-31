import type { PollenObservation } from "../domain/pollenObservation";
import { formatChinaDate } from "../time/chinaDate";
import type { PollenProvider } from "./PollenProvider";

export interface NearbyPollenEstimateInput {
  readonly locationId: string;
  readonly value: number;
  readonly riskLabel: string;
  readonly validFrom: Date;
  readonly createdAt: Date;
  readonly updatedAt?: Date;
}

export const nearbyPollenProvider: PollenProvider = {
  id: "nearby",
  name: "Nearby interpolation",
  capabilities: ["TOTAL_ESTIMATE"],
  supportedTaxa: [],
};

export function createNearbyPollenEstimate(
  input: NearbyPollenEstimateInput,
): PollenObservation {
  return {
    id: `nearby:${input.locationId}:${formatChinaDate(input.validFrom)}`,
    locationId: input.locationId,
    scope: "TOTAL",
    measurementType: "ESTIMATE",
    value: input.value,
    unit: "level",
    riskLevel: input.value,
    riskLabel: input.riskLabel,
    provider: nearbyPollenProvider.id,
    sourceName: nearbyPollenProvider.name,
    confidence: 2,
    validFrom: input.validFrom,
    createdAt: input.createdAt,
    updatedAt: input.updatedAt ?? input.createdAt,
  };
}
