const fs = require('fs');
const os = require('os');
const path = require('path');
const bcrypt = require('bcryptjs');
const { before, after, test } = require('node:test');
const assert = require('node:assert/strict');
const { randomBytes } = require('node:crypto');
const { initializeDatabase } = require('../src/db/connection');
const { seedDatabase } = require('../src/db/seed');
const { createConfig } = require('../src/config');
const { createApp } = require('../src/app');
const { createAdministrator } = require('../src/scripts/createAdmin');

const strongPassword = (label) => `${label}!Aa1${randomBytes(24).toString('hex')}`;
const TEST_SECRET = randomBytes(48).toString('hex');
const STUDENT_PASSWORD = strongPassword('student');
const ADMIN_PASSWORD = strongPassword('administrator');
const REGISTRATION_PASSWORD = strongPassword('registration');
const REJECTED_PASSWORD = strongPassword('rejected');

let temporaryDirectory;
let databasePath;
let db;
let server;
let baseUrl;
let studentToken;
let adminToken;
let catalog;
let createdSchedule;

async function request(pathname, { method = 'GET', token, body } = {}) {
  const headers = { Accept: 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  const response = await fetch(`${baseUrl}${pathname}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const payload = await response.json();
  return { response, payload };
}

function insertUser({ fullName, email, password, role = 'STUDENT', status = 'ACTIVE' }) {
  const passwordHash = bcrypt.hashSync(password, 4);
  const info = db.prepare(`
    INSERT INTO users (full_name, email, password_hash, role, status)
    VALUES (?, ?, ?, ?, ?)
  `).run(fullName, email, passwordHash, role, status);
  return Number(info.lastInsertRowid);
}

async function login(email, password) {
  return request('/api/auth/login', { method: 'POST', body: { email, password } });
}

before(async () => {
  temporaryDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'diu-transport-phase2-'));
  databasePath = path.join(temporaryDirectory, 'isolated-test.db');
  db = initializeDatabase(databasePath);
  seedDatabase(db);
  insertUser({ fullName: 'Fictional Student', email: 'student@example.test', password: STUDENT_PASSWORD });
  insertUser({ fullName: 'Inactive Fictional User', email: 'inactive@example.test', password: STUDENT_PASSWORD, status: 'INACTIVE' });
  createAdministrator(db, { fullName: 'Fictional Administrator', email: 'admin@example.test', password: ADMIN_PASSWORD });

  const config = createConfig({
    env: {
      NODE_ENV: 'test', PORT: '0', CLIENT_ORIGIN: 'http://localhost',
      WEB_DATABASE_PATH: databasePath, AUTH_SECRET: TEST_SECRET,
      AUTH_EXPIRES_IN: '10m', PUBLIC_REGISTRATION_ROLES: 'STUDENT,TEACHER',
    },
  });
  server = createApp({ db, config }).listen(0);
  await new Promise((resolve) => server.once('listening', resolve));
  baseUrl = `http://127.0.0.1:${server.address().port}`;

  ({ payload: { token: studentToken } } = await login('student@example.test', STUDENT_PASSWORD));
  ({ payload: { token: adminToken } } = await login('admin@example.test', ADMIN_PASSWORD));
  ({ payload: { data: catalog } } = await request('/api/catalog', { token: adminToken }));
});

after(async () => {
  if (server) await new Promise((resolve) => server.close(resolve));
  if (db?.open) db.close();
  if (temporaryDirectory && temporaryDirectory.startsWith(os.tmpdir())) {
    fs.rmSync(temporaryDirectory, { recursive: true, force: true });
  }
});

test('test database is isolated and foreign keys are enabled', () => {
  const developmentPath = createConfig({ env: {}, requireAuthSecret: false }).databasePath;
  assert.notEqual(path.resolve(databasePath), path.resolve(developmentPath));
  assert.equal(db.pragma('foreign_keys', { simple: true }), 1);
});

test('health endpoint reports only safe service information', async () => {
  const { response, payload } = await request('/api/health');
  assert.equal(response.status, 200);
  assert.deepEqual(Object.keys(payload).sort(), ['database', 'status', 'version']);
  assert.equal(payload.database, 'connected');
});

test('public STUDENT registration succeeds with a salted hash and no hash response', async () => {
  const { response, payload } = await request('/api/auth/register', {
    method: 'POST',
    body: { full_name: 'New Fictional Student', email: 'new.student@example.test', role: 'STUDENT', password: REGISTRATION_PASSWORD },
  });
  assert.equal(response.status, 201);
  assert.ok(payload.token);
  assert.equal(payload.user.role, 'STUDENT');
  assert.equal('password_hash' in payload.user, false);
  const row = db.prepare('SELECT password_hash FROM users WHERE email=?').get('new.student@example.test');
  assert.notEqual(row.password_hash, REGISTRATION_PASSWORD);
  assert.equal(bcrypt.compareSync(REGISTRATION_PASSWORD, row.password_hash), true);
});

test('public TEACHER registration succeeds', async () => {
  const { response, payload } = await request('/api/auth/register', {
    method: 'POST',
    body: { full_name: 'New Fictional Teacher', email: 'teacher@example.test', role: 'TEACHER', password: REGISTRATION_PASSWORD },
  });
  assert.equal(response.status, 201);
  assert.equal(payload.user.role, 'TEACHER');
});

test('public administrator registration and casing variants are rejected', async () => {
  for (const [index, role] of ['ADMIN', 'admin', 'Admin', '  aDmIn  '].entries()) {
    const { response, payload } = await request('/api/auth/register', {
      method: 'POST',
      body: { full_name: 'Rejected User', email: `rejected-${index}@example.test`, role, password: REJECTED_PASSWORD },
    });
    assert.equal(response.status, 400);
    assert.equal('token' in payload, false);
  }
});

test('public STAFF and unknown roles are rejected', async () => {
  for (const [index, role] of ['STAFF', 'OPERATOR'].entries()) {
    const { response } = await request('/api/auth/register', {
      method: 'POST',
      body: { full_name: 'Rejected User', email: `role-${index}@example.test`, role, password: REJECTED_PASSWORD },
    });
    assert.equal(response.status, 400);
  }
});

test('valid fictional account can log in without exposing its password hash', async () => {
  const { response, payload } = await login('student@example.test', STUDENT_PASSWORD);
  assert.equal(response.status, 200);
  assert.ok(payload.token);
  assert.equal(payload.user.email, 'student@example.test');
  assert.equal('password_hash' in payload.user, false);
});

test('wrong password fails closed and issues no token', async () => {
  const { response, payload } = await login('student@example.test', strongPassword('wrong'));
  assert.equal(response.status, 401);
  assert.equal('token' in payload, false);
});

test('unknown user fails closed and issues no token', async () => {
  const { response, payload } = await login('missing@example.test', STUDENT_PASSWORD);
  assert.equal(response.status, 401);
  assert.equal('token' in payload, false);
});

test('inactive user cannot authenticate', async () => {
  const { response, payload } = await login('inactive@example.test', STUDENT_PASSWORD);
  assert.equal(response.status, 401);
  assert.equal('token' in payload, false);
});

test('anonymous schedule access is rejected', async () => {
  assert.equal((await request('/api/schedules')).response.status, 401);
});

test('authenticated non-admin can list and view active schedules', async () => {
  const list = await request('/api/schedules', { token: studentToken });
  assert.equal(list.response.status, 200);
  assert.ok(list.payload.data.length >= 2);
  const detail = await request(`/api/schedules/${list.payload.data[0].id}`, { token: studentToken });
  assert.equal(detail.response.status, 200);
});

test('non-admin schedule mutation receives 403', async () => {
  const result = await request('/api/schedules', { method: 'POST', token: studentToken, body: {} });
  assert.equal(result.response.status, 403);
});

test('administrator can create a valid non-conflicting schedule', async () => {
  const body = {
    route_id: catalog.routes[0].id, bus_id: catalog.buses[0].id, driver_id: catalog.drivers[0].id,
    service_date: '2031-05-20', departure_time: '11:00', arrival_time: '12:00',
    trip_type: 'REGULAR', notes: 'Fictional integration test.',
  };
  const { response, payload } = await request('/api/schedules', { method: 'POST', token: adminToken, body });
  assert.equal(response.status, 201);
  createdSchedule = payload.data;
  assert.equal(createdSchedule.status, 'ACTIVE');
});

test('missing required schedule fields are rejected', async () => {
  assert.equal((await request('/api/schedules', { method: 'POST', token: adminToken, body: {} })).response.status, 400);
});

test('client-supplied schedule identifiers are rejected', async () => {
  const result = await request('/api/schedules', {
    method: 'POST', token: adminToken, body: {
      id: 999,
      route_id: catalog.routes[0].id, bus_id: catalog.buses[0].id, driver_id: catalog.drivers[0].id,
      service_date: '2030-02-20', departure_time: '16:00', arrival_time: '17:00',
    },
  });
  assert.equal(result.response.status, 400);
  assert.match(result.payload.message, /assigned by the server/i);
});

test('invalid route, bus, and driver assignments are rejected', async () => {
  const base = {
    route_id: catalog.routes[0].id, bus_id: catalog.buses[0].id, driver_id: catalog.drivers[0].id,
    service_date: '2031-05-21', departure_time: '12:00', arrival_time: '13:00',
  };
  for (const field of ['route_id', 'bus_id', 'driver_id']) {
    const result = await request('/api/schedules', {
      method: 'POST', token: adminToken, body: { ...base, [field]: 999999 },
    });
    assert.equal(result.response.status, 400);
  }
});

test('inactive bus and inactive driver assignments are rejected', async () => {
  const inactiveBus = Number(db.prepare("INSERT INTO buses (bus_number, capacity, status) VALUES ('TEST-INACTIVE', 20, 'INACTIVE')").run().lastInsertRowid);
  const inactiveDriver = Number(db.prepare("INSERT INTO drivers (full_name, status) VALUES ('Inactive Test Driver', 'INACTIVE')").run().lastInsertRowid);
  const base = {
    route_id: catalog.routes[0].id, bus_id: catalog.buses[0].id, driver_id: catalog.drivers[0].id,
    service_date: '2031-05-21', departure_time: '14:00', arrival_time: '15:00',
  };
  assert.equal((await request('/api/schedules', { method: 'POST', token: adminToken, body: { ...base, bus_id: inactiveBus } })).response.status, 400);
  assert.equal((await request('/api/schedules', { method: 'POST', token: adminToken, body: { ...base, driver_id: inactiveDriver } })).response.status, 400);
});

test('arrival before departure is rejected', async () => {
  const result = await request('/api/schedules', {
    method: 'POST', token: adminToken, body: {
      route_id: catalog.routes[0].id, bus_id: catalog.buses[0].id, driver_id: catalog.drivers[0].id,
      service_date: '2031-05-22', departure_time: '15:00', arrival_time: '14:00',
    },
  });
  assert.equal(result.response.status, 400);
});

test('bus exact, partial, and containing overlaps are rejected', async () => {
  const base = {
    route_id: catalog.routes[1].id, bus_id: createdSchedule.bus.id, driver_id: catalog.drivers[1].id,
    service_date: createdSchedule.service_date,
  };
  for (const times of [['11:00', '12:00'], ['11:30', '12:30'], ['10:30', '12:30']]) {
    const result = await request('/api/schedules', {
      method: 'POST', token: adminToken,
      body: { ...base, departure_time: times[0], arrival_time: times[1] },
    });
    assert.equal(result.response.status, 409);
  }
});

test('driver overlap is rejected when the bus differs', async () => {
  const result = await request('/api/schedules', {
    method: 'POST', token: adminToken, body: {
      route_id: catalog.routes[1].id, bus_id: catalog.buses[1].id, driver_id: createdSchedule.driver.id,
      service_date: createdSchedule.service_date, departure_time: '11:15', arrival_time: '11:45',
    },
  });
  assert.equal(result.response.status, 409);
  assert.match(result.payload.message, /driver/i);
});

test('a non-conflicting schedule is accepted', async () => {
  const result = await request('/api/schedules', {
    method: 'POST', token: adminToken, body: {
      route_id: catalog.routes[1].id, bus_id: catalog.buses[1].id, driver_id: catalog.drivers[1].id,
      service_date: createdSchedule.service_date, departure_time: '13:00', arrival_time: '14:00',
    },
  });
  assert.equal(result.response.status, 201);
});

test('updating a schedule excludes itself from conflict detection', async () => {
  const result = await request(`/api/schedules/${createdSchedule.id}`, {
    method: 'PATCH', token: adminToken, body: {
      route_id: createdSchedule.route.id, bus_id: createdSchedule.bus.id, driver_id: createdSchedule.driver.id,
      service_date: createdSchedule.service_date, departure_time: '11:00', arrival_time: '12:00',
      trip_type: 'SPECIAL', notes: 'Updated without self-conflict.',
    },
  });
  assert.equal(result.response.status, 200);
  assert.equal(result.payload.data.trip_type, 'SPECIAL');
});

test('schedule deactivation is admin-only and preserves a cancelled record', async () => {
  assert.equal((await request(`/api/schedules/${createdSchedule.id}`, { method: 'DELETE', token: studentToken })).response.status, 403);
  const adminResult = await request(`/api/schedules/${createdSchedule.id}`, { method: 'DELETE', token: adminToken });
  assert.equal(adminResult.response.status, 200);
  assert.equal(adminResult.payload.data.status, 'CANCELLED');
});

test('one-time administrator provisioning rejects a second administrator', () => {
  assert.throws(() => createAdministrator(db, {
    fullName: 'Second Fictional Admin', email: 'second-admin@example.test', password: strongPassword('second'),
  }), /already been provisioned/i);
});
