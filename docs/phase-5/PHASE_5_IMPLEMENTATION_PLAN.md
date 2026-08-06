# Phase 5 Implementation Plan

## Verified Phase 4 baseline

- Working directory: `F:\Projects\DIU_Transport`.
- Required branch: `feature/phase-5-production-readiness-governance`.
- Initial status was clean and tracked `origin/feature/phase-5-production-readiness-governance`.
- Remote URL: `https://github.com/mrinamoto/DIU_Transport.git`.
- `HEAD` was `12145d9`; verified Phase 4 commit `809eb38` is an ancestor.
- Schema version 3, the Phase 4 API/UI modules, 68-test backend baseline, four-test Chromium workflow, backup/export scripts, and all Phase 1-4 reports are present.

## Exact Phase 5 scope

Phase 5 adds identity lifecycle administration, immediate JWT revocation, bounded one-time recovery, data inventory/export/retention controls, a transactional NOOP notification outbox, redacted JSON operational logging, liveness/readiness/protected metrics, expanded browser/accessibility evidence, disaster-recovery evidence, and a conditional synthetic PostgreSQL rehearsal. SQLite remains the canonical development database. No external delivery or deployment is in scope.

## Files expected to be created

- `backend/src/db/migrations/004_phase5_production_readiness.sql`
- `backend/src/services/identityService.js`, `outboxService.js`, `dataGovernanceService.js`, `metricsService.js`
- `backend/src/routes/adminUsers.js`, `account.js`, `outbox.js`, `operations.js`
- `backend/src/middleware/operationalLogging.js`
- `backend/src/scripts/retention.js`, `postgresRehearsal.js`
- `backend/test/phase5.test.js`, `backend/test/phase5-governance.test.js`
- `frontend/js/phase5.js`
- Phase 5 reports named by the implementation prompt and ten files under `docs/phase-5/runbooks/`.

## Files expected to be modified

- `package.json`, `.env.example`, `README.md`, `playwright.config.js`
- `backend/src/app.js`, `config/index.js`, `db/connection.js`, `middleware/authenticate.js`, `middleware/errors.js`, `middleware/rateLimits.js`
- `backend/src/routes/auth.js`, `health.js`, `notifications.js`
- `backend/src/services/notificationService.js`
- `frontend/index.html`, `frontend/css/styles.css`, `frontend/js/app.js`
- `browser-test/workflow.spec.js` and a new or extended Phase 5 browser specification.

The exact final list may be smaller or include narrowly necessary Phase 5 test/support files; every deviation will be reported.

## Files that must remain untouched

- Every `src/**/*.java` Java Swing legacy file.
- Existing migrations `001_phase2_baseline.sql`, `002_phase3_catalog_audit.sql`, and `003_phase4_operations_support.sql`.
- Compiled legacy `.class` files and bundled JDBC artifacts.
- Git history and all production/external systems.

## Identity-lifecycle design

The existing `users.status` remains the ACTIVE/INACTIVE eligibility switch. A new `security_status` stores ACTIVE/LOCKED/SUSPENDED, avoiding a destructive users-table rebuild. Admin APIs provide paginated safe records and controlled role/status/security transitions. Any transition that would leave no active, usable administrator is rejected transactionally. Sensitive hashes and counters are never returned.

## Session-revocation strategy

Each user has a monotonically increasing `auth_version`. JWTs carry that version and authentication compares it with the current row on every request. Role/status/security changes, explicit revocation, and completed recovery increment the version, immediately invalidating earlier tokens.

## Password-recovery strategy

Administrators can initiate recovery and receive a cryptographically random development-only token exactly once. Only a SHA-256 hash is stored. Tokens expire, are single-use, are invalidated when superseded, and completion replaces the password hash and increments `auth_version`. Responses remain generic where account enumeration is possible; no provider is contacted.

## Data-classification model

Data is catalogued as public, internal, confidential, or restricted. Authentication secrets and recovery hashes are restricted; personal account/contact/feedback fields are confidential; operational identifiers and audit metadata are internal; intentionally published schedule/contact fields are public-to-authenticated audiences. Logs, metrics, exports, and screenshots expose only allowlisted safe fields.

## Retention and lifecycle strategy

The retention command defaults to a read-only plan. Apply requires an explicit CLI flag plus an environment safety guard, creates and verifies a database backup first, uses an allowlist of lifecycle operations, runs transactionally, and records the result. Tests use isolated temporary databases. User self-export and audited admin export return allowlisted data. An anonymization rehearsal is synthetic and dry-run by default.

## Notification outbox architecture

