# Database Audit

## Confirmed state

- Engine intended by both implementations: SQLite.
- No database file exists in the repository or working tree.
- No real records were inspected, printed, created, or modified.
- Java connection: `DriverManager` with `jdbc:sqlite:diu_transport.db` and `org.sqlite.JDBC`.
- Node connection: synchronous `better-sqlite3`, default root `diu_transport.db`, WAL, foreign keys enabled.
- ORM: none.

## Two incompatible schemas

| Concept | Java embedded schema | Node SQL schema |
|---|---|---|
| Users | `username`, plaintext `password`, title-case roles | `email`, bcrypt `password_hash`, lowercase roles, active flag |
| Primary identifiers | Mixed text IDs (`bus_id`, `route_id`, etc.) | Integer autoincrement IDs |
| Routes/stops | Comma-separated `stops`, `timing` | JSON `stops`, start/end/details/day type |
| Schedules | no driver column; text day list; active integer | bus/route/driver FKs; direction/day/status |
| Cards | card ID, payment status | card number, route and balance |
| Contacts | `contacts` | `emergency_contacts` |
| Extra Java tables | `fines`, `employees` | none |
| Extra Node tables | `feedback`, `notification_reads` | none |

Running both against one file would cause column-not-found and semantic failures because `CREATE TABLE IF NOT EXISTS` does not reconcile existing columns.

## Java schema overview

Twelve tables are created in `src/util/DatabaseConnection.java`: `users`, `buses`, `routes`, `schedules`, `transport_cards`, `billing`, `fines`, `drivers`, `employees`, `notifications`, `lost_found`, and `contacts`.

- Primary keys exist on all tables; several are caller-generated text IDs.
- Foreign keys cover schedule bus/route, cards/billing/fines user, and driver bus.
- Unique constraints: username, email, bus number/registration, driver license.
- Checks exist for several role/status/capacity/amount fields.
- No explicit indexes beyond those implicit in primary/unique keys.
- Dates and times are strings; no timezone is stored.
- Schema initialization also inserts default contacts and `UserService` inserts a default admin.
- Connections/statements usually use try-with-resources, but one global connection serves the entire desktop process.
- `executeQuery(String)` leaks statement ownership, and generic `executeUpdate(String)` accepts raw SQL.
- Backup/restore syntax is not standard SQLite SQL; restore deletes the current file first and concatenates a path into SQL.

## Node schema overview

Twelve tables are defined in `backend/models/db-schema.sql`: `users`, `buses`, `drivers`, `routes`, `schedules`, `transport_cards`, `notifications`, `notification_reads`, `lost_found`, `emergency_contacts`, `billing`, and `feedback`.

- Integer primary keys and foreign keys are generally present with `SET NULL`/`CASCADE` behaviors.
- Unique constraints: user email, bus number/plate, driver license, card number, notification read pair.
- Five explicit indexes cover schedule route/day, card user, lost-found status, and notification role.
- `student_id` is not unique.
- Booleans are unconstrained integers; amounts/balances lack non-negative checks; issue/expiry/due ordering is not constrained.
- Stops are stored as JSON text; ordered stops have no relational keys or per-stop timings.
- Dates/times and semester labels are text; timezone is absent. Schedules use presentation strings such as `7:00 AM`.
- Prepared statements are used for values in reviewed routes; dynamic filter clauses are assembled only from fixed fragments.
- `database.js` enables WAL and foreign keys, appropriate for a small single-host demo.
- `seed.js` deletes all rows then inserts samples without an explicit transaction. It contains known credentials and personal-looking dummy data and must be treated as destructive.

## Security and consistency

- Java password storage is plaintext: unacceptable for any real use.
- Node uses bcrypt with cost 10, a sound baseline, but allows public admin registration and a fallback JWT secret.
- Neither schema records an audit trail for administrative changes. Node has notification reads, not security auditing.
- Java and Node use different enum vocabularies (`Active` vs `active`, `Partial` vs no partial in Node billing, etc.).
- Personal-data fields include names, email, phone, student ID, license number, contact info, department, and profile photo path.

## Suitability

- SQLite is suitable for a single-user desktop course demo or a low-concurrency local web demonstration with backups and one owning process.
- SQLite plus local filesystem persistence is a poor fit for horizontally scaled/free ephemeral cloud hosting and a real university production system.
- PostgreSQL may be advisable later for multi-user production, but migration is **not** a Phase 2 prerequisite until the canonical schema and target architecture are selected and tested.

## Safe next step

Create no migration yet. First select one schema, give desktop and web distinct development database paths, document data ownership, add schema tests and a non-destructive fixture strategy, then decide whether PostgreSQL is warranted.
