import { Elysia } from "elysia";

import { getLocationById, LOCATION_DEFINITIONS, type LocationDefinition } from "../domain/location";
import { MEASUREMENT_TYPES, type MeasurementType, type PollenObservation } from "../domain/pollenObservation";
import { getTaxonByCode, TAXON_DEFINITIONS } from "../domain/taxon";
import { pollenProviders } from "../providers/providerRegistry";
import type { PollenProvider } from "../providers/PollenProvider";
import type { PollenObservationRepository } from "../repositories/PollenObservationRepository";
import type { SyncRunRepository } from "../repositories/SyncRunRepository";
import type { PollenSyncRun } from "../domain/pollenSyncRun";
import { queryLocationAllergens } from "../services/allergenQueryService";
import type { ObservationStore } from "../services/ObservationStore";

export interface AllergenV1ApiOptions {
  readonly providers?: readonly PollenProvider[];
  readonly observationStore?: Pick<ObservationStore, "persist">;
  readonly observationRepository?: Pick<PollenObservationRepository, "findByLocation" | "findByLocationAndTaxon">;
  readonly syncRunRepository?: Pick<SyncRunRepository, "getLatestRun">;
}

export function createAllergenV1Api(options: AllergenV1ApiOptions = {}) {
  const providers = options.providers ?? pollenProviders;
  const observationStore = options.observationStore;
  const observationRepository = options.observationRepository;
  const syncRunRepository = options.syncRunRepository;

  return new Elysia()
    .get("/api/v1/allergens", () => TAXON_DEFINITIONS)
    .get("/api/v1/providers", () => providers.map((provider) => ({
      id: provider.id,
      name: provider.name,
      capabilities: provider.capabilities,
      supportedTaxa: provider.supportedTaxa,
    })))
    .get("/api/v1/locations", () => LOCATION_DEFINITIONS.map(toPublicLocation))
    .get("/api/v1/sync/status", async () => ({
      latestRun: syncRunRepository ? await syncRunRepository.getLatestRun().then(serializeSyncRun) : null,
    }))
    .get("/api/v1/locations/:locationId/history", async ({ params, query, set }) => {
      const location = getLocationById(params.locationId);
      if (!location) return notFound(set, "LOCATION_NOT_FOUND", "Unknown location");
      if (!observationRepository) return unavailable(set);

      const taxon = typeof query.taxon === "string" ? getTaxonByCode(query.taxon.toUpperCase()) : undefined;
      if (typeof query.taxon === "string" && !taxon) {
        return notFound(set, "TAXON_NOT_FOUND", "Unknown allergen");
      }
      const measurementType = parseMeasurementType(query.measurementType);
      if (query.measurementType !== undefined && !measurementType) {
        return badRequest(set, "INVALID_MEASUREMENT_TYPE", "Invalid measurementType");
      }
      const limit = parseLimit(query.limit);
      if (!limit) return badRequest(set, "INVALID_LIMIT", "limit must be an integer from 1 to 500");

      const options = { ...(measurementType ? { measurementType } : {}), limit };
      const observations = taxon
        ? await observationRepository.findByLocationAndTaxon(location.id, taxon.code, options)
        : await observationRepository.findByLocation(location.id, options);

      return {
        location: toPublicLocation(location),
        observations: observations.map(serializeObservation),
      };
    })
    .get("/api/v1/locations/:locationId/allergens", async ({ params, set }) => {
      const location = getLocationById(params.locationId);
      if (!location) return notFound(set, "LOCATION_NOT_FOUND", "Unknown location");

      const result = await queryLocationAllergens({
        locationId: location.id,
        providers,
      });
      await observationStore?.persist(result.observations);
      return toApiQueryResult(location, result);
    })
    .get("/api/v1/locations/:locationId/allergens/:taxon", async ({ params, set }) => {
      const location = getLocationById(params.locationId);
      if (!location) return notFound(set, "LOCATION_NOT_FOUND", "Unknown location");

      const taxon = getTaxonByCode(params.taxon.toUpperCase());
      if (!taxon) return notFound(set, "TAXON_NOT_FOUND", "Unknown allergen");

      const result = await queryLocationAllergens({
        locationId: location.id,
        taxonCode: taxon.code,
        providers,
      });
      await observationStore?.persist(result.observations);
      return toApiQueryResult(location, result);
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
      retrievedAt: observation.updatedAt.toISOString(),
      createdAt: observation.createdAt.toISOString(),
      updatedAt: observation.updatedAt.toISOString(),
    },
  };
}

function serializeSyncRun(run: PollenSyncRun | null) {
  if (!run) return null;

  return {
    status: run.status,
    startedAt: run.startedAt.toISOString(),
    ...(run.finishedAt ? { finishedAt: run.finishedAt.toISOString() } : {}),
    trigger: run.trigger,
    locationsAttempted: run.locationsAttempted,
    locationsSucceeded: run.locationsSucceeded,
    locationsFailed: run.locationsFailed,
    observationsReceived: run.observationsReceived,
    observationsPersisted: run.observationsPersisted,
  };
}

function notFound(set: { status?: number | string }, code: string, message: string) {
  set.status = 404;
  return { error: { code, message } };
}

function badRequest(set: { status?: number | string }, code: string, message: string) {
  set.status = 400;
  return { error: { code, message } };
}

function unavailable(set: { status?: number | string }) {
  set.status = 503;
  return { error: { code: "HISTORY_UNAVAILABLE", message: "History storage is unavailable" } };
}

function parseMeasurementType(value: unknown): MeasurementType | undefined {
  return typeof value === "string" && MEASUREMENT_TYPES.includes(value as MeasurementType)
    ? value as MeasurementType
    : undefined;
}

function parseLimit(value: unknown): number | undefined {
  if (value === undefined) return 100;
  if (typeof value !== "string" || !/^\d+$/.test(value)) return undefined;

  const limit = Number(value);
  return Number.isInteger(limit) && limit >= 1 && limit <= 500 ? limit : undefined;
}
