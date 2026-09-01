import { describe, expect, test } from "bun:test";

import { parseRuntimeConfig } from "./config";

describe("parseRuntimeConfig", () => {
  test("rejects a missing production database URL without echoing configuration", () => {
    expect(() => parseRuntimeConfig({ NODE_ENV: "production", ALLOWED_ORIGINS: "https://app.example.com" }))
      .toThrow("missing DATABASE_URL");
  });

  test("requires explicit HTTPS CORS origins outside development", () => {
    expect(() => parseRuntimeConfig({
      NODE_ENV: "staging",
      DATABASE_URL: "postgresql://user:password@db.example.com/pollen",
    })).toThrow("missing ALLOWED_ORIGINS");

    expect(() => parseRuntimeConfig({
      NODE_ENV: "production",
      DATABASE_URL: "postgresql://user:password@db.example.com/pollen",
      ALLOWED_ORIGINS: "http://app.example.com",
    })).toThrow("invalid ALLOWED_ORIGINS");
  });

  test("uses development-only external provider defaults", () => {
    const development = parseRuntimeConfig({
      NODE_ENV: "development",
      DATABASE_URL: "postgresql://user:password@localhost/pollen",
    });
    const staging = parseRuntimeConfig({
      NODE_ENV: "staging",
      DATABASE_URL: "postgresql://user:password@db.example.com/pollen",
      ALLOWED_ORIGINS: "https://staging.example.com",
    });

    expect(development.externalPollenFetchEnabled).toBe(true);
    expect(development.providerEnabled.weatherDt).toBe(true);
    expect(development.providerEnabled.beijingPollen).toBe(true);
    expect(staging.externalPollenFetchEnabled).toBe(false);
    expect(staging.providerEnabled.weatherDt).toBe(false);
    expect(staging.providerEnabled.beijingPollen).toBe(false);
  });

  test("does not allow wildcard origins", () => {
    expect(() => parseRuntimeConfig({
      NODE_ENV: "production",
      DATABASE_URL: "postgresql://user:password@db.example.com/pollen",
      ALLOWED_ORIGINS: "*",
    })).toThrow("invalid ALLOWED_ORIGINS");
  });
});
