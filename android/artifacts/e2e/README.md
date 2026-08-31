# Android Phase B E2E evidence

Device: Pixel 9 Pro XL AVD (`emulator-5556`, API 35)

The debug client reached the local backend through `http://10.0.2.2:8080/`.

| Check | Result | Evidence |
| --- | --- | --- |
| Home loads total pollen and independent Artemisia states | Passed | `home_beijing.png` |
| Artemisia has an explicit empty state when no independent observations exist | Passed | `home_beijing.png`, `history_artemisia_empty.png` |
| Location picker lists cities and Beijing districts | Passed | `locations.png`, `locations_districts.png` |
| Selected Chaoyang location survives process restart through DataStore | Passed | `home_datastore_restart.png` |
| History filters render and Artemisia history stays empty rather than falling back to total pollen | Passed | `history.png`, `history_artemisia_empty.png` |
| My and data-explanation screens render; system Back returns to My | Passed | `settings.png`, `data_notes.png` |
| Backend outage displays a safe retry message without raw socket details | Passed | `network_error.png` |
| Backend recovery and Retry restore the Home screen | Passed | `home_network_recovered.png` |

The automated `connectedDebugAndroidTest` suite repeats the navigation, debug-network configuration, empty-Artemisia, and stale-connection recovery checks against this AVD.
