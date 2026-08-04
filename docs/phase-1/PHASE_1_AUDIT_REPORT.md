# Phase 1 Repository Audit Report

Audit date: 2026-08-04 (Asia/Dhaka)  
Repository: `F:\Projects\DIU_Transport`  
Branch: `main`  
Scope: evidence-based, non-destructive Phase 1 audit only

## Executive summary

This repository is an **incomplete hybrid containing two separate applications**:

1. A tracked Java 17 Swing desktop application under `src/`, backed by direct SQLite/JDBC service calls and accompanied by tracked compiled `.class` files in four output locations.
2. A pre-existing, entirely untracked Node.js/Express/SQLite API under `backend/`, with a single placeholder browser page at `frontend/index.html`.

Neither application is currently reproducibly runnable in the audited environment. The Java classes target class-file version 61 (Java 17), but only Java 8 is on `PATH`, `javac` is unavailable, the configured JDK 17 path does not exist, and `lib/jdbc-api-1.4.jar` is not an SQLite driver. The Node sources pass `node --check`, but all six declared dependencies are missing and `npm start` exits on missing `dotenv`. No automated tests were found.

The two implementations define incompatible schemas while both default to `diu_transport.db`. They must not be pointed at the same database. Source evidence does not support the README's claim of a complete system.

## Audit basis and limitations

- The requested root file `PHASE_1_CODEX_AUDIT_PROMPT.txt` was absent. The supplied attachment `pasted-text.txt` was read as the authoritative prompt; the attachment was not copied into the repository.
- All repository files were inventoried. Text source/configuration was inspected; binary classes and JAR contents were identified by metadata/hash/archive entries rather than decompiled.
- No database file existed, so no personal database records were opened or changed.
- No dependencies were installed. No seed, delete, restore, backup, or database initialization command was run.
- No interactive GUI claims are made. Java startup failed before application code because of bytecode/runtime mismatch.

## Current confirmed state

| Area | Finding | Confidence |
|---|---|---|
| Actual project type | Incomplete hybrid: tracked Swing desktop app plus untracked Express API and placeholder web page | CONFIRMED |
| Java UI | Swing (`javax.swing`); no JavaFX/FXML | CONFIRMED |
| Java architecture | GUI -> concrete services -> shared static JDBC connection -> SQLite; model/enums alongside | CONFIRMED |
| Web backend | Express REST API using `better-sqlite3`, JWT, bcrypt, CORS, dotenv | CONFIRMED from source; not runnable |
| Web frontend | One placeholder `index.html`; empty `admin/`, `css/`, and `js/` folders | CONFIRMED |
| Python | No Python files or runtime role | CONFIRMED |
| Tests | No test source, framework, script, or CI workflow | CONFIRMED |
| Database | No `.db` file; two incompatible SQLite schema definitions | CONFIRMED |

## Severity register

Counts below are unique actionable issues; informational observations are excluded.

| Severity | Count |
|---|---:|
| CRITICAL | 2 |
| HIGH | 10 |
| MEDIUM | 15 |
| LOW | 9 |

### Critical

| ID | Confidence | Finding | Evidence |
|---|---|---|---|
| C-01 | CONFIRMED | Desktop authentication stores and compares plaintext passwords and auto-creates a known default administrator account. | `src/services/UserService.java:26-70,183-204,586-598`; `src/util/DatabaseConnection.java:77-93`; `src/gui/DIUTransportSystem.java:119-121` |
| C-02 | CONFIRMED | Public web registration accepts `role: admin`, enabling administrator self-registration when the API becomes runnable. | `backend/routes/auth.js:43-91`; `backend/utils/validators.js:4,17-19,45-49` |

### High

