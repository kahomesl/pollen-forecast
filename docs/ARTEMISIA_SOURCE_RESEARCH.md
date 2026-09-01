# Artemisia data-source and licensing research

**Phase:** G1
**Last verified:** 2026-09-01
**Decision status:** research only — this document does not authorize a new production provider.

## Executive decision

> **NO QUALIFIED NATIONAL ARTEMISIA PROVIDER FOUND**

No candidate below simultaneously has publicly verified China coverage, an explicit
Artemisia/Mugwort taxon, suitable data semantics, and terms that permit the
platform's server persistence, Android offline cache, and public/commercial display.
Therefore Phase G1 makes **no** provider-registry, API, sync, storage, Android, or
UI change.

The platform must keep the following taxonomy rule:

- Map a source value to `ARTEMISIA` only when the source explicitly identifies
  `Artemisia`, `Mugwort`, or the already reviewed Beijing pair
  `plantCode=JKHS` + `plantName=菊科蒿属`.
- Never infer `ARTEMISIA` from `weed`, `菊科`, `草本`, a total pollen index, or a
  similarly named field. In particular, Tomorrow.io `weedIndex` is not Artemisia.

`CURRENT` means a provider's latest/current product unless the provider explicitly
documents an observation/measurement method. A modeled product is `FORECAST` or
`ESTIMATE`, not `OBSERVATION`. No unit conversion is authorized by this research.

## Candidate matrix

Legend: **Y** = documented; **N** = documented unavailable; **?** = not publicly
verified. “History” means an API history product, not a claim that its values are
observations. `CONFIRMED`, `PARTIAL`, `UNKNOWN`, and `REJECTED` describe research
confidence; `REJECT` means “do not integrate for China ARTEMISIA,” not that the
vendor is unsuitable for all other products.

