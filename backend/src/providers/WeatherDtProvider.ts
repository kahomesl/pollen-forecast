import type { PollenObservation } from "../domain/pollenObservation";
import { getWeatherDtCityCode, type LocationId } from "../domain/location";
import { formatChinaDate, parseChinaDateStart } from "../time/chinaDate";
import type { PollenProvider, PollenProviderQuery } from "./PollenProvider";

const WEATHER_DT_ENDPOINT = "https://graph.weatherdt.com/ty/pollen/v2/hfindex.html";

export interface WeatherDtDailyLevel {
  readonly date: string;
  readonly levelCode: number;
  readonly levelName: string;
  readonly color: string;
  readonly message: string;
}

export interface WeatherDtDailyLevelQuery {
  readonly locationId: string;
  readonly startDate: string;
  readonly endDate: string;
  readonly includeForecast: boolean;
}

export interface WeatherDtProviderOptions {
  readonly fetchImpl?: (url: string, options?: RequestInit) => Promise<Response>;
  readonly now?: () => Date;
}

interface WeatherDtResponse {
  dataList?: unknown;
}

interface WeatherDtRow {
  readonly levelCode?: unknown;
  readonly addTime?: unknown;
  readonly color?: unknown;
  readonly level?: unknown;
  readonly levelMsg?: unknown;
}

export class WeatherDtProvider implements PollenProvider {
  readonly id = "weatherdt";
  readonly name = "WeatherDT";
  readonly capabilities = [
    "TOTAL_CURRENT",
    "TOTAL_FORECAST",
    "HISTORY",
  ] as const;
  readonly supportedTaxa = [] as const;
  readonly supportsLocation = (locationId: LocationId): boolean => getWeatherDtCityCode(locationId) !== undefined;

  private readonly fetchImpl: (url: string, options?: RequestInit) => Promise<Response>;
  private readonly now: () => Date;

  constructor(options: WeatherDtProviderOptions = {}) {
    this.fetchImpl = options.fetchImpl ?? ((url, requestOptions) => fetch(url, requestOptions));
    this.now = options.now ?? (() => new Date());
  }

  async fetchCurrent(query: PollenProviderQuery): Promise<PollenObservation[]> {
    const today = formatChinaDate(this.now());
    const { locationId, weatherDtCityCode } = this.requireLocation(query);
    const levels = await this.fetchDailyLevels({
      locationId: weatherDtCityCode,
      startDate: today,
      endDate: today,
      includeForecast: false,
    });

    return levels
      .filter((level) => level.date === today)
      .map((level) => this.toObservation(locationId, level, "CURRENT"));
  }

  async fetchHistory(query: PollenProviderQuery): Promise<PollenObservation[]> {
    const endDate = formatChinaDate(query.to ?? this.now());
    const startDate = formatChinaDate(query.from ?? this.now());
    const { locationId, weatherDtCityCode } = this.requireLocation(query);
    const levels = await this.fetchDailyLevels({
      locationId: weatherDtCityCode,
      startDate,
      endDate,
      includeForecast: false,
    });

    return levels
      .filter((level) => level.date >= startDate && level.date <= endDate)
      .map((level) => this.toObservation(locationId, level, "CURRENT"));
  }

  async fetchForecast(query: PollenProviderQuery): Promise<PollenObservation[]> {
    const today = formatChinaDate(this.now());
    const { locationId, weatherDtCityCode } = this.requireLocation(query);
    const levels = await this.fetchDailyLevels({
      locationId: weatherDtCityCode,
      startDate: formatChinaDate(query.from ?? this.now()),
      endDate: formatChinaDate(query.to ?? this.now()),
      includeForecast: true,
    });

    return levels
      .filter((level) => level.date > today)
      .map((level) => this.toObservation(locationId, level, "FORECAST"));
  }

  async fetchDailyLevels(query: WeatherDtDailyLevelQuery): Promise<WeatherDtDailyLevel[]> {
    const response = await this.fetchImpl(this.buildUrl(query), {
      headers: { "User-Agent": "Mozilla/5.0" },
      signal: AbortSignal.timeout(10000),
    });
    if (!response.ok) return [];

    const payload = await response.json() as unknown;
    if (!isWeatherDtResponse(payload) || !Array.isArray(payload.dataList)) return [];

    return payload.dataList.flatMap((row) => {
      const level = toDailyLevel(row);
      return level ? [level] : [];
    });
  }

  private requireLocation(query: PollenProviderQuery): { locationId: LocationId; weatherDtCityCode: string } {
    if (!query.locationId) {
      throw new Error("WeatherDT requires a locationId");
    }

    const weatherDtCityCode = getWeatherDtCityCode(query.locationId);
    if (!weatherDtCityCode) {
      throw new Error(`WeatherDT does not support location ${query.locationId}`);
    }

    return { locationId: query.locationId, weatherDtCityCode };
  }

  private buildUrl(query: WeatherDtDailyLevelQuery): string {
    const params = new URLSearchParams({
      eletype: "1",
      city: query.locationId,
      start: query.startDate,
      end: query.endDate,
      predictFlag: String(query.includeForecast),
    });

    return `${WEATHER_DT_ENDPOINT}?${params}`;
  }

  private toObservation(
    locationId: string,
    level: WeatherDtDailyLevel,
    measurementType: "CURRENT" | "FORECAST",
  ): PollenObservation {
    const timestamp = this.now();

    return {
      id: `${this.id}:${locationId}:${level.date}:${level.levelCode}:${measurementType}`,
      locationId,
      scope: "TOTAL",
      measurementType,
      value: level.levelCode,
      unit: "level",
      riskLevel: level.levelCode,
      riskLabel: level.levelName,
      provider: this.id,
      sourceName: this.name,
      sourceUrl: WEATHER_DT_ENDPOINT,
      confidence: 3,
      ...(measurementType === "FORECAST" ? { validFrom: parseChinaDateStart(level.date) } : {}),
      createdAt: timestamp,
      updatedAt: timestamp,
    };
  }
}

function isWeatherDtResponse(value: unknown): value is WeatherDtResponse {
  return typeof value === "object" && value !== null;
}

function toDailyLevel(value: unknown): WeatherDtDailyLevel | null {
  if (typeof value !== "object" || value === null) return null;

  const row = value as WeatherDtRow;
  if (
    typeof row.levelCode !== "number"
    || row.levelCode < 0
    || typeof row.addTime !== "string"
  ) {
    return null;
  }

  return {
    date: row.addTime,
    levelCode: row.levelCode,
    levelName: typeof row.level === "string" ? row.level : "暂无",
    color: typeof row.color === "string" ? row.color : "",
    message: typeof row.levelMsg === "string" ? row.levelMsg : "",
  };
}
