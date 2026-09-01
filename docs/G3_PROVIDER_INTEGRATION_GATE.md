# Phase G3 provider integration gate

No provider may be enabled for public/staging external polling until every item below is recorded as approved.

1. Written provider permission is stored outside Git and referenced in `DATA_LICENSE_REGISTER.md` without secrets.
2. Rights explicitly cover the intended taxon, China geography, collection, persistence, Android cache, public display, notifications/derived severity, redistribution, commercial use, attribution, retention, rate limits, and SLA/support.
3. Taxon mapping is source-verifiable. Never map TOTAL, weed, 菊科, or another broad category to ARTEMISIA.
4. Provider sample responses and terms are archived in the restricted evidence store, and parsing has fixture tests for valid, malformed, unavailable, and rate-limited responses.
5. A provider adapter uses bounded timeouts, error classification, safe structured logs, capability declaration, and an explicit runtime gate defaulting off outside development.
6. API/Android behaviour preserves `CURRENT`, `FORECAST`, `ESTIMATE`, `UNKNOWN`, source attribution, and empty Artemisia semantics. CURRENT must not become OBSERVATION.
7. Privacy, retention, attribution, alerting, observability, rollback, and release documentation are updated and reviewed.

Only after all gates pass may a separately approved G3 change create or enable a provider adapter. No automatic fallback may infer ARTEMISIA from a broad pollen value.
