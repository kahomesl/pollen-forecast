# Data licence register

**Status date:** 2026-09-01
**Scope:** internal controlled-beta governance. This register is not a licence and
does not authorize any fetch, cache, display, notification, redistribution, or
commercial use.

## Status vocabulary

- `NOT_REQUESTED`: no outreach has been sent.
- `REQUEST_DRAFTED`: a repository draft exists but has not been sent.
- `REQUEST_SENT`: a dated request was sent; record only a secure evidence reference.
- `RESPONSE_RECEIVED`: a response exists but has not completed the G3 gate.
- `APPROVED`, `REJECTED`, `EXPIRED`: only after written evidence is reviewed and
recorded under `docs/provider-evidence/` without committing confidential material.

An externally reachable endpoint, a dashboard account, a marketing statement, or
a response that merely says “we have pollen data” is **not** `APPROVED`.

## Active and contacted candidates

### WeatherDT

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | WeatherDT / city TOTAL pollen-level endpoint |
| Data scope / taxon scope | China city daily pollen-risk level; TOTAL only, never ARTEMISIA |
| Owner | Legal data owner and licensing contact not verified |
| Official documentation / terms URL | No official developer documentation or reuse terms located; technical endpoint is recorded in `DATA_SOURCE_NOTES.md` |
| Contact | Unknown; use `provider-outreach/WEATHERDT_OUTREACH.md` only after a verified contact is found |
| Commercial status | Unknown |
| Caching / backend persistence / Android offline cache | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Redistribution / derived alerts / public consumer display | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Commercial use / attribution | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Retention limits / rate limits / SLA | Unknown |
| Evidence date / source | 2026-09-01 / public endpoint response and project source notes |
| Written permission status | `REQUEST_DRAFTED` |
| Internal decision / blocker | Existing internal-development TOTAL source only. Do not enable it for a public or staging deployment until written permission covers server fetch, persistence, Room cache, history/current display, derived normalized severity, notifications, distribution, commercial use, attribution, retention, rate, and SLA. |

