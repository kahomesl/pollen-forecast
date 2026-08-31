import { describe, expect, test } from "bun:test";

import {
  getBeijingAreaCode,
  getLocationById,
  getWeatherDtCityCode,
  LOCATION_DEFINITIONS,
  type LocationId,
} from "./location";

// @ts-expect-error Provider-facing IDs must not accept WeatherDT city codes.
const invalidWeatherDtLocation: LocationId = "beijing";
void invalidWeatherDtLocation;

// @ts-expect-error Provider-facing IDs must not accept Beijing administrative codes.
const invalidBeijingAreaLocation: LocationId = "110105";
void invalidBeijingAreaLocation;

describe("canonical pollen locations", () => {
  test("covers the 42 existing cities with stable canonical identifiers", () => {
    const cityLocations = LOCATION_DEFINITIONS.filter((location) => location.scope === "CITY");

    expect(cityLocations).toHaveLength(42);
    expect(getLocationById("cn-city-beijing")).toMatchObject({
      id: "cn-city-beijing",
      nameCn: "北京",
      scope: "CITY",
    });
    expect(getWeatherDtCityCode("cn-city-beijing")).toBe("beijing");
  });

  test("maps confirmed Beijing district locations to area codes without exposing them as IDs", () => {
    const districtLocations = LOCATION_DEFINITIONS.filter((location) => location.scope === "DISTRICT");

    expect(districtLocations).toHaveLength(16);
    expect(getLocationById("cn-beijing-chaoyang")).toMatchObject({
      id: "cn-beijing-chaoyang",
      nameCn: "朝阳区",
      scope: "DISTRICT",
    });
    expect(getBeijingAreaCode("cn-beijing-chaoyang")).toBe("110105");
    expect(getWeatherDtCityCode("cn-beijing-chaoyang")).toBeUndefined();
  });
});
