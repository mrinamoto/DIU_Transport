// backend/server.js
// DIU Transport Schedule System — Express server entry point

require('dotenv').config();
const path = require('path');
const express = require('express');
const cors = require('cors');

const db = require('./database'); // ensures schema exists on boot

const app = express();
const PORT = process.env.PORT || 3000;

// ── Core middleware ──
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ── Serve the frontend as static files ──
const FRONTEND_DIR = path.join(__dirname, '..', 'frontend');
app.use(express.static(FRONTEND_DIR));

// ── Health check (useful to confirm server + DB are alive) ──
app.get('/api/health', (req, res) => {
  try {
    const row = db.prepare('SELECT COUNT(*) AS userCount FROM users').get();
    res.json({
      status: 'ok',
      message: 'DIU Transport System backend is running',
      users_in_db: row.userCount,
      timestamp: new Date().toISOString(),
    });
  } catch (err) {
    res.status(500).json({ status: 'error', message: err.message });
  }
});

// ── API routes ──
app.use('/api/auth', require('./routes/auth'));
app.use('/api/schedules', require('./routes/schedules'));
app.use('/api/routes', require('./routes/routesApi'));
app.use('/api/cards', require('./routes/cards'));
app.use('/api/notifications', require('./routes/notifications'));
app.use('/api/lostfound', require('./routes/lostfound'));
app.use('/api/contacts', require('./routes/contacts'));
app.use('/api/users', require('./routes/users'));
app.use('/api/billing', require('./routes/billing'));
app.use('/api/feedback', require('./routes/feedback'));

// ── Fallback: send index.html for any non-API route (SPA-style) ──
app.get('*', (req, res, next) => {
  if (req.path.startsWith('/api/')) return next();
  res.sendFile(path.join(FRONTEND_DIR, 'index.html'));
});

// ── 404 handler for unknown API routes ──
app.use('/api', (req, res) => {
  res.status(404).json({ status: 'error', message: 'API endpoint not found' });
});

// ── Global error handler ──
app.use((err, req, res, next) => {
  console.error('Unhandled error:', err);
  res.status(500).json({ status: 'error', message: 'Internal server error' });
});

app.listen(PORT, () => {
  console.log('═══════════════════════════════════════════════════════');
  console.log('  🚍  DIU Transport Schedule System');
  console.log(`  🌐  Running at: http://localhost:${PORT}`);
  console.log(`  📁  Serving frontend from: ${FRONTEND_DIR}`);
  console.log('═══════════════════════════════════════════════════════');
});
