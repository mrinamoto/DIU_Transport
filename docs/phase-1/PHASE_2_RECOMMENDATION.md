# Phase 2 Recommendation

## Recommended objective

**Select and stabilize one canonical application path, fail closed on authentication, and establish one isolated, reproducible database/runtime baseline with automated smoke tests.**

Do not redesign the UI, migrate to PostgreSQL, or implement the entire feature list in Phase 2.

## Architecture recommendation

For an actual multi-user university system, evidence currently favors a **controlled web migration** based on the existing Express API, not a big-bang rewrite. The API already has route-level admin checks, bcrypt, prepared statements, and CRUD coverage; the Swing UI is a local single-user application whose feature screens are largely mock data.

For a classroom-only offline demonstration, desktop repair is viable and smaller. This is an owner decision because deployment audience—not code alone—determines the correct target.

## Prioritized order of work

1. **Decision gate:** choose `WEB` or `DESKTOP` as canonical; freeze the other as reference. Assign distinct development DB filenames immediately.
2. **Security gate:** prohibit public admin creation, remove default credentials and demo-login bypass, require configured secrets, and define server-side role policy.
3. **Reproducibility gate:** document the exact supported runtime; create the selected build/dependency lock through normal project tooling; verify a clean checkout.
4. **Database gate:** select one schema, add non-destructive initialization, fixture isolation, constraints, and a backup/restore policy. Do not run the current seed on valued data.
5. **Test gate:** add smoke tests for startup/health, registration role restrictions, login failure, authorization, and schema creation in a temporary DB.
6. **Vertical slice:** make one real flow work end-to-end: non-admin login -> view schedule; admin login -> create/update/cancel schedule -> user sees result.
7. **Documentation:** replace the README prompt with prerequisites, setup, safe commands, architecture status, and known limitations.

## Track-specific dependencies

### If web is selected

- Confirm Node version policy and create a reviewed lockfile.
- Make missing `JWT_SECRET` a startup error.
- Restrict registration to Student/Teacher/Staff or require admin provisioning.
- Implement the minimum login/schedule/admin schedule pages referenced by routes.
- Add API/integration tests using a temporary SQLite DB.

### If desktop is selected

- Provide a supported JDK 17 toolchain and a real SQLite JDBC dependency through a reproducible build descriptor.
- Remove compiled artifacts only after a clean build is proven.
- Hash passwords and remove default/demo access.
- Wire dashboards to services or clearly label mock views.
- Add service tests against a temporary SQLite DB.

## Phase 2 acceptance criteria

- One canonical target is documented; the other cannot accidentally use its DB.
- A clean checkout builds/starts with one documented command on the supported runtime.
- Missing secrets/dependencies/DB fail closed with a useful error.
- Public registration cannot create an admin.
- Wrong credentials never create a session.
- No plaintext password is stored or returned.
- Temporary test DB initialization is repeatable and non-destructive.
- Automated tests cover auth denial, role enforcement, and one schedule vertical slice.
- README matches verified behavior.
- No production deployment or PostgreSQL migration is included.

## Decisions required from the project owner

1. Is the deliverable a local classroom desktop demo or a multi-user browser application?
2. Are the names, IDs, emails, phone numbers, licenses, routes, and schedules in `backend/seed.js` confirmed fictional/approved?
3. Is any existing off-repository database data required to be preserved, and which schema created it?
4. Should administrators be provisioned only by an existing administrator/installation process?
5. Is the DIU logo/reference screenshot licensed and available from an approved source?

Phase 2 should not begin until decisions 1-3 are answered.
