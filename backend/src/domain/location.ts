import { majorCities } from "../cityDirectory";

export type LocationScope = "CITY" | "DISTRICT";

export type LocationId = `cn-city-${string}` | `cn-beijing-${string}`;

export interface LocationDefinition {
  readonly id: LocationId;
  readonly nameCn: string;
  readonly nameEn?: string;
  readonly scope: LocationScope;
  readonly latitude?: number;
  readonly longitude?: number;
  /** Internal WeatherDT city parameter; never expose this as a canonical ID. */
  readonly weatherDtCityCode?: string;
  /** Internal Beijing pollen area code; never expose this as a canonical ID. */
  readonly beijingAreaCode?: string;
}

const cityLocations: readonly LocationDefinition[] = majorCities.map((city) => ({
  id: `cn-city-${city.en}` as LocationId,
  nameCn: city.cn,
  scope: "CITY",
  latitude: city.lat,
  longitude: city.lng,
  weatherDtCityCode: city.en,
}));

const beijingDistrictLocations: readonly LocationDefinition[] = [
  { id: "cn-beijing-dongcheng", nameCn: "东城区", scope: "DISTRICT", latitude: 39.9175, longitude: 116.4188, beijingAreaCode: "110101" },
  { id: "cn-beijing-xicheng", nameCn: "西城区", scope: "DISTRICT", latitude: 39.9153, longitude: 116.3668, beijingAreaCode: "110102" },
  { id: "cn-beijing-chaoyang", nameCn: "朝阳区", scope: "DISTRICT", latitude: 39.9215, longitude: 116.4864, beijingAreaCode: "110105" },
  { id: "cn-beijing-fengtai", nameCn: "丰台区", scope: "DISTRICT", latitude: 39.8636, longitude: 116.2870, beijingAreaCode: "110106" },
  { id: "cn-beijing-shijingshan", nameCn: "石景山区", scope: "DISTRICT", latitude: 39.9146, longitude: 116.1954, beijingAreaCode: "110107" },
  { id: "cn-beijing-haidian", nameCn: "海淀区", scope: "DISTRICT", latitude: 39.9561, longitude: 116.3103, beijingAreaCode: "110108" },
  { id: "cn-beijing-mentougou", nameCn: "门头沟区", scope: "DISTRICT", latitude: 39.9372, longitude: 116.1054, beijingAreaCode: "110109" },
  { id: "cn-beijing-fangshan", nameCn: "房山区", scope: "DISTRICT", latitude: 39.7355, longitude: 116.1392, beijingAreaCode: "110111" },
  { id: "cn-beijing-tongzhou", nameCn: "通州区", scope: "DISTRICT", latitude: 39.9025, longitude: 116.6586, beijingAreaCode: "110112" },
  { id: "cn-beijing-shunyi", nameCn: "顺义区", scope: "DISTRICT", latitude: 40.1289, longitude: 116.6535, beijingAreaCode: "110113" },
  { id: "cn-beijing-changping", nameCn: "昌平区", scope: "DISTRICT", latitude: 40.2181, longitude: 116.2359, beijingAreaCode: "110114" },
  { id: "cn-beijing-daxing", nameCn: "大兴区", scope: "DISTRICT", latitude: 39.7289, longitude: 116.3380, beijingAreaCode: "110115" },
  { id: "cn-beijing-huairou", nameCn: "怀柔区", scope: "DISTRICT", latitude: 40.3243, longitude: 116.6371, beijingAreaCode: "110116" },
  { id: "cn-beijing-pinggu", nameCn: "平谷区", scope: "DISTRICT", latitude: 40.1448, longitude: 117.1123, beijingAreaCode: "110117" },
  { id: "cn-beijing-miyun", nameCn: "密云区", scope: "DISTRICT", latitude: 40.3774, longitude: 116.8434, beijingAreaCode: "110118" },
  { id: "cn-beijing-yanqing", nameCn: "延庆区", scope: "DISTRICT", latitude: 40.4653, longitude: 115.9850, beijingAreaCode: "110119" },
];

export const LOCATION_DEFINITIONS: readonly LocationDefinition[] = [
  ...cityLocations,
  ...beijingDistrictLocations,
];

const locationById = new Map(LOCATION_DEFINITIONS.map((location) => [location.id, location]));

export function getLocationById(locationId: string): LocationDefinition | undefined {
  return locationById.get(locationId as LocationId);
}

export function getWeatherDtCityCode(locationId: LocationId): string | undefined {
  return getLocationById(locationId)?.weatherDtCityCode;
}

export function getBeijingAreaCode(locationId: LocationId): string | undefined {
  return getLocationById(locationId)?.beijingAreaCode;
}
