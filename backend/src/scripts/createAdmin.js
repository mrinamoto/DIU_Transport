const bcrypt = require('bcryptjs');
const { createConfig } = require('../config');
const { openDatabase } = require('../db/connection');
const { normalizeEmail, isValidEmail, validatePassword } = require('../utils/validation');
const { BCRYPT_ROUNDS } = require('../routes/auth');

function createAdministrator(db, { fullName, email, password }) {
  const normalizedEmail = normalizeEmail(email);
  const normalizedName = typeof fullName === 'string' ? fullName.trim() : '';
  const errors = [];
  if (normalizedName.length < 2 || normalizedName.length > 100) errors.push('ADMIN_FULL_NAME must be 2-100 characters.');
  if (!isValidEmail(normalizedEmail)) errors.push('ADMIN_EMAIL must be a valid email address.');
  errors.push(...validatePassword(password));
  if (errors.length) throw new Error(errors.join(' '));

  if (db.prepare("SELECT 1 FROM users WHERE role='ADMIN'").get()) {
    throw new Error('An administrator has already been provisioned.');
  }
  if (db.prepare('SELECT 1 FROM users WHERE email=?').get(normalizedEmail)) {
    throw new Error('An account with this email already exists.');
  }

  const passwordHash = bcrypt.hashSync(password, BCRYPT_ROUNDS);
  db.prepare(`
    INSERT INTO users (full_name, email, password_hash, role)
    VALUES (?, ?, ?, 'ADMIN')
  `).run(normalizedName, normalizedEmail, passwordHash);
}

function main() {
  const config = createConfig({ requireAuthSecret: false });
  const db = openDatabase(config.databasePath);
  try {
    createAdministrator(db, {
      fullName: process.env.ADMIN_FULL_NAME,
      email: process.env.ADMIN_EMAIL,
      password: process.env.ADMIN_PASSWORD,
    });
    console.log('Administrator provisioned successfully. The password was not displayed or stored in source.');
  } finally {
    db.close();
  }
}

if (require.main === module) {
  try {
    main();
  } catch (error) {
    console.error(`Administrator provisioning failed: ${error.message}`);
    process.exitCode = 1;
  }
}

module.exports = { createAdministrator, main };
