function main(env = process.env) {
  if (env.POSTGRES_REHEARSAL_SYNTHETIC !== 'true' || !env.POSTGRES_REHEARSAL_URL) {
    throw new Error('Deferred: set POSTGRES_REHEARSAL_SYNTHETIC=true and an approved synthetic POSTGRES_REHEARSAL_URL before running.');
  }
  throw new Error('Deferred: no project-local PostgreSQL driver or approved runtime is installed; no connection was attempted.');
}
if (require.main === module) { try { main(); } catch (error) { console.error(error.message); process.exitCode = 2; } }
module.exports = { main };
