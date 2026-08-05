# Phase 4 Implementation Plan

Date: 2026-08-05  
Branch: `feature/phase-4-operations-support`  
Initial status: clean (`## feature/phase-4-operations-support...origin/feature/phase-4-operations-support`)  
Verified Phase 3 baseline: commit `dbf58d2` is present in current history; Phase 2 baseline `d719043` is an ancestor of `HEAD`.

## Verified Phase 3 baseline

The Node.js/Express application under `backend/src/` is canonical and serves the plain HTML/CSS/JavaScript frontend. It has isolated migration-backed SQLite storage, bcrypt/JWT authentication, explicit one-time administrator provisioning, backend RBAC, schedule create/update/soft-cancel, bus/driver overlap checks, catalog and ordered-stop management, active-only assignment, redacted audit logging, request IDs, Helmet, exact-origin CORS, authentication/general API rate limits, controlled errors, SQLite backup/temporary restore verification, sanitized export rehearsal, 46 isolated backend tests, and two Chromium/axe browser tests. Java Swing and old unmounted backend routes remain legacy/reference code.

Before this plan, the exact branch, clean status, remote, recent history, Phase 3 migration/services/routes/scripts/tests/browser infrastructure, README, manifests, all canonical and legacy backend/frontend source, all automated tests, and every Phase 1–3 report were inspected. No `AGENTS.md` exists. The requested root Phase 4 prompt is absent; the complete supplied attachment is authoritative.

## Exact Phase 4 scope

1. Administrator-managed transport employees, separate from users and drivers.
2. Administrator special trips in `EXAM`, `CLUB_EVENT`, `INDUSTRIAL_VISIT`, and `OTHER` categories with explicit workflow states.
3. Database notifications, automatic schedule/special-trip events, audience filtering, and per-user read state.
4. Administrator-managed emergency contacts visible to authenticated users.
5. Authenticated feedback submission, owner-only history, and administrator assignment/resolution.
6. Plain-frontend views, isolated API/browser/axe tests, updated backup/export coverage, README, and all required Phase 4 evidence reports.

Cards, billing, payments, lost-and-found, external delivery, GPS/maps, PostgreSQL, deployment, production identity, frontend-framework migration, Java changes, and Phase 5 implementation are excluded.

## Expected files to create

- `backend/src/db/migrations/003_phase4_operations_support.sql`
- `backend/src/services/employeeService.js`
- `backend/src/services/notificationService.js`
- `backend/src/services/specialTripService.js`
- `backend/src/services/contactService.js`
- `backend/src/services/feedbackService.js`
- `backend/src/routes/employees.js`
- `backend/src/routes/specialTrips.js`
- `backend/src/routes/notifications.js`
- `backend/src/routes/contacts.js`
- `backend/src/routes/feedback.js`
- `backend/test/phase4.test.js`
- `browser-test/phase4-workflow.spec.js`
- `docs/phase-4/EMPLOYEE_MANAGEMENT.md`
- `docs/phase-4/SPECIAL_TRIPS.md`
- `docs/phase-4/NOTIFICATIONS_AND_CONTACTS.md`
- `docs/phase-4/FEEDBACK_MANAGEMENT.md`
- `docs/phase-4/TEST_EVIDENCE.md`
- `docs/phase-4/PHASE_4_COMPLETION_REPORT.md`
- `docs/phase-4/PHASE_5_RECOMMENDATION.md`

## Expected files to modify

- `.env.example` — only implemented Phase 4 page-size and feedback-limit settings.
- `.gitignore` — only if new generated artifacts are not already covered.
- `README.md` — verified Phase 4 setup, APIs, behavior, evidence, and limitations.
- `package.json` — Phase 4 test script and combined test inventory; no dependency is currently expected.
- `backend/src/app.js` — construct shared Phase 4 services and mount canonical routes/rate limit.
- `backend/src/config/index.js` — validate used Phase 4 environment settings.
- `backend/src/db/connection.js` — append migration 003 without rewriting prior migrations.
- `backend/src/db/operations.js` — required-table verification and privacy-safe export coverage.
- `backend/src/middleware/rateLimits.js` — dedicated deterministic feedback submission limiter.
- `backend/src/services/auditService.js` — narrowly extend metadata allowlist only where non-personal operational fields are useful.
- `backend/src/services/scheduleService.js` — automatic update/cancellation notification integration and approved-special-trip visibility, without duplicating conflict logic.
- `backend/src/utils/validation.js` — centralized Phase 4 validation and protected-field rejection.
- `backend/src/scripts/check.js` only if the existing recursive scope needs adjustment.
- `frontend/index.html`, `frontend/css/styles.css`, `frontend/js/app.js` — role-specific accessible Phase 4 views.
- existing database-operation/browser tests only when an expectation must reflect schema version 3 while preserving every case.