| Provider / source | Source type | China coverage / priority-city coverage | Taxon / scope | Current / observation / forecast / history | Unit / resolution / refresh | Authentication / price | Cache / redistribute / commercial / attribution | Reliability evidence / documentation | Status / decision |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Google Pollen API | Commercial location forecast API | **CN is listed for `grasses` only**; no Mugwort for Xi'an, Beijing, Taiyuan, Hohhot, Shijiazhuang, Jinan, Zhengzhou, Chengdu, Lanzhou, Yinchuan, Urumqi, Shenyang, or Changchun. | `MUGWORT` enum exists, but is absent from China coverage. Species/plant-level where available. | N / N / Y, daily up to 5 days / N documented. | Universal Pollen Index (UPI); up to 1 km; provider does not publish a pollen refresh cadence. | API key or OAuth; valid billing account; pay-as-you-go Pollen SKU. | **N for this architecture:** policies generally prohibit prefetching, caching, and storage; raw redistribution is not authorized by public docs. Commercial API use is billed but remains subject to Maps terms; Google logo/text attribution and map-display rules apply. | Official coverage lists exact country + plant availability; official policies explicitly govern storage and attribution. [G1–G4] | **CONFIRMED / REJECT** — monitor only for a future non-China use case. |
| Open-Meteo Air Quality API | Open-data modeled air-quality/pollen forecast API | `mugwort_pollen` is **Europe-only during pollen season**; no China or priority-city result. | `mugwort_pollen`; plant-specific Mugwort field. | N as observation / N / Y, 4-day forecast / archived forecast via “past days”, not observed history. | `grains/m³`; CAMS Europe 0.1° (~11 km); every 24 h. | No key for free non-commercial tier; paid customer endpoint/key for commercial use. | Y under CC BY 4.0, subject to attribution; commercial use requires a subscription for the hosted API. | Official API docs identify the field, Europe-only limit, unit, model grid, and refresh; official licence permits sharing/adaptation with credit. [O1–O3] | **CONFIRMED / REJECT** for China; retain as a future Europe candidate. |
| Ambee Pollen API v3 / legacy pollen docs | Commercial environmental-data API, vendor-modelled pollen product | Vendor says global/current endpoints cover land locations and legacy docs say Asia returns type/count/risk. **It explicitly limits species/subspecies availability to Europe; China ARTEMISIA and every priority city remain unverified without a contracted response.** | Generic tree/grass/weed globally; legacy docs explicitly list Mugwort only for Europe. Do not map China `weed` to ARTEMISIA. | “Latest/current hour” Y as product, **not documented as a physical observation** / N / Y (48 h / 120 h) / Y (48 h API; longer archive is sales-led). | Current v3 docs: hourly interval; forecast 48 h hourly or 120 h/3 h. Vendor pages conflict on 500 m versus 5 km/sub-km-on-request; schema unit for China requires a sample/contract. | API key after signup; public price is quote/contact sales. | Public agreement grants a limited, non-transferable licence; reseller/raw-data rights require a Reseller Addendum. Cache, mobile offline persistence, public app display, and attribution are **?** pending order/API terms. | Vendor claims 30+ allergens, global coverage, and Chicago validation; these are vendor evidence, not China validation. v3 docs also show a 2026 legacy decommission notice. [A1–A4] | **PARTIAL / CONTACT_PROVIDER** — strongest national commercial lead, but not eligible to integrate. |
| Tomorrow.io Pollen | Commercial weather API pollen layer | Pollen category fields are marked worldwide, but there is no public China city validation for taxon-specific data. | `treeIndex`, `grassIndex`, `weedIndex`, `weedRagweedIndex`; no Mugwort/Artemisia field. Category only. | Timeline availability spans -7 days to +108 h; it is not documented as a trap observation / N / available in a time range / available in that range. | 0–5 category index; pollen page does not publish China spatial resolution or refresh cadence. | API key and plan entitlement (pollen is a premium layer); public price/terms needed before any evaluation. | ? / ? / ? / ? — contract review required. | Official field reference is enough to prove the taxonomy gap. [T1] | **CONFIRMED / REJECT** — never map `weedIndex` to ARTEMISIA. |
| AccuWeather Enterprise API | Commercial forecast API | Official FAQ: air-quality and pollen forecasts are only available in the US and Europe; no China / priority cities. | Pollen taxonomy insufficient for ARTEMISIA decision. | Forecast product available only where supported; no China Artemisia contract. | Not applicable to China ARTEMISIA. | Account/API key; plan pricing and data rights require contract. | ? / ? / ? / ? — not researched further after coverage rejection. | Official FAQ gives the geographic exclusion. [AC1] | **CONFIRMED / REJECT**. |
| Meteomatics Weather API | Commercial weather/environment API | API markets worldwide coordinate queries, but public sources do **not** confirm `mugwort_pollen_warning:idx` availability or quality for China or any priority city. | `mugwort_pollen_warning:idx` is explicitly documented; it is a DWD-derived warning index, not a physical mugwort concentration. Available concentration fields list birch/grass/olive/ragweed, not mugwort. | Query supports dated ranges, but source/model semantics for mugwort in China are not public; do not call it an observation. | Mugwort: warning index 0–3; no public China resolution/refresh guarantee for this parameter. | Contract authentication; quote-based pricing. | Contract-dependent. Public terms say offers/contracts govern services; cache, redistribution, mobile persistence, commercial display, and attribution need written confirmation. | Official parameter page identifies exact mugwort warning and DWD warning origin; global API marketing is not China pollen coverage evidence. [M1–M5] | **PARTIAL / CONTACT_PROVIDER** — validate China coordinate response, source, and licence first. |
| Beijing Meteorological pollen service (`pollenwechat.bjpws.com`) | Local public service endpoint currently consumed by the project | Beijing city/district only; no national coverage and no Xi'an or other priority city coverage. | Exact reviewed pair `JKHS` + `菊科蒿属` may map to `ARTEMISIA`; do not generalize `菊科` alone. | Existing classify endpoint is forecast; no documented observation contract for the ARTEMISIA forecast field / N confirmed / Y / history ? | Upstream calls it a concentration index; range/level meaning remains unresolved; district-level request; refresh cadence not formally documented. | No published developer authentication/pricing found. | **? / ? / ? / ?** — a technically reachable endpoint is not a licence. Existing long-term scrape/cache/re-display remains a production legal blocker. | Beijing government confirms an official pollen-monitoring/forecast channel; this does not publish API/reuse terms. Existing endpoint parsing is documented in `DATA_SOURCE_NOTES.md`. [C1; project notes] | **PARTIAL / CONTACT_PROVIDER** — the only Chinese exact-taxon lead, but not licensed for expansion. |
| CMA / China Meteorological Science Institute research and local public channels | Public-sector research, local forecast and social/mini-program dissemination | Research confirms a national high-resolution pollen-model effort; publicly documented reusable API, station feed, city list, terms, and city coverage for the priority set were **not found in this audit**. Beijing, Tianjin, Hohhot and Nanjing demonstrate local work, not nationwide API coverage. | CMA research explicitly discusses Artemisia/蒿属 among native allergen pollen; public output contract not found. | A model product is forecast/estimate, not an observation unless a service says otherwise. | Public technical publications discuss models and monitoring but do not specify a reusable national API unit/grid/cadence/licence. | Public information channels; no published developer commercial terms found. | **? / ? / ? / ?** — contact owner before use. | Official CMA/CAMS and local government sources demonstrate research and local services, not an integration entitlement. [C2–C6] | **UNKNOWN / RESEARCH_MORE** — evidence/source-discovery track, not a provider. |
| WeatherDT (existing TOTAL provider) | Existing third-party total-pollen index endpoint | Existing project coverage is multi-city, including parts of the priority set, but no verified ARTEMISIA field. | TOTAL only; no valid ARTEMISIA mapping. | Existing current/forecast presentation is a provider level product, not confirmed observation. | `level` index; unit, method, and refresh terms unresolved. | Existing unauthenticated endpoint; public price/terms not verified. | **? / ? / ? / ?** — production readiness blocker retained. | Existing provider implementation and `DATA_SOURCE_NOTES.md`; no public licence was established by this audit. | **PARTIAL / REJECT** for ARTEMISIA (retain current TOTAL behavior unchanged). |

