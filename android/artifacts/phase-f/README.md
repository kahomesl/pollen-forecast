# Phase F Android visual evidence

These screenshots were captured from the Pixel 9 Pro XL API 36 AVD against the local backend at `10.0.2.2:8080`.

- `home-beijing-api36.png`: the Beijing city-level Artemisia child-location state.
- `home-district-api36.png`: the Beijing district valid-empty state; it never presents empty data as low risk.
- `location-district-focus-api36.png`: the user-triggered Beijing district list; it does not select a district automatically.
- `history-api36.png`: all-taxa history with measurement-type filters.
- `my-api36.png`: notification default-off and product settings.
- `data-info-api36.png`: data-boundary explanations.

The API 35 `google_atd` AVD has `hw.gpu.enabled=no` in its existing AVD configuration. Its accessibility tree and instrumentation tests run correctly, but framebuffer captures are black; it is therefore used for automated compatibility verification rather than visual screenshots.
