# Pollen observation persistence

`pollen_observations` is an additive normalized-record table. It does not
replace `pollen_data`, `scrape_log`, or `pollen_ratings`; legacy APIs continue
to use their existing tables.

## Identity and upsert

- `id` is the provider's normalized stable observation identity and is the
  primary key.
- Repeated successful provider fetches use `INSERT ... ON CONFLICT (id) DO
  UPDATE` to refresh values, ranges, risk, source, confidence, temporal fields,
  `updated_at`, and `stored_at` without adding duplicate rows.
- `created_at` preserves the provider-normalized creation timestamp on the
  first insert. `stored_at` records when this platform last wrote the row.

## Semantics and nullable time

- `OBSERVATION` is a confirmed direct observation.
- `CURRENT` is provider-published current-period data whose production method
  is not confirmed as direct observation.
- `FORECAST` is a prediction, and `ESTIMATE` is a platform-derived estimate.
- `observed_at`, `valid_from`, and `valid_to` are nullable. In particular,
  Beijing classified forecasts leave validity fields empty while public upstream
  `title`, `dataTime`, and `vti` semantics are unresolved.

## Read and cache policy

Current allergen routes fetch Providers first, then persist successful
normalized observations. If persistence fails, the live Provider response is
still returned and the API does not expose database details.

The current routes never fall back to stored records. If an upstream Provider
returns no data, including Beijing `isValid=false`, the current endpoint returns
an empty collection. Stored data is only returned by the explicit history route.

Schema initialization remains additive `CREATE ... IF NOT EXISTS`. A production
deployment should move this schema to a reviewed migration system before
introducing destructive or versioned schema changes.
