# DIU Transport Schedule System

Phase 4 keeps the Node.js/Express web application as the canonical implementation and adds transport-operations and support workflows to the verified Phase 3 security baseline. The Java 17 Swing application remains unchanged legacy/reference code and must not use the web database.

SQLite is suitable for this controlled development baseline only. This phase does not deploy the application or claim production readiness.

## Capabilities

- Public registration for `STUDENT` and `TEACHER` only; administrators are provisioned explicitly.
- bcrypt password hashes, expiring JWT authentication, fail-closed identity checks, backend role authorization, request IDs, explicit CORS, Helmet headers, body limits, and configurable rate limits.
- Authenticated schedule viewing and admin-only schedule create/update/soft-cancel with active assignment and overlap checks.
- Admin bus, driver, and route CRUD/safe-deactivation workflows, ordered route stops, redacted audits, SQLite backup verification, and sanitized export rehearsal.
- Admin employee create/update/deactivate/reactivate with normalized unique codes, validated roles/shifts, and redacted audits.
- Special trips for `EXAM`, `CLUB_EVENT`, `INDUSTRIAL_VISIT`, and `OTHER`, with draft, approval, cancellation, and completion states. Approval reuses schedule assignment and conflict rules.
- Manual administrator notifications and automatic schedule/special-trip notifications with role audiences, lifecycle visibility, deduplication, and per-user read state.
- Admin-managed emergency contacts with authenticated active-contact viewing.
- Authenticated feedback submission, owner-only history, administrator filtering/assignment/response/resolution, content validation, and a dedicated rate limit.
- Plain HTML/CSS/JavaScript interfaces and isolated API, Chromium workflow, and axe accessibility tests.

Transport cards, billing/payment, lost-and-found, live GPS/maps, email/SMS delivery, recurrence, password reset, production identity, deployment, React migration, and PostgreSQL migration remain out of scope.

## Prerequisites and installation

- Node.js 20 through 24
- npm 10 or 11
- Java 17 only when inspecting the legacy desktop source

```powershell
npm install
Copy-Item .env.example .env
```

Set `AUTH_SECRET` in local `.env` to an unpredictable value of at least 32 characters. Never commit `.env`, credentials, or personal data. `CLIENT_ORIGIN` must exactly match the browser origin. Keep `WEB_DATABASE_PATH` separate from the legacy Java `diu_transport.db`.

The Phase 4 page-size variables and feedback limiter are documented in `.env.example`. Rate limits are per-process development controls, not a distributed production defense.

## Initialize fictional development data

```powershell
npm run db:init
npm run db:seed
```

Initialization applies numbered migrations in order and refuses an unknown schema. The idempotent seed contains fictional baseline catalog and schedule data only; it neither deletes rows nor creates an administrator. Migration tests cover both fresh initialization and non-destructive Phase 3-to-Phase 4 upgrade.

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

Open `http://localhost:5000`. Express serves both the frontend and API. `npm run dev` uses Node watch mode for local development only.

- `GET /api/health` - safe service/database status
- `/api/auth/*` - registration, login, and current user
- `/api/schedules/*` - authenticated schedule viewing and administrator management
- `/api/catalog`, `/api/buses`, `/api/drivers`, `/api/routes` - active choices and administrator catalog management
- `/api/employees` - active minimum-data reads and administrator employee management
- `/api/special-trips` - approved-trip reads and administrator lifecycle management
- `/api/notifications` - visible notification/read state and administrator publishing
- `/api/contacts` - active emergency contacts and administrator management
- `/api/feedback`, `/api/admin/feedback` - owner submission/history and administrator review
- `/api/audit-logs` - administrator-only filtered audit history

Notifications are in-app records only; this repository does not send email or SMS.

## Verification

```powershell
npm run check
npm run test:phase2
npm run test:phase3
npm run test:phase4
npm test
npm run test:browser
npm audit --audit-level=low
```

Tests create isolated temporary SQLite databases with runtime-generated fictional credentials. They do not modify the tracked development database or require external services. Exact final counts, browser coverage, and accessibility results are recorded in [Phase 4 test evidence](docs/phase-4/TEST_EVIDENCE.md).

Only Chromium automation is configured. Firefox, WebKit, screen-reader, complete keyboard-only, 200% zoom, high-contrast, and cross-platform visual verification are not claimed.

## Backup, restore verification, and export rehearsal

```powershell
npm run db:backup
npm run db:backup:verify
npm run db:export
```

Backups and exports are timestamped in ignored directories. Backup uses SQLite's online backup operation. Restore verification uses a unique temporary copy, checks integrity/schema/required tables and safe counts, then deletes only that copy. Export creates sanitized JSON and excludes credential, identity/contact, and operational free-text fields from users, drivers, employees, special trips, notifications, contacts, and feedback. These commands do not perform a PostgreSQL import.

## Security notes

- The server, not hidden frontend controls, enforces every role boundary.
- Public role input cannot create `ADMIN` or `STAFF` accounts.
- Invalid/expired tokens, unknown or disabled users, bad passwords, and database failures fail closed.
- Tokens stay in page memory, not browser storage.
- Audit metadata is allowlisted and never receives request bodies, credentials, tokens, cookies, authorization headers, hashes, raw SQL, feedback bodies, or contact details.
- Request IDs are bounded safe client values or generated UUIDs and appear in controlled errors/audits.
- CORS allows only `CLIENT_ORIGIN`; no credentialed wildcard CORS is used.
- Authentication, general API, and feedback-submission limits use in-memory stores suitable only for this single-process development baseline.

## Structure and status

```text
backend/src/       canonical Express application, migrations, services, and scripts
backend/test/      isolated API and database-operation tests
browser-test/      isolated Playwright and axe workflow tests
frontend/          plain HTML, CSS, and JavaScript
docs/phase-1/      repository audit evidence
docs/phase-2/      secure web baseline evidence
docs/phase-3/      catalog/security/operations/browser evidence
docs/phase-4/      operations/support implementation and verification evidence
src/, lib/, run.bat legacy Java 17 reference application
```

Older unmounted backend modules remain reference code and are not registered by `backend/src/app.js`. Root backend compatibility entry points continue to target the canonical web implementation.

## Known limitations and next direction

SQLite remains single-host development storage. Audit/feedback retention policy, distributed rate limiting, TLS/reverse-proxy operations, secure token revocation, account recovery, production monitoring, database encryption/key management, external notification delivery, and comprehensive WCAG testing are not implemented.

Phase 5 should first define production-readiness and data-governance decisions: identity/account lifecycle, notification delivery architecture, retention and incident procedures, observability, and a separately approved PostgreSQL migration rehearsal. It should not combine those decisions with deployment or payment work by default. See [the Phase 4 completion report](docs/phase-4/PHASE_4_COMPLETION_REPORT.md) and [Phase 5 recommendation](docs/phase-4/PHASE_5_RECOMMENDATION.md).

## License

No software license has been selected. This is an academic development project, not an approved production university service.
