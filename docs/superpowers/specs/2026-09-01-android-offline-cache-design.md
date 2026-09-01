# Android Offline Cache Design

## Goal

Allow the Android client to retain the last successful result for locations, current pollen queries, and history queries without ever presenting cached data as a current network result.

## Boundaries

`NetworkAllergenRepository` remains the HTTP adapter. `OfflineFirstAllergenRepository` owns the policy: a successful HTTP response is authoritative and replaces the matching Room query cache in one transaction; only `IOException` and `ApiException` status 500–599 can fall back to Room. Structured 4xx errors always propagate.

`RepositoryResult<T>` is the UI-facing contract. It carries data, a source (`NETWORK` or `CACHE`), and the local cache write time when its source is `CACHE`. It does not expose Retrofit or Room details.

## Room schema (version 1)

- `location_cache`: canonical `locationId`, name, scope, and optional coordinates.
- `observation_cache`: one flattened API observation per `(cacheKey, observationId)`, including every nullable time and numeric field from API v1 plus local `cachedAt`.
- `query_cache_metadata`: one row per query identity with `cacheKey`, location, response kind, cache time, latest upstream `retrievedAt`, and serialised provider-error ids where applicable.

Keys are namespaced and never shared between locations or taxa:

- `locations`
- `current:{locationId}:total`
- `current:{locationId}:taxon:{taxonCode}`
- `history:{locationId}:taxon:{taxonCode|ALL}:measurement:{measurementType|ALL}`

An empty successful response still clears all observations for that exact cache key and writes metadata. Consequently an empty online ARTEMISIA response cannot revive a prior cache entry.

## UI and background behaviour

Home and location screens display a text cache marker with the cached time only for `CACHE` results. History displays an offline-history marker. Cache markers are never shown for network responses and no freshness threshold is invented.

`AllergenRefreshWorker` refreshes only the selected location’s TOTAL and ARTEMISIA current keys through the offline-first repository. The unique periodic work name is `allergen-background-refresh`; it runs every two hours with `NetworkType.CONNECTED` and exponential backoff. Empty responses finish successfully, transient failures retry, and 4xx responses fail. No risk notification is implemented.

## Verification

Room instrumentation tests use an in-memory database and a deterministic fake network repository. They cover entity round trips, cache isolation, authoritative empty responses, fallback eligibility, locations, and worker result mapping. Pixel 9 Pro XL E2E additionally exercises the real `10.0.2.2:8080` backend plus offline restart and recovery.
