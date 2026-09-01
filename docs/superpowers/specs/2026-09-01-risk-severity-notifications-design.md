# Normalized Risk Severity and Android Notifications Design

## Goal

Add a backwards-compatible `risk.severity` to API v1 and opt-in local Android
alerts without equating provider units, total pollen, Artemisia, or medical
risk.

## Evidence and normalization

`RiskSeverity` is `UNKNOWN`, `LOW`, `MODERATE`, `HIGH`, or `VERY_HIGH`. It is
derived at API-read time from only `provider`, raw risk level/label, scope, and
taxon code. Raw `risk.level` and `risk.label` remain unchanged.

WeatherDT public responses verified on 2026-09-01 pair codes 1 through 5 with
the labels 很低, 低, 中, 高, 很高. The normalizer maps only those known codes for
TOTAL records without a taxon. Code 0 (未检测到花粉) is the provider's lowest
published state and maps to LOW. Missing, negative, or unknown codes map to
UNKNOWN. Values and ranges are never inspected.

The Beijing classified `JKHS` response reports `level=2` with a description
calling it 中等, while `/v1/pollen/legends` assigns `level=2` the label 较低.
That conflict means no Beijing level-to-severity mapping is confirmed in this
phase. Beijing observations therefore serialize as UNKNOWN even though their
raw level is retained.

## Backend API

The platform adds `risk.severity` to the existing v1 observation shape. The
serializer calls one provider-specific normalizer for both live and historical
observations, so stored legacy observations acquire the current deterministic
severity without a PostgreSQL migration. Unknown providers and absent raw risk
also serialize as UNKNOWN.

## Android data and cache

Android decodes severity with a resilient enum serializer: absent or unknown
wire values become `UNKNOWN`. Room becomes version 2 and adds non-null
`riskSeverity` to `observation_cache`, defaulting existing rows to UNKNOWN in
`MIGRATION_1_2`; no destructive fallback is used. New cache rows retain the
server severity while legacy cache rows continue to display safely as unknown.

## Alert policy

DataStore owns opt-in settings: master enabled (default false), TOTAL and
ARTEMISIA targets, minimum severity (default HIGH), and one last fingerprint
per target. A pure evaluator permits an alert only when all of these hold:

- master and matching target are enabled;
- source is NETWORK;
- scope/taxon exactly identify TOTAL or Artemisia respectively;
- severity is known and at least the configured threshold;
- measurement is CURRENT or FORECAST, never ESTIMATE;
- the fingerprint differs from the persisted target fingerprint.

The fingerprint contains location id, target, measurement type, observation
id, and severity. It suppresses repeated observations yet permits a severity
increase or a new observation. Empty results produce no candidate.

The existing `allergen-background-refresh` worker returns its network results
to an alert coordinator after cache updates. Cache fallback retains the Phase C
retry policy and is rejected by the evaluator, so cached data cannot notify.
Local notifications use one normal-importance `pollen_risk_alerts` channel and
open the existing launcher activity. No foreground service, FCM, alarm, or
medical instruction is added.

## Permission and UI

The My screen exposes risk-alert settings. Turning the master switch on is the
only path that requests `POST_NOTIFICATIONS` on Android 13+. A denied or
system-disabled permission leaves alerts off and shows `系统通知权限未开启`.
The UI keeps the provider's original risk label prominent; UNKNOWN adds
`风险等级暂未标准化` rather than representing it as low risk.

## Verification

Backend tests cover confirmed WeatherDT mappings, unknown inputs, taxon/scope
isolation, raw-value independence, current/history serialization, and legacy
records. Android tests cover resilient decoding, Room v1-to-v2 preservation,
all alert policy gates, dedupe/escalation, and worker integration. Pixel 9 Pro
XL verifies opt-in permission timing, deterministic local notifications,
dedupe, cache/unknown suppression, persistence, and the real backend smoke.
