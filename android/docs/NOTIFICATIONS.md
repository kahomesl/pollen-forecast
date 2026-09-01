# Risk notifications

Risk alerts are local, opt-in notifications. The master setting defaults to off;
the user must turn it on before Android 13+ requests `POST_NOTIFICATIONS`.
Denied permission leaves alerts off and shows `系统通知权限未开启`.

Settings in DataStore include TOTAL/ARTEMISIA targets, `MODERATE`/`HIGH`/
`VERY_HIGH` threshold (default HIGH), and a per-target fingerprint. Alerts use
the normal-importance `pollen_risk_alerts` channel and open the app home.

Only NETWORK results with a recognized normalized severity and `CURRENT` or
`FORECAST` measurement can notify. CACHE, ESTIMATE, UNKNOWN, empty Artemisia,
and mismatched TOTAL/ARTEMISIA records cannot notify. The fingerprint contains
location, target, measurement type, observation id, and severity, suppressing
duplicates while allowing a severity increase or new observation.
