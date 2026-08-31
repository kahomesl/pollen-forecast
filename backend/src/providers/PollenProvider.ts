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

export interface PollenProvider<
  TCurrent = unknown,
  THistory = unknown,
  TForecast = unknown,
> {
  readonly id: string;
  readonly name: string;
  readonly capabilities: readonly PollenProviderCapability[];
  readonly supportedTaxa: readonly TaxonCode[];

  readonly fetchCurrent?: (query: PollenProviderQuery) => Promise<TCurrent>;
  readonly fetchHistory?: (query: PollenProviderQuery) => Promise<THistory>;
  readonly fetchForecast?: (query: PollenProviderQuery) => Promise<TForecast>;
}
