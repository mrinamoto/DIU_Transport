// backend/seed.js
// Seeds the database with real DIU Transport Schedule data
// (Source: "Transport Schedule for Mid-Term Exam Semester: Summer-2026", DIU)
// Run with: npm run seed

const bcrypt = require('bcryptjs');
const db = require('./database');

console.log('🌱 Seeding DIU Transport System database...');

// Wipe existing data (safe re-run during development)
const tables = [
  'notification_reads', 'feedback', 'billing', 'lost_found', 'notifications',
  'transport_cards', 'schedules', 'routes', 'drivers', 'buses',
  'emergency_contacts', 'users'
];
db.exec('PRAGMA foreign_keys = OFF;');
for (const t of tables) db.exec(`DELETE FROM ${t};`);
db.exec('PRAGMA foreign_keys = ON;');

// ────────────────────────────────────────────────────────────
// USERS
// ────────────────────────────────────────────────────────────
const insertUser = db.prepare(`
  INSERT INTO users (full_name, email, password_hash, role, student_id, phone, department, semester)
  VALUES (@full_name, @email, @password_hash, @role, @student_id, @phone, @department, @semester)
`);

function hash(pw) { return bcrypt.hashSync(pw, 10); }

const users = [
  { full_name: 'System Administrator', email: 'admin@diu.edu.bd', password_hash: hash('Admin@1234'), role: 'admin', student_id: 'ADMIN-001', phone: '01711000000', department: 'Transport Office', semester: null },
  { full_name: 'Fahim Rahman', email: 'student@diu.edu.bd', password_hash: hash('Student@1234'), role: 'student', student_id: '211-15-3847', phone: '01712345678', department: 'CSE', semester: 'Summer 2026' },
  { full_name: 'Dr. Anisur Rahman', email: 'teacher@diu.edu.bd', password_hash: hash('Teacher@1234'), role: 'teacher', student_id: 'TCH-034', phone: '01811222333', department: 'CSE', semester: null },
  { full_name: 'Razia Sultana', email: 'staff@diu.edu.bd', password_hash: hash('Staff@1234'), role: 'staff', student_id: 'STF-011', phone: '01911444555', department: 'Administration', semester: null },
];
const userIds = {};
for (const u of users) {
  const info = insertUser.run(u);
  userIds[u.email] = info.lastInsertRowid;
}
console.log(`  • Inserted ${users.length} users`);

// ────────────────────────────────────────────────────────────
// BUSES
// ────────────────────────────────────────────────────────────
const insertBus = db.prepare(`
  INSERT INTO buses (bus_number, capacity, status, model, registration_plate)
  VALUES (@bus_number, @capacity, @status, @model, @registration_plate)
`);
const buses = [
  { bus_number: 'DIU-01', capacity: 52, status: 'running', model: 'Hino RM1', registration_plate: 'DHAKA-METRO-GA-1101' },
  { bus_number: 'DIU-02', capacity: 45, status: 'running', model: 'Tata LPO 1623', registration_plate: 'DHAKA-METRO-GA-1102' },
  { bus_number: 'DIU-03', capacity: 52, status: 'maintenance', model: 'Hino RM1', registration_plate: 'DHAKA-METRO-GA-1103' },
  { bus_number: 'DIU-04', capacity: 45, status: 'running', model: 'Hyundai Universe', registration_plate: 'DHAKA-METRO-GA-1104' },
  { bus_number: 'DIU-05', capacity: 40, status: 'running', model: 'Tata Starbus', registration_plate: 'DHAKA-METRO-GA-1105' },
  { bus_number: 'DIU-06', capacity: 45, status: 'running', model: 'Hino RM1', registration_plate: 'DHAKA-METRO-GA-1106' },
  { bus_number: 'DIU-07', capacity: 40, status: 'available', model: 'Tata LPO 1623', registration_plate: 'DHAKA-METRO-GA-1107' },
  { bus_number: 'DIU-08', capacity: 52, status: 'running', model: 'Hyundai Universe', registration_plate: 'DHAKA-METRO-GA-1108' },
];
const busIds = {};
for (const b of buses) {
  const info = insertBus.run(b);
  busIds[b.bus_number] = info.lastInsertRowid;
}
console.log(`  • Inserted ${buses.length} buses`);

