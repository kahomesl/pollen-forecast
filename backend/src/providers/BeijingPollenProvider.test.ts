import { describe, expect, test } from "bun:test";

import { BeijingPollenProvider } from "./BeijingPollenProvider";

const validForecastPayload = {
  code: 200,
  msg: "Success.",
  data: {
    isValid: true,
    value: {
      "202608300900": {
        date: "202608300900",
        title: "08月30日08时-08月31日08时",
        data: [
          {
            areaCode: "110105",
            areaName: "朝阳区",
            plantCode: "JKHS",
            plantName: "菊科蒿属",
            baseTime: "202608300900",
            dataTime: "202608300900",
            vti: 24,
            level: 2,
            min: 12,
            max: 33,
            description: "菊科蒿属花粉浓度指数中等。",
          },
          {
            areaCode: "110105",
            areaName: "朝阳区",
            plantCode: "JUKE",
            plantName: "菊科",
            dataTime: "202608300900",
            vti: 24,
            level: 2,
            min: 12,
            max: 33,
          },
        ],
      },
    },
  },
};

function createProvider(payload = validForecastPayload) {
  const requestedUrls: string[] = [];
  const provider = new BeijingPollenProvider({
    fetchImpl: async (url) => {
      requestedUrls.push(url);
      return new Response(JSON.stringify(payload));
    },
    now: () => new Date("2026-08-30T01:30:00.000Z"),
  });

  return { provider, requestedUrls };
}

describe("BeijingPollenProvider", () => {
  test("declares confirmed Artemisia genus forecast capability", () => {
    const { provider } = createProvider();

    expect(provider).toMatchObject({
      id: "beijing-pollen",
      capabilities: ["GENUS_FORECAST"],
      supportedTaxa: ["ARTEMISIA"],
    });
  });

  test("normalizes only confirmed JKHS Artemisia forecasts as index ranges", async () => {
    const { provider, requestedUrls } = createProvider();

    const forecast = await provider.fetchForecast({ locationId: "110105" });

    expect(forecast).toHaveLength(1);
    expect(forecast[0]).toMatchObject({
      locationId: "110105",
      taxonCode: "ARTEMISIA",
      taxonNameCn: "蒿属",
      taxonNameEn: "Artemisia",
      scope: "GENUS",
      measurementType: "FORECAST",
      minValue: 12,
      maxValue: 33,
      unit: "index",
      riskLevel: 2,
      provider: "beijing-pollen",
      sourceName: "北京花粉监测",
      confidence: 4,
    });
    expect(forecast[0]?.value).toBeUndefined();
    // title, dataTime, and vti disagree in the upstream response, so no validity is inferred.
    expect(forecast[0]?.validFrom).toBeUndefined();
    expect(forecast[0]?.validTo).toBeUndefined();
    expect(requestedUrls[0]).toContain("areaCode=110105");
  });

  test("returns no forecast when the upstream payload is expired", async () => {
    const { provider } = createProvider({
      ...validForecastPayload,
      data: { ...validForecastPayload.data, isValid: false },
    });

    expect(await provider.fetchForecast({ locationId: "110105" })).toEqual([]);
  });

  test("ignores forecast rows with an invalid upstream data time", async () => {
    const malformedPayload = structuredClone(validForecastPayload);
    malformedPayload.data.value["202608300900"].data[0]!.dataTime = "invalid";
    const { provider } = createProvider(malformedPayload);

    expect(await provider.fetchForecast({ locationId: "110105" })).toEqual([]);
  });
});
