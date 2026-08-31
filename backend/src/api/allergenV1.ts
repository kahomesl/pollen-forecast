import { Elysia } from "elysia";

import { getLocationById, LOCATION_DEFINITIONS, type LocationDefinition } from "../domain/location";
import type { PollenObservation } from "../domain/pollenObservation";
import { getTaxonByCode, TAXON_DEFINITIONS } from "../domain/taxon";
import { pollenProviders } from "../providers/providerRegistry";
import type { PollenProvider } from "../providers/PollenProvider";
import { queryLocationAllergens } from "../services/allergenQueryService";

export interface AllergenV1ApiOptions {
  readonly providers?: readonly PollenProvider[];
}

export function createAllergenV1Api(options: AllergenV1ApiOptions = {}) {
  const providers = options.providers ?? pollenProviders;

  return new Elysia()
    .get("/api/v1/allergens", () => TAXON_DEFINITIONS)
    .get("/api/v1/providers", () => providers.map((provider) => ({
      id: provider.id,
      name: provider.name,
      capabilities: provider.capabilities,
      supportedTaxa: provider.supportedTaxa,
    })))
    .get("/api/v1/locations", () => LOCATION_DEFINITIONS.map(toPublicLocation))
    .get("/api/v1/locations/:locationId/allergens", async ({ params, set }) => {
      const location = getLocationById(params.locationId);
      if (!location) return notFound(set, "LOCATION_NOT_FOUND", "Unknown location");

      return toApiQueryResult(location, await queryLocationAllergens({
        locationId: location.id,
        providers,
      }));
    })
    .get("/api/v1/locations/:locationId/allergens/:taxon", async ({ params, set }) => {
      const location = getLocationById(params.locationId);
      if (!location) return notFound(set, "LOCATION_NOT_FOUND", "Unknown location");

      const taxon = getTaxonByCode(params.taxon.toUpperCase());
      if (!taxon) return notFound(set, "TAXON_NOT_FOUND", "Unknown allergen");

      return toApiQueryResult(location, await queryLocationAllergens({
        locationId: location.id,
        taxonCode: taxon.code,
        providers,
      }));
    });
}

function toPublicLocation(location: LocationDefinition) {
  return {
    id: location.id,
    nameCn: location.nameCn,
    ...(location.nameEn ? { nameEn: location.nameEn } : {}),
    scope: location.scope,
    ...(location.latitude !== undefined ? { latitude: location.latitude } : {}),
    ...(location.longitude !== undefined ? { longitude: location.longitude } : {}),
  };
}

function toApiQueryResult(
  location: LocationDefinition,
  result: Awaited<ReturnType<typeof queryLocationAllergens>>,
) {
  return {
    location: toPublicLocation(location),
    observations: result.observations.map(serializeObservation),
    providersWithErrors: result.providersWithErrors,
  };
}

function serializeObservation(observation: PollenObservation) {
  return {
    id: observation.id,
    locationId: observation.locationId,
    ...(observation.stationId ? { stationId: observation.stationId } : {}),
    ...(observation.taxonCode ? {
      taxon: {
        code: observation.taxonCode,
        nameCn: observation.taxonNameCn,
        nameEn: observation.taxonNameEn,
      },
    } : {}),
    scope: observation.scope,
    measurementType: observation.measurementType,
    ...(observation.value !== undefined ? { value: observation.value } : {}),
    ...(observation.minValue !== undefined ? { minValue: observation.minValue } : {}),
    ...(observation.maxValue !== undefined ? { maxValue: observation.maxValue } : {}),
    unit: observation.unit,
    risk: {
      ...(observation.riskLevel !== undefined ? { level: observation.riskLevel } : {}),
      ...(observation.riskLabel ? { label: observation.riskLabel } : {}),
    },
    provider: observation.provider,
    source: {
      name: observation.sourceName,
      ...(observation.sourceUrl ? { url: observation.sourceUrl } : {}),
    },
    confidence: observation.confidence,
    time: {
      ...(observation.observedAt ? { observedAt: observation.observedAt.toISOString() } : {}),
      ...(observation.validFrom ? { validFrom: observation.validFrom.toISOString() } : {}),
      ...(observation.validTo ? { validTo: observation.validTo.toISOString() } : {}),
      createdAt: observation.createdAt.toISOString(),
      updatedAt: observation.updatedAt.toISOString(),
    },
  };
}

function notFound(set: { status?: number | string }, code: string, message: string) {
  set.status = 404;
  return { error: { code, message } };
}