## Semantics and mapping gates

1. A name such as `MUGWORT` is sufficient only where it occurs in the source
   response or source-supported taxonomy for the exact queried location. It is not
   a licence to infer availability for China.
2. `weed`, `weedIndex`, `草本`, `菊科`, generic “pollen”, and TOTAL indexes fail the
   ARTEMISIA precision gate. They may be useful in another product only with their
   own taxonomy and risk policy.
3. Google is explicitly daily forecast/UPI; Open-Meteo is a CAMS forecast; Ambee
   calls its product “latest/current” but its public material does not establish
   a China trap observation; Meteomatics' mugwort parameter is a DWD warning.
   None may be represented as a China ARTEMISIA observation without a source-level
   measurement statement.
4. Do not convert an index, warning value, or vendor risk label to `grains/m³`.
   The platform may keep a source unit verbatim after a future contract and sample
   verify it.

## China coverage findings

The searched public record establishes local capability and research, not an
openly reusable national provider:

- CAMS/CMA reports a high-resolution pollen numerical forecast system and names
  Artemisia as a summer/autumn allergen. It is scientific/operational evidence,
  not a public API or data licence. [C2]
- Beijing has an official pollen monitoring/forecast communication channel, and
  the existing code observes a distinct `JKHS / 菊科蒿属` forecast value. Its formal
  developer/reuse terms have not been located. [C1]
- Hohhot and Inner Mongolia public-sector reporting describes monitoring,
  forecast research, historic data, and Artemisia-focused work. It does not grant
  a public reusable feed. [C3, C4]
- Nanjing began daily air-pollen meteorological publication in 2026, another local
  service signal rather than an Android/backend data licence. [C5]

Academic, clinical, and government-health pages that identify Artemisia seasons
or risks are **EVIDENCE_SOURCE** only. They must never be promoted to
`CURRENT`, `OBSERVATION`, `FORECAST`, or a source provider without an explicit
live-data contract. This includes evidence for northern and north-western cities:
the public sources support the importance of Artemisia, not coverage for Xi'an,
Taiyuan, Hohhot, Shijiazhuang, Jinan, Zhengzhou, Chengdu, Lanzhou, Yinchuan,
Urumqi, Shenyang, or Changchun.

## Contact questions (draft only — do not send automatically)

### Ambee

1. For the priority China cities, does Pollen API v3 return an explicit
   `Mugwort`/`Artemisia` species value? Provide a response schema and one
   non-production sample for Xi'an and Beijing.
2. Is China “latest” a measured observation, nowcast, or model estimate? Identify
   source networks, timestamps, unit, grid, and quality flags.
