# Phase 3 Implementation Plan

Date: 2026-08-05  
Branch: `feature/phase-3-catalog-security-hardening`  
Initial status: clean (`## feature/phase-3-catalog-security-hardening...origin/feature/phase-3-catalog-security-hardening`)  
Verified baseline ancestor: `d719043`

## Verified Phase 2 baseline

The Node.js/Express application is canonical and serves the plain HTML/CSS/JavaScript frontend. Phase 2 provides fail-closed JWT authentication, bcrypt password hashing, backend role authorization, an explicitly initialized web-only SQLite database, schedule list/create/update/soft-cancel behavior, conflict validation, safe fictional seed data, and 25 passing isolated integration tests. Java 17 Swing remains unmodified legacy/reference code.

The Phase 3 root prompt named by the user is not present in the working tree. The supplied Phase 3 attachment was read completely and is treated as the authoritative specification. The branch, ancestry, clean status, remote, Phase 1 reports, Phase 2 reports, manifests, migrations, backend, frontend, and tests were reviewed before this plan.

## Exact Phase 3 scope

1. Admin CRUD, safe deactivation, and reactivation for buses, drivers, and routes.
2. Active-catalog schedule integration and small transactional ordered-route-stop support.
3. Redacted audit logging plus an admin-only filtered/paginated audit view.
4. Request IDs, strict auth and general API rate limits, explicit CORS, security headers, controlled errors, and protected-field rejection.
5. SQLite-safe timestamped backup, non-destructive restore verification, and sanitized logical export rehearsal.
6. Preserved Phase 2 tests, additional isolated API/operations tests, Chromium workflow automation, automated axe accessibility checks, and an in-app browser walkthrough where available.
7. README and the seven required Phase 3 evidence/recommendation reports.

No deployment, PostgreSQL import, frontend framework migration, Java repair, real data, or Phase 4 implementation is included.

## Files to modify

- `package.json` and `package-lock.json` — justified dependencies and reproducible scripts.
- `.env.example` — only used rate-limit, proxy, audit, backup, export, and browser variables.
- `.gitignore` — backups, exports, Playwright artifacts, and test/runtime outputs.
- `README.md` — verified Phase 3 operation, security, testing, and limitations.
- `backend/src/app.js` — request/security middleware and new canonical routes.
- `backend/src/config/index.js` — validated Phase 3 environment settings.
- `backend/src/db/connection.js` — ordered migration runner supporting fresh and Phase-2-to-Phase-3 databases.
- `backend/src/middleware/authorize.js` and `backend/src/middleware/errors.js` — request-aware denial and controlled request IDs.
- `backend/src/routes/auth.js` and `backend/src/routes/schedules.js` — safe audit integration.
- `backend/src/scripts/check.js` and `backend/src/scripts/createAdmin.js` — browser-test syntax scope and provisioning audit.
- `backend/src/services/scheduleService.js` — atomic audit events for mutations/conflicts.
- `backend/src/utils/validation.js` — catalog and protected-field validation.
- `backend/test/integration.test.js` — preserve the original 25 Phase 2 tests unchanged and rerun them against the ordered migration runner.
- `frontend/index.html`, `frontend/css/styles.css`, `frontend/js/app.js` — catalog/audit management and accessibility improvements.

## Files to create

- `backend/src/db/migrations/002_phase3_catalog_audit.sql`
- `backend/src/middleware/requestId.js`
- `backend/src/middleware/rateLimits.js`
- `backend/src/services/auditService.js`
- `backend/src/services/catalogService.js`
- `backend/src/routes/catalogResources.js`
- `backend/src/routes/audit.js`
- `backend/src/db/operations.js`
- `backend/src/scripts/backupDatabase.js`
- `backend/src/scripts/verifyBackup.js`
- `backend/src/scripts/exportDatabase.js`
- `backend/test/phase3.test.js`
- `backend/test/database-operations.test.js`
- `browser-test/workflow.spec.js`
- `playwright.config.js`
- `docs/phase-3/CATALOG_MANAGEMENT.md`
- `docs/phase-3/AUDIT_AND_SECURITY_HARDENING.md`
- `docs/phase-3/BACKUP_AND_MIGRATION_REHEARSAL.md`
- `docs/phase-3/BROWSER_ACCESSIBILITY_TEST_EVIDENCE.md`
- `docs/phase-3/PHASE_3_COMPLETION_REPORT.md`
- `docs/phase-3/PHASE_4_RECOMMENDATION.md`

The final completion report will record the actual exact set if implementation evidence requires a small adjustment.

## Files and areas that must remain untouched

- `src/**/*.java`
- tracked Java bytecode under `bin/`, `gui/`, `model/`, `services/`, and `util/`
- `lib/`, `.vscode/`, and `sources.txt`
- legacy Java `diu_transport.db`, if present
- Phase 1 and Phase 2 reports
- already-applied `001_phase2_baseline.sql`
- unmounted pre-Phase-2 backend modules except the existing compatibility entry points if strictly necessary
- Git history, branch, remote, and configuration
- unknown databases or user `.env` files

## Database migration plan

Migration 002 will preserve Phase 2 tables and add `audit_logs`, ordered `route_stops`, case-insensitive uniqueness indexes for normalized bus/route names, and query-driven future-assignment/audit indexes. The migration runner will apply missing versions in order, retain schema history, enable foreign keys, and optimize after migration. Tests will cover a fresh database and a deliberately constructed version-1 database upgraded without losing its rows.

