const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { randomBytes } = require('node:crypto');
const bcrypt = require('bcryptjs');
const { before, after, test } = require('node:test');
const assert = require('node:assert/strict');
const { initializeDatabase } = require('../src/db/connection');
const { seedDatabase } = require('../src/db/seed');
const { createAdministrator } = require('../src/scripts/createAdmin');
const { createConfig } = require('../src/config');
const { createApp } = require('../src/app');

const password = (label) => `${label}!Aa1${randomBytes(20).toString('hex')}`;
const ADMIN_PASSWORD = password('administrator');
const STUDENT_PASSWORD = password('student');
let temporaryDirectory; let databasePath; let db; let server; let baseUrl; let adminToken; let studentToken;
let bus; let driver; let route; let futureSchedule;

async function request(pathname, { method = 'GET', token, body, headers = {} } = {}) {
  const requestHeaders = { Accept: 'application/json', ...headers };
  if (token) requestHeaders.Authorization = `Bearer ${token}`;
  if (body !== undefined) requestHeaders['Content-Type'] = 'application/json';
  const response = await fetch(`${baseUrl}${pathname}`, { method, headers: requestHeaders, body: body === undefined ? undefined : JSON.stringify(body) });
  const payload = await response.json().catch(() => ({}));
  return { response, payload };
}

before(async () => {
  temporaryDirectory = fs.mkdtempSync(path.join(os.tmpdir(), 'diu-transport-phase3-api-'));
  databasePath = path.join(temporaryDirectory, 'api.db');
  db = initializeDatabase(databasePath); seedDatabase(db);
  const studentHash = bcrypt.hashSync(STUDENT_PASSWORD, 4);
  db.prepare("INSERT INTO users (full_name,email,password_hash,role) VALUES (?,?,?,'STUDENT')")
    .run('Fictional Phase Three Student', 'phase3.student@example.test', studentHash);
  createAdministrator(db, { fullName: 'Fictional Phase Three Administrator', email: 'phase3.admin@example.test', password: ADMIN_PASSWORD });
  const config = createConfig({ env: {
    NODE_ENV:'test', PORT:'0', CLIENT_ORIGIN:'http://approved.test', WEB_DATABASE_PATH:databasePath,
    AUTH_SECRET:randomBytes(48).toString('hex'), AUTH_EXPIRES_IN:'10m', PUBLIC_REGISTRATION_ROLES:'STUDENT,TEACHER',
    AUTH_RATE_LIMIT_WINDOW_MS:'60000', AUTH_RATE_LIMIT_MAX:'100', API_RATE_LIMIT_WINDOW_MS:'60000', API_RATE_LIMIT_MAX:'500',
    TRUST_PROXY:'false', AUDIT_PAGE_SIZE_MAX:'25', BACKUP_DIRECTORY:path.join(temporaryDirectory,'backups'), EXPORT_DIRECTORY:path.join(temporaryDirectory,'exports'),
  } });
  server = createApp({ db, config }).listen(0); await new Promise((resolve) => server.once('listening', resolve));
  baseUrl = `http://127.0.0.1:${server.address().port}`;
  adminToken = (await request('/api/auth/login', { method:'POST', body:{ email:'phase3.admin@example.test', password:ADMIN_PASSWORD } })).payload.token;
  studentToken = (await request('/api/auth/login', { method:'POST', body:{ email:'phase3.student@example.test', password:STUDENT_PASSWORD } })).payload.token;
});

after(async () => {
  if (server) await new Promise((resolve) => server.close(resolve));
  if (db?.open) db.close();
  if (temporaryDirectory?.startsWith(os.tmpdir())) fs.rmSync(temporaryDirectory, { recursive:true, force:true });
});

