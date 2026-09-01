export type RuntimeEnvironment = "development" | "test" | "staging" | "production";

export interface ProviderEnabledConfig {
  readonly weatherDt: boolean;
  readonly beijingPollen: boolean;
}

export interface RuntimeConfig {
  readonly environment: RuntimeEnvironment;
  readonly databaseUrl: string;
  readonly port: number;
  readonly allowedOrigins: readonly string[];
  readonly logLevel: LogLevel;
  readonly rateLimitWindowSeconds: number;
  readonly rateLimitMax: number;
  readonly externalPollenFetchEnabled: boolean;
  readonly providerEnabled: ProviderEnabledConfig;
}

export type LogLevel = "debug" | "info" | "warn" | "error";

const DEVELOPMENT_ORIGINS = ["http://localhost:5173", "http://127.0.0.1:5173"] as const;

export function parseRuntimeConfig(env: Record<string, string | undefined> = process.env): RuntimeConfig {
  const environment = parseEnvironment(env.NODE_ENV);
  const databaseUrl = parseDatabaseUrl(env.DATABASE_URL);
  const externalPollenFetchEnabled = parseBoolean(
    env.EXTERNAL_POLLEN_FETCH_ENABLED,
    environment === "development",
    "EXTERNAL_POLLEN_FETCH_ENABLED",
  );

  return {
    environment,
    databaseUrl,
    port: parseInteger(env.PORT, 8080, 1, 65_535, "PORT"),
    allowedOrigins: parseAllowedOrigins(env.ALLOWED_ORIGINS, environment),
    logLevel: parseLogLevel(env.LOG_LEVEL),
    rateLimitWindowSeconds: parseInteger(env.RATE_LIMIT_WINDOW_SECONDS, 60, 1, 3_600, "RATE_LIMIT_WINDOW_SECONDS"),
    rateLimitMax: parseInteger(env.RATE_LIMIT_MAX, 120, 1, 10_000, "RATE_LIMIT_MAX"),
    externalPollenFetchEnabled,
    providerEnabled: {
      weatherDt: parseBoolean(
        env.POLLEN_PROVIDER_WEATHERDT_ENABLED,
        externalPollenFetchEnabled && environment === "development",
        "POLLEN_PROVIDER_WEATHERDT_ENABLED",
      ) && externalPollenFetchEnabled,
      beijingPollen: parseBoolean(
        env.POLLEN_PROVIDER_BEIJING_ENABLED,
        externalPollenFetchEnabled && environment === "development",
        "POLLEN_PROVIDER_BEIJING_ENABLED",
      ) && externalPollenFetchEnabled,
    },
  };
}

function parseEnvironment(value: string | undefined): RuntimeEnvironment {
  if (value === undefined || value === "") return "development";
  if (value === "development" || value === "test" || value === "staging" || value === "production") return value;
  throw new Error("invalid NODE_ENV");
}

function parseDatabaseUrl(value: string | undefined): string {
  if (!value) throw new Error("missing DATABASE_URL");
  try {
    const parsed = new URL(value);
    if (parsed.protocol !== "postgres:" && parsed.protocol !== "postgresql:") throw new Error();
    return value;
  } catch {
    throw new Error("invalid DATABASE_URL");
  }
}

function parseAllowedOrigins(value: string | undefined, environment: RuntimeEnvironment): readonly string[] {
  if (!value) {
    if (environment === "development" || environment === "test") return DEVELOPMENT_ORIGINS;
    throw new Error("missing ALLOWED_ORIGINS");
  }

  const origins = value.split(",").map((origin) => origin.trim()).filter(Boolean);
  if (origins.length === 0 || origins.some((origin) => !isValidOrigin(origin, environment))) {
    throw new Error("invalid ALLOWED_ORIGINS");
  }
  return [...new Set(origins)];
}

function isValidOrigin(origin: string, environment: RuntimeEnvironment): boolean {
  if (origin === "*") return false;
  try {
    const parsed = new URL(origin);
    if (parsed.origin !== origin || (parsed.protocol !== "http:" && parsed.protocol !== "https:")) return false;
    return environment === "development" || environment === "test" || parsed.protocol === "https:";
  } catch {
    return false;
  }
}

function parseLogLevel(value: string | undefined): LogLevel {
  if (value === undefined || value === "") return "info";
  if (value === "debug" || value === "info" || value === "warn" || value === "error") return value;
  throw new Error("invalid LOG_LEVEL");
}

function parseBoolean(value: string | undefined, fallback: boolean, name: string): boolean {
  if (value === undefined || value === "") return fallback;
  if (value === "true") return true;
  if (value === "false") return false;
  throw new Error(`invalid ${name}`);
}

function parseInteger(value: string | undefined, fallback: number, min: number, max: number, name: string): number {
  if (value === undefined || value === "") return fallback;
  if (!/^\d+$/.test(value)) throw new Error(`invalid ${name}`);
  const parsed = Number(value);
  if (!Number.isSafeInteger(parsed) || parsed < min || parsed > max) throw new Error(`invalid ${name}`);
  return parsed;
}
