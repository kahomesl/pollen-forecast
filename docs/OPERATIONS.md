# Operations runbook

## Safe runtime modes

Development defaults to local browser CORS and external pollen fetches for existing developer workflows. Staging and production require explicit HTTPS `ALLOWED_ORIGINS` and default all external pollen activity off. Enable a provider only after its G3 gate and written authorization are complete.

Relevant environment variables: `DATABASE_URL`, `NODE_ENV`, `PORT`, `ALLOWED_ORIGINS`, `LOG_LEVEL`, `RATE_LIMIT_WINDOW_SECONDS`, `RATE_LIMIT_MAX`, `EXTERNAL_POLLEN_FETCH_ENABLED`, `POLLEN_PROVIDER_WEATHERDT_ENABLED`, `POLLEN_PROVIDER_BEIJING_ENABLED`, `POLLEN_BACKGROUND_SYNC_ENABLED`, and `POLLEN_STARTUP_SCRAPE_ENABLED`.

## Routine checks

- Probe `GET /health`; it reports PostgreSQL readiness and returns 503 on failure without contacting providers.
- Review structured `provider_sync_completed` records for provider ID, status, duration, timeout, and received/persisted row counts. Do not add raw request, provider response, credential, IP, or GPS logging.
- Use `bun run migrate:db` once per deployment before scaling the app. Current migrations are idempotent schema initialization, not a replica-safe automatic startup migration protocol.
- Keep background sync and startup scrape off for controlled beta. Manual sync only uses runtime-enabled providers.

## Incident and rollback

If a release fails readiness, stop rollout, retain the request IDs and non-sensitive deployment logs, restore the last known-good application image, and validate database compatibility before any data rollback. Database restore, provider disablement, legal escalation, and user communication require the named operator/owner outside Git. Never solve an incident by exposing provider keys, writing sensitive diagnostics, or turning broad CORS on.
