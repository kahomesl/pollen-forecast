# Normalized risk severity

`RiskSeverity` is the platform's provider-confirmed display and local-alert
ordering:

`UNKNOWN` · `LOW` · `MODERATE` · `HIGH` · `VERY_HIGH`

It is not a physical concentration, an interchangeability claim between
Provider units, a medical severity rating, or a prediction of an individual's
allergic reaction. API v1 retains every Provider's raw `risk.level` and
`risk.label` alongside additive `risk.severity`.

## Confirmed mapping

WeatherDT public responses verified on 2026-09-01 pair total-pollen
`levelCode` with the following source labels and order:

| WeatherDT code | Source label | Platform severity |
| --- | --- | --- |
| 0 | 未检测到花粉 | LOW |
| 1 | 很低 | LOW |
| 2 | 低 | LOW |
| 3 | 中 | MODERATE |
| 4 | 高 | HIGH |
| 5 | 很高 | VERY_HIGH |

The map applies only to `provider=weatherdt`, `scope=TOTAL`, and no taxon.
`-1=暂无`, absent/unknown codes, unknown Providers, or taxon-bearing records
are `UNKNOWN`.

## Beijing Artemisia

The exact `plantCode=JKHS` / `plantName=菊科蒿属` classified forecast remains
the only confirmed ARTEMISIA source mapping. Its raw level is preserved, but
normalized severity is deliberately `UNKNOWN`: on 2026-09-01 its classified
`level=2` description said `中等`, whereas `/v1/pollen/legends` described level
2 as `较低`. The platform does not guess which semantic controls the classified
forecast.

## Derivation and history

Severity is derived deterministically while API records are serialized from
`provider`, raw risk level/label, scope, and taxon code. It therefore applies
to legacy `pollen_observations` reads without changing the PostgreSQL schema.
Raw observation `value`, `minValue`, and `maxValue` are not inputs.
