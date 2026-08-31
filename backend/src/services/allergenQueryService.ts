import type { LocationId } from "../domain/location";
import type { PollenObservation } from "../domain/pollenObservation";
import type { TaxonCode } from "../domain/taxon";
import type { PollenProvider } from "../providers/PollenProvider";

export interface LocationAllergenQuery {
  readonly locationId: LocationId;
  readonly taxonCode?: TaxonCode;
  readonly providers: readonly PollenProvider[];
}

export interface LocationAllergenQueryResult {
  readonly observations: readonly PollenObservation[];
  readonly providersWithErrors: readonly string[];
}

export async function queryLocationAllergens(
  query: LocationAllergenQuery,
): Promise<LocationAllergenQueryResult> {
  const providers = query.providers.filter((provider) => (
    provider.supportsLocation(query.locationId)
    && (!query.taxonCode || provider.supportedTaxa.includes(query.taxonCode))
  ));
  const results = await Promise.all(providers.map((provider) => queryProvider(provider, query)));

  return {
    observations: results.flatMap((result) => result.observations),
    providersWithErrors: results.flatMap((result) => result.hadError ? [result.providerId] : []),
  };
}

async function queryProvider(
  provider: PollenProvider,
  query: LocationAllergenQuery,
): Promise<{ providerId: string; observations: readonly PollenObservation[]; hadError: boolean }> {
  const methods = query.taxonCode
    ? [provider.fetchForecast]
    : [provider.fetchCurrent, provider.fetchForecast];
  const observations: PollenObservation[] = [];

  try {
    for (const fetchObservations of methods) {
      if (!fetchObservations) continue;
      observations.push(...await fetchObservations({
        locationId: query.locationId,
        taxonCode: query.taxonCode,
      }));
    }
    return { providerId: provider.id, observations, hadError: false };
  } catch {
    return { providerId: provider.id, observations, hadError: true };
  }
}
