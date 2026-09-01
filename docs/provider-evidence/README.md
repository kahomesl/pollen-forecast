# Provider evidence handling

This folder contains **metadata and redacted decision records only**. Do not commit
emails, private contacts, signed contracts, invoices, API keys, authentication
headers, raw response dumps that contain credentials, database backups, or NDA
documents.

## Secure evidence record

For each future response, create a small Markdown record with:

- provider and product;
- evidence type (email, public terms, executed agreement, API schema, sample JSON,
  official taxonomy, or pricing/SLA document);
- received/effective/expiry dates;
- redacted secure-storage reference controlled by the project owner (for example,
  “owner-controlled contract vault / record ID”), never a password, direct private
  URL, or email address;
- exact rights confirmed or denied: server fetch, backend persistence, Android
  cache, history, display, redistribution, derived severity, notifications,
  commercial use, attribution, retention, rate, and SLA;
- taxon, unit, measurement semantics, geography, and sample-schema decision; and
- reviewer, decision, and next review date.

## Required workflow

1. Preserve the original evidence in an access-controlled owner-managed location.
2. Confirm the person/entity has authority to grant the relevant data rights.
3. Add only a redacted record here and update `DATA_LICENSE_REGISTER.md`.
4. Re-check taxonomy, unit, current/observation/forecast semantics, geography,
   cache rights, display rights, derived notifications, pricing, rate, and SLA.
5. Update `ARTEMISIA_PROVIDER_SCORECARD.md` only after the evidence is reviewed.
6. Use `G3_PROVIDER_INTEGRATION_GATE.md`; do not start implementation from a
   marketing claim or informal “yes”.

No response is `APPROVED` until the legal owner, scope, and every required right
are documented in writing.
