# Android Location UX and Beta Readiness Design

## Scope

Phase E adds an explicitly user-triggered nearby-supported-location flow and release-readiness safeguards. It preserves the existing canonical-location API, manual picker, TOTAL/ARTEMISIA separation, cache provenance, notification policy, and the single background worker.

## Location flow

The Location screen gets a `使用当前位置` action. It is the only code path that requests `ACCESS_COARSE_LOCATION`; first launch makes no location request. After permission is granted, the app requests one foreground location through `LocationManagerCompat.getCurrentLocation`, cancels after 10 seconds, and feeds latitude/longitude directly into a pure matcher. Raw coordinates never enter repository calls, Room, DataStore, logs, analytics, artifacts, or backend requests.

The app first uses the existing `AllergenDataRepository.getLocations()` result. A `NETWORK` list is cached by the existing offline-first repository; on an eligible failure, the existing Room `location_cache` list is returned with source `CACHE`. The nearby result renders `基于离线缓存的位置列表匹配` when appropriate. A match is merely a nearest canonical point and the UI says `附近支持位置`, never an administrative-boundary claim. Selecting a candidate requires an explicit `使用此位置` confirmation, which writes only `selectedLocationId`.

## Matching policy

`SupportedLocationMatcher` is pure Kotlin and uses Haversine distance with Earth radius 6371.0088 km. A candidate farther than 150 km is unsupported. The threshold matches the prior legacy nearest-city guard while preventing a location in another country or a distant unsupported Chinese area from being silently assigned data.

For Beijing, the matcher calculates the closest city and district points separately. A district is selected only if it is at least 5 km closer than the closest city; otherwise the city is selected. This is a display/matching policy, not administrative identification. Locations lacking either latitude or longitude are ignored; an empty eligible set is unsupported.

## Android platform and privacy

The manifest declares foreground `ACCESS_COARSE_LOCATION` only. There is no FINE, background location, location worker, listener, geofence, or location upload. `LocationManagerCompat.getCurrentLocation` is an asynchronous one-shot API that supplies a cancellation signal; the client converts null, disabled providers, `SecurityException`, and timeout to user-facing nonblocking states.

The app uses Android backup include rules: only the two non-sensitive DataStore preference files (canonical selected location and notification settings) participate in cloud/device transfer. The Room pollen cache and all other app files remain excluded. R8 remains disabled for this beta because no release-specific reflection rules have been validated.

## Release configuration

Debug keeps its emulator-only default `http://10.0.2.2:8080/`. Any release-variant API URL must be explicitly provided as `API_BASE_URL`, must be HTTPS, and must not be localhost, `10.0.2.2`, or the previous placeholder. A release assemble/bundle additionally requires all four `ALLERGENRADAR_RELEASE_*` signing environment variables; signing material is never written to source control. The beta version is `0.1.0-beta.1` with a monotonically increased version code.

## Backend readiness

`GET /health` is a lightweight probe: it returns only status, timestamp, and a boolean database availability signal, and never invokes scrape/sync. Legacy startup scraping becomes explicitly opt-in through an environment flag; the existing `bun run sync:pollen` command remains the production-safe manual/external-scheduler entry point. Production documentation records unresolved source redistribution rights as legal blockers rather than asserting a license.

## Verification

Pure matcher tests cover zero distance, Beijing city/district policy, Xi'an, Shanghai, missing coordinates, empty input, and the distance guard. Permission and deterministic emulator-location flows run on Pixel 9 Pro XL API 35 and API 36. Release URL checks run without a real production URL; a test HTTPS value is command-line only. The final gate includes backend tests/typecheck/build, Android unit/lint/assemble, both connected matrices, APK signing, and `git diff --check`.