// ────────────────────────────────────────────────────────────
// DRIVERS
// ────────────────────────────────────────────────────────────
const insertDriver = db.prepare(`
  INSERT INTO drivers (full_name, phone, license_number, shift, bus_id)
  VALUES (@full_name, @phone, @license_number, @shift, @bus_id)
`);
const drivers = [
  { full_name: 'Karim Uddin', phone: '01712100001', license_number: 'BDL-4421', shift: 'morning', bus_id: busIds['DIU-01'] },
  { full_name: 'Rahim Ali', phone: '01812200002', license_number: 'BDL-3312', shift: 'morning', bus_id: busIds['DIU-02'] },
  { full_name: 'Jamal Hossain', phone: '01912300003', license_number: 'BDL-1187', shift: 'morning', bus_id: busIds['DIU-04'] },
  { full_name: 'Sumon Mia', phone: '01612400004', license_number: 'BDL-5521', shift: 'morning', bus_id: busIds['DIU-05'] },
  { full_name: 'Belal Ahmed', phone: '01512500005', license_number: 'BDL-2298', shift: 'morning', bus_id: busIds['DIU-06'] },
  { full_name: 'Mizanur Rahman', phone: '01712600006', license_number: 'BDL-7743', shift: 'evening', bus_id: busIds['DIU-08'] },
];
const driverIds = {};
for (const d of drivers) {
  const info = insertDriver.run(d);
  driverIds[d.full_name] = info.lastInsertRowid;
}
console.log(`  • Inserted ${drivers.length} drivers`);

// ────────────────────────────────────────────────────────────
// ROUTES — extracted from the real Summer 2026 Mid-Term schedule PDF
// ────────────────────────────────────────────────────────────
const insertRoute = db.prepare(`
  INSERT INTO routes (route_code, route_name, start_point, end_point, stops, route_details, day_type)
  VALUES (@route_code, @route_name, @start_point, @end_point, @stops, @route_details, @day_type)
`);

