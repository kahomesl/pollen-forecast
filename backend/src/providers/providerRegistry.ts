import { BeijingPollenProvider } from "./BeijingPollenProvider";
import { nearbyPollenProvider } from "./NearbyPollenEstimate";
import type { PollenProvider } from "./PollenProvider";
import { WeatherDtProvider } from "./WeatherDtProvider";

export const pollenProviders: readonly PollenProvider[] = [
  new WeatherDtProvider(),
  nearbyPollenProvider,
  new BeijingPollenProvider(),
];
