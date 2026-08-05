# DIU Transport Schedule System

Phase 2 establishes the Node.js/Express application as the canonical implementation. It provides secure registration and login, backend role authorization, an isolated SQLite database, and a tested schedule-management vertical slice with a plain HTML/CSS/JavaScript interface.

The Java 17 Swing application remains unchanged as legacy/reference code. It is not the canonical application, has not been repaired in Phase 2, and must never use the web database. Do not treat it as secure or production-ready.

## Current capabilities

- Public registration for `STUDENT` and `TEACHER` only
- bcrypt password hashing and expiring JWT authentication
- Explicit, one-time administrator provisioning
- Authenticated schedule list/detail access
- Admin-only schedule creation, update, and safe cancellation
- Active route, bus, and driver validation
- Same-day bus and driver overlap prevention
- Responsive minimal frontend served by Express
- Isolated, deterministic integration tests

Features such as employee management, transport cards, billing, notifications, lost-and-found, recurrence, production identity integration, and deployment remain out of scope.

## Prerequisites

- Node.js 20 through 24 (verified with Node.js 24.18.0)
- npm 10 or 11 (verified with npm 11.16.0)
- Java 17 only if inspecting the legacy desktop source

No global npm package is required.

## Install and configure

```powershell
npm install
Copy-Item .env.example .env
```

Edit `.env` locally. Set `AUTH_SECRET` to a long, unpredictable value of at least 32 characters. Never commit `.env` or reuse a development secret in production.

The default configuration serves the frontend and API together at `http://localhost:5000`. `CLIENT_ORIGIN` is an exact allowed browser origin. `WEB_DATABASE_PATH` must remain distinct from the legacy Java `diu_transport.db`.

## Initialize demonstration data

Initialization is explicit and refuses an existing database whose schema is unknown:

```powershell
npm run db:init
npm run db:seed
```

The seed is idempotent and contains only clearly fictional routes, buses, drivers, and dated schedules. It never deletes existing rows and never creates an administrator or a known password.

## Provision the administrator

Set the following variables only in the current shell, then run the one-time command:

```powershell
$env:ADMIN_FULL_NAME = 'Fictional Administrator'
$env:ADMIN_EMAIL = 'admin@example.test'
$env:ADMIN_PASSWORD = Read-Host 'Temporary administrator password'
npm run admin:create
Remove-Item Env:ADMIN_PASSWORD
```

Use a password with at least 12 characters including uppercase, lowercase, number, and special character. The command hashes the password, does not display it, and rejects a second administrator. Change the illustrative identity before use; do not use real personal data in development.

## Run

```powershell
npm start
```

Open `http://localhost:5000`. `npm run dev` currently uses the same deterministic command; automatic file watching is intentionally not an added dependency.

Useful endpoints:

- `GET /api/health` — safe service/database status
- `/api/auth/*` — registration, login, and current user
- `/api/schedules/*` — authenticated schedule vertical slice
- `/api/catalog/*` — authenticated form options

## Verify

```powershell
npm run check
npm test
```

`npm run check` syntax-checks backend and frontend JavaScript. Tests create and remove an OS-temporary SQLite database, require no network or real secret, and never modify the development database.

## Structure

```text
backend/
  src/
    config/             centralized environment configuration
    db/                 connection, migration, init, and safe seed
    middleware/         authentication, authorization, errors
    routes/             health, auth, catalog, schedules
    services/           schedule rules and transactions
    scripts/            checks and administrator provisioning
    app.js              Express application factory
    server.js           HTTP listener
  test/                 isolated integration suite
  data/                 ignored local web database; .gitkeep only
frontend/               plain HTML, CSS, and JavaScript
src/, lib/, run.bat      legacy Java 17 reference application
docs/phase-1/            repository audit evidence
docs/phase-2/            implementation and verification evidence
```

Root `backend/server.js`, `backend/database.js`, and `backend/seed.js` are compatibility entry points into the canonical Phase 2 implementation. Older unmounted backend route/schema modules remain reference code and are not registered by `backend/src/app.js`.

## Security notes

- Public role input is normalized and allowlisted; it cannot create `ADMIN` or `STAFF`.
- Authentication fails closed. Disabled accounts, invalid/expired tokens, unknown users, wrong passwords, and database failures do not grant access.
- JWTs contain only a user identifier and standard claims. The current user and role are reloaded from SQLite for every protected request.
- Admin controls hidden in the frontend are only a usability measure; Express middleware enforces authorization.
- Tokens are kept in page memory, not local storage. Refreshing the page signs the user out.
- Runtime databases, sidecars, secrets, logs, dependencies, and test artifacts are ignored.
- This academic baseline has no TLS termination, rate limiting, CSRF protection for future cookie auth, password reset, audit log, backup automation, or production privacy controls.

## Known limitations and next direction

Schedules use concrete service dates only; recurrence and timezone-aware instants are deferred. Cancellation is a soft state change, not permanent deletion. Catalog management, account administration, accessibility/browser matrix testing, production observability, and formal migration tooling require later work.

Phase 3 should harden the web baseline and expand catalog administration before any production rollout. PostgreSQL remains a future multi-user migration target; it was not introduced in Phase 2. No cloud deployment is configured.

See [the Phase 2 completion report](docs/phase-2/PHASE_2_COMPLETION_REPORT.md) for exact verification evidence and remaining work.

## License and status

No software license has been selected. This is an academic development project and is not an approved production university service.
