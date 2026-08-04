# Build and Runtime Report

## Environment detected

| Tool | Result |
|---|---|
| `java -version` | Java 8 (`1.8.0_471`), exit 0 |
| `javac -version` | Command not found, exit 1 |
| Configured JDK | `C:\Program Files\Java\jdk-17` does not exist |
| `mvn -version` | Command not found, exit 1 |
| `gradle -version` | Command not found, exit 1 |
| `node -v` | `v24.18.0`, exit 0 |
| `npm -v` | `11.16.0`, exit 0 |
| `python --version` / `py --version` | `3.14.6`; Python is not used by repository source |

## Commands attempted

| Command | Working directory | Exit | Important result | Classification |
|---|---|---:|---|---|
| `java -version` | project root | 0 | Java 8 only | Environment |
| `javac -version` | project root | 1 | `javac` not recognized | Environment blocker |
| `mvn -version` | project root | 1 | Maven not recognized | Environment; no `pom.xml` anyway |
| `gradle -version` | project root | 1 | Gradle not recognized | Environment; no Gradle files anyway |
| `java -cp "bin;lib/*" gui.DIUTransportSystem --help` | project root | 1 | `UnsupportedClassVersionError`: class version 61, runtime supports through 52 | Environment/runtime mismatch |
| `node --check <each backend .js>` | project root | 0 each | All 17 JavaScript files parse | Verified syntax only |
| `npm ls --depth=0` | project root | 1 | Six unmet dependencies | Missing local dependencies |
| `npm start` | project root | 1 | `Cannot find module 'dotenv'` from `backend/server.js:4` | Missing dependency |

No test command exists. `npm seed` was intentionally not run because `backend/seed.js:18` deletes existing table contents. No database connection was attempted because doing so would initialize/create a database and the prerequisite runtimes/dependencies already failed.

## Build conclusion

### Java desktop

- **Does it build? NOT VERIFIED / currently blocked.** There is no reproducible build descriptor and no compiler in the audited environment.
- **Does existing bytecode start? NO.** It fails before application logic due to Java 17 bytecode on Java 8.
- Even with Java 17, database startup is expected to fail: `DatabaseConnection` loads `org.sqlite.JDBC`, which is absent from `lib/jdbc-api-1.4.jar`.
- Existing `.class` files prove a compilation happened elsewhere; they do not prove current source builds or runs.

### Node/web

- **Does it build?** No build step is defined. JavaScript syntax is verified.
- **Does it start? NO.** Local dependencies are missing.
- No `package-lock.json` exists, so dependency resolution is not reproducible.
- The only frontend is static placeholder HTML and requires no build.

## Documented/reproducible commands if prerequisites are later approved

These are source-derived instructions, not successful Phase 1 results:

```text
Web: npm start
Web development: npm run dev
Destructive demo reset: npm run seed   [DO NOT RUN against valued data]
Java intended by VS Code: run gui.Main or gui.DIUTransportSystem with Java 17 and a real SQLite JDBC driver
```

There is no exact portable Java compile command in the repository. `sources.txt` contains stale absolute paths.

## Blockers in priority order

1. Choose the canonical desktop or web implementation and isolate its database.
2. Remove administrator self-registration/default credentials before any shared testing.
3. Establish a reproducible runtime/dependency baseline (without doing so in Phase 1).
4. Provide an actual SQLite JDBC dependency if desktop is retained.
5. Add tests before running destructive seed/administration paths.
6. Implement or provide the web pages referenced by login redirects if web is retained.
