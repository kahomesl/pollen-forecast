import { describe, expect, test } from "bun:test";

import {
  fetchProviderResponse,
  getProviderTimeoutMs,
  ProviderRequestError,
} from "./providerRequest";

describe("provider request safeguards", () => {
  test("uses a bounded ten-second default timeout configuration", () => {
    expect(getProviderTimeoutMs(undefined)).toBe(10_000);
    expect(getProviderTimeoutMs("invalid")).toBe(10_000);
    expect(getProviderTimeoutMs("999")).toBe(10_000);
    expect(getProviderTimeoutMs("12000")).toBe(12_000);
    expect(getProviderTimeoutMs("999999")).toBe(60_000);
  });

  test("classifies a 5xx response as retryable HTTP failure while preserving 4xx empty-data handling", async () => {
    await expect(fetchProviderResponse(async () => new Response(null, { status: 503 }), "https://example.test", {}))
      .rejects.toMatchObject({ name: "ProviderRequestError", errorType: "HTTP" } satisfies Partial<ProviderRequestError>);

    await expect(fetchProviderResponse(async () => new Response(null, { status: 404 }), "https://example.test", {}))
      .resolves.toMatchObject({ status: 404 });
  });
});
