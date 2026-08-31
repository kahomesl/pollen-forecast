import type {
  ConfidenceLevel,
  MeasurementType,
  PollenObservation,
  PollenUnit,
} from "../domain/pollenObservation";
import type { TaxonCode, TaxonScope } from "../domain/taxon";

export interface PollenObservationRow {
  readonly id: string;
  readonly location_id: string;
  readonly station_id: string | null;
  readonly taxon_code: string | null;
  readonly taxon_name_cn: string | null;
  readonly taxon_name_en: string | null;
  readonly scope: string;
  readonly measurement_type: string;
  readonly value: number | null;
  readonly min_value: number | null;
  readonly max_value: number | null;
  readonly unit: string;
  readonly risk_level: number | null;
  readonly risk_label: string | null;
  readonly provider: string;
  readonly source_name: string;
  readonly source_url: string | null;
  readonly confidence: number;
  readonly observed_at: Date | string | null;
  readonly valid_from: Date | string | null;
  readonly valid_to: Date | string | null;
  readonly created_at: Date | string;
  readonly updated_at: Date | string;
}

export interface PollenObservationSql {
  (strings: TemplateStringsArray, ...values: readonly unknown[]): Promise<readonly PollenObservationRow[]>;
}

export interface FindPollenObservationsOptions {
  readonly measurementType?: MeasurementType;
  readonly limit?: number;
}

export class PollenObservationRepository {
  constructor(private readonly sql: PollenObservationSql) {}

  async save(observation: PollenObservation): Promise<PollenObservation> {
    const rows = await this.sql`
      INSERT INTO pollen_observations (
        id, location_id, station_id, taxon_code, taxon_name_cn, taxon_name_en,
        scope, measurement_type, value, min_value, max_value, unit,
        risk_level, risk_label, provider, source_name, source_url, confidence,
        observed_at, valid_from, valid_to, created_at, updated_at
      ) VALUES (
        ${observation.id}, ${observation.locationId}, ${observation.stationId ?? null},
        ${observation.taxonCode ?? null}, ${observation.taxonNameCn ?? null}, ${observation.taxonNameEn ?? null},
        ${observation.scope}, ${observation.measurementType}, ${observation.value ?? null},
        ${observation.minValue ?? null}, ${observation.maxValue ?? null}, ${observation.unit},
        ${observation.riskLevel ?? null}, ${observation.riskLabel ?? null}, ${observation.provider},
        ${observation.sourceName}, ${observation.sourceUrl ?? null}, ${observation.confidence},
        ${observation.observedAt ?? null}, ${observation.validFrom ?? null}, ${observation.validTo ?? null},
        ${observation.createdAt}, ${observation.updatedAt}
      )
      ON CONFLICT (id) DO UPDATE SET
        location_id = EXCLUDED.location_id,
        station_id = EXCLUDED.station_id,
        taxon_code = EXCLUDED.taxon_code,
        taxon_name_cn = EXCLUDED.taxon_name_cn,
        taxon_name_en = EXCLUDED.taxon_name_en,
        scope = EXCLUDED.scope,
        measurement_type = EXCLUDED.measurement_type,
        value = EXCLUDED.value,
        min_value = EXCLUDED.min_value,
        max_value = EXCLUDED.max_value,
        unit = EXCLUDED.unit,
        risk_level = EXCLUDED.risk_level,
        risk_label = EXCLUDED.risk_label,
        provider = EXCLUDED.provider,
        source_name = EXCLUDED.source_name,
        source_url = EXCLUDED.source_url,
        confidence = EXCLUDED.confidence,
        observed_at = EXCLUDED.observed_at,
        valid_from = EXCLUDED.valid_from,
        valid_to = EXCLUDED.valid_to,
        updated_at = EXCLUDED.updated_at,
        stored_at = NOW()
      RETURNING
        id, location_id, station_id, taxon_code, taxon_name_cn, taxon_name_en,
        scope, measurement_type, value, min_value, max_value, unit,
        risk_level, risk_label, provider, source_name, source_url, confidence,
        observed_at, valid_from, valid_to, created_at, updated_at
    `;
    const row = rows[0];
    if (!row) throw new Error("Observation upsert returned no row");

    return rowToPollenObservation(row);
  }

