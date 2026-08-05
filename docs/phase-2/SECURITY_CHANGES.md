# Phase 2 Security Changes

## Authentication and registration

- Public registration is server-allowlisted to `STUDENT` and `TEACHER`.
- Role input is trimmed and uppercased before comparison; `ADMIN`, casing/spacing variants, `STAFF`, missing values, and unknown roles are rejected.
- Passwords require at least 12 characters with uppercase, lowercase, number, and special character.
- bcryptjs cost 12 creates salted password hashes. Plaintext passwords and hashes are never returned by the API.
- Login uses one generic credential failure and never grants a demo/fallback session.
- Inactive accounts cannot authenticate.
- JWT signing requires an environment secret of at least 32 characters. There is no source fallback. Tokens expire, use an explicit issuer/audience and HS256, and carry only the user ID plus standard claims.
- Protected requests reload the active user and canonical role from SQLite.

## Administrator provisioning

`npm run admin:create` is the only Phase 2 administrator-creation path. It reads identity and password from environment variables, validates and hashes the password, rejects an existing email, and rejects provisioning when any administrator already exists. It never prints the password. Seeding creates no users or administrator.

## Authorization and API handling

- All schedule and catalog endpoints require authentication.
- Schedule mutations require `ADMIN` in backend middleware and return 403 otherwise.
- Central 404 and error middleware returns non-sensitive messages; unexpected stack traces and SQL stay server-side.
- Helmet security headers, a 100 KiB JSON limit, disabled `X-Powered-By`, parameterized SQL, transactions, and an exact CORS origin reduce baseline exposure.
- The frontend hides admin controls but does not act as the security boundary.
- Browser tokens live only in memory; passwords are never persisted.

## Data and secret review

`.env`, runtime databases and SQLite sidecars, logs, dependencies, coverage, builds, and test databases are ignored. `.env.example` contains placeholders only. Demonstration records use reserved `.test` addresses, obviously fictional labels, and non-routable placeholder phone values. No real credentials or personal data were added.

## Evidence

The isolated suite proves valid and failed login behavior, inactive-user rejection, no hash leakage, role-bypass rejection, anonymous rejection, non-admin 403 responses, and allowed admin mutation. A transient fictional administrator was also provisioned successfully in the ignored development database; a second provision attempt is rejected in automated testing.

## Remaining security work

This is not a production security certification. Rate limiting, account lockout policy, secret rotation, TLS/reverse-proxy policy, refresh/session revocation, audit logging, backup and restore, dependency monitoring, browser accessibility/security testing, and university identity/privacy review remain. Old Java and unmounted pre-Phase-2 Node code is legacy/reference and is not asserted secure.
