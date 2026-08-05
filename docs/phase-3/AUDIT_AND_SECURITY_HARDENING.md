# Audit and Security Hardening

Date: 2026-08-05

## Audit design

Migration 002 adds `audit_logs` with actor, action, entity type/id, outcome, request ID, redacted JSON metadata, and creation time plus query indexes. The audit service accepts only explicit metadata keys and recursively rejects sensitive key names. It never receives raw request bodies.

Recorded events include administrator provisioning; catalog create/update/deactivate/reactivate; schedule create/update/deactivate; important schedule conflicts; safe-summary authentication failures; and denied administrative mutations where an audit service is available. Catalog and schedule success events use the same SQLite transaction as their mutation. Conflict failures are recorded after the rejected transaction rolls back.

The admin-only `GET /api/audit-logs` endpoint sorts newest first and supports bounded pagination plus action, entity, actor, outcome, and date filters. `AUDIT_PAGE_SIZE_MAX` is a strict server maximum. The frontend exposes the same filters to administrators.

Forbidden audit content includes passwords, hashes, tokens, cookies, authorization headers, complete bodies, secrets, raw SQL, stack traces, and personal contact details. Tests stringify returned audit data and assert sensitive marker/value absence.

## Security controls

- A request ID middleware validates bounded `[A-Za-z0-9._:-]` input or generates a UUID, returns `X-Request-Id`, and adds it to controlled errors and audit rows.
- `express-rate-limit` provides a strict login/registration policy and a more generous `/api` policy. Windows/maxima are validated environment values. Controlled 429 JSON includes the request ID. Isolated apps/stores make tests deterministic.
- Helmet 8.3.0 default middleware is installed and enabled; Phase 3 intentionally disables no Helmet header. Effective TLS/HSTS operation still depends on a future reverse proxy/deployment.
- CORS accepts exactly `CLIENT_ORIGIN`. It does not use wildcard credentials and rejects unapproved origins through centralized controlled errors.
- JSON bodies remain limited to 100 KiB.
- Catalog and schedule update validation rejects unknown and server-owned fields.
- Centralized 404/error handling omits SQL, stack traces, and internal exception details.
- `TRUST_PROXY` is parsed explicitly instead of trusting arbitrary forwarded values.

Frontend-hidden controls are not a security boundary. Authentication reloads the current account from SQLite, and authorization is enforced at each backend route.

## Dependency review

Justified additions:

- `express-rate-limit` 8.6.2 for maintained route limits.
- `@playwright/test` 1.62.1 for isolated browser automation.
- `@axe-core/playwright` 4.12.1 for automated accessibility rules.

```powershell
npm audit --audit-level=low
```

Result: 0 vulnerabilities. No forced upgrade was used.

## Verification evidence

The 21 Phase 3 API/operations tests cover redacted successful audit rows, admin-only audit access, filters/pagination/request IDs, controlled rate-limit behavior, normal traffic, security headers/CORS/errors, catalog RBAC, and validation. All passed. The 25 Phase 2 regression tests also passed.

## Remaining limitations

Rate-limit state is in-process and is not suitable for a horizontally scaled deployment. There is no central security event sink, retention/erasure policy, account lockout, MFA, password reset, token revocation list, CSRF control for future cookie authentication, TLS termination, or production privacy review.