| ID | Confidence | Finding | Evidence |
|---|---|---|---|
| H-01 | CONFIRMED | Web JWT signing/verification falls back to a source-known secret when configuration is absent. | `backend/routes/auth.js:15`; `backend/middleware/authMiddleware.js:9-27` |
| H-02 | CONFIRMED | A failed desktop login creates a user from entered values and grants a live demo session instead of denying access. | `src/gui/LoginFrame.java:448-471` |
| H-03 | CONFIRMED | Desktop role enforcement is principally navigation/UI based; public service methods have no authorization context. | `src/gui/LoginFrame.java:451-470`; all `src/services/*.java`; `src/util/SessionManager.java` |
| H-04 | CONFIRMED | Java cannot compile/start here: no compiler/JDK 17, Java 8 runtime vs class version 61. | Command evidence in `BUILD_AND_RUNTIME_REPORT.md` |
| H-05 | CONFIRMED | Bundled `jdbc-api-1.4.jar` lacks `org.sqlite.JDBC`; desktop DB connection cannot succeed with repository dependencies. | `lib/jdbc-api-1.4.jar` archive entries; `src/util/DatabaseConnection.java:40` |
| H-06 | CONFIRMED | Java and Node define incompatible tables/columns/casing but use the same default filename. | `src/util/DatabaseConnection.java:76-234`; `backend/models/db-schema.sql`; both default to `diu_transport.db` |
| H-07 | CONFIRMED | The Node application cannot start because all declared dependencies are absent and there is no lockfile. | `package.json`; `npm ls --depth=0`; `npm start` |
| H-08 | CONFIRMED | Web login returns dashboard paths that do not exist; the only page explicitly says it is a placeholder. | `backend/routes/auth.js:20-23`; `frontend/index.html`; empty `frontend/admin`, `css`, `js` |
| H-09 | CONFIRMED | No automated tests exist for either implementation, including authentication and destructive administration paths. | Complete inventory; `package.json:scripts` |
| H-10 | CONFIRMED | Java backup/restore builds SQL from paths; restore deletes the live DB before using non-SQLite `RESTORE FROM`, risking loss/failure. | `src/util/DatabaseConnection.java:337-394` |

### Medium

M-01 stale JWTs do not re-check current role/active status on each protected request; M-02 unrestricted CORS; M-03 no authentication rate limiting; M-04 Node validation permits weak passwords and does not constrain many update fields consistently; M-05 `student_id` is not unique; M-06 Node schedule CRUD has no conflict detection; M-07 route stops are JSON/text rather than constrained ordered rows; M-08 date/time values are text and no timezone policy exists; M-09 multi-row seeding is not wrapped in an explicit transaction; M-10 Swing GUI classes are 661-1,138 lines and mix UI, mock data, navigation, and behavior; M-11 several Swing forms display success without persistence; M-12 tracked generated classes exist in four output trees and are inconsistent/stale; M-13 Java has no build descriptor or reproducible dependency declaration; M-14 no CI, contributing/security guidance, or license file; M-15 Node sources and README contain encoding/mojibake artifacts.

### Low

L-01 fixed desktop window dimensions; L-02 limited accessibility metadata/keyboard semantics; L-03 referenced `images/icon.png` and `images/` are absent; L-04 empty frontend directories; L-05 outdated 2024 copyright/sample dates; L-06 broad exception-to-console handling gives weak user diagnostics; L-07 README is an aspirational prompt rather than operator documentation; L-08 `.vscode/launch.json` contains invalid/nonexistent launch targets and project names; L-09 `sources.txt` hardcodes a different absolute project path (`F:\DIU_Transport`).

## Architecture conclusion

The tracked Java application is recoverable as a classroom desktop project, but not by merely fixing one compile error: its authentication, driver dependency, mock screens, build reproducibility, and schema must be stabilized first. The untracked web API is a separate partial implementation with stronger password hashing and route-level RBAC, but no usable frontend or installed dependency set.

## Go/no-go recommendation

- **NO-GO** for production, deployment, database sharing, or presenting all advertised features as complete.
- **Conditional GO** for Phase 2 stabilization after the owner selects one canonical target.
- Current evidence favors **controlled web migration** if the actual goal is multi-user university access, because a substantive Express API already exists and Swing screens are mostly mock/hardcoded. If the goal is only a local course demonstration, repairing the desktop application is smaller in scope. Do not merge the schemas or run the seed until that decision is made.

See `PHASE_2_RECOMMENDATION.md` for the prioritized next phase.

## Phase 1 files and final Git status

The audit created only these documentation files:

```text
docs/phase-1/ARCHITECTURE_AND_CODE_QUALITY_REPORT.md
docs/phase-1/BUILD_AND_RUNTIME_REPORT.md
docs/phase-1/CLEANUP_CANDIDATES.md
docs/phase-1/DATABASE_AUDIT.md
docs/phase-1/FEATURE_IMPLEMENTATION_MATRIX.md
docs/phase-1/PHASE_1_AUDIT_REPORT.md
docs/phase-1/PHASE_2_RECOMMENDATION.md
docs/phase-1/REPOSITORY_INVENTORY.md
docs/phase-1/SECURITY_AND_PRIVACY_AUDIT.md
docs/phase-1/UI_AND_ASSET_AUDIT.md
```

Exact final `git status --short --branch`:

```text
## main...origin/main
 M README.md
?? .env.example
?? .gitignore
?? backend/
?? docs/
?? frontend/
?? package.json
```

The first six non-`docs/` entries were present before the audit. With untracked files expanded (`git status --porcelain=v1 -uall`), `docs/` consists exactly of the ten files listed above. No source, configuration, dependency, database, asset, compiled binary, or Git-history file was modified by this audit.
