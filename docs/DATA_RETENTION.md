# Data retention and deletion operations

## Current repository behaviour

The backend persists pollen observations, legacy city readings, ratings, and sync-run summaries in PostgreSQL. Android retains its local Room cache and selected-location/notification preferences. The app does not include analytics, advertising identifiers, or a server account system.

## Controlled-beta policy

- Use an isolated staging database and delete it after the beta unless an approved incident or test record requires a shorter/longer documented retention.
- Do not retain provider contract documents, invoices, emails, or credential material in Git; place them in the approved restricted evidence store referenced by `docs/provider-evidence/README.md`.
- Do not log request headers, IP addresses, raw GPS coordinates, provider credentials, or raw provider error bodies. Request IDs are operational correlation values only.
- Before any public release, the data owner must approve exact PostgreSQL backup retention, Android cache retention, deletion request handling, access controls, and incident-log retention. This document is not that approval.

## Deletion runbook

An authorized operator identifies the isolated environment, takes an approved backup only if policy requires it, deletes the target data through the platform/database change process, verifies the target was removed, and records scope, time, operator, and result outside Git. Never use broad filesystem deletion or unreviewed SQL in response to a request.
