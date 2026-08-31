const CHINA_TIME_ZONE = "Asia/Shanghai";

export function formatChinaDate(date: Date = new Date()): string {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: CHINA_TIME_ZONE,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).formatToParts(date);
  const values = Object.fromEntries(
    parts
      .filter((part) => part.type !== "literal")
      .map((part) => [part.type, part.value]),
  );

  return `${values.year}-${values.month}-${values.day}`;
}

export function parseChinaDateStart(date: string): Date {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) {
    throw new Error(`Invalid China date: ${date}`);
  }

  const parsed = new Date(`${date}T00:00:00+08:00`);
  if (Number.isNaN(parsed.getTime()) || formatChinaDate(parsed) !== date) {
    throw new Error(`Invalid China date: ${date}`);
  }

  return parsed;
}

export function parseChinaDateTime(dateTime: string): Date {
  const match = /^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})$/.exec(dateTime);
  if (!match) {
    throw new Error(`Invalid China date-time: ${dateTime}`);
  }

  const [, year, month, day, hour, minute] = match;
  const parsed = new Date(`${year}-${month}-${day}T${hour}:${minute}:00+08:00`);
  if (Number.isNaN(parsed.getTime())) {
    throw new Error(`Invalid China date-time: ${dateTime}`);
  }

  return parsed;
}
