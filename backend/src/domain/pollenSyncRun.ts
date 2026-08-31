export const POLLEN_SYNC_RUN_STATUSES = ["RUNNING", "SUCCESS", "PARTIAL", "FAILED"] as const;
export type PollenSyncRunStatus = (typeof POLLEN_SYNC_RUN_STATUSES)[number];

export const POLLEN_SYNC_TRIGGERS = ["MANUAL", "SCHEDULED", "STARTUP"] as const;
export type PollenSyncTrigger = (typeof POLLEN_SYNC_TRIGGERS)[number];

export interface PollenSyncRun {
  readonly id: number;
  readonly startedAt: Date;
  readonly finishedAt?: Date;
  readonly status: PollenSyncRunStatus;
  readonly trigger: PollenSyncTrigger;
  readonly locationsAttempted: number;
  readonly locationsSucceeded: number;
  readonly locationsFailed: number;
  readonly observationsReceived: number;
  readonly observationsPersisted: number;
  readonly createdAt: Date;
}

export interface CompletePollenSyncRunInput {
  readonly status: Exclude<PollenSyncRunStatus, "RUNNING">;
  readonly finishedAt: Date;
  readonly locationsAttempted: number;
  readonly locationsSucceeded: number;
  readonly locationsFailed: number;
  readonly observationsReceived: number;
  readonly observationsPersisted: number;
}
