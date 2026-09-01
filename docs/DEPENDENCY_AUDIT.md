# Dependency audit

## 2026-09-01 result

`bun audit` initially reported 30 vulnerabilities (11 high, 18 moderate, 1 low) through direct `axios@1.14.0`, including its `follow-redirects` and `form-data` dependency chain. Source search found no axios imports; it remained declared by both root and frontend manifests.

Both manifests now resolve `axios@1.20.0`. The frontend update also resolved direct `echarts@6.1.0` and `vite@8.2.2` plus their advisory-affected transitive dependencies. Re-running `bun audit` at root reported **No vulnerabilities found (50 packages)**; `bun audit --cwd frontend` reported **No vulnerabilities found (226 packages)**.

## Scope and remaining operations

This is a Bun advisory check for root dependencies. Android has no vulnerability-audit plugin configured; its release risk is controlled by pinned Gradle version catalog entries, CI build/lint/test gates, and a future dependency-review process. Before any production release, rerun the audits in the controlled build environment and record the exact tool/version/output outside Git.
