# Security and Privacy Audit

All credential and personal-data values are redacted in this report. Findings cite locations and data types only.

## Risk summary

| Area | Risk | Evidence |
|---|---|---|
| Desktop passwords | CRITICAL: plaintext storage/comparison and password exposed through model getter | `src/model/User.java`; `src/services/UserService.java`; Java schema |
| Default desktop admin | CRITICAL: known hardcoded account created automatically and printed by help | `UserService.initializeAdminUser`; `DIUTransportSystem.printHelp` |
| Web admin registration | CRITICAL: unauthenticated caller may request admin role | `backend/routes/auth.js`; `backend/utils/validators.js` |
| Web JWT secret | HIGH: deterministic fallback secret | `backend/routes/auth.js:15`; `backend/middleware/authMiddleware.js:26` |
| Desktop auth bypass | HIGH: failure leads to demo session | `src/gui/LoginFrame.java:466-471` |
| Desktop authorization | HIGH: services do not enforce role/session policy | `src/services/*.java` |
| Restore path | HIGH: path concatenated into SQL; existing DB deleted first | `src/util/DatabaseConnection.java:337-394` |
| Seed/reset | HIGH: deletes all rows and exposes known demo credentials | `backend/seed.js:7-18,23-40,334-340` |
| Token freshness | MEDIUM: protected routes trust role/identity in token without active-account lookup | authentication middleware and route chain |
| Abuse controls | MEDIUM: no login rate limit, lockout, or request-size policy beyond Express defaults | `backend/server.js`; auth route |
| CORS | MEDIUM: unrestricted `cors()` | `backend/server.js:15` |

## Authentication

### Desktop

- `users.password` stores plaintext; login uses `WHERE username = ? AND password = ?`.
- Password change/reset writes plaintext.
- The `User` object retains and exposes the password.
- A default admin with a source-known password is inserted automatically.
- If DB authentication fails, the GUI deliberately logs in a constructed demo user. This hides operational errors and defeats authentication for user-level screens.
- The forgot-password dialog displays a success claim but sends no email and performs no reset (`LoginFrame.java:592-618`).

### Web

- Positive: bcrypt hashing at cost 10; password hash is removed from response objects; invalid login message does not enumerate email existence.
- Critical: registration validates `admin` as a public role.
- High: missing `JWT_SECRET` selects a known fallback instead of refusing startup.
- Tokens last seven days by default and carry role/active-independent claims. Deactivation/role changes do not immediately revoke existing tokens.
- No refresh-token, revocation, logout invalidation, MFA, rate limiting, or account lockout exists.

## Authorization

- Web admin CRUD usually combines `authenticateToken` and `requireRole('admin')`; this is real server-side RBAC for those routes.
- Public endpoints expose schedules/routes and active emergency contacts by design. Schedule responses include driver phone data, which may be excessive for public access (`backend/routes/schedules.js:21,50`).
- Web `/api/auth/me` reloads the user but does not reject an inactive account.
- Desktop dashboards are selected in the UI, but the service APIs accept no actor/session and therefore cannot enforce admin-only operations. Hiding buttons is not authorization.
- No confirmed direct object reference flaw was found in self-service web routes: card/billing/lost-found `my` queries use `req.user.id`. Admin routes intentionally accept IDs.

## Input and SQL handling

- Node CRUD predominantly uses `better-sqlite3` parameters. Dynamic search/filter SQL is composed from fixed fragments; no confirmed conventional SQL injection was found.
- Java CRUD predominantly uses `PreparedStatement` for values.
- Java `backupDatabase`/`restoreDatabase` concatenates caller-controlled paths into SQL. Restore also deletes the live DB before attempting unsupported restore syntax.
- Java `getTodaySchedules` concatenates an internally generated day token, so it is not currently user-controlled injection, but the pattern should not spread.
- Node validation is narrow. Many IDs, dates, amounts, text lengths, statuses, route existence, and issue/expiry ordering rely on SQLite errors or are unchecked.
- No file upload implementation exists. `image_path`/`profile_photo` are plain text fields; therefore path traversal via upload is not currently implemented, but future upload handling needs explicit controls.

## Secrets and personal data search

| Data type | Finding | Handling |
|---|---|---|
| `.env` | No real `.env`; `.env.example` contains variable names and example values only | Values not reproduced |
| API keys/private keys/tokens | No committed key material found | JWT fallback secret reported without value |
| Passwords | Known default/demo passwords in Java and Node seed | Values redacted |
| Names/emails/phones/student IDs/licenses | Personal-looking samples in seed/default contacts and model/UI data | Values redacted; provenance not verifiable |
| Database records | No DB file exists | None accessed |
| Git remote credentials | Remote contains no displayed credential | URL sanitized during audit |

The sample records may be fictional, but that is not provable. Treat them as personal data until the owner confirms consent/provenance.

## Logging and error exposure

- Java frequently prints database exception messages to stdout/stderr; connection code logs stack traces. Desktop users may see generic dialogs while sensitive details reach local logs/console.
- Node logs full error objects server-side and returns generic messages in most routes. `/api/health` returns a database user count, and its error path returns `err.message`; reduce this before production.
- No audit log records administrative security actions, role changes, deletes, or password resets.

## Remediation priority

1. Remove public admin registration and all default/plaintext password paths.
2. Fail closed when secrets/dependencies/DB are missing; remove demo authentication bypass.
3. Separate databases/schemas before any run or seed.
4. Add server-side authorization tests, active-account checks, rate limiting, and token revocation/versioning.
5. Minimize public driver/contact personal data and confirm sample-data provenance.
6. Replace unsafe backup/restore with a tested, parameter-safe operational procedure.

No secret was changed, revoked, or removed in Phase 1.
