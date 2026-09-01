import sql, { initDB } from "./db";
import { parseRuntimeConfig } from "./config";
import { getEnabledPollenProviders } from "./providers/providerRegistry";
import { PollenObservationRepository, type PollenObservationSql } from "./repositories/PollenObservationRepository";
import { SyncRunRepository, type PollenSyncRunSql } from "./repositories/SyncRunRepository";
import { ObservationStore } from "./services/ObservationStore";
import { PollenSyncService, type PollenSyncResult } from "./services/PollenSyncService";
import { createStructuredLogger } from "./observability";

async function main(): Promise<void> {
  const config = parseRuntimeConfig();
  await initDB();

  const observationRepository = new PollenObservationRepository(sql as unknown as PollenObservationSql);
  const observationStore = new ObservationStore(observationRepository);
  const syncRunRepository = new SyncRunRepository(sql as unknown as PollenSyncRunSql);
  const service = new PollenSyncService({
    providers: getEnabledPollenProviders(config.providerEnabled),
    observationStore,
    syncRunRepository,
    logger: createStructuredLogger(config.logLevel),
  });
  const result = await service.runPollenSync("MANUAL");

  console.log(formatSyncResult(result));
}

function formatSyncResult(result: PollenSyncResult): string {
  return [
    "Pollen sync complete",
    `Status: ${result.status}`,
    `Locations: ${result.locationsSucceeded}/${result.locationsAttempted}`,
    `Observations: ${result.observationsReceived}`,
    `Persisted: ${result.observationsPersisted}`,
  ].join("\n");
}

try {
  await main();
} catch {
  console.error("Pollen sync failed");
  process.exitCode = 1;
} finally {
  await sql.end({ timeout: 5 });
}
