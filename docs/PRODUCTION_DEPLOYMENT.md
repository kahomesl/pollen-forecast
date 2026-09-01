# Production deployment status: do not deploy

Production deployment is blocked. This repository contains deployment hardening and a controlled-beta staging path, not an authorization to publish or operate a public service.

## Blocking conditions

- Written, retained authorization for every enabled provider covering collection, server persistence, Android cache, public display, alerting/derived use, redistribution, commercial use, attribution, retention, rate limits, and SLA/support.
- A qualified national ARTEMISIA source or an explicitly licensed and reviewed alternative. Phase G1 found no currently qualified provider.
- Approved privacy notice, retention policy, incident contact, backup/restore exercise, monitoring ownership, and release signer/key custody.
- A real HTTPS backend, release signing material in the deployment secret store, and a completed signed-release validation.

`EXTERNAL_POLLEN_FETCH_ENABLED` and individual provider gates must remain `false` in production until the corresponding written approval and G3 provider integration gate are complete. Never use public demo endpoints as evidence of a licence.

See `PRODUCTION_READINESS.md`, `DATA_LICENSE_REGISTER.md`, `RELEASE_SIGNING.md`, and `G3_PROVIDER_INTEGRATION_GATE.md`.
