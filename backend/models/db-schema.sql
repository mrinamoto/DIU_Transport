-- DIU Transport Schedule System — Database Schema
-- SQLite (better-sqlite3)

PRAGMA foreign_keys = ON;

-- ============ USERS ============
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  full_name TEXT NOT NULL,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT CHECK(role IN ('student','teacher','staff','admin')) DEFAULT 'student',
  student_id TEXT,
  phone TEXT,
  department TEXT,
  semester TEXT,
  profile_photo TEXT,
  is_active INTEGER DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============ BUSES ============
CREATE TABLE IF NOT EXISTS buses (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  bus_number TEXT UNIQUE NOT NULL,
  capacity INTEGER NOT NULL,
  status TEXT CHECK(status IN ('available','running','maintenance','cancelled')) DEFAULT 'available',
  model TEXT,
  registration_plate TEXT UNIQUE
);

-- ============ DRIVERS ============
CREATE TABLE IF NOT EXISTS drivers (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  full_name TEXT NOT NULL,
  phone TEXT NOT NULL,
  license_number TEXT UNIQUE NOT NULL,
  shift TEXT CHECK(shift IN ('morning','evening','night')),
  bus_id INTEGER REFERENCES buses(id) ON DELETE SET NULL,
  is_active INTEGER DEFAULT 1
);

-- ============ ROUTES ============
CREATE TABLE IF NOT EXISTS routes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  route_code TEXT UNIQUE NOT NULL,      -- e.g. R1, R2, F1
  route_name TEXT NOT NULL,             -- e.g. "Dhanmondi <> DSC"
  start_point TEXT NOT NULL,
  end_point TEXT NOT NULL DEFAULT 'Daffodil Smart City (DSC)',
  stops TEXT,                           -- JSON array of stop names
  route_details TEXT,                   -- full text description of the path
  day_type TEXT CHECK(day_type IN ('regular','friday')) DEFAULT 'regular',
  distance_km REAL,
  estimated_duration TEXT
);

-- ============ SCHEDULES ============
CREATE TABLE IF NOT EXISTS schedules (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  bus_id INTEGER REFERENCES buses(id) ON DELETE SET NULL,
  route_id INTEGER REFERENCES routes(id) ON DELETE CASCADE,
  driver_id INTEGER REFERENCES drivers(id) ON DELETE SET NULL,
  departure_time TEXT NOT NULL,         -- e.g. "07:00 AM"
  departure_note TEXT,                  -- e.g. "will go upto Mirpur-10, Pallabi & ECB only"
  arrival_time TEXT NOT NULL,
  direction TEXT CHECK(direction IN ('to_campus','from_campus')) NOT NULL,
  day_type TEXT CHECK(day_type IN ('weekday','friday','exam')) DEFAULT 'weekday',
  status TEXT CHECK(status IN ('active','cancelled','delayed')) DEFAULT 'active',
  semester_label TEXT DEFAULT 'Mid-Term Exam, Summer 2026'
);

-- ============ TRANSPORT CARDS ============
CREATE TABLE IF NOT EXISTS transport_cards (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  card_number TEXT UNIQUE NOT NULL,
  issue_date DATE NOT NULL,
  expiry_date DATE NOT NULL,
  status TEXT CHECK(status IN ('active','expired','pending','suspended')) DEFAULT 'pending',
  route_id INTEGER REFERENCES routes(id) ON DELETE SET NULL,
  balance REAL DEFAULT 0
);

-- ============ NOTIFICATIONS ============
CREATE TABLE IF NOT EXISTS notifications (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  message TEXT NOT NULL,
  type TEXT CHECK(type IN ('info','warning','emergency','update')) DEFAULT 'info',
  target_role TEXT DEFAULT 'all',
  created_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_active INTEGER DEFAULT 1
);

-- ============ NOTIFICATION READ TRACKING ============
CREATE TABLE IF NOT EXISTS notification_reads (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  notification_id INTEGER REFERENCES notifications(id) ON DELETE CASCADE,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  read_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(notification_id, user_id)
);

-- ============ LOST & FOUND ============
CREATE TABLE IF NOT EXISTS lost_found (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  reported_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
  item_name TEXT NOT NULL,
  description TEXT,
  bus_number TEXT,
  route_name TEXT,
  date_lost DATE,
  status TEXT CHECK(status IN ('reported','found','claimed','returned')) DEFAULT 'reported',
  contact_info TEXT,
  remarks TEXT,
  image_path TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============ EMERGENCY / SUPPORT CONTACTS ============
CREATE TABLE IF NOT EXISTS emergency_contacts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  role TEXT NOT NULL,
  phone TEXT NOT NULL,
  email TEXT,
  available_hours TEXT,
  is_active INTEGER DEFAULT 1
);

-- ============ BILLING ============
CREATE TABLE IF NOT EXISTS billing (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  semester_label TEXT NOT NULL,
  amount REAL NOT NULL,
  due_date DATE,
  paid_date DATE,
  method TEXT,
  status TEXT CHECK(status IN ('paid','due','pending','overdue')) DEFAULT 'due',
  description TEXT
);

-- ============ FEEDBACK ============
CREATE TABLE IF NOT EXISTS feedback (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
  subject TEXT NOT NULL,
  message TEXT NOT NULL,
  status TEXT CHECK(status IN ('new','reviewed','resolved')) DEFAULT 'new',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============ INDEXES ============
CREATE INDEX IF NOT EXISTS idx_schedules_route ON schedules(route_id);
CREATE INDEX IF NOT EXISTS idx_schedules_daytype ON schedules(day_type);
CREATE INDEX IF NOT EXISTS idx_cards_user ON transport_cards(user_id);
CREATE INDEX IF NOT EXISTS idx_lostfound_status ON lost_found(status);
CREATE INDEX IF NOT EXISTS idx_notifications_role ON notifications(target_role);
