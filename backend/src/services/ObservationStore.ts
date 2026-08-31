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
    await this.persistAndCount(observations);
  }

  async persistAndCount(observations: readonly PollenObservation[]): Promise<number> {
    if (observations.length === 0) return 0;

    try {
      const saved = await this.repository.saveMany(observations);
      return saved.length;
    } catch {
      this.logger.error("Failed to persist normalized pollen observations.");
      return 0;
    }
  }
}