  async saveMany(observations: readonly PollenObservation[]): Promise<PollenObservation[]> {
    return Promise.all(observations.map((observation) => this.save(observation)));
  }

  async findByLocation(
    locationId: string,
    options: FindPollenObservationsOptions = {},
  ): Promise<PollenObservation[]> {
    return this.find(locationId, undefined, options);
  }

  async findByLocationAndTaxon(
    locationId: string,
    taxonCode: TaxonCode,
    options: FindPollenObservationsOptions = {},
  ): Promise<PollenObservation[]> {
    return this.find(locationId, taxonCode, options);
  }

  private async find(
    locationId: string,
    taxonCode: TaxonCode | undefined,
    options: FindPollenObservationsOptions,
  ): Promise<PollenObservation[]> {
    const limit = options.limit ?? 100;
    const rows = taxonCode
      ? options.measurementType
        ? await this.sql`
            SELECT * FROM pollen_observations
            WHERE location_id = ${locationId} AND taxon_code = ${taxonCode}
              AND measurement_type = ${options.measurementType}
            ORDER BY COALESCE(observed_at, valid_from, created_at) DESC
            LIMIT ${limit}
          `
        : await this.sql`
            SELECT * FROM pollen_observations
            WHERE location_id = ${locationId} AND taxon_code = ${taxonCode}
            ORDER BY COALESCE(observed_at, valid_from, created_at) DESC
            LIMIT ${limit}
          `
      : options.measurementType
        ? await this.sql`
            SELECT * FROM pollen_observations
            WHERE location_id = ${locationId} AND measurement_type = ${options.measurementType}
            ORDER BY COALESCE(observed_at, valid_from, created_at) DESC
            LIMIT ${limit}
          `
        : await this.sql`
            SELECT * FROM pollen_observations
            WHERE location_id = ${locationId}
            ORDER BY COALESCE(observed_at, valid_from, created_at) DESC
            LIMIT ${limit}
          `;

    return rows.map(rowToPollenObservation);
  }
}

export function rowToPollenObservation(row: PollenObservationRow): PollenObservation {
  return {
    id: row.id,
    locationId: row.location_id,
    ...(row.station_id ? { stationId: row.station_id } : {}),
    ...(row.taxon_code ? {
      taxonCode: row.taxon_code as TaxonCode,
      taxonNameCn: row.taxon_name_cn ?? undefined,
      taxonNameEn: row.taxon_name_en ?? undefined,
    } : {}),
    scope: row.scope as TaxonScope,
    measurementType: row.measurement_type as MeasurementType,
    ...(row.value !== null ? { value: row.value } : {}),
    ...(row.min_value !== null ? { minValue: row.min_value } : {}),
    ...(row.max_value !== null ? { maxValue: row.max_value } : {}),
    unit: row.unit as PollenUnit,
    ...(row.risk_level !== null ? { riskLevel: row.risk_level } : {}),
    ...(row.risk_label ? { riskLabel: row.risk_label } : {}),
    provider: row.provider,
    sourceName: row.source_name,
    ...(row.source_url ? { sourceUrl: row.source_url } : {}),
    confidence: row.confidence as ConfidenceLevel,
    ...(row.observed_at ? { observedAt: toDate(row.observed_at) } : {}),
    ...(row.valid_from ? { validFrom: toDate(row.valid_from) } : {}),
    ...(row.valid_to ? { validTo: toDate(row.valid_to) } : {}),
    createdAt: toDate(row.created_at),
    updatedAt: toDate(row.updated_at),
  };
}

function toDate(value: Date | string): Date {
  return value instanceof Date ? value : new Date(value);
}
