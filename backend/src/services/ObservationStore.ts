import type { PollenObservation } from "../domain/pollenObservation";

export interface PollenObservationPersister {
  saveMany(observations: readonly PollenObservation[]): Promise<readonly PollenObservation[]>;
}

export interface ObservationStoreLogger {
  error(message: string): void;
}

export class ObservationStore {
  constructor(
    private readonly repository: PollenObservationPersister,
    private readonly logger: ObservationStoreLogger = console,
  ) {}

  async persist(observations: readonly PollenObservation[]): Promise<void> {
    if (observations.length === 0) return;

    try {
      await this.repository.saveMany(observations);
    } catch {
      this.logger.error("Failed to persist normalized pollen observations.");
    }
  }
}
