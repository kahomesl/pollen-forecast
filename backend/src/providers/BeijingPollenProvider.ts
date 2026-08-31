import type { PollenObservation } from "../domain/pollenObservation";
import { ARTEMISIA } from "../domain/taxon";
import type { PollenProvider, PollenProviderQuery } from "./PollenProvider";

const BEIJING_CLASSIFY_FORECAST_ENDPOINT = "https://pollenwechat.bjpws.com/v2/pollen/classify/forecast";
const ARTEMISIA_PLANT_CODE = "JKHS";
const ARTEMISIA_PLANT_NAME = "菊科蒿属";

export interface BeijingPollenProviderOptions {
  readonly fetchImpl?: (url: string, options?: RequestInit) => Promise<Response>;
  readonly now?: () => Date;
}

export class BeijingPollenProvider implements PollenProvider {
  readonly id = "beijing-pollen";
  readonly name = "北京花粉监测";
  readonly capabilities = ["GENUS_FORECAST"] as const;
  readonly supportedTaxa = [ARTEMISIA.code] as const;

  private readonly fetchImpl: (url: string, options?: RequestInit) => Promise<Response>;
  private readonly now: () => Date;

  constructor(options: BeijingPollenProviderOptions = {}) {
    this.fetchImpl = options.fetchImpl ?? ((url, requestOptions) => fetch(url, requestOptions));
    this.now = options.now ?? (() => new Date());
  }

  async fetchForecast(query: PollenProviderQuery): Promise<PollenObservation[]> {
    const locationId = this.requireLocationId(query);
    const response = await this.fetchImpl(this.buildForecastUrl(locationId), {
      headers: { "User-Agent": "PollenForecast/1.0" },
      signal: AbortSignal.timeout(10000),
    });
    if (!response.ok) return [];

    const payload = await response.json() as unknown;
    return extractForecastRows(payload)
      .filter((row) => row.areaCode === locationId)
      .flatMap((row) => this.toArtemisiaForecast(row));
  }

  private requireLocationId(query: PollenProviderQuery): string {
    if (!query.locationId) {
      throw new Error("Beijing pollen forecasts require an area-code locationId");
    }

    return query.locationId;
  }

  private buildForecastUrl(areaCode: string): string {
    return `${BEIJING_CLASSIFY_FORECAST_ENDPOINT}?${new URLSearchParams({ areaCode })}`;
  }

  private toArtemisiaForecast(row: BeijingForecastRow): PollenObservation[] {
    if (
      row.plantCode !== ARTEMISIA_PLANT_CODE
      || row.plantName !== ARTEMISIA_PLANT_NAME
      || typeof row.dataTime !== "string"
      || typeof row.vti !== "number"
      || typeof row.level !== "number"
    ) {
      return [];
    }

    if (!/^\d{12}$/.test(row.dataTime)) {
      return [];
    }
    const timestamp = this.now();

    return [{
      id: `${this.id}:${row.areaCode}:${row.plantCode}:${row.dataTime}`,
      locationId: row.areaCode,
      taxonCode: ARTEMISIA.code,
      taxonNameCn: ARTEMISIA.nameCn,
      taxonNameEn: ARTEMISIA.nameEn,
      scope: ARTEMISIA.scope,
      measurementType: "FORECAST",
      // The upstream description calls min/max a pollen concentration index, not a physical unit.
      minValue: typeof row.min === "number" ? row.min : undefined,
      maxValue: typeof row.max === "number" ? row.max : undefined,
      unit: "index",
      riskLevel: row.level,
      provider: this.id,
      sourceName: this.name,
      sourceUrl: BEIJING_CLASSIFY_FORECAST_ENDPOINT,
      confidence: 4,
      createdAt: timestamp,
      updatedAt: timestamp,
    }];
  }
}

interface BeijingForecastRow {
  readonly areaCode: string;
  readonly plantCode?: string;
  readonly plantName?: string;
  readonly dataTime?: string;
  readonly vti?: number;
  readonly level?: number;
  readonly min?: number;
  readonly max?: number;
}

function extractForecastRows(payload: unknown): BeijingForecastRow[] {
  if (!isRecord(payload) || !isRecord(payload.data)) return [];
  if (payload.data.isValid !== true || !isRecord(payload.data.value)) return [];

  return Object.values(payload.data.value).flatMap((value) => {
    if (!isRecord(value) || !Array.isArray(value.data)) return [];

    return value.data.flatMap((row) => toForecastRow(row));
  });
}

function toForecastRow(value: unknown): BeijingForecastRow[] {
  if (!isRecord(value) || typeof value.areaCode !== "string") return [];

  return [{
    areaCode: value.areaCode,
    plantCode: typeof value.plantCode === "string" ? value.plantCode : undefined,
    plantName: typeof value.plantName === "string" ? value.plantName : undefined,
    dataTime: typeof value.dataTime === "string" ? value.dataTime : undefined,
    vti: typeof value.vti === "number" ? value.vti : undefined,
    level: typeof value.level === "number" ? value.level : undefined,
    min: typeof value.min === "number" ? value.min : undefined,
    max: typeof value.max === "number" ? value.max : undefined,
  }];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}
