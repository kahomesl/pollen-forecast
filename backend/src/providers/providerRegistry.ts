import { BeijingPollenProvider } from "./BeijingPollenProvider";
import { nearbyPollenProvider } from "./NearbyPollenEstimate";
import type { PollenProvider } from "./PollenProvider";
import { WeatherDtProvider } from "./WeatherDtProvider";

const allPollenProviders: readonly PollenProvider[] = [
  new WeatherDtProvider(),
  nearbyPollenProvider,
  new BeijingPollenProvider(),
];

export interface ProviderFeatureGates {
  readonly weatherDt: boolean;
  readonly beijingPollen: boolean;
}

export function getEnabledPollenProviders(gates: ProviderFeatureGates): readonly PollenProvider[] {
  return allPollenProviders.filter((provider) => {
    if (provider.id === "weatherdt") return gates.weatherDt;
    if (provider.id === "beijing-pollen") return gates.beijingPollen;
    return true;
  });
}

// Compatibility export for development tools that have not yet adopted runtime gates.
export const pollenProviders = allPollenProviders;
