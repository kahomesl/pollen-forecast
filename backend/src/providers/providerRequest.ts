export type ProviderRequestErrorType = "TIMEOUT" | "NETWORK" | "HTTP";

export class ProviderRequestError extends Error {
  constructor(readonly errorType: ProviderRequestErrorType) {
    super("Provider request failed");
    this.name = "ProviderRequestError";
  }
}

export function getProviderTimeoutMs(value = process.env.POLLEN_PROVIDER_TIMEOUT_MS): number {
  if (typeof value !== "string" || !/^\d+$/.test(value)) return 10_000;

  const timeoutMs = Number(value);
  if (!Number.isInteger(timeoutMs) || timeoutMs < 1_000) return 10_000;
  return Math.min(timeoutMs, 60_000);
}

export async function fetchProviderResponse(
  fetchImpl: (url: string, options?: RequestInit) => Promise<Response>,
  url: string,
  options: RequestInit,
): Promise<Response> {
  let response: Response;
  try {
    response = await fetchImpl(url, options);
  } catch (error) {
    if (isTimeoutError(error)) throw new ProviderRequestError("TIMEOUT");
    throw new ProviderRequestError("NETWORK");
  }

  if (response.status >= 500) throw new ProviderRequestError("HTTP");
  return response;
}

function isTimeoutError(error: unknown): boolean {
  return error instanceof Error && (error.name === "TimeoutError" || error.name === "AbortError");
}
