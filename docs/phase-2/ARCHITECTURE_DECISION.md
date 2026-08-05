# Phase 2 Architecture Decision

## Status

Accepted for Phase 2 on branch `feature/phase-2-web-baseline`.

## Decision

The canonical application is the Node.js/Express web implementation. Express serves both the JSON API and the existing plain HTML/CSS/JavaScript frontend. SQLite remains the local development database.

The Java 17 Swing source, its launcher, bundled library, and legacy database remain untouched reference material. They are not started, repaired, migrated, or presented as secure. The canonical web application defaults to `backend/data/diu_transport_web.db`, never the Java `diu_transport.db`.

## Runtime boundaries

- `backend/src/app.js` constructs the app without listening, enabling isolated tests.
- `backend/src/server.js` opens the configured web database and starts HTTP.
- Root backend entry points delegate to Phase 2 code for compatibility.
- `backend/src/config/index.js` is the single environment boundary.
- The browser calls same-origin `/api` routes; `CLIENT_ORIGIN` provides an exact CORS allowlist.
- Authentication uses only expiring JWT bearer tokens. No parallel cookie session scheme exists.
- SQLite schema is versioned by an explicit migration. Initialization does not run as a server-start side effect.

## Alternatives deferred

PostgreSQL, React or another frontend framework, JavaFX, cloud deployment, production identity integration, and repairs to the legacy Java application are explicitly deferred. These would increase scope without improving the requested vertical-slice evidence.

## Consequences

The baseline is reproducible and testable on one local Node process. SQLite is appropriate for local development but is not yet the selected production multi-user datastore. Old unmounted Node modules remain in the repository for traceability; they should not be mistaken for canonical routes and may be classified for later cleanup.
