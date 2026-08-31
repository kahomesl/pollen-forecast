# Data Source Notes

Last verified against public responses on 2026-08-31. These notes record
source semantics, not an endorsement of a source's reliability or redistribution
terms. No credentials, cookies, or keys are stored here.

## WeatherDT

Endpoint: `https://graph.weatherdt.com/ty/pollen/v2/hfindex.html`

### CONFIRMED

- `eletype=1` returns a city-level `dataList` with `addTime`, `levelCode`,
  `level`, `levelMsg`, `color`, and sometimes `createDate`.
- `predictFlag=false` returned dated current-and-past entries. The 2026-08-31
  entry was published with `createDate=2026-08-30T23:30:00`.
- `predictFlag=true` returned future dated entries as well as dated entries with
  `levelCode=-1` / `level=暂无` when no value was available.
- `levelCode` is an ordinal risk level. `seasonLevel` supplies ranges and
  symptom-percentage descriptions; the response does not label those ranges
  with a physical concentration unit.

### INFERRED

- The source supplies a published daily pollen-risk index rather than a raw
  physical concentration. This is why the normalized unit is `level`.

### UNKNOWN

- The public response does not establish that `predictFlag=false` values are
  direct measurements. They are therefore represented as `CURRENT`, never
  `OBSERVATION`.
- The production methodology, the physical unit behind `seasonLevel`, and the
  exact distinction between same-day and historical values are not documented
  by this public endpoint.

## Beijing pollen monitoring

Base host: `https://pollenwechat.bjpws.com`

### CONFIRMED

- Public endpoints responded on 2026-08-31:
  - `/v1/weatherPollen/pollens`
  - `/api/pollen/obs/latestPollenLevels`
  - `/api/pollen/obs/history24?staId=54399`
  - `/v1/pollen/forecast?plantCode=zongleibie`
  - `/v2/pollen/classify/forecast?areaCode=110105`
  - `/v1/pollen/legends`
- The classified forecast identifies the target group exactly as
  `plantCode=JKHS` and `plantName=菊科蒿属`. Its description states that it is a
  `菊科蒿属花粉浓度指数`; only this exact code/name pair maps to `ARTEMISIA`.
- Its `min`/`max` are range values for that index, so normalized data uses
  `unit=index`, `minValue`, and `maxValue`. They are not treated as physical
  pollen concentration.
- `level` is the source risk level. `/v1/pollen/legends` exposes level/range
  legends for Beijing pollen but does not provide a separate physical unit for
  classified `JKHS` values.
- The live classification response had `isValid=false`; the provider returns
  no forecast in that case.
- `latestPollenLevels` and `history24` expose station `hfH`/`hfHLv` total data,
  not an Artemisia field. They are not mapped to `ARTEMISIA`.

### INFERRED

- `baseTime` appears to be a generation time in the total forecast response:
  it was `09:00` while corresponding `dataTime` values were `08:00`. This is
  not used as a classified-forecast validity boundary.

### UNKNOWN

- In the classified response, `title` was `08月30日08时-08月31日08时`, while
  `baseTime`, `dataTime`, and `vti=24` were `202608300900`,
  `202608300900`, and `24`. The public response does not resolve this conflict.
  The provider deliberately emits neither `validFrom` nor `validTo`.
- The formal documentation for classified `baseTime`, `dataTime`, `vti`, and
  title validity semantics is not publicly confirmed.
