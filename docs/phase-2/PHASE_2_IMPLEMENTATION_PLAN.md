# Phase 2 Implementation Plan

Date: 2026-08-05  
Branch: `feature/phase-2-web-baseline`  
Initial status: clean (`## feature/phase-2-web-baseline...origin/feature/phase-2-web-baseline`)

## Current architecture

The repository contains a Java 17 Swing application and a separate Express application. Phase 2 makes the Express application canonical and serves a plain HTML/CSS/JavaScript frontend from it. The Java source, compiled output, and configuration remain untouched as legacy/reference material.

The existing web code mixes startup, database initialization, routes, and configuration. Its public registration accepts administrator roles, JWT verification has a fallback secret, the seed is destructive, schedules are public, and the frontend is a placeholder. Phase 2 will introduce a testable application factory and an isolated, explicitly initialized SQLite database while retaining old web modules as unmounted reference/compatibility code where replacement is unnecessary.

## Files to modify

- `package.json` — canonical scripts, tested engines, dependency baseline.
- `.env.example` — safe web-specific configuration names/placeholders.
- `.gitignore` — web/test database, environment, dependency, log, coverage, build, IDE, and OS exclusions.
- `README.md` — verified Phase 2 setup, security, legacy note, and limitations.
- `backend/server.js` — compatibility entry that starts the canonical server.
- `backend/database.js` — compatibility entry for the canonical database connection.
- `backend/seed.js` — replace destructive/real-looking seed with safe canonical seed entry.
- `frontend/index.html` — semantic login, registration, schedule, and admin-management interface.

## Files to create

- `backend/src/app.js`
- `backend/src/server.js`
- `backend/src/config/index.js`
- `backend/src/db/connection.js`
- `backend/src/db/init.js`
- `backend/src/db/seed.js`
- `backend/src/db/migrations/001_phase2_baseline.sql`
- `backend/src/middleware/authenticate.js`
- `backend/src/middleware/authorize.js`
- `backend/src/middleware/errors.js`
- `backend/src/routes/auth.js`
- `backend/src/routes/catalog.js`
- `backend/src/routes/health.js`
- `backend/src/routes/schedules.js`
- `backend/src/services/scheduleService.js`
- `backend/src/utils/validation.js`
- `backend/src/scripts/createAdmin.js`
- `backend/src/scripts/check.js`
- `backend/test/integration.test.js`
- `backend/data/.gitkeep`
- `frontend/css/styles.css`
- `frontend/js/app.js`
- `package-lock.json`
- `docs/phase-2/ARCHITECTURE_DECISION.md`
- `docs/phase-2/SECURITY_CHANGES.md`
- `docs/phase-2/DATABASE_BASELINE.md`
- `docs/phase-2/TEST_EVIDENCE.md`
- `docs/phase-2/PHASE_2_COMPLETION_REPORT.md`
- `docs/phase-2/PHASE_3_RECOMMENDATION.md`

The exact set may shrink if a planned compatibility file is unnecessary. Any addition will be documented in the completion report.

## Files and areas that must remain untouched

- `src/**/*.java`
- tracked Java bytecode under `bin/`, `gui/`, `model/`, `services/`, and `util/`
- `lib/`, `.vscode/`, and `sources.txt`
- Phase 1 reports
- Git history, branch, remote, and configuration
- any unknown database or `.env` file (none exists at planning time)

## Implementation order

1. Update npm scripts/configuration and install project-local dependencies to create a lockfile.
2. Add centralized configuration and an explicit, migration-backed web database lifecycle.
3. Add secure JWT authentication, normalized roles, public registration restrictions, and one-time admin provisioning.
4. Add authenticated catalog reads and the admin schedule CRUD/deactivation service with assignment and overlap validation.
5. Replace the placeholder page with a responsive, accessible plain-JavaScript workflow.
6. Add deterministic integration tests using a temporary database.
7. Verify install, initialization, safe seed, syntax, tests, startup, health, auth, authorization, conflicts, and frontend serving.
8. Update README and complete Phase 2 evidence/recommendation reports.
9. Scan changes for secrets/personal data, confirm Java files are unchanged, and capture final Git status.

## Security risks and controls

| Risk | Phase 2 control |
|---|---|
| Public administrator creation | Server allowlist permits only `STUDENT` and `TEACHER`; case-normalized attempts at `ADMIN` are rejected. |
| Known/fallback JWT secret | Startup requires an environment-provided secret of at least 32 characters. Tests inject a fictional isolated secret. |
| Default administrator | No admin seed; explicit `npm run admin:create` reads transient environment variables and hashes the password. |
| Plaintext password | bcryptjs salted hash; hash never returned. |
| Stale/disabled identity | Authentication re-loads the account on each request and requires `ACTIVE` status. |
| Authorization bypass | Schedule writes require backend `ADMIN`; frontend controls are only an additional affordance. |
| Database collision | Web-only `WEB_DATABASE_PATH` defaults to `backend/data/diu_transport_web.db`; Java continues to reference `diu_transport.db`. |
| Destructive seed | Idempotent fictional inserts only; no delete/reset statements. |
| Conflict/data integrity | Foreign keys, controlled status values, time/date validation, transactions, and overlap checks. |
| Token exposure | Browser token remains in memory only; reload/log out clears it. Passwords are never retained. |

## Database strategy

- SQLite remains local-only for Phase 2.
- The runtime DB is ignored and created only by `npm run db:init`.
- Migration `001_phase2_baseline.sql` creates only users, buses, drivers, routes, schedules, schema history, and query-driven indexes.
- Startup refuses a missing/uninitialized DB rather than silently creating schema.
- Seed is optional, transactional, idempotent, and contains clearly fictional catalog/schedule records but no administrator.
- Tests use unique OS-temporary database directories and delete only their own temporary directory.
- Existing/unknown databases are never migrated or overwritten.

## Testing strategy

Use Node's built-in test runner and HTTP `fetch`, avoiding an additional test framework. Each test suite will:

- create and migrate a unique temporary SQLite database;
- inject a fictional secret without reading development configuration;
- create hashed fictional users, including an explicitly test-scoped admin;
- start the exported Express app on an ephemeral port;
- verify health, foreign keys, DB isolation, registration/login failures, inactive users, response redaction, anonymous/role denial, schedule list/create/update/deactivate, assignment validation, time validation, bus conflict, driver conflict, and self-exclusion on update;
- close the server/database and remove its own temporary files.

## Acceptance criteria

- Clean npm installation from a lockfile succeeds on the verified Node/npm baseline.
- Explicit initialization and safe seed operate only on the web-specific database.
- Backend and static frontend start with documented commands; health returns safe status.
- Public registration cannot create `ADMIN` or `STAFF`; approved roles can register.
- Failed/inactive authentication issues no token; hashes never leave the API.
- Administrator creation is explicit, hashed, duplicate-safe, and never prints a password.
- Every schedule endpoint requires authentication; only `ADMIN` can mutate.
- Valid create/update/deactivate works; invalid relationships/times and bus/driver overlaps fail.
- Automated critical tests pass against an isolated DB.
- README and seven Phase 2 reports match executed evidence.
- Java/legacy files remain byte-for-byte untouched; no commit, push, deployment, PostgreSQL, React, JavaFX, or Phase 3 implementation occurs.