test('request IDs, Helmet headers, explicit CORS, and normal API limits work', async () => {
  const safeId = 'phase3-request-0001';
  const approved = await request('/api/health', { headers:{ Origin:'http://approved.test', 'X-Request-ID':safeId } });
  assert.equal(approved.response.status, 200); assert.equal(approved.response.headers.get('x-request-id'), safeId);
  assert.equal(approved.response.headers.get('access-control-allow-origin'), 'http://approved.test');
  assert.equal(approved.response.headers.get('x-content-type-options'), 'nosniff');
  const generated = await request('/api/health', { headers:{ 'X-Request-ID':'unsafe value' } });
  assert.match(generated.response.headers.get('x-request-id'), /^[0-9a-f-]{36}$/);
  const rejected = await request('/api/health', { headers:{ Origin:'http://rejected.test' } });
  assert.equal(rejected.response.status, 403); assert.ok(rejected.payload.request_id);
  for (let index=0; index<5; index += 1) assert.equal((await request('/api/health')).response.status, 200);
});

test('catalog endpoints reject anonymous and non-admin mutations', async () => {
  assert.equal((await request('/api/buses')).response.status, 401);
  const denied = await request('/api/buses', { method:'POST', token:studentToken, body:{ bus_number:'P3-DENIED', capacity:20 } });
  assert.equal(denied.response.status, 403);
});

test('administrator creates and updates a valid bus', async () => {
  const created = await request('/api/buses', { method:'POST', token:adminToken, body:{ bus_number:'  p3-bus-301  ', capacity:36 } });
  assert.equal(created.response.status, 201); bus=created.payload.data; assert.equal(bus.bus_number,'P3-BUS-301');
  const updated = await request(`/api/buses/${bus.id}`, { method:'PATCH', token:adminToken, body:{ capacity:40 } });
  assert.equal(updated.response.status, 200); assert.equal(updated.payload.data.capacity,40);
});

test('bus normalized duplicates and invalid capacity, status, or protected fields are rejected', async () => {
  assert.equal((await request('/api/buses',{method:'POST',token:adminToken,body:{bus_number:'p3-bus-301',capacity:30}})).response.status,409);
  for(const body of [{bus_number:'P3-BAD-CAP',capacity:0},{bus_number:'P3-BAD-STATUS',capacity:20,status:'BROKEN'},{bus_number:'P3-ID',capacity:20,id:99},{bus_number:'P3-UNKNOWN',capacity:20,model:'x'}]) {
    assert.equal((await request('/api/buses',{method:'POST',token:adminToken,body})).response.status,400);
  }
});

test('administrator creates and updates a valid driver; invalid input is rejected', async () => {
  let result=await request('/api/drivers',{method:'POST',token:adminToken,body:{full_name:'Fictional Driver Gamma',phone:'000-000-0301'}});
  assert.equal(result.response.status,201);driver=result.payload.data;
  result=await request(`/api/drivers/${driver.id}`,{method:'PATCH',token:adminToken,body:{phone:'+000 000 0302'}});
  assert.equal(result.response.status,200);assert.equal(result.payload.data.phone,'+000 000 0302');
  for(const body of [{full_name:'',phone:'000-000-0000'},{full_name:'Fictional',phone:'x'},{full_name:'Fictional',phone:'000-000-0000',status:'SUSPENDED'}]) {
    assert.equal((await request('/api/drivers',{method:'POST',token:adminToken,body})).response.status,400);
  }
});

test('administrator creates and transactionally reorders a valid route with stops', async () => {
  let result=await request('/api/routes',{method:'POST',token:adminToken,body:{route_name:'Phase Three Loop',origin:'Demo Terminal East',destination:'Demo Campus West',stops:['Demo Stop One','Demo Stop Two']}});
  assert.equal(result.response.status,201);route=result.payload.data;assert.deepEqual(route.stops.map((stop)=>stop.stop_order),[1,2]);
  result=await request(`/api/routes/${route.id}`,{method:'PATCH',token:adminToken,body:{stops:['Demo Stop Two','Demo Stop One','Demo Stop Three']}});
  assert.equal(result.response.status,200);assert.deepEqual(result.payload.data.stops.map((stop)=>stop.stop_name),['Demo Stop Two','Demo Stop One','Demo Stop Three']);
});

test('route duplicate, equal endpoints, invalid status, and protected fields are rejected', async () => {
  assert.equal((await request('/api/routes',{method:'POST',token:adminToken,body:{route_name:' phase three loop ',origin:'A',destination:'B'}})).response.status,409);
  for(const body of [{route_name:'P3 Equal',origin:'Same Place',destination:' same   place '},{route_name:'P3 Status',origin:'A',destination:'B',status:'PAUSED'},{route_name:'P3 ID',origin:'A',destination:'B',created_at:'x'}]) {
    assert.equal((await request('/api/routes',{method:'POST',token:adminToken,body})).response.status,400);
  }
});