const DSC = 'Daffodil Smart City (DSC)';
const routesData = [
  {
    route_code: 'R1', route_name: 'Dhanmondi <> DSC', start_point: 'Dhanmondi - Sobhanbag', day_type: 'regular',
    stops: ['Dhanmondi - Sobhanbag', 'Shyamoli Square', 'Technical Mor', 'Majar Road Gabtoli', 'Konabari Bus Stop', 'Eastern Housing', 'Rupnagar', 'Birulia Bus Stand', 'Daffodil Smart City'],
    route_details: 'Dhanmondi - Sobhanbag <> Shyamoli Square <> Technical Mor <> Majar Road Gabtoli <> Konabari Bus Stop <> Eastern Housing <> Rupnagar <> Birulia Bus Stand <> Daffodil Smart City',
  },
  {
    route_code: 'R2', route_name: 'ECB Chattor <> Mirpur <> DSC', start_point: 'ECB Chattor', day_type: 'regular',
    stops: ['ECB Chattor', 'Kalshi Mor', 'Mirpur 12', 'Mirpur 10', 'Mirpur 02', 'Mirpur 01 - Sony Cinema Hall', 'Commerce College', 'Gudaraghat', 'Beribadh', 'Eastern Housing', 'Birulia', 'Akran', 'Daffodil Smart City'],
    route_details: 'ECB Chattor <> Kalshi Mor <> Mirpur 12 <> Mirpur 10 <> Mirpur 02 <> Mirpur 01 - Sony Cinema Hall <> Commerce College <> Gudaraghat <> Beribadh <> Eastern Housing <> Birulia <> Akran <> Daffodil Smart City',
  },
  {
    route_code: 'R3', route_name: 'Tongi College Gate <> DSC', start_point: 'Tongi College Gate Bus Stand', day_type: 'regular',
    stops: ['Tongi College Gate Bus Stand', 'Kamarpara', 'Dhour', 'Birulia', 'Daffodil Smart City'],
    route_details: 'Tongi College Gate Bus Stand <> Kamarpara <> Dhour <> Birulia <> Daffodil Smart City',
  },
  {
    route_code: 'R4', route_name: 'Uttara - Rajlokkhi <> DSC', start_point: 'Uttara - Rajlokkhi', day_type: 'regular',
    stops: ['Uttara - Rajlokkhi', 'House Building', 'Grand Zamzam Tower', 'Diyabari Bridge', 'Beribadh', 'Birulia', 'Khagan', 'Daffodil Smart City'],
    route_details: 'Uttara - Rajlokkhi <> House building <> Grand Zamzam Tower <> Diyabari Bridge <> Beribadh <> Birulia <> Khagan <> Daffodil Smart City',
  },
  {
    route_code: 'R5', route_name: 'Konabari <> Ashulia <> DSC', start_point: 'Konabari Pukur Par', day_type: 'regular',
    stops: ['Konabari Pukur Par', 'Norshingpur', 'Ghosbag', 'Zirabo', 'Ashulia Bazar', 'Paragram', 'Daffodil Smart City'],
    route_details: 'Konabari Pukur Par <> Norshingpur <> Ghosbag <> Zirabo <> Ashulia Bazar <> Paragram <> Daffodil Smart City',
  },
  {
    route_code: 'R6', route_name: 'Savar <> C&B <> DSC', start_point: 'Savar Bus Stand', day_type: 'regular',
    stops: ['Savar Bus Stand', 'Radio Colony', 'C&B', 'Kolma', 'Charabag', 'Kumkumari', 'Daffodil Smart City'],
    route_details: 'Savar Bus Stand <> Radio Colony <> C&B <> Kolma <> Charabag <> Kumkumari <> Daffodil Smart City',
  },
  {
    route_code: 'R7', route_name: 'Baipail <> Nabinagar <> C&B <> DSC', start_point: 'Baipail', day_type: 'regular',
    stops: ['Baipail', 'Palli Bidyut', 'Nabinagar', 'Bismail', 'Prantik', 'JU', 'C&B', 'Kolma', 'Charabag', 'Kumkumari', 'Daffodil Smart City'],
    route_details: 'Baipail <> Palli Bidyut <> Nabinagar <> Bismail <> Prantik <> JU <> C&B <> Kolma <> Charabag <> Kumkumari <> Daffodil Smart City',
  },
  {
    route_code: 'R8', route_name: 'Dhamrai <> Nabinagar <> C&B <> DSC', start_point: 'Dhamrai Bus Stand', day_type: 'regular',
    stops: ['Dhamrai Bus Stand', 'Kohinur Market', 'Gonosastho', 'Nabinagar', 'Bismail', 'Prantik', 'JU', 'C&B', 'Kolma', 'Charabag', 'Kumkumari', 'Daffodil Smart City'],
    route_details: 'Dhamrai Bus Stand <> Kohinur Market <> Gonosastho <> Nabinagar <> Bismail <> Prantik <> JU <> C&B <> Kolma <> Charabag <> Kumkumari <> Daffodil Smart City',
  },
  {
    route_code: 'R9', route_name: 'Narayanganj Chasara <> Dhanmondi <> DSC (Shuttle)', start_point: 'Narayanganj Chasara', day_type: 'regular',
    stops: ['Narayanganj Chasara', 'Sign Board', 'Sonir Akhra', 'Saydabad Bus Stand', 'Gulistan', 'Chankharpul', 'Nilkhet', 'New Market', 'Kolabagan', 'Dhanmondi - Sobhanbag', 'Shyamoli Square', 'Daffodil Smart City'],
    route_details: 'Narayanganj Chasara > Sign Board > Sonir Akhra > Saydabad Bus Stand > Gulistan > Chankharpul > Nilkhet > New Market > Kolabagan > Dhanmondi - Sobhanbag <> Shyamoli Square <> Daffodil Smart City. Return: Daffodil Smart City > Dhanmondi-Sobhanbag > Kolabagan > New Market > Nilkhet > Dhaka University > Chankharpul > Fly Over Bridge > Kajla > Sonir Akhra > Sign Board > Narayanganj Chasara',
  },
  {
    route_code: 'R11', route_name: 'Tongi Station <> DSC', start_point: 'Tongi Station', day_type: 'regular',
    stops: ['Tongi Station', 'Kamar Para', 'Dhour', 'Birulia', 'Daffodil Smart City'],
    route_details: 'Tongi Station Route <> Kamar Para <> Dhour <> Birulia <> Daffodil Smart City',
  },
  {
    route_code: 'R12', route_name: 'Uttara Metro Rail <> DSC', start_point: 'Uttara Center Metro Rail', day_type: 'regular',
    stops: ['Uttara Metro Rail Center', 'Beribadh', 'Birulia', 'Khagan', 'Daffodil Smart City'],
    route_details: 'Uttara Metro Rail Center <> Beribadh <> Birulia <> Khagan <> Daffodil Smart City',
  },
  {
    route_code: 'R14', route_name: 'Mirpur-1 <> Sony Cinema Hall <> DSC', start_point: 'Mirpur-1', day_type: 'regular',
    stops: ['Mirpur-1', 'Sony Cinema Hall', 'Gudaraghat', 'Beribadh', 'Eastern Housing', 'Birulia', 'Akran', 'Daffodil Smart City'],
    route_details: 'Mirpur-1 <> Sony Cinema Hall <> Gudaraghat <> Beribadh <> Eastern Housing <> Birulia <> Akran <> Daffodil Smart City',
  },
  {
    route_code: 'R15', route_name: 'Uttara Moylar Mor <> DSC', start_point: 'Uttara Moylar Mor', day_type: 'regular',
    stops: ['Uttara Moylar Mor', 'Uttara Khalpar', 'Diyabari Bridge', 'Beribadh', 'Birulia', 'Khagan', 'Daffodil Smart City'],
    route_details: 'Uttara Moylar Mor <> Uttara Khalpar <> Diyabari Bridge <> Beribadh <> Birulia <> Khagan <> Daffodil Smart City',
  },
  // Friday-only schedule
  {
    route_code: 'F1', route_name: 'Dhanmondi <> DSC (Friday)', start_point: 'Dhanmondi - Sobhanbag', day_type: 'friday',
    stops: ['Dhanmondi - Sobhanbag', 'Shyamoli Square', 'Technical Mor', 'Majar Road Gabtoli', 'Mirpur Konabari', 'Eastern Housing Rup Nogor', 'Birulia Bus Stand', 'Daffodil Smart City'],
    route_details: 'Dhanmondi - Sobhanbag <> Shyamoli Square <> Technical Mor <> Majar Road Gabtoli <> Mirpur Konabari <> Eastern Housing Rup Nogor <> Birulia Bus Stand <> Daffodil Smart City',
  },
  {
    route_code: 'F2', route_name: 'Tongi College Gate <> DSC (Friday)', start_point: 'Tongi College Gate', day_type: 'friday',
    stops: ['Tongi College Gate', 'Uttara', 'Daffodil Smart City'],
    route_details: 'Tongi College Gate <> Uttara <> Daffodil Smart City',
  },
  {
    route_code: 'F3', route_name: 'Savar <> Nabinagar <> C&B <> DSC (Friday)', start_point: 'Savar', day_type: 'friday',
    stops: ['Savar', 'Nabinagar', 'C&B', 'Daffodil Smart City'],
    route_details: 'Savar > Nabinagar > C&B > Daffodil Smart City',
  },
  {
    route_code: 'F4', route_name: 'Uttara - Rajlokkhi <> DSC (Friday)', start_point: 'Uttara - Rajlokkhi', day_type: 'friday',
    stops: ['Uttara - Rajlokkhi', 'House Building', 'Grand Zomzom Tower', 'Diyabari Bridge', 'Beribadh', 'Birulia', 'Khagan', 'Daffodil Smart City'],
    route_details: 'Uttara - Rajlokkhi <> House building <> Grand Zomzom Tower <> Diyabari Bridge <> Beribadh <> Birulia <> Khagan <> Daffodil Smart City',
  },
];

