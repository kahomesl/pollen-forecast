import { describe, expect, test } from "bun:test";

import { isStartupScrapeEnabled } from "./startupScrape";

describe("isStartupScrapeEnabled", () => {
  test("is disabled unless explicitly set to true", () => {
    expect(isStartupScrapeEnabled(undefined)).toBe(false);
    expect(isStartupScrapeEnabled("false")).toBe(false);
    expect(isStartupScrapeEnabled("true")).toBe(true);
  });
});