test('active catalog records support a future schedule', async () => {
  const created=await request('/api/schedules',{method:'POST',token:adminToken,body:{route_id:route.id,bus_id:bus.id,driver_id:driver.id,service_date:'2099-06-15',departure_time:'08:00',arrival_time:'09:00',notes:'Fictional Phase 3 schedule.'}});
  assert.equal(created.response.status,201);futureSchedule=created.payload.data;
});

test('future schedules block bus, driver, and route deactivation with 409', async () => {
  for(const [endpoint,id] of [['buses',bus.id],['drivers',driver.id],['routes',route.id]]) {
    const result=await request(`/api/${endpoint}/${id}`,{method:'DELETE',token:adminToken});
    assert.equal(result.response.status,409);assert.match(result.payload.message,/future active schedule/i);
  }
});

test('inactive and maintenance buses cannot be assigned', async () => {
  const inactive=(await request('/api/buses',{method:'POST',token:adminToken,body:{bus_number:'P3-INACTIVE',capacity:20,status:'INACTIVE'}})).payload.data;
  const maintenance=(await request('/api/buses',{method:'POST',token:adminToken,body:{bus_number:'P3-MAINT',capacity:20,status:'MAINTENANCE'}})).payload.data;
  const base={route_id:route.id,driver_id:driver.id,service_date:'2099-06-16',departure_time:'10:00',arrival_time:'11:00'};
  assert.equal((await request('/api/schedules',{method:'POST',token:adminToken,body:{...base,bus_id:inactive.id}})).response.status,400);
  assert.equal((await request('/api/schedules',{method:'POST',token:adminToken,body:{...base,bus_id:maintenance.id}})).response.status,400);
});

test('inactive drivers and routes cannot be assigned, then can be reactivated', async () => {
  const spareBus=(await request('/api/buses',{method:'POST',token:adminToken,body:{bus_number:'P3-SPARE',capacity:24}})).payload.data;
  const inactiveDriver=(await request('/api/drivers',{method:'POST',token:adminToken,body:{full_name:'Fictional Inactive Driver',phone:'000-000-0401',status:'INACTIVE'}})).payload.data;
  const inactiveRoute=(await request('/api/routes',{method:'POST',token:adminToken,body:{route_name:'Fictional Inactive Route',origin:'Demo A',destination:'Demo B',status:'INACTIVE'}})).payload.data;
  const base={route_id:route.id,bus_id:spareBus.id,driver_id:driver.id,service_date:'2099-06-17',departure_time:'10:00',arrival_time:'11:00'};
  assert.equal((await request('/api/schedules',{method:'POST',token:adminToken,body:{...base,driver_id:inactiveDriver.id}})).response.status,400);
  assert.equal((await request('/api/schedules',{method:'POST',token:adminToken,body:{...base,route_id:inactiveRoute.id}})).response.status,400);
  assert.equal((await request(`/api/drivers/${inactiveDriver.id}`,{method:'PATCH',token:adminToken,body:{status:'ACTIVE'}})).payload.data.status,'ACTIVE');
  assert.equal((await request(`/api/routes/${inactiveRoute.id}`,{method:'PATCH',token:adminToken,body:{status:'ACTIVE'}})).payload.data.status,'ACTIVE');
});

test('non-admin catalog reads expose active minimum data only', async () => {
  const drivers=await request('/api/drivers',{token:studentToken});assert.equal(drivers.response.status,200);
  assert.equal('phone' in drivers.payload.data[0],false);assert.ok(drivers.payload.data.every((item)=>!('status' in item)));
  const buses=await request('/api/buses',{token:studentToken});assert.ok(buses.payload.data.every((item)=>!('status' in item)));
});