const routeIds = {};
for (const r of routesData) {
  const info = insertRoute.run({
    route_code: r.route_code,
    route_name: r.route_name,
    start_point: r.start_point,
    end_point: DSC,
    stops: JSON.stringify(r.stops),
    route_details: r.route_details,
    day_type: r.day_type,
  });
  routeIds[r.route_code] = info.lastInsertRowid;
}
console.log(`  • Inserted ${routesData.length} routes (real Summer 2026 mid-term data)`);

// ────────────────────────────────────────────────────────────
// SCHEDULES — real departure/return times per route from the PDF
// ────────────────────────────────────────────────────────────
const insertSchedule = db.prepare(`
  INSERT INTO schedules (bus_id, route_id, driver_id, departure_time, departure_note, arrival_time, direction, day_type, status, semester_label)
  VALUES (@bus_id, @route_id, @driver_id, @departure_time, @departure_note, @arrival_time, @direction, @day_type, @status, @semester_label)
`);

const busPool = Object.values(busIds);
const driverPool = Object.values(driverIds);
let busCursor = 0, driverCursor = 0;
function nextBus() { const id = busPool[busCursor % busPool.length]; busCursor++; return id; }
function nextDriver() { const id = driverPool[driverCursor % driverPool.length]; driverCursor++; return id; }

