export function isStartupScrapeEnabled(value = process.env.POLLEN_STARTUP_SCRAPE_ENABLED): boolean {
  return value === "true";
}