test('schedule mutation and conflicts create redacted audit records with request IDs', async () => {
  const conflict=await request('/api/schedules',{method:'POST',token:adminToken,headers:{'X-Request-ID':'phase3-conflict-01'},body:{route_id:route.id,bus_id:bus.id,driver_id:driver.id,service_date:'2099-06-15',departure_time:'08:30',arrival_time:'08:45'}});
  assert.equal(conflict.response.status,409);
  const rows=db.prepare('SELECT * FROM audit_logs ORDER BY id').all();assert.ok(rows.some((row)=>row.action==='BUS_CREATE'&&row.outcome==='SUCCESS'));
  assert.ok(rows.some((row)=>row.action==='SCHEDULE_CREATE'&&row.outcome==='CONFLICT'&&row.request_id==='phase3-conflict-01'));
  const serializedMetadata=rows.map((row)=>row.metadata_json).join(' ').toLowerCase();
  for(const forbidden of ['password_hash','authorization','bearer ','cookie','admin_password','token','secret']) assert.equal(serializedMetadata.includes(forbidden),false);
  for(const row of rows) assert.doesNotThrow(()=>JSON.parse(row.metadata_json));
});

test('audit API is admin-only and supports filters, pagination, and strict page size', async () => {
  assert.equal((await request('/api/audit-logs',{token:studentToken})).response.status,403);
  const filtered=await request('/api/audit-logs?action=BUS_CREATE&entity=BUS&outcome=SUCCESS&limit=2',{token:adminToken});
  assert.equal(filtered.response.status,200);assert.ok(filtered.payload.data.length>=1);assert.ok(filtered.payload.data.every((item)=>item.action==='BUS_CREATE'));
  assert.equal(filtered.payload.pagination.limit,2);
  assert.equal((await request('/api/audit-logs?limit=26',{token:adminToken})).response.status,400);
});

test('authentication failures are audited without identity or credential material', async () => {
  const failure=await request('/api/auth/login',{method:'POST',headers:{'X-Request-ID':'phase3-authfail-01'},body:{email:'missing@example.test',password:password('wrong')}});
  assert.equal(failure.response.status,401);
  const row=db.prepare("SELECT * FROM audit_logs WHERE action='AUTH_LOGIN_FAILURE' AND request_id=?").get('phase3-authfail-01');
  assert.ok(row);assert.equal(row.actor_user_id,null);assert.deepEqual(Object.keys(JSON.parse(row.metadata_json)),['reason']);
});

test('strict authentication rate limit returns controlled 429 deterministically', async () => {
  const config=createConfig({env:{NODE_ENV:'test',PORT:'0',CLIENT_ORIGIN:'http://approved.test',WEB_DATABASE_PATH:databasePath,AUTH_SECRET:randomBytes(48).toString('hex'),AUTH_EXPIRES_IN:'10m',PUBLIC_REGISTRATION_ROLES:'STUDENT,TEACHER',AUTH_RATE_LIMIT_WINDOW_MS:'60000',AUTH_RATE_LIMIT_MAX:'2',API_RATE_LIMIT_WINDOW_MS:'60000',API_RATE_LIMIT_MAX:'100',TRUST_PROXY:'false',AUDIT_PAGE_SIZE_MAX:'25',BACKUP_DIRECTORY:path.join(temporaryDirectory,'backups'),EXPORT_DIRECTORY:path.join(temporaryDirectory,'exports')}});
  const limitedServer=createApp({db,config}).listen(0);await new Promise((resolve)=>limitedServer.once('listening',resolve));const limitedBase=`http://127.0.0.1:${limitedServer.address().port}`;
  try{for(let index=0;index<2;index+=1){const response=await fetch(`${limitedBase}/api/auth/login`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email:'missing@example.test',password:password('wrong')})});assert.equal(response.status,401);}const response=await fetch(`${limitedBase}/api/auth/login`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email:'missing@example.test',password:password('wrong')})});assert.equal(response.status,429);const payload=await response.json();assert.ok(payload.request_id);}
  finally{await new Promise((resolve)=>limitedServer.close(resolve));}
});

test('schedule can be cancelled and previously blocked catalog records safely deactivated', async () => {
  assert.equal((await request(`/api/schedules/${futureSchedule.id}`,{method:'DELETE',token:adminToken})).response.status,200);
  for(const [endpoint,id] of [['buses',bus.id],['drivers',driver.id],['routes',route.id]]) assert.equal((await request(`/api/${endpoint}/${id}`,{method:'DELETE',token:adminToken})).response.status,200);
});
