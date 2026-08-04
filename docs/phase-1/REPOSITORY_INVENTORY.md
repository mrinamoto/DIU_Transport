# Repository Inventory

## Root and initial Git state

- Working directory: `F:\Projects\DIU_Transport`
- Branch: `main`, tracking `origin/main`
- History: one visible commit, `6656309 first commit`; no tags
- Sanitized remote: `https://github.com/SayMoy/DIU_Transport.git`
- Initial status:

```text
## main...origin/main
 M README.md
?? .env.example
?? .gitignore
?? backend/
?? frontend/
?? package.json
```

These were pre-existing changes. No `AGENTS.md`, contributing guide, license file, CI workflow, issue templates, or PR template was found.

## Complete collapsed tree

Generated/binary trees are shown but their individual `.class` entries are collapsed here. They were counted and included in the full filesystem inventory.

```text
.
|-- .env.example                    # untracked; variable names only audited
|-- .gitignore                      # untracked
|-- .vscode/
|   |-- launch.json
|   `-- settings.json
|-- backend/                        # untracked Express application
|   |-- database.js
|   |-- seed.js
|   |-- server.js
|   |-- middleware/{authMiddleware.js,roleMiddleware.js}
|   |-- models/db-schema.sql
|   |-- routes/{auth,billing,cards,contacts,feedback,lostfound,notifications,routesApi,schedules,users}.js
|   `-- utils/{pagination,validators}.js
|-- bin/                            # 81 tracked compiled .class files
|   |-- gui/
|   |-- model/enums/
|   |-- services/
|   `-- util/
|-- frontend/                       # untracked
|   |-- index.html                  # placeholder only
|   |-- admin/                      # empty
|   |-- css/                        # empty
|   `-- js/                         # empty
|-- gui/                            # 46 tracked compiled .class files
|-- lib/
|   |-- jdbc-api-1.4.jar            # tracked; not SQLite JDBC
|   |-- inc_linux/{jni.h,jni_md.h}
|   |-- inc_win/{jni.h,jni_md.h}
|   `-- inc_mac/                    # empty
|-- model/                          # 27 tracked compiled .class files
|-- services/                       # 10 tracked compiled .class files
|-- src/
|   |-- gui/{AdminDashboard,DIUTransportSystem,LoginFrame,Main,RegisterFrame,UserDashboard}.java
|   |-- model/{AddFine,Admin,Billing,Bus,Contact,Driver,Employee,LostFound,Notification,Route,Schedule,Staff,Student,Teacher,TransportCard,User}.java
|   |-- model/enums/{BusStatus,CardStatus,ContactRole,DriverShift,EmployeeRole,FineStatus,FineType,LostFoundStatus,NotificationType,PaymentStatus,UserRole}.java
|   |-- services/{BillingService,BusService,DriverService,LostFoundService,NotificationService,ScheduleService,TransportCardService,UserService}.java
|   `-- util/{Constants,DatabaseConnection,SessionManager}.java
|-- util/                           # 3 tracked compiled .class files
|-- package.json                    # untracked; no lockfile
|-- README.md                       # modified before audit
`-- sources.txt                    # tracked absolute javac source list
```

## File-type summary

| Type | Count | Approx. lines | Notes |
|---|---:|---:|---|
| `.java` | 44 | 10,347 | Tracked Swing/model/service/JDBC source |
| `.class` | 167 | n/a | Tracked generated output in 4 roots |
| `.js` | 17 | 2,030 | Untracked Express backend |
| `.sql` | 1 | 160 | Untracked Node schema |
| `.html` | 1 | 18 | Placeholder page |
| `.jar` | 1 | n/a | 24,600-byte legacy JDBC API archive, not SQLite driver |
| JNI headers | 4 | n/a | Tracked platform headers, no native source/build using them found |
| JSON | 3 | 98 | package and VS Code configuration |
| Markdown | 1 pre-audit | 125 | README only |

No Python, CSS, browser JavaScript, TypeScript, React/Vue/Angular, FXML, image, font, archive, log, test, Maven, Gradle, shell, batch, or database file was found. There are no zero-byte files; four empty directories are listed in the tree.

## Entry points and build files

| Candidate | Evidence | Status |
|---|---|---|
| `gui.Main` | `src/gui/Main.java:6` | Swing entry point; instantiates `LoginFrame` |
| `gui.DIUTransportSystem` | `src/gui/DIUTransportSystem.java:14` | Swing splash + DB entry point |
| `gui.LoginFrame.main` | `src/gui/LoginFrame.java:656` | Direct Swing entry point |
| `backend/server.js` | `package.json:main`, `scripts.start` | Express entry point, untracked |

There is no Java build file. VS Code settings name `src` and `bin`, reference `lib/*.jar`, and request a nonexistent `C:\Program Files\Java\jdk-17`. `sources.txt` is not portable because it points to `F:\DIU_Transport`, not this checkout.

## Databases and schema sources

- No `diu_transport.db` exists.
- Desktop schema is embedded in `src/util/DatabaseConnection.java`.
- Web schema is `backend/models/db-schema.sql` and is loaded by `backend/database.js` at startup.
- Both default to root `diu_transport.db`, despite incompatible definitions.

## Assets

No images or fonts exist. `src/util/Constants.java` references `images/` and `images/icon.png`, but neither exists. Emoji and system fonts are used by Swing; `frontend/index.html` contains only inline CSS.

## Generated, duplicate, and sensitive warnings

- `bin/`, `gui/`, `model/`, `services/`, and `util/` contain 167 tracked generated `.class` files. Hashes show the root outputs are not byte-identical to `bin/`, indicating different compilation generations.
- The checked-in JAR duplicates standard `java.sql` classes and is mislabeled for the project's need.
- `.env.example` exposes no secret value, only `PORT`, `JWT_SECRET`, `JWT_EXPIRES_IN`, and `DB_PATH` names.
- Source contains known default/demo credentials and personal-looking sample names, IDs, emails, licenses, and phone numbers. Values are intentionally not repeated in these reports.
- `backend/seed.js` begins by deleting all rows from 12 tables. It was read but not executed.
