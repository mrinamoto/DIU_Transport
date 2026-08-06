# Phase 5 Test Evidence

Executed on 2026-08-06 in `F:\Projects\DIU_Transport` on branch `feature/phase-5-production-readiness-governance`.

## Reproducibility and static checks

- Node `v24.18.0`; npm `11.16.0`.
- `npm ci`: passed; 131 packages installed from the lockfile, 0 vulnerabilities reported. npm warned that `prebuild-install@7.1.3` is deprecated and that the better-sqlite3 install script is not covered by npm's local allow-scripts policy.
- `npm run check`: 76 JavaScript files passed syntax checking.

## Backend and regression tests

- `npm test`: 88/88 passed, 0 failed/skipped/todo.
- This contains the preserved 68-test Phase 2-4 regression baseline, 19 Phase 5 API/governance tests, and one explicit Phase 4-to-5 migration test.
- `npm run test:phase5`: 19/19 passed.
- Phase 5 API/security/observability tests: 14/14 passed.
- Phase 5 governance/backup/export tests: 5/5 passed.
- Fresh schema-v4, Phase 2-to-5, Phase 3-to-5, and Phase 4-to-5 migration paths passed with foreign-key validation and preserved synthetic records.

Covered identity authorization/redaction, final-admin protection, JWT revocation, role-change invalidation, deterministic account lock/unlock, one-time hash-only recovery, self/admin exports, outbox enqueue/retry/cancel/audit, liveness/readiness/metrics, structured-log field allowlisting, retention plan/apply on temporary data, anonymization dry-run, backup/restore, and sanitized export.

## Browser and accessibility

- `npm run test:browser`: 6/6 passed in Chromium 151.0.7922.34; local bundle identifier 1234 under Playwright 1.62.1.
- Existing Phase 2-4 workflows: 4/4 retained and passed.
- New Phase 5 workflows: 2/2 passed.
- Automated axe: 0 serious/critical violations across every scanned state.
- Viewports/features: desktop default, 390×844, 768×1024, 200% zoom simulation, forced colors, keyboard tab/submit/navigation, skip link, recovery dialog Escape/focus return.
- In-app browser manual smoke: admin login, Users, Outbox, Operations; schema 4 and zero captured console errors.
- Firefox, WebKit, and Edge: not executed because their local binaries were unavailable; no installation or unsupported claim was made.

## Governance and operational rehearsals

- Retention plan on an isolated schema-v4 database: passed, dry-run, zero synthetic candidates.
- Retention apply without the safety guard: correctly refused with exit 1. Destructive apply was not run against the development database.
- SQLite backup creation: passed on an isolated schema-v4 database.
- Non-destructive restore verification: passed; temporary restore removed.
- Sanitized JSON export: passed; no real records or credentials used.
- PostgreSQL rehearsal command: exit 2 / DEFERRED because no approved local runtime/URL/driver existed; no connection was attempted.

## Initial failed checks resolved

During implementation, the first combined regression run had one expected version assertion (`4 !== 3`); the assertion was updated to preserve the Phase 4 schema while accepting additive migrations. Initial Phase 5 tests exposed a test-fixture provisioning error and a prepared-statement invocation error; both were corrected. The first browser run found dialog focus return was lost because the triggering card was re-rendered; the UI now retains and restores focus. Final results above are the subsequent clean executions.
