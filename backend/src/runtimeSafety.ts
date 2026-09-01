export interface ApiRateLimiterOptions {
  readonly windowMs?: number;
  readonly maxRequests?: number;
  readonly now?: () => number;
}

export interface PublicApiSafetyOptions {
  readonly rateLimiter?: InMemoryApiRateLimiter;
  readonly now?: () => number;
}

interface RateLimitBucket {
  startedAt: number;
  requests: number;
}

export class InMemoryApiRateLimiter {
  private readonly buckets = new Map<string, RateLimitBucket>();
  private readonly windowMs: number;
  private readonly maxRequests: number;
  private readonly now: () => number;

  constructor({ windowMs = 60_000, maxRequests = 120, now = () => Date.now() }: ApiRateLimiterOptions = {}) {
    this.windowMs = windowMs;
    this.maxRequests = maxRequests;
    this.now = now;
  }

  consume(key = "public-api", at = this.now()): { readonly allowed: boolean; readonly retryAfterSeconds: number } {
    const current = this.buckets.get(key);
    if (!current || at - current.startedAt >= this.windowMs) {
      this.buckets.set(key, { startedAt: at, requests: 1 });
      return { allowed: true, retryAfterSeconds: 0 };
    }
    if (current.requests >= this.maxRequests) {
      return { allowed: false, retryAfterSeconds: Math.max(1, Math.ceil((current.startedAt + this.windowMs - at) / 1_000)) };
    }
    current.requests += 1;
    return { allowed: true, retryAfterSeconds: 0 };
  }
}

export function createPublicApiSafetyHandlers({
  rateLimiter = new InMemoryApiRateLimiter(),
  now = () => Date.now(),
}: PublicApiSafetyOptions = {}) {
  const requests = new WeakMap<Request, { requestId: string; startedAt: number }>();

  return {
    onRequest({ request, set }: { readonly request: Request; readonly set: { headers: Record<string, string | number>; status?: number | string } }) {
      const requestId = requestIdFor(request);
      requests.set(request, { requestId, startedAt: now() });
      set.headers["x-request-id"] = requestId;

      const path = new URL(request.url).pathname;
      if (!path.startsWith("/api/")) return;
      const limit = rateLimiter.consume();
      if (limit.allowed) return;

      set.status = 429;
      set.headers["retry-after"] = String(limit.retryAfterSeconds);
      return {
        error: { code: "RATE_LIMITED", message: "Too many requests" },
        requestId,
      };
    },
    onError({ request, set, code }: {
      readonly request: Request;
      readonly set: { headers: Record<string, string | number>; status?: number | string };
      readonly code: string | number;
    }) {
      const requestId = requests.get(request)?.requestId ?? requestIdFor(request);
      set.headers["x-request-id"] = requestId;
      if (code === "NOT_FOUND") {
        set.status = 404;
        return { error: { code: "NOT_FOUND", message: "Not found" }, requestId };
      }
      set.status = 500;
      return { error: { code: "INTERNAL_ERROR", message: "Internal server error" }, requestId };
    },
  };
}

function requestIdFor(request: Request): string {
  const supplied = request.headers.get("x-request-id");
  return supplied && /^[A-Za-z0-9_-]{1,64}$/.test(supplied) ? supplied : crypto.randomUUID();
}
