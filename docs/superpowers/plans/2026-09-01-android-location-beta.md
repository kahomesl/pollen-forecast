# Android Location UX and Beta Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Android client safe for opt-in nearby location selection and beta release preparation without changing pollen semantics.

**Architecture:** A pure matcher chooses a canonical location locally from the existing cached/network location list. A one-shot foreground-only Android client is isolated behind a small interface; Compose owns the user-triggered runtime permission flow and the view model owns candidate/confirmation state. Release validation remains Gradle configuration, while production checks are explicit backend/doc/CI slices.

**Tech Stack:** Kotlin/Compose/AndroidX Core/Room/DataStore/WorkManager; Bun/Elysia/PostgreSQL; GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-01-android-location-beta-design.md`

## Global Constraints

- Request only `ACCESS_COARSE_LOCATION` after a user taps `使用当前位置`; never request background location or obtain GPS in WorkManager.
- Never upload, persist, log, or place raw coordinates in test artifacts; persist only canonical `selectedLocationId` after explicit confirmation.
- Preserve TOTAL/ARTEMISIA, CURRENT/OBSERVATION, NETWORK/CACHE, severity, notification, Room cache, and manual location semantics.
- Use a 150 km maximum and the explicit Beijing district policy; nearest points are not administrative-boundary claims.
- Release URLs are explicit HTTPS only; signing values come only from environment variables; no production secret is committed.
- Do not add a provider, map SDK, account, health record, FCM, background location, or merge `main`.

---

### Task 1: Document the approved design

**Files:**
- Create: `docs/superpowers/specs/2026-09-01-android-location-beta-design.md`
- Create: `docs/superpowers/plans/2026-09-01-android-location-beta.md`

- [ ] Commit the reviewed design and implementation plan as a documentation-only save point.

### Task 2: Add pure supported-location matching

**Files:**
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/location/SupportedLocationMatcher.kt`
- Create: `android/app/src/test/java/com/kahomesl/allergenradar/location/SupportedLocationMatcherTest.kt`

- [ ] Write failing JUnit cases for equal coordinates, Beijing city preference, clearly nearer Chaoyang district, Xi'an, Shanghai, 150 km rejection, missing coordinates, and empty input.
- [ ] Implement `match(latitude: Double, longitude: Double, locations: List<LocationDto>): SupportedLocationMatch?` using Haversine and the city/district rule.
- [ ] Run `gradlew.bat :app:testDebugUnitTest` and commit the matcher slice.

### Task 3: Add one-shot location client and opt-in candidate UI

**Files:**
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/location/OneShotLocationClient.kt`
- Modify: `android/app/src/main/AndroidManifest.xml`
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/AppContainer.kt`
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/ui/viewmodel/ViewModels.kt`
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/ui/AllergenRadarApp.kt`
- Test: `android/app/src/androidTest/java/com/kahomesl/allergenradar/MainActivitySmokeTest.kt`

- [ ] Add a failing unit test for candidate confirmation writing only a canonical id and for cached-list provenance.
- [ ] Implement an interface returning one coordinate or a typed failure; Android implementation calls `LocationManagerCompat.getCurrentLocation` once with a cancellation signal and a ten-second timeout.
- [ ] Add coarse location manifest permission, permission launcher after the button tap only, denied/settings fallback, candidate copy, cache provenance copy, and explicit confirmation.
- [ ] Run focused Android tests and commit the vertical slice.

### Task 4: Verify deterministic device permission/location flows

**Files:**
- Create: `android/artifacts/phase-e/README.md`
- Create when captured: `android/artifacts/phase-e/location_permission.png`, `location_nearby_beijing.png`, `location_nearby_xian.png`, `location_unsupported.png`

- [ ] Use `adb emu geo fix` for Beijing, Chaoyang, Xi'an, and an unsupported coordinate; capture no raw coordinates in committed image metadata/readme.
- [ ] Reset/grant/revoke only the app's coarse permission; verify no first-launch dialog, denied fallback, confirmation, restart persistence, and manual picker availability.

### Task 5: Harden Android beta/release configuration

**Files:**
- Modify: `android/app/build.gradle.kts`
- Modify: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/res/xml/backup_rules.xml`
- Create: `android/app/src/main/res/xml/data_extraction_rules.xml`
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/ui/AllergenRadarApp.kt`
- Test: Gradle verification tasks and Android unit tests where applicable.

- [ ] Add version `0.1.0-beta.1`, increment version code, and display both values in About.
- [ ] Make release API URL absence/non-HTTPS/local targets fail with a precise Gradle exception; allow a command-line HTTPS validation value.
- [ ] Add environment-only release signing interface and a precise missing-signing failure for release package tasks.
- [ ] Include only canonical location/notification preference DataStores in backup rules; exclude the Room cache by omission; retain `usesCleartextTraffic=false` and documented disabled R8.
- [ ] Run debug test/lint/assemble plus release configuration checks and commit.

### Task 6: Add privacy, release, and production-readiness documentation

**Files:**
- Create: `android/docs/PRIVACY.md`
- Create: `android/docs/RELEASE_CHECKLIST.md`
- Create: `docs/PRODUCTION_READINESS.md`
- Modify: `android/README.md`

- [ ] State only observable behavior: no account, ads SDK, Firebase, analytics, coordinate upload/persistence, or health data.
- [ ] Record URL/signing/version/backup/R8/permission/offline/notification/device matrix gates.
- [ ] Audit DB initialization, sync CLI/scheduler, CORS, provider timeout, source licensing, logging, and deployment configuration; label unverified redistribution rights as legal blockers.
- [ ] Commit documentation as its own slice.

### Task 7: Add production health and startup-scrape guard

**Files:**
- Create: `backend/src/health.ts`
- Create: `backend/src/health.test.ts`
- Create: `backend/src/services/startupScrape.ts`
- Create: `backend/src/services/startupScrape.test.ts`
- Modify: `backend/src/db.ts`
- Modify: `backend/src/index.ts`

- [ ] Write Bun tests for health response shapes and startup-scrape environment gating.
- [ ] Add `GET /health` with status/timestamp/database boolean only; it must not scrape.
- [ ] Change legacy startup scrape to explicit `POLLEN_STARTUP_SCRAPE_ENABLED=true`; leave `bun run sync:pollen` and the optional scheduler intact.
- [ ] Run `bun test` and commit the backend slice.

### Task 8: Add feature-branch CI and final matrix

**Files:**
- Create: `.github/workflows/ci.yml`
- Modify as needed: `package.json`, `tsconfig.backend.json`

- [ ] Add Bun backend typecheck/test and frontend build jobs, plus Android JDK/wrapper unit/lint/assemble job; do not run emulator tests on hosted CI.
- [ ] Run local `bun test`, backend typecheck, frontend build, Android unit/lint/assemble, API 35/API 36 connected tests, release failure/HTTPS checks, APK signature, and diff check.
- [ ] Commit CI and evidence, push `feature/allergen-platform`, then report the matrix and remaining external legal/deployment blockers.
