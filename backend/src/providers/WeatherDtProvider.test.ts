import { describe, expect, test } from "bun:test";

import { WeatherDtProvider } from "./WeatherDtProvider";

const dataList = [
  {
    levelCode: 4,
    addTime: "2026-08-31",
    color: "#FFAF13",
    city: "北京",
    cityCode: "beijing",
    level: "高",
    levelMsg: "易引发过敏，加强防护。",
    createDate: "2026-08-30T23:30:00",
  },
  {
    levelCode: 3,
    addTime: "2026-09-01",
    color: "#F5EE32",
    city: "北京",
    cityCode: "beijing",
    level: "中",
    levelMsg: "易引发过敏，加强防护。",
  },
];

function createProvider() {
  const requestedUrls: string[] = [];
  const provider = new WeatherDtProvider({
    fetchImpl: async (url) => {
      requestedUrls.push(url);
      return new Response(JSON.stringify({ dataList }));
    },
    now: () => new Date("2026-08-31T12:00:00.000Z"),
  });

  return { provider, requestedUrls };
}

describe("WeatherDtProvider", () => {
  test("declares only total-pollen capabilities", () => {
    const { provider } = createProvider();

    expect(provider.id).toBe("weatherdt");
    expect(provider.capabilities).toEqual([
      "TOTAL_CURRENT",
      "TOTAL_FORECAST",
      "HISTORY",
    ]);
    expect(provider.supportedTaxa).toEqual([]);
    expect(provider.supportsLocation("cn-city-beijing")).toBe(true);
    expect(provider.supportsLocation("cn-beijing-chaoyang")).toBe(false);
  });

  test("normalizes current and forecast levels without assigning Artemisia", async () => {
    const { provider, requestedUrls } = createProvider();

    const current = await provider.fetchCurrent({ locationId: "cn-city-beijing" });
    const forecast = await provider.fetchForecast({ locationId: "cn-city-beijing" });

    expect(current).toMatchObject([
      {
        locationId: "cn-city-beijing",
        scope: "TOTAL",
        measurementType: "CURRENT",
        value: 4,
        unit: "level",
        riskLevel: 4,
        riskLabel: "高",
        provider: "weatherdt",
        confidence: 3,
      },
    ]);
    expect(current[0]?.taxonCode).toBeUndefined();
    expect(forecast).toMatchObject([
      {
        locationId: "cn-city-beijing",
        scope: "TOTAL",
        measurementType: "FORECAST",
        value: 3,
        unit: "level",
        validFrom: new Date("2026-08-31T16:00:00.000Z"),
      },
    ]);
    expect(requestedUrls[0]).toContain("predictFlag=false");
    expect(requestedUrls[1]).toContain("predictFlag=true");
  });

  test("limits historical results to the requested China date range", async () => {
    const { provider, requestedUrls } = createProvider();

    const history = await provider.fetchHistory({
      locationId: "cn-city-beijing",
      from: new Date("2026-08-30T16:00:00.000Z"),
      to: new Date("2026-08-31T15:59:59.000Z"),
    });

    expect(history).toHaveLength(1);
    expect(history[0]).toMatchObject({
      scope: "TOTAL",
      measurementType: "CURRENT",
      riskLevel: 4,
    });
    expect(requestedUrls[0]).toContain("predictFlag=false");
  });

  test("provides normalized daily levels for the legacy scraper adapter", async () => {
    const { provider } = createProvider();

    const levels = await provider.fetchDailyLevels({
      locationId: "beijing",
      startDate: "2026-08-30",
      endDate: "2026-08-31",
      includeForecast: true,
    });

    expect(levels).toEqual([
      {
        date: "2026-08-31",
        levelCode: 4,
        levelName: "高",
        color: "#FFAF13",
        message: "易引发过敏，加强防护。",
      },
      {
        date: "2026-09-01",
        levelCode: 3,
        levelName: "中",
        color: "#F5EE32",
        message: "易引发过敏，加强防护。",
      },
    ]);
  });
});