### Beijing pollen service

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Beijing pollen monitoring/forecast service / classified forecast endpoint |
| Data scope / taxon scope | Beijing city/district classified forecast. Only exact `plantCode=JKHS` + `plantName=菊科蒿属` is reviewed as ARTEMISIA; no family/weed inference. |
| Owner | Endpoint operator is not necessarily the legal data owner; owner must be confirmed with the Beijing meteorological service/data-rights contact. |
| Official documentation / terms URL | Public communication channel: [Beijing government notice](https://english.beijing.gov.cn/latest/news/202103/t20210311_2304803.html). No developer/reuse terms located. |
| Contact | To be confirmed; see `provider-outreach/BEIJING_POLLEN_OUTREACH.md` |
| Commercial status | Unknown |
| Caching / backend persistence / Android offline cache | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Redistribution / derived alerts / public consumer display | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Commercial use / attribution | Unknown — **BLOCKER_BEFORE_PUBLIC_RELEASE** |
| Retention limits / rate limits / SLA | Unknown |
| Evidence date / source | 2026-09-01 / `DATA_SOURCE_NOTES.md`, public endpoint behavior, and public Beijing notice |
| Written permission status | `REQUEST_DRAFTED` |
| Internal decision / blocker | Do not treat endpoint availability as permission. Keep severity `UNKNOWN`; do not normalize its classified level before its documented legend and semantics are supplied. |

### Ambee

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Ambee / Pollen API v3 |
| Data scope / taxon scope | Vendor describes global category data. Public legacy documentation lists Mugwort species support in Europe; mainland-China Artemisia/Mugwort responses are unverified. |
| Owner | Ambee, Inc. |
| Official documentation / terms URL | [Pollen docs](https://docs.ambeedata.com/apis/pollen), [service agreement](https://www.getambee.com/servicesagreement) |
| Contact | Vendor sales/legal contact; use `provider-outreach/AMBEE_OUTREACH.md` |
| Commercial status | Contract/quote required; raw-data resale needs a Reseller Addendum under the public agreement. |
| Caching / backend persistence / Android offline cache | Contract-specific and not public-confirmed |
| Redistribution / derived alerts / public consumer display | Contract-specific and not public-confirmed |
| Commercial use / attribution | Contract-specific and not public-confirmed |
| Retention limits / rate limits / SLA | Quote/contract required |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `REQUEST_DRAFTED` |
| Internal decision / blocker | `CONTACT_PROVIDER`; no China ARTEMISIA integration, account signup, purchase, or implementation before a written response passes G3. |

### Meteomatics

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Meteomatics / `mugwort_pollen_warning:idx` |
| Data scope / taxon scope | Exact Mugwort warning parameter exists; it is a DWD-derived warning index. Parameter-specific China coordinate coverage, model, and semantics are unverified. |
| Owner | Meteomatics entity named in the applicable offer/contract |
| Official documentation / terms URL | [Pollen parameters](https://www.meteomatics.com/en/api/available-parameters/particles/), [terms](https://www.meteomatics.com/en/gtc/) |
| Contact | Vendor sales/legal/support; use `provider-outreach/METEOMATICS_OUTREACH.md` |
| Commercial status | Quote/contract required |
| Caching / backend persistence / Android offline cache | Contract-specific and not public-confirmed |
| Redistribution / derived alerts / public consumer display | Contract-specific and not public-confirmed |
| Commercial use / attribution | Contract-specific and not public-confirmed |
| Retention limits / rate limits / SLA | Quote/contract required |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `REQUEST_DRAFTED` |
| Internal decision / blocker | `CONTACT_PROVIDER`; do not infer China suitability from worldwide weather coverage or from a DWD warning parameter. |

## Researched but not contacted candidates

### Google Pollen API

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Google Maps Platform / Pollen API |
| Data scope / taxon scope | China coverage lists grasses only, not Mugwort; `MUGWORT` exists elsewhere. |
| Owner | Google Maps Platform |
| Official documentation / terms URL | [Coverage](https://developers.google.com/maps/documentation/pollen/coverage), [policies](https://developers.google.com/maps/documentation/pollen/policies) |
| Contact | Google Maps Platform sales/support if future non-China work requires it |
| Commercial status | Billing required; pay-as-you-go Pollen SKU |
| Caching / backend persistence / Android offline cache | Public policies generally prohibit prefetching, caching, and storage; incompatible with this architecture. |
| Redistribution / derived alerts / public consumer display | Raw redistribution not approved by public material; display/attribution and map requirements apply. |
| Commercial use / attribution | Contracted paid use; Google attribution required. |
| Retention limits / rate limits / SLA | Storage prohibited except stated exceptions; documented default 6,000 QPM; SLA contract-specific. |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `NOT_REQUESTED` |
| Internal decision / blocker | `REJECT` for China ARTEMISIA and for this offline-cache architecture. |

### Open-Meteo

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Open-Meteo / Air Quality API `mugwort_pollen` |
| Data scope / taxon scope | Modelled Mugwort pollen, Europe-only during pollen season; no China coverage. |
| Owner | Open-Meteo and upstream open-data licensors |
| Official documentation / terms URL | [Air Quality API](https://open-meteo.com/en/docs/air-quality-api), [licence](https://open-meteo.com/en/license), [pricing](https://open-meteo.com/en/pricing) |
| Contact | `info@open-meteo.com` only for a future Europe/commercial evaluation; do not contact for Phase G2 China integration. |
| Commercial status | Free tier is non-commercial; paid hosted plan grants commercial API use. |
| Caching / backend persistence / Android offline cache | CC BY 4.0 data permits sharing/adaptation with required attribution; hosted-plan terms and upstream attribution must be followed. |
| Redistribution / derived alerts / public consumer display | Permitted by CC BY 4.0 with proper attribution and change indication. |
| Commercial use / attribution | Commercial hosted use requires subscription; attribution required. |
| Retention limits / rate limits / SLA | Free tier limits documented; paid plan capacity/SLA is plan-specific. |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `NOT_REQUESTED` |
| Internal decision / blocker | `REJECT` for China ARTEMISIA; retain only as a future Europe research reference. |

### Tomorrow.io

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | Tomorrow.io / premium pollen layer |
| Data scope / taxon scope | Tree/grass/weed/ragweed category indexes only; no Mugwort/Artemisia field. |
| Owner | Tomorrow.io |
| Official documentation / terms URL | [Pollen field reference](https://docs.tomorrow.io/reference/data-layers-pollen) |
| Contact | Not needed for China ARTEMISIA |
| Commercial status | Plan entitlement required; public terms/price still require product-specific review. |
| Caching / backend persistence / Android offline cache | Unknown |
| Redistribution / derived alerts / public consumer display | Unknown |
| Commercial use / attribution | Unknown |
| Retention limits / rate limits / SLA | Plan-specific/unknown |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `NOT_REQUESTED` |
| Internal decision / blocker | `REJECT`: generic `weedIndex` is never ARTEMISIA. |

### AccuWeather

| Field | Current evidence / decision |
| --- | --- |
| Provider / product | AccuWeather Enterprise API / AirAndPollen forecast fields |
| Data scope / taxon scope | Official FAQ limits pollen forecasts to the US and Europe; China ARTEMISIA unsupported. |
| Owner | AccuWeather Intl., LLC |
| Official documentation / terms URL | [FAQ](https://apidev.accuweather.com/developers/faq), [terms](https://developer.accuweather.com/documentation/terms-of-use) |
| Contact | Not needed for China ARTEMISIA |
| Commercial status | Account and contracted plan required |
| Caching / backend persistence / Android offline cache | Unknown for this use case |
| Redistribution / derived alerts / public consumer display | Unknown for this use case |
| Commercial use / attribution | Contract-specific |
| Retention limits / rate limits / SLA | Contract-specific |
| Evidence date / source | 2026-09-01 / G1 official-source audit |
| Written permission status | `NOT_REQUESTED` |
| Internal decision / blocker | `REJECT` for China ARTEMISIA. |

## Evidence-update procedure

When a provider responds, follow [provider-evidence/README.md](provider-evidence/README.md),
then update this register and the G1 scorecard before any integration work.
