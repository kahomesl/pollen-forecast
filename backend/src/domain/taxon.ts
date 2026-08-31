export type TaxonScope =
  | "TOTAL"
  | "CATEGORY"
  | "FAMILY"
  | "GENUS"
  | "SPECIES";

export type TaxonCode = "ARTEMISIA";

export interface TaxonDefinition {
  readonly code: TaxonCode;
  readonly nameCn: string;
  readonly nameEn: string;
  readonly aliases: readonly string[];
  readonly scope: TaxonScope;
}

export const ARTEMISIA: TaxonDefinition = {
  code: "ARTEMISIA",
  nameCn: "蒿属",
  nameEn: "Artemisia",
  aliases: ["Artemisia", "Mugwort", "蒿属", "菊科蒿属"],
  scope: "GENUS",
};

export const TAXON_DEFINITIONS: readonly TaxonDefinition[] = [ARTEMISIA];
