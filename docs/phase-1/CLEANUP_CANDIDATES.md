# Cleanup Candidates

No file was deleted, moved, renamed, or cleaned during Phase 1.

| Path | Classification | Evidence | Removal risk | Referenced by | Recommended phase |
|---|---|---|---|---|---|
| `src/` | KEEP | Only tracked Java source of desktop app | Critical | compiled classes, VS Code config | Phase 2 canonical-target decision |
| `backend/` | KEEP BUT RELOCATE LATER | Substantial untracked Express API | High until committed/reviewed | `package.json` | Phase 2 if web selected |
| `frontend/index.html` | REPLACE LATER | Explicit Phase 1 placeholder | Low functionally; current root response depends on it | `backend/server.js` | Phase 3 after API stabilization |
| `frontend/admin`, `frontend/css`, `frontend/js` | UNKNOWN — MANUAL REVIEW REQUIRED | Empty directories; may be intended scaffolding | Low | No references found | Phase 2 owner decision |
| `bin/` | GENERATED — ADD TO .gitignore | 81 compiled classes; VS Code output path | Medium; useful only as unreproducible fallback | `.vscode/settings.json`, launch config | After reproducible build in Phase 2/3 |
| root `gui/` | DUPLICATE CANDIDATE | 46 compiled classes outside configured output | High; bytecode differs from `bin` | Direct manual classpath possible | Phase 3 after backup/build proof |
| root `model/` | DUPLICATE CANDIDATE | 27 compiled classes outside configured output | High | Direct manual classpath possible | Phase 3 |
| root `services/` | DUPLICATE CANDIDATE | 10 compiled classes outside configured output | High | Direct manual classpath possible | Phase 3 |
| root `util/` | DUPLICATE CANDIDATE | 3 compiled classes outside configured output | High | Direct manual classpath possible | Phase 3 |
| all tracked `.class` files | DELETE CANDIDATE AFTER BACKUP | Generated, stale/inconsistent, 167 files | High until source build passes | Runtime currently only has bytecode | Phase 3, never Phase 1 |
| `lib/jdbc-api-1.4.jar` | REPLACE LATER | Contains standard `java.sql` API classes; no `org.sqlite.JDBC` | High; current classpath references `lib/*` | VS Code config | Phase 2 desktop track |
| `lib/inc_linux`, `inc_win`, `inc_mac` | UNKNOWN — MANUAL REVIEW REQUIRED | JNI headers; no native source/build found | Medium; unknown provenance | No repository reference found | Phase 3 |
| `.vscode/settings.json` | REFACTOR LATER | Nonexistent hardcoded JDK 17 path | Low | VS Code Java extension | Phase 2 desktop track |
| `.vscode/launch.json` | REFACTOR LATER | Missing `RunDemo`, wrong main class/package/project names | Low | Developer tooling only | Phase 2 desktop track |
| `sources.txt` | REPLACE LATER | Absolute paths point to different checkout | Low | Manual compilation only | Phase 2 desktop track |
| `README.md` | REPLACE LATER | Aspirational generation prompt, not setup/status docs | Medium; captures requirements | Humans only | Phase 2 documentation |
| `.gitignore` | KEEP | Sensibly excludes DB, `.env`, logs, node modules | Low | Git | Phase 2 review/add Java outputs |
| `.env.example` | KEEP | Documents non-secret web variables | Low | dotenv/users | Phase 2 web track |
| `package.json` | KEEP | Declares untracked web runtime | Medium; lacks lock/test | npm | Phase 2 web track |
| `backend/seed.js` | SENSITIVE — REMOVE FROM GIT HISTORY LATER if data is real; otherwise REFACTOR LATER | destructive reset, known credentials, personal-looking data | Critical if run against valued DB | `npm run seed` | Phase 2 provenance and safety review |
| Java default credentials in `UserService`/`DIUTransportSystem` | SENSITIVE — REMOVE FROM GIT HISTORY LATER | source-known administrator credential | Critical | Desktop auth/help | Phase 2 security fix; history decision later |
| Node sample identities/credentials | SENSITIVE — REMOVE FROM GIT HISTORY LATER if not confirmed fictional | personal-looking names/IDs/contact data | High privacy risk | seed only | Phase 2 owner confirmation |
| `docs/phase-1/` | KEEP | Required audit evidence | Low | Project governance | Preserve |

## Git hygiene actions for later

1. Do not delete generated classes until the selected application builds reproducibly from source.
2. Extend `.gitignore` for Java output roots only after resolving why four roots exist.
3. Before committing the untracked web tree, remove security blockers, establish a lockfile deliberately, and review sample-data provenance.
4. Use a history-rewrite plan only if the owner confirms real secrets/personal data entered Git. No history rewrite is justified solely by these working-tree samples yet.
