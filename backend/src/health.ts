export interface HealthResponse {
  readonly status: "ok" | "degraded";
  readonly timestamp: string;
  readonly database: boolean;
}

export interface HealthCheckOptions {
  readonly checkDatabase: () => Promise<void>;
  readonly now?: () => Date;
}

export async function checkHealth({ checkDatabase, now = () => new Date() }: HealthCheckOptions): Promise<HealthResponse> {
  const timestamp = now().toISOString();
  try {
    await checkDatabase();
    return { status: "ok", timestamp, database: true };
  } catch {
    return { status: "degraded", timestamp, database: false };
  }
}
