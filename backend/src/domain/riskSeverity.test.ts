import { describe, expect, test } from "bun:test";

import { normalizeRiskSeverity } from "./riskSeverity";

describe("normalizeRiskSeverity", () => {
  test("maps confirmed WeatherDT total level codes without using concentration values", () => {
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 1, scope: "TOTAL" })).toBe("LOW");
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 2, scope: "TOTAL" })).toBe("LOW");
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 3, scope: "TOTAL" })).toBe("MODERATE");
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 4, scope: "TOTAL" })).toBe("HIGH");
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 5, scope: "TOTAL" })).toBe("VERY_HIGH");
  });

  test("keeps unrecognized WeatherDT levels and missing risk information unknown", () => {
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLevel: 6, scope: "TOTAL" })).toBe("UNKNOWN");
    expect(normalizeRiskSeverity({ provider: "weatherdt", riskLabel: "未知", scope: "TOTAL" })).toBe("UNKNOWN");
    expect(normalizeRiskSeverity({ provider: "weatherdt", scope: "TOTAL" })).toBe("UNKNOWN");
  });

  test("does not transfer total WeatherDT severity to Artemisia", () => {
    expect(normalizeRiskSeverity({
      provider: "weatherdt",
      riskLevel: 5,
      scope: "GENUS",
      taxonCode: "ARTEMISIA",
    })).toBe("UNKNOWN");
  });

  test("keeps Beijing levels unknown while classified forecast and legends disagree", () => {
    expect(normalizeRiskSeverity({
      provider: "beijing-pollen",
      riskLevel: 2,
      riskLabel: "中等",
      scope: "GENUS",
      taxonCode: "ARTEMISIA",
    })).toBe("UNKNOWN");
  });

  test("keeps unknown providers unknown", () => {
    expect(normalizeRiskSeverity({ provider: "unknown-provider", riskLevel: 5, scope: "TOTAL" })).toBe("UNKNOWN");
  });
});
