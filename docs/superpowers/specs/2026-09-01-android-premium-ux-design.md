# Android premium UX and product-semantics design

## Goal

Ship a premium, restrained Android beta UI without weakening the established
data, cache, notification, location, or release contracts from Phases A–E.

## Design read

This is a native environmental-health app for people making everyday decisions
from imperfect data. It uses quiet, warm surfaces, deep green identity, clear
risk hierarchy, modest motion, and Android-native navigation. Reference images
set the compositional and craft bar; API and Provider facts determine all
visible product claims.

## Product truth model

The client must never infer Artemisia availability from a location name or ID.
`GET /api/v1/locations` will add a backward-compatible `taxonAvailability`
collection. Each entry names a taxon and one of:

- `UNSUPPORTED`: no provider currently supports that taxon at this canonical
  location.
- `CHILD_LOCATION_REQUIRED`: this location is an aggregate; a known child
  location is required for the taxon. It carries the canonical child scope and
  a public label for the picker.
- `SUPPORTED`: one or more registered providers support the taxon at this
  location.

The server derives this from Provider `supportsLocation` declarations and
explicit location hierarchy metadata. Android combines `SUPPORTED` with the
authoritative query result: an online empty result means "当前时段暂无有效蒿属预报";
an I/O/5xx fallback result remains a cache/offline state. An `UNKNOWN` severity
shows the raw data but never a fabricated lower-risk label or scale position.

## UI architecture

`ui/designsystem` owns semantic tokens and focused Compose primitives:
colors, typography, spacing, shapes, elevation, card, list-row, state banner,
risk badge/scale, source/data-type badges, buttons, empty/error/loading states,
and navigation chrome. Feature screens remain presentation-only functions in
`ui/screens`, while `AllergenRadarApp` owns app navigation and coordinates
existing view models and permission launchers.

Tokens use the fixed 4/8/12/16/20/24/32 dp spacing scale; 12/16/24/full shapes;
warm off-white and ink-green light surfaces; soft charcoal/forest dark
surfaces; and severity colors solely for normalized risk. Material 3 supplies
native behavior and accessibility, but the visual hierarchy is custom.

## Page behavior

- Home: full-width total card, source/time/risk facts, Artemisia availability
  card, a compact data-meaning link, partial-source notice, cache marker, and
  a content-preserving refresh state.
- Location: user-triggered coarse location, nearest supported candidate,
  cache provenance, search, and explicit city/district grouping. The Beijing
  action scrolls the existing district group into view; it never chooses a
  district.
- History: real filters and rows with source, retrieval time, cache provenance,
  measurement labels, and UNKNOWN risk honesty.
- My: location, opt-in notification settings, denied-notification settings
  handoff, simple synchronization wording, privacy, data explanation, and
  version from `BuildConfig`.
- Information: editorial but factual explanations of TOTAL vs Artemisia,
  measurement types, availability, risk normalization, sources, privacy, and
  the non-medical disclaimer.

## Non-goals and invariants

No provider expansion, map, account, data fabrication, taxonomy shortcut,
schema change, notification-policy change, location tracking, or release
configuration relaxation. Existing cache authority, WorkManager, Room v2
migration, explicit permission flows, and API v1 fields remain intact.

## Verification

Backend unit tests establish availability metadata. Android unit and
instrumentation tests cover semantic state selection, labels, cache behavior,
permission behavior, notifications, Room v2, and navigation. Final validation
uses API 35 and API 36 emulators, light/dark screenshots, font scaling,
`bun test`, typecheck/build, Android unit/lint/assemble/connected tests,
`apksigner verify`, and `git diff --check`.