3. What exact licence covers backend persistence, Room offline cache, public mobile
   display, commercial use, attribution, and derived risk notifications?
4. Does the required licence permit a consumer app, and does it require a Reseller
   Addendum? What are production price, rate, retention, and audit terms?
5. Reconcile 500 m legacy docs with the 5 km/sub-km-on-request marketing claim,
   plus the forecast refresh cadence difference.

### Meteomatics

1. Does `mugwort_pollen_warning:idx` return non-empty values for each priority
   China coordinate? Which model/source is used there, and is it DWD-only?
2. Is it a forecast, a nowcast, or an observation? What are issue time, horizon,
   grid, update cadence, and warning calibration for China?
3. Does a production agreement permit backend and Android caching, public display,
   commercial use, redistribution/derived notification, and what attribution is
   mandatory? Quote the specific contract clauses and price.

### Beijing / CMA / local operators

1. Identify the service owner, published API/data-use terms, and a commercial or
   public-data licence that expressly covers server cache, mobile offline cache,
   display, and notification use.
2. Provide the taxon dictionary, unit, risk-scale methodology, district/city scope,
   observation-vs-forecast semantics, quality flags, refresh rate, retention, and
   historical access.
3. State whether `JKHS / 菊科蒿属` is the intended formal taxon code, and whether
   any values are measured concentrations or modelled forecasts.

## Sources

All web sources were checked on 2026-09-01. Links are intentionally first-party
unless labelled project notes.

- **G1** — [Google Pollen country/plant coverage](https://developers.google.com/maps/documentation/pollen/coverage)
- **G2** — [Google Pollen API overview/reference](https://developers.google.com/maps/documentation/pollen/reference)
- **G3** — [Google Pollen policies and attribution](https://developers.google.com/maps/documentation/pollen/policies)
- **G4** — [Google Pollen usage and billing](https://developers.google.com/maps/documentation/pollen/usage-and-billing)
- **O1** — [Open-Meteo Air Quality API](https://open-meteo.com/en/docs/air-quality-api)
- **O2** — [Open-Meteo licence](https://open-meteo.com/en/license)
- **O3** — [Open-Meteo pricing and commercial use](https://open-meteo.com/en/pricing)
- **A1** — [Ambee Pollen API product page](https://www.getambee.com/api/pollen)
- **A2** — [Ambee pollen API documentation](https://docs.ambeedata.com/apis/pollen)
- **A3** — [Ambee API endpoint overview](https://docs.ambeedata.com/apis/overview)
- **A4** — [Ambee service agreement](https://www.getambee.com/servicesagreement)
- **T1** — [Tomorrow.io pollen fields](https://docs.tomorrow.io/reference/data-layers-pollen)
- **AC1** — [AccuWeather Enterprise API FAQ](https://apidev.accuweather.com/developers/faq)
- **M1** — [Meteomatics pollen/particle parameters](https://www.meteomatics.com/en/api/available-parameters/particles/)
- **M2** — [Meteomatics alphabetic parameter list](https://www.meteomatics.com/en/api/available-parameters/alphabetic-list/)
- **M3** — [Meteomatics API request format](https://www.meteomatics.com/en/api/request/)
- **M4** — [Meteomatics pricing](https://www.meteomatics.com/en/pricing/)
- **M5** — [Meteomatics general terms](https://www.meteomatics.com/en/gtc/)
- **C1** — [Beijing government: pollen monitoring/forecast channel](https://english.beijing.gov.cn/latest/news/202103/t20210311_2304803.html)
- **C2** — [Chinese Academy of Meteorological Sciences: pollen forecast system](https://www.camscma.cn/article/5475.html)
- **C3** — [Inner Mongolia public-sector Artemisia monitoring/forecast project](https://kjj.ordos.gov.cn/kjgz_129901/202606/t20260624_1725465.html)
- **C4** — [Hohhot pollen monitoring report](https://www.weather.com.cn/neimenggu/sy/tqyw/07/3552311.shtml)
- **C5** — [CAS/Nanjing Meteorological daily pollen publication](https://nigpas.cas.cn/zhxw/202603/t20260325_8175138.html)
- **C6** — [Shanxi CDC Artemisia health notice](https://wjw.shanxi.gov.cn/zfxxgk/fdzdgknr/jkzx/202608/t20260820_10203366.shtml)