Route stops are approved because they fit a small isolated table and transactional replacement inside route create/update. Stops use server-owned IDs, required names, and unique `(route_id, stop_order)`; the API accepts a bounded ordered string array. No coordinates, maps, timings, or route engine will be added.

## Catalog API plan

Canonical authenticated endpoints will be `/api/buses`, `/api/drivers`, and `/api/routes`. Non-admin reads return active scheduling fields only. Admin reads include all statuses and timestamps. POST/PATCH/DELETE require backend `ADMIN`; DELETE means safe deactivation. PATCH may reactivate through an approved `status` value. Input is normalized, unknown/protected fields are rejected, uniqueness is case-insensitive, and deactivation/maintenance is blocked with HTTP 409 while a future active schedule references the record. Historical schedules remain intact.

## Frontend plan

The existing single-page plain-JavaScript workspace will gain accessible admin navigation and dedicated bus, driver, route, and audit panels. Each catalog panel will include labeled create/edit forms, loading/empty states, tables with headers, controlled validation/conflict messages, edit, deactivate confirmation, and reactivate controls. Schedule selects will continue to be populated only from the active `/api/catalog` response and will refresh after catalog changes. Non-admin users will not receive management controls.

## Audit-log plan

An allowlist-only audit service will record actor, action, entity, outcome, request ID, and deliberately limited metadata. It will never accept raw request bodies or sensitive header/auth/password fields. Catalog and schedule mutations will write their audit row in the same SQLite transaction. Authentication failures and provisioning use safe reason/action metadata only. The admin API will provide newest-first pagination with a strict maximum plus action, entity, actor, outcome, and date filters.

## Rate limiting and security plan

Use maintained `express-rate-limit` middleware: a configurable general `/api` policy and stricter login/registration policy with deterministic memory-store tests and controlled 429 JSON. A request-ID middleware will accept only bounded safe correlation IDs or generate UUIDs, return the ID header, and include it in errors/audits. Helmet remains enabled with its documented defaults. CORS will allow exactly the configured origin, never wildcard credentials, and produce controlled errors. The existing 100 KiB body limit remains. Catalog/schedule updates will reject unknown and server-owned fields.

## Backup, restore-verification, and export plan

Reusable operations will use `better-sqlite3`'s online backup API, timestamped non-overwriting paths, source/backup `integrity_check`, and ignored directories. Verification copies a selected backup into a unique OS-temporary file, enables foreign keys, verifies integrity, schema version, required tables, and safe counts, then removes only that temporary copy. The source and backup remain unchanged. Export creates a timestamped JSON manifest/bundle in an ignored directory, preserving non-sensitive keys/timestamps and row counts while omitting password hashes, secrets, contact values, and other credential material. It performs no PostgreSQL import.

## Browser and accessibility plan

Add Playwright test source in `browser-test/` with a self-contained temporary Phase 3 database and automatic ephemeral server lifecycle. Run Chromium for the required student/admin workflows and `@axe-core/playwright` for automated accessibility checks. Firefox/WebKit will run only if their executables are actually installed. Separately use the available in-app browser for a controlled walkthrough and DOM/accessibility inspection. Document automated violations and clearly separate remaining manual keyboard, screen-reader, 200% zoom, high-contrast, mobile, and cross-browser checks.

## Risks and rollback

- Migration risk is controlled by isolated fresh/upgrade tests and no automatic migration of unknown schemas. Rollback is restoration from a verified backup into a separate operator-selected target in a later controlled operation; Phase 3 verification never replaces the active DB.
- Case-insensitive indexes could fail on legacy duplicates. Migration must fail without deleting data; operators must resolve duplicates explicitly.
- Audit metadata leakage is controlled with an allowlist and regression scans/tests.
- Rate limits could disrupt existing tests; every test app receives explicit deterministic limits and isolated stores.
- Browser downloads may be environment-blocked. Approval will be requested only for justified package/browser installation; unexecuted browsers will not be claimed.
- Frontend expansion may introduce accessibility regressions; semantic landmarks/tables/forms, visible focus, axe, and browser inspection are acceptance gates.

## Implementation order

1. Add migration runner/version 2 and isolated upgrade test foundation.
2. Add request IDs, rate limits, hardened errors/CORS configuration, and audit service.
3. Add catalog service/routes and connect schedule/provisioning audits.
4. Add backup/verify/export operations and scripts.
5. Expand the plain frontend with catalog and audit administration.
6. Add API, operations, Playwright, and axe tests while preserving the original 25 cases.
7. Install only justified dependencies and refresh the lockfile.
8. Execute the prompt's ordered syntax, API, backup/export, runtime, browser, accessibility, process, secret, and scope verification.
9. Update README and all Phase 3 reports with actual evidence and exact final status.

## Acceptance criteria

- Admin catalog CRUD/deactivation/reactivation works; non-admin mutation returns 403.
- Validation, normalized uniqueness, active assignment, route-stop order, and future-assignment deactivation rules are tested.
- Schedule forms use active APIs and no hardcoded catalog.
- Redacted mutation/auth/provisioning audit records and admin-only filtered pagination work.
- Rate limits return controlled 429; request IDs, Helmet, explicit CORS, body limit, and controlled errors are verified.
- All original 25 Phase 2 cases plus new API and operations tests pass in isolated databases.
- Backup/integrity/temporary restore verification and sanitized export pass without changing the development DB.
- Chromium workflow and automated axe checks execute; only actually tested browsers are claimed.
- README and seven Phase 3 reports match executed evidence.
- Java/legacy areas remain byte-for-byte untouched; no secrets/real data, deployment, PostgreSQL import, commit, push, merge, rebase, or Phase 4 implementation occurs.
