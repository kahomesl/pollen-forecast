import type { TaxonCode, TaxonScope } from "./taxon";

/**
 * A provider-confirmed, platform display/alert level. It is neither a
 * concentration unit nor a person's medical risk.
 */
export const RISK_SEVERITIES = ["UNKNOWN", "LOW", "MODERATE", "HIGH", "VERY_HIGH"] as const;
export type RiskSeverity = (typeof RISK_SEVERITIES)[number];

export interface RiskSeverityInput {
  readonly provider: string;
  readonly riskLevel?: number;
  readonly riskLabel?: string;
  readonly scope: TaxonScope;
  readonly taxonCode?: TaxonCode;
}

/**
 * Maps only source semantics that have been confirmed for the exact provider
 * and taxonomic scope. Unknown input deliberately remains UNKNOWN.
 */
export function normalizeRiskSeverity(input: RiskSeverityInput): RiskSeverity {
  if (input.provider === "weatherdt" && input.scope === "TOTAL" && !input.taxonCode) {
    return weatherDtSeverity(input.riskLevel);
  }

  // Beijing classified JKHS levels currently conflict with the public legends
  // response, so no level-to-severity mapping is safe yet.
  return "UNKNOWN";
}

function weatherDtSeverity(level: number | undefined): RiskSeverity {
  switch (level) {
    case 0:
    case 1:
    case 2:
      return "LOW";
    case 3:
      return "MODERATE";
    case 4:
      return "HIGH";
    case 5:
      return "VERY_HIGH";
    default:
      return "UNKNOWN";
  }
}
