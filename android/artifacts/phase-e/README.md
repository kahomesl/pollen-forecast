# Phase E device evidence

This directory contains UI screenshots from the Pixel 9 Pro XL emulator.
They demonstrate the opt-in location permission and nearby supported-location
states only. Filenames and this document deliberately omit injected raw
coordinates; matching always occurs locally and the screenshots show only the
canonical candidate name and approximate distance presented to the user.

The device matrix is executed on API 35 and API 36. The debug client uses the
real local backend through `10.0.2.2:8080`; deterministic coordinate injection
is used only to make the emulator location source reproducible.
