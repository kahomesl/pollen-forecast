import type { PollenObservation } from "../domain/pollenObservation";
import type { LocationId } from "../domain/location";
import type { TaxonCode } from "../domain/taxon";

export type PollenProviderCapability =
  | "TOTAL_CURRENT"
  | "TOTAL_OBSERVATION"
  | "TOTAL_FORECAST"
  | "CATEGORY_FORECAST"
  | "GENUS_OBSERVATION"
  | "GENUS_FORECAST"
  | "TOTAL_ESTIMATE"
  | "HISTORY";

export interface PollenProviderQuery {
  readonly locationId?: LocationId;
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
  readonly supportsLocation: (locationId: LocationId) => boolean;

  readonly fetchCurrent?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
  readonly fetchHistory?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
  readonly fetchForecast?: (query: PollenProviderQuery) => Promise<PollenObservation[]>;
}