Publishing a notification and inserting an idempotent NOOP outbox record occur in the same SQLite transaction. Records have bounded attempts and PENDING/PROCESSING/DELIVERED/FAILED/CANCELLED lifecycle states. The provider boundary serializes only minimal recipient references and performs no network activity. Admin endpoints can inspect, retry eligible failures, and cancel pending records.

## Logging and observability architecture

One JSON event is emitted per request with timestamp, severity, event, request ID, method, route, status/outcome, duration, and environment. Redaction and field allowlisting exclude tokens, passwords, bodies, emails, database paths, and secrets. Liveness is process-only; readiness checks database access, schema version, and safe configuration. Protected metrics expose aggregate counters and gauges without personal or secret data.

## Incident-response plan

Ten runbooks will cover startup, shutdown, authentication incident, secret exposure, database corruption, backup/restore, outbox failure, user recovery, audit review, and release rollback. Each will state signals, immediate containment, diagnosis, recovery, verification, escalation, and evidence handling with redaction.

## Accessibility and browser-test plan

Preserve the Chromium baseline and cover identity administration, recovery, outbox, and operations UI. Exercise keyboard-only navigation, focus, skip link, dialogs/forms, 200% zoom, mobile/tablet/desktop viewports, and forced-colors. Firefox and WebKit run only if project-local Playwright browser support is already usable without global installation.

## Synthetic PostgreSQL rehearsal plan

First inspect only for an approved project-local Docker/workflow, an already approved local PostgreSQL service, or an explicitly synthetic connection URL. Do not install or start global infrastructure and never contact an unknown host. If prerequisites are absent, provide a versioned synthetic-only schema/export/parity/rollback rehearsal script and record the execution as deferred rather than passed.

## Backup and rollback strategy

Before destructive lifecycle application, create a verified SQLite backup using the existing safe tooling. Validate schema version, integrity, row-count parity, and restoration into an isolated path. Rollback for Phase 5 remains restoring the verified Phase 4/5 backup or application release; migrations are append-only and never reverse-edit historical migration files.

## Risks

- Race conditions around the final administrator are controlled with a database transaction and fresh count.
- Lockout can be abused for denial of service; thresholds are bounded/configurable and responses are generic.
- Recovery-token disclosure is reduced by one-time display, hash-only persistence, short TTL, and audit events.
- Retention mistakes are controlled by dry-run default, allowlists, explicit guards, verified backup, and transactions.
- Outbox duplication is controlled with unique idempotency keys.
- Operational telemetry leakage is controlled with an allowlist and redaction tests.
- Cross-browser execution may be limited by locally installed browser binaries.
- PostgreSQL rehearsal may be deferred when approved local prerequisites are absent.

## Stop conditions

Stop if the branch changes, the Phase 4 baseline is missing, Java modifications are required, a migration would require destructive changes to real data, any requested provider/deployment needs external credentials or contact, PostgreSQL points to an unknown/non-synthetic target, or safe completion would require commit/push/merge/rebase/Phase 6 work.

## Implementation order

1. Record baseline and repository evidence.
2. Add and verify schema version 4 on fresh and Phase 4 databases.
3. Implement identity lifecycle, revocation, lockout, recovery, and exports.
4. Implement retention tooling with safety interlocks.
5. Implement transactional outbox and NOOP provider boundary.
6. Implement structured logging, liveness/readiness, metrics, and runbooks.
7. Extend frontend and isolated API/governance/browser/accessibility tests.
8. Execute prior regressions, Phase 5 suites, backup/export checks, and conditional PostgreSQL rehearsal.
9. Update README and evidence reports; inspect diffs, secrets, Java preservation, ports, and final Git status.

## Acceptance criteria

- All earlier migrations and Java files are unchanged and all prior automated tests still pass.
- Fresh and Phase 4-to-Phase 5 migrations reach version 4 safely.
- Identity administration is authorized, audited, redacted, revokes stale JWTs, and cannot disable the final active administrator.
- Lockout and one-time recovery are bounded, generic externally, hash-only at rest, and tested.
- Exports are allowlisted; lifecycle apply is guarded, backed up, transactional, audited, and tested only on temporary data.
- Notification publishing creates one idempotent NOOP outbox job in the same transaction; retry/cancel paths are authorized and tested.
- Logs, readiness, and protected metrics are useful and contain no secret or personal values.
- The minimal frontend supports required Phase 5 administrator and recovery workflows accessibly.
- Browser and accessibility coverage accurately reports every engine/feature actually exercised or unavailable.
- PostgreSQL rehearsal is synthetic and executed only if its safety prerequisites are proven.
- Required documentation, exact test counts, limitations, changed files, and final Git status are recorded without committing or pushing.