The completion report will record the actual exact set.

## Files and areas that must remain untouched

- `src/**/*.java`, all tracked Java bytecode, `lib/`, `.vscode/`, and `sources.txt`.
- Java `diu_transport.db`, unknown databases, user `.env`, Git history/config/remotes.
- Phase 1–3 reports and migrations `001`/`002`.
- unmounted pre-Phase-2 backend routes/schema/middleware.
- transport-card, billing, payment, lost-and-found, GPS/map, deployment, and PostgreSQL modules.

## Database migration plan

Migration 003 will add `employees`, `special_trips`, `notifications`, `notification_reads`, `emergency_contacts`, and `feedback`, with explicit foreign-key actions, enum/check constraints, case-insensitive employee-code uniqueness, automatic-notification dedupe keys, timestamps, and query indexes. It will preserve every Phase 3 row and advance schema version to 3 through the ordered runner. Fresh and deliberately constructed version-2 upgrade tests will prove table creation and Phase 3 data preservation.

Backup verification will require all Phase 4 tables. Sanitized export will preserve operational keys/status/timestamps while excluding user identities/hashes, employee and contact personal fields, notification message bodies, feedback subject/message/response text, and other private values.

## Employee model and permissions

Employees are operational records, never login accounts and never drivers. Fields are employee code, full name, controlled role, practical phone, optional validated email, controlled shift, status, optional notes, and server timestamps. Roles: `TRANSPORT_OFFICER`, `HELPER`, `MAINTENANCE`, `OTHER`; shifts: `MORNING`, `EVENING`, `NIGHT`, `FLEXIBLE`; statuses: `ACTIVE`, `INACTIVE`.

Administrators receive full list/detail/create/update/deactivate/reactivate. Authenticated non-admin reads return active minimum operational fields and omit phone, email, notes, and timestamps. Every mutation is transactionally audited; no physical delete is exposed.

## Special-trip model and state transitions

Each special trip extends one existing `schedules` row through a unique `schedule_id`; category/title/description/organizer, workflow state, requester/approver, and timestamps live only in `special_trips`. Creation makes a `SPECIAL` schedule in the inactive/cancelled state, preserving requested assignment/time without exposing it as an active schedule. Approval calls the existing schedule service to activate the schedule, so active route/bus/driver validation and overlap detection remain authoritative.

Allowed transitions are exactly `DRAFT → APPROVED`, `DRAFT → CANCELLED`, `APPROVED → CANCELLED`, and `APPROVED → COMPLETED`. DRAFT metadata/assignment may be edited; completed/cancelled trips are immutable. User reads show APPROVED only. Approval/cancellation/completion, create/update, conflicts, notifications, and audit events are transactional where practical.

## Notification architecture

Notifications remain local database records; no email/SMS/push transport exists. Administrators create/update/publish/cancel/expire manual records. Active user queries show published, non-expired records for all users or the current role and include a left-joined per-user `is_read`. `notification_reads` has a composite primary key so a user can idempotently mark only their own visible notification read.

Automatic records are generated for schedule updates/cancellations and special-trip approval/cancellation. Emergency manual notices publish through the same service. A unique non-null dedupe key derived from entity/event/request or irreversible transition prevents duplicate automatic records on retry. Related entity type/id is validated against a strict allowlist and existing records.

## Contact and feedback workflow

Emergency contacts have controlled role, required practical phone, optional email, availability, non-negative display order, active/inactive status, and server timestamps. Administrators create/update/reorder/deactivate/reactivate; authenticated users see active contacts ordered by display order. No unauthenticated exposure is added.

Authenticated users submit bounded categorized feedback and see only their own rows. High-risk credential patterns are rejected. Administrators receive bounded filters/pagination/detail and may transition `NEW → IN_REVIEW → RESOLVED → CLOSED`, with `NEW → CLOSED` and `IN_REVIEW → CLOSED` also permitted, add a bounded response, and assign only an active employee. `resolved_at` is server-controlled. Audit metadata records only changed fields/status/assignment identifiers, never feedback content.

