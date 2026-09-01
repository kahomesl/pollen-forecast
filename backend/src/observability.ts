import type { LogLevel } from "./config";

const LEVELS: Record<LogLevel, number> = { debug: 10, info: 20, warn: 30, error: 40 };

export interface StructuredLogger {
  info(event: Record<string, string | number | boolean>): void;
  warn(event: Record<string, string | number | boolean>): void;
  error(event: Record<string, string | number | boolean>): void;
}

export function createStructuredLogger(level: LogLevel): StructuredLogger {
  const write = (eventLevel: Exclude<LogLevel, "debug">, event: Record<string, string | number | boolean>) => {
    if (LEVELS[eventLevel] < LEVELS[level]) return;
    console.log(JSON.stringify({ level: eventLevel, ...event }));
  };

  return {
    info: (event) => write("info", event),
    warn: (event) => write("warn", event),
    error: (event) => write("error", event),
  };
}
