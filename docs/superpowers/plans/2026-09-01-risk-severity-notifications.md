# Normalized Risk Severity and Android Notifications Implementation Plan

> **For agentic workers:** Implement in tested vertical slices and commit each independently verifiable slice.

**Goal:** Add provider-evidence-based API severity and opt-in local Android risk alerts without altering pollen data semantics.

**Architecture:** API v1 derives severity from a central backend normalizer at serialization time. Android safely decodes and caches it, while a pure alert evaluator gates the existing refresh worker's NETWORK observations before a local publisher persists a per-target fingerprint.

**Tech Stack:** Bun/Elysia/TypeScript; Kotlin/Compose/Room/DataStore/WorkManager/NotificationManager.

**Spec:** `docs/superpowers/specs/2026-09-01-risk-severity-notifications-design.md`

## Global Constraints

- Keep `/api/v1` additive; preserve raw risk and all legacy APIs.
- Never compare WeatherDT `level` units with Beijing `index` ranges.
- Never use TOTAL as Artemisia, infer severity from values/ranges, or turn no data into LOW.
- Alerts remain opt-in, local, NETWORK-only, and exclude ESTIMATE.
- No destructive Room migration, second periodic worker, Firebase/FCM, medical advice, or main merge.

### Task 1: Correct the contract and establish normalizer evidence

**Files:** `docs/API_V1.md`, `docs/DATA_SOURCE_NOTES.md`, `backend/src/domain/riskSeverity.ts`, backend tests.

- [ ] Replace the invalid TOTAL/ARTEMISIA Common observation with WeatherDT TOTAL without taxon and add a separate Beijing GENUS Artemisia forecast example.
- [ ] Record the verified WeatherDT label/code pairs and the conflicting Beijing legend/classified response.
- [ ] Write failing tests proving WeatherDT mappings, unknown handling, value/range independence, and TOTAL/taxon isolation.
- [ ] Add the provider-specific normalizer and run focused Bun tests.
- [ ] Commit the documentation correction and backend normalizer separately.

### Task 2: Add the v1 severity extension

**Files:** `backend/src/api/allergenV1.ts`, API tests, `docs/RISK_SEVERITY.md`.

- [ ] Write current and history serialization tests for retained raw risk plus severity, including legacy records.
- [ ] Add `risk.severity` through the serializer only; do not add a database column.
- [ ] Run the backend suite and commit.

### Task 3: Consume and persist severity on Android

**Files:** Android DTOs, Room entity/database/cache, AppContainer, contract/migration tests.

- [ ] Write failing decoder tests for absent/unknown enum values and an instrumented v1 database migration preservation test.
- [ ] Add resilient `RiskSeverityDto`, `riskSeverity` cache column, version 2 migration, and explicit `addMigrations(MIGRATION_1_2)`.
- [ ] Add minimal consistent severity color/unknown copy to observation cards and history rows.
- [ ] Run focused unit/instrumented tests and commit.

### Task 4: Add opt-in alert settings and pure policy

**Files:** DataStore settings, alert evaluator/policy tests, My screen and manifest.

- [ ] Write pure failing tests for defaults, thresholds, targets, unknown/cache/estimate suppression, copy, fingerprints, and escalation.
- [ ] Implement typed settings and a pure evaluator with no Android framework dependency.
- [ ] Add the My settings controls and request POST_NOTIFICATIONS only from an attempted opt-in; present denial state.
- [ ] Run unit tests and commit.

### Task 5: Integrate local publishing into the existing worker

**Files:** local notification publisher/coordinator, refresh worker/application/container, worker tests.

- [ ] Write failing worker/coordinator tests for NETWORK notifications, CACHE suppression, dedupe, and empty Artemisia.
- [ ] Reuse `allergen-background-refresh` results, publish only evaluator-approved alerts, and persist fingerprints after publishing.
- [ ] Add the one normal-importance channel and launcher PendingIntent; run focused tests and commit.

### Task 6: Verify and document

**Files:** `android/docs/NOTIFICATIONS.md`, `android/README.md`, `android/artifacts/phase-d/README.md`.

- [ ] Run `bun test`, TypeScript checks, Android unit/lint/assemble/connected tests, `git diff --check`, and apksigner verification.
- [ ] On Pixel 9 Pro XL, verify permission timing, TOTAL/Artemisia notification fixtures, dedupe/escalation, cache/unknown/estimate suppression, preference restart, and the live `10.0.2.2:8080` smoke.
- [ ] Commit documentation/evidence, push `feature/allergen-platform`, and report the evidence and known unknowns.
