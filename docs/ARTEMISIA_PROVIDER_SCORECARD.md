# Artemisia provider scorecard

**Phase:** G1
**Last verified:** 2026-09-01
**Scope:** China ARTEMISIA provider due diligence, not an implementation ranking.

## Scoring method

Each dimension is 0–5. Scores measure publicly verifiable suitability for this
project, not vendor quality in general. `?` is conservatively scored as 0–2.

| Dimension | Meaning |
| --- | --- |
| China coverage | A documented response/service for China, then priority cities. |
| Artemisia precision | Explicit taxon at the queried China location; category-only data scores 0. |
| Semantic clarity | Clear observation/forecast/estimate type, issue time, and methodology. |
| Temporal freshness | Documented update cadence and useful horizon/history. |
| Spatial resolution | Publicly specified grid/location granularity for the relevant product. |
| Licensing clarity | Explicit cache, persistence, redistribution, consumer display, and attribution terms. |
| Commercial viability | Publicly usable commercial entitlement and cost path. |
| Technical reliability | Documented sources/method and usable operational evidence. |
| Cost | 5 = predictable/low public cost; 0 = no price or no usable entitlement. |

**Hard gates:** licensing clarity `<=1` cannot be recommended; Artemisia precision
`<=2` cannot be used as `ARTEMISIA`. “Latest” is not an observation without a
measurement statement.

## Scores

| Candidate | China | Precision | Semantics | Freshness | Spatial | Licence | Commercial | Reliability | Cost | Total / 45 | Gate result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Google Pollen API | 0 | 5 | 4 | 3 | 5 | 1 | 3 | 5 | 3 | 29 | Reject: China has grasses only; caching/storage conflicts with platform architecture. |
| Open-Meteo | 0 | 5 | 4 | 4 | 2 | 5 | 4 | 4 | 4 | 32 | Reject for China; geographically suitable only for future Europe work. |
| Ambee Pollen API | 2 | 2 | 2 | 4 | 2 | 1 | 2 | 3 | 1 | 19 | Contact: global/type claims do not verify China Mugwort or consumer cache/reuse rights. |
| Tomorrow.io | 2 | 0 | 2 | 3 | 1 | 1 | 2 | 3 | 1 | 15 | Reject: generic weed/ragweed is not Artemisia. |
| AccuWeather | 0 | 0 | 2 | 2 | 1 | 1 | 2 | 3 | 1 | 12 | Reject: pollen coverage is US/Europe only. |
| Meteomatics | 1 | 4 | 2 | 3 | 1 | 2 | 2 | 3 | 1 | 19 | Contact: exact Mugwort warning exists, but China availability, model meaning, and rights are unverified. |
| Beijing pollen service | 1 | 5 | 3 | 2 | 4 | 0 | 0 | 3 | 0 | 18 | Contact: exact taxon and local forecast, but all reuse/licence rights unresolved. |
| CMA/local public research channels | 1 | 3 | 2 | 1 | 1 | 0 | 0 | 4 | 0 | 12 | Research more: evidence of capability, no published reusable provider contract. |
| WeatherDT (existing TOTAL) | 4 | 0 | 2 | 2 | 2 | 0 | 0 | 2 | 0 | 12 | Reject for ARTEMISIA; current TOTAL usage is out of scope and unchanged. |

The scorecard is deliberately not a selection algorithm: it prevents a strong
marketing claim from overcoming an unmet taxonomy or licensing gate.

## Conditional shortlist (maximum three)

None is approved for integration. These three merit the next commercial/legal
due-diligence action because they are the only paths with a plausible exact-taxon
or China-coverage route.

| Rank | Candidate | Why it is shortlisted | Missing proof / next action | Contact needed | Cost / China fit / Artemisia fit |
| ---: | --- | --- | --- | --- |
| 1 | Beijing pollen service | Existing service has the reviewed exact pair `JKHS / 菊科蒿属`, district forecasts, and an established platform parser. | Obtain written owner authorisation; taxon dictionary; unit/risk/forecast semantics; retention/cache/display/notification rights; history and SLA. | Beijing Meteorological Service / named data owner. | Public price unknown; Beijing-only; exact source taxon only after owner confirms formal dictionary. |
| 2 | Ambee | Public docs advertise China-region type/count/risk and a commercial API with current, forecast, and history endpoints. | Contracted China samples for Xi'an and Beijing must show explicit Mugwort; establish whether latest is measured/modelled, grid/cadence, price, offline cache, consumer display, redistribution, attribution, and reseller terms. | Ambee sales/legal. | Quote only; vendor claims global China fit but unverified; Mugwort publicly listed for Europe, not China. |
| 3 | Meteomatics | Official API exposes `mugwort_pollen_warning:idx` and accepts coordinates. | Query/contract proof for each priority China coordinate, source/model and warning calibration, plus cache/public app/notification rights and price. | Meteomatics sales/legal/support. | Quote only; generic global weather coverage is not pollen proof; exact mugwort warning but not a verified China measurement. |

## Next strategies if no contract qualifies

1. **Beijing real provider:** first legal/technical validation target; keep it local
   and preserve `UNKNOWN` severity unless the official scale is supplied.
2. **City-by-city legal providers:** discover each local meteorological/health
   service separately; do not extrapolate coverage across a province or city list.
3. **Negotiate commercial licences:** prefer agreements that explicitly permit
   backend persistence, Room offline cache, public display, attribution, and
   derived alerts; record retention and audit limits before writing a provider.
4. **Future own model:** only after a separately licensed input-data programme,
   validation plan, and governance decision. It must be labelled `ESTIMATE`, never
   `OBSERVATION`, and must not reuse restricted third-party values.

## G2 entry criteria

Phase G2 should not start a production integration until one candidate supplies:

- a signed or otherwise binding permission covering the required persistence,
  cache, display, redistribution/derived notification, commercial, and attribution
  rights;
- repeatable samples for at least Beijing and Xi'an (and the intended city set),
  with an explicit source taxon and unit;
- source semantics, model/observation status, timestamp, issue/horizon, grid,
  cadence, risk-scale definition, error/empty behavior, and history policy;
- a cost/rate/SLA decision and a named operational/legal owner; and
- a mapping review showing no inference from generic weed/family/total fields.

Until then, the outcome is **research more / contact provider**, not an API task.
