import { describe, expect, test } from "bun:test";

import { formatChinaDate, parseChinaDateStart } from "./chinaDate";

describe("chinaDate", () => {
  test("uses Asia/Shanghai when UTC crosses the local date boundary", () => {
    expect(formatChinaDate(new Date("2026-08-31T15:59:59.000Z"))).toBe("2026-08-31");
    expect(formatChinaDate(new Date("2026-08-31T16:00:00.000Z"))).toBe("2026-09-01");
  });

  test("parses a China calendar date at the start of that China day", () => {
    expect(parseChinaDateStart("2026-09-01").toISOString()).toBe("2026-08-31T16:00:00.000Z");
  });
});
