import sql, { initDB } from "./db";

try {
  await initDB();
  console.log(JSON.stringify({ event: "database_migration_completed" }));
} catch {
  console.error(JSON.stringify({ event: "database_migration_failed" }));
  process.exitCode = 1;
} finally {
  await sql.end({ timeout: 5 });
}
