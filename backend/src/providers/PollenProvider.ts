import type { PollenObservation } from "../domain/pollenObservation";
import type { TaxonCode } from "../domain/taxon";

export type PollenProviderCapability =
  | "TOTAL_OBSERVATION"
  | "TOTAL_FORECAST"
  | "CATEGORY_FORECAST"
  | "GENUS_OBSERVATION"
  | "GENUS_FORECAST"
  | "HISTORY";

export interface PollenProviderQuery {
  readonly locationId?: string;
  readonly stationId?: string;
  readonly taxonCode?: TaxonCode;
  readonly from?: Date;
  readonly to?: Date;
}

export interface PollenProvider {
  readonly id: string;
  readonly name: string;
  readonly capabilities: readonly PollenProviderCapability[];
  readonly supportedTaxa: readonly TaxonCode[];

  readonly fetchCurrent?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
  readonly fetchHistory?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
  readonly fetchForecast?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
}
