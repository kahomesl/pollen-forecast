# Controlled-beta staging deployment

Staging is an isolated, non-public verification environment. It is not a production release and must not be connected to a production database.

## Required configuration

Use an isolated `DATABASE_URL`, `NODE_ENV=staging`, explicit HTTPS `ALLOWED_ORIGINS`, `EXTERNAL_POLLEN_FETCH_ENABLED=false`, `POLLEN_PROVIDER_WEATHERDT_ENABLED=false`, and `POLLEN_PROVIDER_BEIJING_ENABLED=false`. Keep `POLLEN_BACKGROUND_SYNC_ENABLED=false` and `POLLEN_STARTUP_SCRAPE_ENABLED=false`.

The backend does not read secrets from this repository. Store `DATABASE_URL` only in the chosen platform secret store. Do not copy provider credentials into staging until a written licence and an approved G3 implementation exist.

## Deployment order

1. Build the container and run it with the required environment variables.
2. Run `bun run migrate:db` exactly once against the isolated staging database.
3. Start one application replica, probe `GET /health`, then run read-only API smoke checks.
4. Record the deployment time, image digest, migration output, and responsible operator in the external release record.

`/health` is dependency-aware readiness: it checks only PostgreSQL and returns 503 if unavailable. It never starts a scrape or provider sync. Docker's health check uses this endpoint. A separate process liveness endpoint is not currently needed because the runtime and orchestrator own process supervision.

## Controlled-beta data rule

With external fetching disabled, APIs may return data already present in the isolated database but must not refresh WeatherDT, Beijing, QWeather, or any other external pollen source. Use synthetic/fixture data only for beta notification and UI verification.
