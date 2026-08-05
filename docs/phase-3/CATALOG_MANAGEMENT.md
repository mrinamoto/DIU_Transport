# Catalog Management Evidence

Date: 2026-08-05  
Canonical application: Node.js/Express web implementation

## Implemented model and API

The version 2 migration preserves Phase 2 data and adds ordered `route_stops`; the existing `buses`, `drivers`, and `routes` tables remain authoritative. Canonical authenticated routes are:

- `GET/POST /api/buses` and `GET/PATCH/DELETE /api/buses/:id`
- `GET/POST /api/drivers` and `GET/PATCH/DELETE /api/drivers/:id`
- `GET/POST /api/routes` and `GET/PATCH/DELETE /api/routes/:id`

`DELETE` is safe deactivation, never physical deletion. Admin PATCH can reactivate or move a bus to/from `MAINTENANCE`. IDs and timestamps are server-owned. Unknown and protected fields are rejected.

Non-admin reads return active scheduling data only. Driver phone/status details are not returned in that view. All create/update/deactivate/reactivate routes require backend `ADMIN` authorization.

## Rules verified

- Bus numbers and route names are trimmed/normalized and case-insensitively unique.
- Capacity is a bounded positive integer; statuses are allowlisted.
- Driver name is required; phone accepts a documented practical `+`, digits, spaces, parentheses, and hyphen format with 7–20 characters.
- Route origin/destination are required and cannot normalize to the same value.
- Inactive/maintenance catalog records cannot be assigned to new or updated schedules.
- A catalog record referenced by a future `ACTIVE` schedule cannot be deactivated; the API returns controlled HTTP 409.
- Historical schedules remain linked and are never deleted by catalog deactivation.
- Route stops are a bounded ordered string array, stored with unique `(route_id, stop_order)` and transactionally replaced on route update. Coordinates, map routing, and stop timing are intentionally absent.

## Frontend

The plain JavaScript workspace provides admin navigation for Buses, Drivers, Routes, and Audit log. Each catalog view has labeled create/edit controls, loading/empty/error/success states, table headers, edit, confirmation before deactivate, and reactivate. Schedule forms reload their choices from the active `/api/catalog` response after catalog mutations; no catalog option is hardcoded.

Student browser evidence confirmed catalog-management navigation was not rendered. This is usability only; API tests independently confirmed the backend returns 403 for non-admin mutations.

## Test evidence

```powershell
npm run test:phase2
npm run test:phase3
npm run test:browser
```

- Phase 2 regression: 25 passed, 0 failed.
- Phase 3 API/operations: 21 passed, 0 failed.
- Chromium workflows: 2 passed, 0 failed.

Coverage includes admin and non-admin access, create/update, normalized duplicates, invalid capacities/phones/statuses/routes, active assignment, future-assignment deactivation conflict, ordered stops, schedule CRUD using created catalogs, audit visibility, and logout. The browser critical workflow exercises all 15 workflow requirements from the Phase 3 prompt.

## Limitations

There is no physical catalog deletion, bulk import, geospatial route model, employee roster integration, or concurrency-oriented multi-user database. SQLite remains development-only.