## Audit and security integration

All routes reuse canonical authentication, `authorize`, request IDs, centralized errors, Helmet, CORS, and the general API limiter. Feedback POST receives a dedicated configurable limiter; notification mutations remain protected by both the general limit and ADMIN RBAC. Inputs reject IDs/timestamps/unknown fields. Audit rows use existing allowlist sanitization and never include employee/contact personal fields or message/feedback bodies.

## Frontend plan

The current single-page plain-JavaScript workspace will gain user navigation for Schedules, Special trips, Notifications, Emergency contacts, and Feedback, plus administrator navigation for Employees, Special trips, Notifications, Contacts, Feedback queue, existing catalogs, schedules, and audit. Forms/tables/cards will have labels, headings, captions, loading/empty/error/success states, confirmation for cancellations/deactivation, field constraints, visible focus, live messages, and 375px responsive behavior. All catalog/employee choices come from APIs.

## API test plan

Preserve the 25 Phase 2 and 21 Phase 3 tests. Add one isolated Phase 4 suite covering fresh/upgrade migration, RBAC, employee validation/lifecycle/history, all special-trip states and invalid transitions, assignment/overlap conflicts, user visibility, manual/automatic/deduplicated notifications, role/expiry/draft visibility, read ownership, contact ordering/lifecycle, feedback ownership/filtering/assignment/transitions/redaction/rate limiting, backup required tables, and sanitized export exclusions.

## Browser test plan

Preserve the two Phase 3 tests and add a serial Chromium Phase 4 workflow using its own temporary database/server/runtime credentials. It will exercise administrator employee/trip creation and approval, student trip/notification/read/contact/feedback workflows, administrator feedback resolution and audit visibility, then logout protection. No external service is required.

## Accessibility plan

Run axe on employee management, special trips, notifications, contacts, student feedback, and admin feedback. Assert no serious/critical violations and verify title/language, landmarks, headings, labels, named buttons, table headers, keyboard focus, and 375px horizontal overflow. Edge/Firefox, full keyboard-only, screen reader, 200% zoom, and high contrast remain explicitly manual unless actually executed.

## Risks and rollback

- Schema complexity: controlled by additive tables, explicit FKs/checks, fresh/v2-upgrade tests, online backup, and no destructive down migration.
- Schedule/special-trip consistency: one schedule service remains the sole assignment/conflict engine; outer transactions prevent partial state where practical.
- Duplicate notifications: non-null unique dedupe keys and idempotent read inserts.
- Privacy leakage: minimized user projections, sanitized exports, redacted audit allowlist, and regression scans/tests.
- Frontend size/accessibility: reuse shared rendering/forms, test critical workflows, axe all new views, and manually inspect a mobile viewport.
- Rollback: restore a verified pre-migration backup into a separate operator-selected target; Phase 4 never overwrites the active database.

## Implementation order

1. Add migration 003 and fresh/version-2 upgrade tests.
2. Add validation, employee, notification, contact, and feedback services/routes.
3. Add special-trip service using schedule operations; connect schedule automatic notifications.
4. Mount routes, feedback limiter, config, backup verification, and sanitized export.
5. Add isolated Phase 4 API tests while preserving the 46 baseline tests.
6. Expand the plain frontend and add Phase 4 Chromium/axe tests.
7. Run the ordered migration, syntax, Phase 2, Phase 3, Phase 4, backup/export, runtime, browser, accessibility, process, privacy, Java-scope, and Git checks.
8. Update README and complete all eight Phase 4 reports with actual evidence.

## Acceptance criteria

- Employee management, validation, safe lifecycle, RBAC, and audit pass.
- Special-trip draft/approval/cancel/complete, invalid transitions, active assignments, overlap conflicts, user visibility, notifications, and audit pass through the existing schedule engine.
- Manual/automatic notifications, audience/expiry/status rules, deduplication, and per-user read state pass.
- Contact lifecycle/order/active visibility and feedback ownership/admin resolution/assignment/rate limit/redaction pass.
- All 46 baseline backend tests and both baseline browser tests still pass; new API/Chromium/axe tests pass in isolated databases.
- Backup/verification/export include schema version 3 without private content or development DB changes.
- README and eight reports match executed evidence; Java, Git history, real data, deployment, PostgreSQL, external delivery, and out-of-scope modules remain untouched.
