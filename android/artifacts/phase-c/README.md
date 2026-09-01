# Android Phase C E2E evidence

Device: Pixel 9 Pro XL AVD (`Pixel_9_Pro_XL`, API 35; ADB serial changed from `emulator-5556` to `emulator-5554` after emulator restart).

The Debug app reached the real local backend through `http://10.0.2.2:8080/` before offline scenarios. `adb shell run-as` confirmed `allergen-radar.db`, WAL and shared-memory files in the app database directory.

| Scenario | Result | Evidence |
| --- | --- | --- |
| Online Home network load | Passed; no offline marker | `online_home.png` |
| Online Locations and History populate Room | Passed; no offline marker | `online_locations.png`, `online_history.png` |
| Kill app, stop backend, restart | Passed; current TOTAL and Artemisia query cache are explicitly marked | `offline_cached_restart.png` |
| Restore backend and Refresh | Passed; cache marker disappears and network result resumes | `recovered_network_home.png` |
| Offline locations | Passed; cached location list remains selectable with a text marker | `offline_locations.png` |
| Offline history | Passed; last successful history query renders with `离线历史缓存` | `offline_history.png` |

ARTEMISIA was empty in the real backend response. The offline restart screenshot still shows `暂无蒿属独立数据` under the distinct cached Artemisia query result; it does not substitute TOTAL pollen. Deterministic Room instrumentation tests separately verify that an online empty ARTEMISIA response clears an older ARTEMISIA cache entry.