const SEMESTER = 'Mid-Term Exam, Summer 2026';

// Regular weekday schedules: [routeCode, toCampusTimes[], fromCampusTimes[], notes]
const weekdaySchedule = [
  { code: 'R1', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'] },
  { code: 'R2', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'], fromNote: 'Return trips go up to Mirpur-1, Mirpur-10, Pallabi & ECB Chattor only' },
  { code: 'R3', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'] },
  { code: 'R4', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'] },
  { code: 'R5', to: ['7:00 AM'], from: ['4:20 PM'] },
  { code: 'R6', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'] },
  { code: 'R7', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['1:30 PM', '4:20 PM'], toNote: '7:00 AM trip goes up to Nabinagar & Palli Bidyut only', fromNote: '1:30 PM return goes up to Nabinagar & Palli Bidyut only' },
  { code: 'R8', to: ['7:00 AM', '9:40 AM', '11:50 AM'], from: ['10:50 AM', '1:30 PM', '4:20 PM'] },
  { code: 'R9', to: ['6:30 AM'], from: ['1:30 PM'], toNote: 'Shuttle service via Dhanmondi' },
  { code: 'R11', to: ['6:30 AM'], from: ['4:00 PM'] },
  { code: 'R12', to: ['6:30 AM'], from: ['4:00 PM'] },
  { code: 'R14', to: ['7:30 AM'], from: ['4:00 PM'] },
  { code: 'R15', to: ['7:30 AM'], from: ['4:00 PM'] },
];

// Friday-only schedules
const fridaySchedule = [
  { code: 'F1', to: ['7:30 AM'], from: ['11:20 AM', '4:00 PM'] },
  { code: 'F2', to: ['7:30 AM'], from: ['11:20 AM', '4:00 PM'] },
  { code: 'F3', to: ['7:30 AM'], from: ['11:20 AM', '4:00 PM'] },
  { code: 'F4', to: ['7:30 AM'], from: ['11:20 AM', '4:00 PM'], fromNote: '11:20 AM return goes up to Mirpur-10 only' },
];

let scheduleCount = 0;

function addSchedules(list, dayType) {
  for (const item of list) {
    const routeId = routeIds[item.code];
    if (!routeId) continue;

    for (const t of item.to) {
      insertSchedule.run({
        bus_id: nextBus(),
        route_id: routeId,
        driver_id: nextDriver(),
        departure_time: t,
        departure_note: item.toNote || null,
        arrival_time: '—', // arrival at DSC varies by traffic; not published per-stop
        direction: 'to_campus',
        day_type: dayType,
        status: 'active',
        semester_label: SEMESTER,
      });
      scheduleCount++;
    }
    for (const t of item.from) {
      insertSchedule.run({
        bus_id: nextBus(),
        route_id: routeId,
        driver_id: nextDriver(),
        departure_time: t,
        departure_note: item.fromNote || null,
        arrival_time: '—',
        direction: 'from_campus',
        day_type: dayType,
        status: 'active',
        semester_label: SEMESTER,
      });
      scheduleCount++;
    }
  }
}

addSchedules(weekdaySchedule, 'weekday');
addSchedules(fridaySchedule, 'friday');
console.log(`  • Inserted ${scheduleCount} schedule entries (weekday + Friday)`);

// ────────────────────────────────────────────────────────────
// TRANSPORT CARDS
// ────────────────────────────────────────────────────────────
const insertCard = db.prepare(`
  INSERT INTO transport_cards (user_id, card_number, issue_date, expiry_date, status, route_id, balance)
  VALUES (@user_id, @card_number, @issue_date, @expiry_date, @status, @route_id, @balance)
`);
insertCard.run({ user_id: userIds['student@diu.edu.bd'], card_number: 'TC-211153847', issue_date: '2026-01-01', expiry_date: '2026-12-31', status: 'active', route_id: routeIds['R1'], balance: 500 });
insertCard.run({ user_id: userIds['teacher@diu.edu.bd'], card_number: 'TC-TCH034001', issue_date: '2026-01-01', expiry_date: '2026-12-31', status: 'active', route_id: routeIds['R6'], balance: 0 });
console.log('  • Inserted 2 transport cards');

// ────────────────────────────────────────────────────────────
// NOTIFICATIONS
// ────────────────────────────────────────────────────────────
const insertNotif = db.prepare(`
  INSERT INTO notifications (title, message, type, target_role, created_by)
  VALUES (@title, @message, @type, @target_role, @created_by)
`);
const adminId = userIds['admin@diu.edu.bd'];
insertNotif.run({ title: 'Mid-Term Exam Transport Schedule Published', message: 'The Summer 2026 mid-term exam transport schedule is now active. Please check your route for updated timings.', type: 'update', target_role: 'all', created_by: adminId });
insertNotif.run({ title: 'Route R7 Partial Service', message: 'The 7:00 AM trip on Route R7 (Baipail) will only run up to Nabinagar and Palli Bidyut during exam week.', type: 'warning', target_role: 'student', created_by: adminId });
insertNotif.run({ title: 'Emergency Contact Update', message: 'The transport emergency hotline is now active 24/7 during exam season.', type: 'info', target_role: 'all', created_by: adminId });
console.log('  • Inserted 3 notifications');

// ────────────────────────────────────────────────────────────
// EMERGENCY CONTACTS
// ────────────────────────────────────────────────────────────
const insertContact = db.prepare(`
  INSERT INTO emergency_contacts (name, role, phone, email, available_hours)
  VALUES (@name, @role, @phone, @email, @available_hours)
`);
insertContact.run({ name: 'Md. Rafiqul Islam', role: 'Transport Manager', phone: '01711234567', email: 'transport@diu.edu.bd', available_hours: '9 AM - 5 PM' });
insertContact.run({ name: 'Nadia Akter', role: 'Transport Officer', phone: '01811345678', email: 'officer@diu.edu.bd', available_hours: '9 AM - 5 PM' });
insertContact.run({ name: 'DIU Hotline', role: '24/7 Emergency', phone: '16394', email: 'emergency@diu.edu.bd', available_hours: '24/7' });
console.log('  • Inserted 3 emergency contacts');

// ────────────────────────────────────────────────────────────
// BILLING (sample for the seeded student)
// ────────────────────────────────────────────────────────────
const insertBilling = db.prepare(`
  INSERT INTO billing (user_id, semester_label, amount, due_date, paid_date, method, status, description)
  VALUES (@user_id, @semester_label, @amount, @due_date, @paid_date, @method, @status, @description)
`);
insertBilling.run({ user_id: userIds['student@diu.edu.bd'], semester_label: 'Spring 2026', amount: 6000, due_date: '2026-02-15', paid_date: '2026-01-10', method: 'Bank Transfer', status: 'paid', description: 'Transport fee - Mirpur route' });
insertBilling.run({ user_id: userIds['student@diu.edu.bd'], semester_label: 'Summer 2026', amount: 6000, due_date: '2026-06-15', paid_date: null, method: null, status: 'due', description: 'Transport fee - Mirpur route' });
console.log('  • Inserted 2 billing records');

console.log('\n✅ Seeding complete!\n');
console.log('Demo accounts:');
console.log('  Admin   : admin@diu.edu.bd   / Admin@1234');
console.log('  Student : student@diu.edu.bd / Student@1234');
console.log('  Teacher : teacher@diu.edu.bd / Teacher@1234');
console.log('  Staff   : staff@diu.edu.bd   / Staff@1234');
