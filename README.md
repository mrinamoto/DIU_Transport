# DIU Transport Schedule System

Phase 3 keeps the Node.js/Express web application as the canonical implementation and adds administrator catalog management, redacted audit logging, API security controls, safe SQLite operational rehearsals, and browser/accessibility automation. The Java 17 Swing application remains unchanged legacy/reference code and must not use the web database.

SQLite is suitable for this controlled development baseline only. This phase does not deploy the application or claim production readiness.

## Capabilities

- Public registration for `STUDENT` and `TEACHER` only; administrators are provisioned explicitly.
- bcrypt password hashes, expiring JWT authentication, fail-closed identity checks, and backend role authorization.
- Authenticated schedule viewing and admin-only schedule create/update/soft-cancel with assignment and overlap checks.
- Admin bus, driver, and route create/read/update/safe-deactivate/reactivate workflows.
- Active-only catalog data for non-admin schedule use; sensitive driver fields are admin-only.
- Ordered route stops with transactional replacement.
- Admin-only, filtered, paginated, newest-first redacted audit history.
- Request IDs, explicit single-origin CORS, Helmet headers, 100 KiB JSON limit, and configurable auth/general API rate limits.
- Timestamped online SQLite backup, temporary-copy restore verification, and sanitized JSON export rehearsal.
- Isolated Node integration tests plus Chromium workflow and axe accessibility checks.

Employee accounts, cards, billing, notifications, lost-and-found, GPS/maps, recurrence, password reset, production identity, observability, deployment, and PostgreSQL migration remain out of scope.

## Prerequisites and installation

- Node.js 20 through 24 (verified with Node.js 24.18.0)
- npm 10 or 11 (verified with npm 11.16.0)
- Java 17 only when inspecting the legacy desktop source

```powershell
npm install
Copy-Item .env.example .env
```

Set `AUTH_SECRET` in the local `.env` to an unpredictable value of at least 32 characters. Never commit `.env`, real credentials, or personal data. `CLIENT_ORIGIN` must exactly match the browser origin. Keep `WEB_DATABASE_PATH` separate from the legacy Java `diu_transport.db`.

## Initialize fictional development data

```powershell
npm run db:init
npm run db:seed
```

Initialization applies numbered migrations in order and refuses an unknown schema. The idempotent seed contains fictional catalog and schedule data only; it neither deletes rows nor creates an administrator.

## Provision the one administrator

```powershell
$env:ADMIN_FULL_NAME = 'Fictional Administrator'
$env:ADMIN_EMAIL = 'admin@example.test'
$env:ADMIN_PASSWORD = Read-Host 'Temporary administrator password'
npm run admin:create
Remove-Item Env:ADMIN_PASSWORD
```

The password must have at least 12 characters with uppercase, lowercase, number, and special character. Provisioning hashes it, never displays it, creates a redacted audit record, and rejects a second administrator.

## Run and use

```powershell
npm start
```

Open `http://localhost:5000`. The Express process serves both the frontend and API.

- `GET /api/health` — safe service/database status
- `/api/auth/*` — registration, login, and current user
- `/api/schedules/*` — authenticated schedule management
- `/api/catalog` — active schedule-form choices
- `/api/buses`, `/api/drivers`, `/api/routes` — active reads or admin management
- `/api/audit-logs` — admin-only filtered audit history

`npm run dev` uses Node's watch mode and is intended only for local development.

## Verification

```powershell
npm run check
npm run test:phase2
npm run test:phase3
npm test
npm run test:browser
npm audit --audit-level=low
```

The final Phase 3 run passed 50 syntax checks, all 25 preserved Phase 2 tests, all 21 Phase 3 API/database-operation tests, all 46 combined backend tests, and 2 Chromium browser tests. The browser suite includes automated axe checks and found no serious or critical violations on the tested login and admin-workspace states. Firefox, WebKit, screen-reader, full keyboard-only, 200% zoom, high-contrast, and Edge/Firefox visual checks were not executed.

Tests create isolated temporary SQLite databases and use runtime-generated fictional credentials. They do not modify the tracked development database or require external services.

## Backup, restore verification, and export rehearsal

```powershell
npm run db:backup
npm run db:backup:verify
npm run db:export
```

Backups and exports are timestamped in ignored directories. Backup uses SQLite's online backup operation and verifies source and destination integrity. Restore verification copies the selected backup to a unique temporary database, enables foreign keys, checks integrity/schema/required tables and safe counts, then deletes only that copy. Export produces sanitized JSON plus a manifest and excludes user names, email addresses, password hashes, and driver names/phones. These commands do not perform a PostgreSQL import.

## Security notes

- The server—not hidden frontend controls—enforces every role boundary.
- Public role input cannot create `ADMIN` or `STAFF` accounts.
- Invalid/expired tokens, unknown or disabled users, bad passwords, and database failures fail closed.
- JWTs contain a user identifier and standard claims; current role/status are reloaded on protected requests.
- Tokens stay in page memory, not local storage.
- Audit metadata is allowlisted and never receives raw request bodies, credentials, tokens, cookies, authorization headers, hashes, raw SQL, or stack traces.
- Request IDs are bounded safe client values or generated UUIDs and appear in controlled errors/audits.
- Helmet defaults are enabled. No Phase 3 header is intentionally disabled; production TLS/HSTS effectiveness still depends on deployment infrastructure.
- CORS allows only `CLIENT_ORIGIN`; credentialed wildcard CORS is not used.
- Rate limits use an in-memory store and are per-process development controls, not a distributed production defense.

## Structure and status

```text
backend/src/       canonical Express application, migrations, services, and scripts
backend/test/      isolated API and database-operation tests
browser-test/      isolated Playwright and axe workflow tests
frontend/          plain HTML, CSS, and JavaScript
docs/phase-1/      repository audit evidence
docs/phase-2/      secure web baseline evidence
docs/phase-3/      catalog/security/operations/browser evidence
src/, lib/, run.bat legacy Java 17 reference application
```

Older unmounted backend modules remain reference code and are not registered by `backend/src/app.js`. Root backend compatibility entry points continue to target the canonical web implementation.

## Known limitations and next direction

SQLite remains single-host development storage. Audit retention/export policy, distributed rate limiting, TLS/reverse-proxy operations, secure token/session revocation, account recovery, production monitoring, database encryption/key management, comprehensive WCAG testing, and disaster-recovery restore into an operator-selected target are not implemented.

Phase 4 should focus on production-readiness decisions and a separately approved PostgreSQL migration pilot—not a deployment by default. See [the Phase 3 completion report](docs/phase-3/PHASE_3_COMPLETION_REPORT.md) and [Phase 4 recommendation](docs/phase-3/PHASE_4_RECOMMENDATION.md).

## License

No software license has been selected. This is an academic development project, not an approved production university service.
