import { describe, expect, test } from "bun:test";
import { Elysia } from "elysia";

import { InMemoryApiRateLimiter, createPublicApiSafetyHandlers } from "./runtimeSafety";

describe("public API safety", () => {
  test("adds a request id and rate-limits API paths without limiting health", async () => {
    const safety = createPublicApiSafetyHandlers({ rateLimiter: new InMemoryApiRateLimiter({ windowMs: 60_000, maxRequests: 1 }) });
    const app = new Elysia()
      .onRequest(safety.onRequest)
      .onError(safety.onError)
      .get("/health", () => ({ ok: true }))
      .get("/api/example", () => ({ ok: true }));

    const health = await app.handle(new Request("http://localhost/health"));
    const first = await app.handle(new Request("http://localhost/api/example", { headers: { "x-request-id": "accepted-id" } }));
    const second = await app.handle(new Request("http://localhost/api/example"));

    expect(health.status).toBe(200);
    expect(health.headers.get("x-request-id")).toMatch(/^[0-9a-f-]{36}$/);
    expect(first.status).toBe(200);
    expect(first.headers.get("x-request-id")).toBe("accepted-id");
    expect(second.status).toBe(429);
    await expect(second.json()).resolves.toEqual({
      error: { code: "RATE_LIMITED", message: "Too many requests" },
      requestId: expect.any(String),
    });
  });

  test("does not expose thrown error details", async () => {
    const safety = createPublicApiSafetyHandlers();
    const app = new Elysia()
      .onRequest(safety.onRequest)
      .onError(safety.onError)
      .get("/api/throws", () => {
        throw new Error("postgresql://secret:password@database.example.com");
      });

    const response = await app.handle(new Request("http://localhost/api/throws"));

    expect(response.status).toBe(500);
    await expect(response.json()).resolves.toEqual({
      error: { code: "INTERNAL_ERROR", message: "Internal server error" },
      requestId: expect.any(String),
    });
  });
});
