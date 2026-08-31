import { describe, expect, test } from "bun:test";

import { ARTEMISIA, type TaxonScope } from "./taxon";

// @ts-expect-error "ORDER" is not a supported TaxonScope.
const invalidTaxonScope: TaxonScope = "ORDER";
void invalidTaxonScope;

describe("taxon definitions", () => {
  test("defines Artemisia as the first supported genus taxon", () => {
    expect(ARTEMISIA.code).toBe("ARTEMISIA");
    expect(ARTEMISIA.nameCn).toBe("蒿属");
    expect(ARTEMISIA.nameEn).toBe("Artemisia");
    expect(ARTEMISIA.scope).toBe("GENUS");
    expect(ARTEMISIA.aliases).toContain("Artemisia");
    expect(ARTEMISIA.aliases).toContain("Mugwort");
    expect(ARTEMISIA.aliases).toContain("蒿属");
    expect(ARTEMISIA.aliases).toContain("菊科蒿属");
  });
});
