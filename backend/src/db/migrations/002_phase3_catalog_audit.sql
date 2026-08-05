CREATE TABLE audit_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  actor_user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
  action TEXT NOT NULL,
  entity_type TEXT NOT NULL,
  entity_id INTEGER,
  outcome TEXT NOT NULL CHECK(outcome IN ('SUCCESS', 'DENIED', 'CONFLICT', 'FAILURE')),
  request_id TEXT NOT NULL,
  metadata_json TEXT NOT NULL DEFAULT '{}',
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE route_stops (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  route_id INTEGER NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  stop_name TEXT NOT NULL CHECK(length(trim(stop_name)) BETWEEN 1 AND 100),
  stop_order INTEGER NOT NULL CHECK(stop_order >= 1),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(route_id, stop_order)
);

CREATE UNIQUE INDEX idx_buses_number_nocase
ON buses(bus_number COLLATE NOCASE);

CREATE UNIQUE INDEX idx_routes_name_nocase
ON routes(route_name COLLATE NOCASE);

CREATE INDEX idx_schedules_route_future_active
ON schedules(route_id, service_date)
WHERE status = 'ACTIVE';

CREATE INDEX idx_audit_logs_created_at
ON audit_logs(created_at DESC, id DESC);

CREATE INDEX idx_audit_logs_action_entity
ON audit_logs(action, entity_type);

CREATE INDEX idx_audit_logs_actor
ON audit_logs(actor_user_id);
