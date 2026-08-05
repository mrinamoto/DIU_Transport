const { createConfig } = require('../config');
const { openDatabase } = require('./connection');

function seedDatabase(db) {
  const seed = db.transaction(() => {
    const insertRoute = db.prepare(`
      INSERT OR IGNORE INTO routes (route_name, origin, destination)
      VALUES (?, ?, ?)
    `);
    insertRoute.run('Demo North Loop', 'Demo North Terminal', 'Demo Campus');
    insertRoute.run('Demo Riverside Loop', 'Demo Riverside Terminal', 'Demo Campus');

    const insertBus = db.prepare('INSERT OR IGNORE INTO buses (bus_number, capacity) VALUES (?, ?)');
    insertBus.run('DEMO-BUS-101', 40);
    insertBus.run('DEMO-BUS-202', 32);

    const insertDriver = db.prepare(`
      INSERT INTO drivers (full_name, phone)
      SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM drivers WHERE full_name = ?)
    `);
    insertDriver.run('Demo Driver Alpha', '000-000-0001', 'Demo Driver Alpha');
    insertDriver.run('Demo Driver Beta', '000-000-0002', 'Demo Driver Beta');

    const routeA = db.prepare("SELECT id FROM routes WHERE route_name='Demo North Loop'").get();
    const routeB = db.prepare("SELECT id FROM routes WHERE route_name='Demo Riverside Loop'").get();
    const busA = db.prepare("SELECT id FROM buses WHERE bus_number='DEMO-BUS-101'").get();
    const busB = db.prepare("SELECT id FROM buses WHERE bus_number='DEMO-BUS-202'").get();
    const driverA = db.prepare("SELECT id FROM drivers WHERE full_name='Demo Driver Alpha'").get();
    const driverB = db.prepare("SELECT id FROM drivers WHERE full_name='Demo Driver Beta'").get();

    const insertSchedule = db.prepare(`
      INSERT INTO schedules (
        route_id, bus_id, driver_id, service_date, departure_time, arrival_time, notes
      )
      SELECT @route_id, @bus_id, @driver_id, @service_date, @departure_time, @arrival_time, @notes
      WHERE NOT EXISTS (
        SELECT 1 FROM schedules
        WHERE route_id=@route_id AND bus_id=@bus_id AND driver_id=@driver_id
          AND service_date=@service_date AND departure_time=@departure_time
      )
    `);
    insertSchedule.run({
      route_id: routeA.id, bus_id: busA.id, driver_id: driverA.id,
      service_date: '2030-01-15', departure_time: '08:00', arrival_time: '09:00',
      notes: 'Fictional Phase 2 demonstration schedule.',
    });
    insertSchedule.run({
      route_id: routeB.id, bus_id: busB.id, driver_id: driverB.id,
      service_date: '2030-01-15', departure_time: '09:30', arrival_time: '10:30',
      notes: 'Fictional Phase 2 demonstration schedule.',
    });
  });
  seed();
}

function main() {
  const config = createConfig({ requireAuthSecret: false });
  const db = openDatabase(config.databasePath);
  try {
    seedDatabase(db);
    console.log('Fictional Phase 2 demonstration data seeded safely.');
  } finally {
    db.close();
  }
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    console.error(`Database seed failed: ${error.message}`);
    process.exitCode = 1;
  }
}

module.exports = { main, seedDatabase };
