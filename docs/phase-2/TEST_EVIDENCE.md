# Phase 2 Test Evidence

## Environment

- Windows PowerShell workspace: `F:\Projects\DIU_Transport`
- Branch: `feature/phase-2-web-baseline`
- Node.js: 24.18.0
- npm: 11.16.0
- Test runner: built-in `node:test`, concurrency 1
- Database: unique OS-temporary SQLite file removed after the run

## Commands and actual results

```text
npm install       PASS — 122 packages added, 123 audited, 0 vulnerabilities
npm run db:init   PASS — web schema version 1 initialized
npm run db:seed   PASS — fictional demonstration data seeded
npm run admin:create PASS — one administrator provisioned; password not displayed
npm run check     PASS — 36 JavaScript files passed syntax checking
npm test          PASS — 25 passed, 0 failed
npm start         PASS — process started and was stopped cleanly after probes
```

The first dependency attempt with `better-sqlite3` 11.10.0 failed on the verified Node 24 runtime because no prebuilt binary was available and the host lacked the native C++ build workload. The manifest was corrected to the maintained Node-24-compatible 12.11.1 line. A subsequent install passed. The initial test invocation also revealed a Windows directory-target issue, and the first executable suite exposed an update self-validation defect. Both were corrected; these intermediate failures are retained here rather than hidden.

## Final automated coverage: 25/25

Authentication and authorization coverage includes valid login, wrong/unknown credentials, no token on failure, inactive account rejection, no hash leakage, STUDENT and TEACHER registration, ADMIN casing bypass rejection, STAFF/unknown role rejection, anonymous protected access, non-admin 403, and allowed administrator mutation.

Schedule coverage includes list/detail, valid create, required-field rejection, server-owned ID enforcement, invalid route/bus/driver rejection, inactive assignment rejection, arrival ordering, exact/partial/containing bus overlaps, driver overlap, accepted non-conflict, update self-exclusion, and admin-only soft cancellation.

Database coverage proves the test database is not the development path and foreign keys are enabled. Each run creates its own database and cleans it up.

## Runtime probes

With a transient unprinted runtime secret, the canonical listener started on port 5000. `GET /api/health` returned HTTP 200 with `status: ok` and `database: connected`. `GET /` returned HTTP 200 and the expected application title. Anonymous `GET /api/schedules` returned HTTP 401. The exact process started for verification was then stopped.

The automated integration suite executed registration, successful and failed login, authorization, complete administrator schedule CRUD/deactivation, and collision cases through HTTP. Static frontend delivery was verified. A human browser walkthrough and visual/accessibility matrix were not executed and remain manual verification.
