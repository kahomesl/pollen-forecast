# Android Offline Cache Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an offline-first Room cache and two-hour selected-location refresh without changing pollen semantics.

**Architecture:** The network repository remains responsible for API v1 conversion. An offline-first wrapper writes authoritative network results to Room and returns `RepositoryResult`; it reads only the matching key after an I/O or 5xx failure. View models receive source metadata, while WorkManager invokes the same repository path.

**Tech Stack:** Kotlin, Room 2.8.4 with KSP, WorkManager 2.11.2, Retrofit/OkHttp, Compose, DataStore.

**Spec:** `docs/superpowers/specs/2026-09-01-android-offline-cache-design.md`

## Global Constraints

- Keep one Android app module and preserve the frozen API v1 network contract.
- TOTAL and ARTEMISIA use distinct query keys; CURRENT is not OBSERVATION.
- Network HTTP 200, including an empty observation list, replaces the exact cache key.
- Cache fallback is allowed only for `IOException` or HTTP 5xx and must be visible in UI.
- Do not add notifications, a stale threshold, a backend endpoint, or a `main` merge.
- Validate with Pixel 9 Pro XL API 35 and push only `feature/allergen-platform`.

---

### Task 1: Repair the Phase B Back assertion

**Files:**
- Modify: `android/app/src/androidTest/java/com/kahomesl/allergenradar/MainActivitySmokeTest.kt`

- [ ] Make the test enter Data Explanation, assert `综合花粉不等于蒿属`, send system Back, assert that body no longer exists, and assert the My-only `最近同步` card exists.
- [ ] Run `gradlew.bat :app:connectedDebugAndroidTest` after the cache work is compiled.

### Task 2: Add Room cache schema and reversible DTO mapping

**Files:**
- Modify: `android/gradle/libs.versions.toml`, `android/build.gradle.kts`, `android/app/build.gradle.kts`
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/data/local/AllergenRadarDatabase.kt`
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/data/local/CacheEntities.kt`
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/data/local/CacheDao.kt`
- Test: `android/app/src/androidTest/java/com/kahomesl/allergenradar/data/local/RoomCacheTest.kt`

- [ ] Write in-memory Room tests for a TOTAL CURRENT round trip, ARTEMISIA FORECAST round trip, and nullable valid range fields; run them red.
- [ ] Add Room version 1 entities, DAO, database and mappers; use composite `(cacheKey, observationId)` identity and replace-query transactions.
- [ ] Re-run the Room tests green and commit `feat(android): add offline observation cache`.

### Task 3: Add offline-first repository semantics

**Files:**
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/data/AllergenRepository.kt`, `android/app/src/main/java/com/kahomesl/allergenradar/AppContainer.kt`
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/data/OfflineFirstAllergenRepository.kt`
- Test: `android/app/src/androidTest/java/com/kahomesl/allergenradar/data/OfflineFirstAllergenRepositoryTest.kt`

- [ ] Write tests for empty ARTEMISIA clearing an old cache, I/O cache fallback, 4xx propagation, isolated TOTAL/ARTEMISIA/history keys, and locations offline fallback; run red.
- [ ] Define `RepositoryResult<T>` and `RepositoryDataSource`; make network responses `NETWORK` and implement only eligible cache fallback as `CACHE`.
- [ ] Re-run focused tests green and commit `feat(android): add offline-first repository`.

### Task 4: Render cache provenance without changing pollen semantics

**Files:**
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/ui/viewmodel/ViewModels.kt`, `android/app/src/main/java/com/kahomesl/allergenradar/ui/AllergenRadarApp.kt`
- Test: `android/app/src/test/java/com/kahomesl/allergenradar/ui/viewmodel/HomeViewModelTest.kt`

- [ ] Write failing view-model assertions that online ARTEMISIA empty is not cache data and cached results expose timestamp/source.
- [ ] Pass repository result metadata to Home, Location and History UI states; show text labels `当前显示离线缓存数据` and `离线历史缓存` only for cache.
- [ ] Re-run unit tests and commit `feat(android): show cached pollen results`.

### Task 5: Schedule background selected-location refresh

**Files:**
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/work/AllergenRefreshWorker.kt`
- Create: `android/app/src/main/java/com/kahomesl/allergenradar/work/AllergenRefreshScheduler.kt`
- Modify: `android/app/src/main/java/com/kahomesl/allergenradar/AllergenRadarApplication.kt`, `android/app/src/main/AndroidManifest.xml`
- Test: `android/app/src/androidTest/java/com/kahomesl/allergenradar/work/AllergenRefreshWorkerTest.kt`

- [ ] Write deterministic worker tests for connected two-hour request, empty success, I/O retry, and 4xx failure; run red.
- [ ] Add unique periodic work with `NetworkType.CONNECTED`, `ExistingPeriodicWorkPolicy.UPDATE`, and ten-minute exponential backoff; worker refreshes TOTAL and ARTEMISIA only through the repository.
- [ ] Re-run worker tests green and commit `feat(android): schedule pollen cache refresh`.

### Task 6: Document and verify on the Pixel 9 Pro XL

**Files:**
- Modify: `android/README.md`
- Create: `android/docs/OFFLINE_CACHE.md`
- Create: `android/artifacts/phase-c/README.md`

- [ ] Document schema, keys, source rules, Artemisia empty semantics, WorkManager policy, and notification deferral.
- [ ] Install Debug APK on `emulator-5556`; verify live backend, cached offline restart, recovery, and online-empty no-fallback.
- [ ] Run `testDebugUnitTest`, `lintDebug`, `assembleDebug`, `connectedDebugAndroidTest`, `git diff --check`, and `apksigner verify`; commit docs/evidence and push all Phase C commits.
