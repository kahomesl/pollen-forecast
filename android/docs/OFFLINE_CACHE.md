# Offline cache

## Scope

Room database `AllergenRadarDatabase` is version **1** and remains inside the app module. It stores:

- `location_cache`: canonical locations used by the location picker.
- `observation_cache`: flattened API v1 observations, including nullable taxon, value range, risk, provider, source,
  confidence, upstream `retrievedAt`, optional observed/validity times, and local `cachedAt`.
- `query_cache_metadata`: query identity, query write time, latest upstream `retrievedAt`, and current-query provider
  errors.

`retrievedAt` is the platform's upstream retrieval timestamp; `cachedAt` is when this Android app wrote Room. They are
never interchangeable.

## Query keys

Keys are exact and isolated:

| Query | Key |
| --- | --- |
| Locations | `locations` |
| TOTAL current | `current:{locationId}:total` |
| Taxon current | `current:{locationId}:taxon:{taxonCode}` |
| History | `history:{locationId}:taxon:{taxonCode\|ALL}:measurement:{measurementType\|ALL}` |

Therefore Beijing TOTAL, Chaoyang ARTEMISIA, and every history filter cannot overwrite each other.

## Network and empty-result semantics

`OfflineFirstAllergenRepository` returns `RepositoryResult` with source `NETWORK` or `CACHE`.

- HTTP 200 is authoritative and is written transactionally to Room.
- HTTP 200 with `observations: []` deletes all observations for that exact key and retains an empty metadata result.
  It does not revive prior cache data.
- `IOException`, timeout, DNS/connection failures, and HTTP 5xx may read only the matching cached result.
- Explicit 4xx API contract errors, including location/taxon/measurement/limit errors, are propagated and never read
  cache data.

This keeps TOTAL distinct from ARTEMISIA, preserves `CURRENT != OBSERVATION`, and ensures that no data is never shown
as low risk.

## UI behaviour

When Room is used, Home card text says `当前显示离线缓存数据` or `离线缓存的蒿属数据` with the local cache
time. The location screen says `当前显示离线缓存位置`; History says `离线历史缓存`. Network responses carry no
cache marker. There is deliberately no universal fresh/stale threshold.

For Artemisia:

1. Online data is displayed as a network result.
2. Online empty data displays `暂无蒿属独立数据`, even if an older local result exists.
3. Network failure with cache displays `离线缓存的蒿属数据`.
4. Network failure without cache displays `离线且暂无缓存的蒿属数据`.

DataStore continues to own only `selectedLocationId`; Room owns the cached location list and query snapshots.

## Background refresh

`AllergenRefreshWorker` calls the same offline-first repository used by the UI. It refreshes only the selected
location's TOTAL and ARTEMISIA current queries. `AllergenRefreshScheduler` registers unique periodic work named
`allergen-background-refresh` every two hours with `NetworkType.CONNECTED`, `ExistingPeriodicWorkPolicy.UPDATE`, and
ten-minute exponential backoff.

An empty ARTEMISIA response succeeds. A transient failure (including a cache fallback, so WorkManager can try again)
retries. An explicit 4xx fails. The app still refreshes immediately when opened; WorkManager is supplementary.

## Deliberate deferral

Phase C does **not** add high-risk alerts, Artemisia notifications, or medical advice notifications. The backend has
not yet defined a normalized severity contract. Notifications remain a later phase after that contract exists.
