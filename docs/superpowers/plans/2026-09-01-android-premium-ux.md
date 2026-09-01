# Android Premium UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Android beta visually premium and semantically explicit without regressing Phase A–E contracts.

**Architecture:** Add server-owned taxon availability facts to public location metadata, map them into Android data/state models, and render all screens through a reusable Compose design system. Keep navigation, repositories, DataStore, Room, WorkManager, and permission entry points intact.

**Tech Stack:** Bun/Elysia/TypeScript; Kotlin, Jetpack Compose Material 3, Retrofit, Room, DataStore, WorkManager.

**Spec:** `docs/superpowers/specs/2026-09-01-android-premium-ux-design.md`

## Global Constraints

- API changes are additive and derive availability only from Provider/location facts.
- Never infer Artemisia support from a location display name or ID in Android.
- Keep cache, permission, notification, Room v2, and release behavior unchanged.
- Use 4/8/12/16/20/24/32 dp spacing and 12/16/24/full shapes; support light/dark and font scaling.
- No new Provider, map, account, medical advice, fake data, copied reference assets, or new periodic worker.

---

### Task 1: Publish taxon availability facts

**Files:**
- Modify: `backend/src/domain/location.ts`, `backend/src/api/allergenV1.ts`, `docs/API_V1.md`
- Test: `backend/src/api/allergenV1.test.ts`

**Interfaces:**
- Produces: `taxonAvailability: [{ taxonCode, status, childScope?, childLocationLabel? }]` on each public location.

- [ ] Add failing API tests for Xian `UNSUPPORTED`, Beijing city `CHILD_LOCATION_REQUIRED`, and Beijing district `SUPPORTED`.
- [ ] Run `bun test backend/src/api/allergenV1.test.ts` and confirm the assertions fail before implementation.
- [ ] Add explicit public hierarchy metadata and server-derived availability serialization.
- [ ] Update the API contract with the additive location fields.
- [ ] Re-run focused backend tests and commit `feat: publish taxon availability metadata`.

### Task 2: Carry availability into Android state

**Files:**
- Modify: `android/.../data/ApiModels.kt`, `PollenApi.kt`, `AllergenRepository.kt`, `ui/viewmodel/ViewModels.kt`
- Create: `android/.../domain/ArtemisiaAvailability.kt`
- Test: `android/app/src/test/.../domain/ArtemisiaAvailabilityTest.kt`

**Interfaces:**
- Consumes: public `TaxonAvailabilityDto`.
- Produces: a sealed Artemisia presentation state with unsupported, child-required, valid-empty, observation, cache, and unavailable branches.

- [ ] Write unit tests for the six documented states and measurement/risk labels.
- [ ] Run the focused unit test and confirm it fails before implementation.
- [ ] Add serializable DTOs and a pure state resolver; expose the resolver output from home state.
- [ ] Run Android unit tests and commit `feat(android): model allergen availability states`.

### Task 3: Introduce design tokens and primitives

**Files:**
- Create: `android/.../ui/designsystem/*.kt`
- Modify: `android/.../ui/theme/Theme.kt`
- Test: `android/app/src/test/.../ui/designsystem/DesignSystemTest.kt`

**Interfaces:**
- Produces: semantic light/dark Material schemes, fixed tokens, and reusable cards, badges, banners, buttons, state panels, risk scale, and navigation primitives.

- [ ] Write token/label tests where logic is pure.
- [ ] Implement focused components with accessible semantics and minimum touch targets.
- [ ] Add previews for light, dark, and large-font core states.
- [ ] Run unit/lint and commit `feat(android): add premium design system`.

### Task 4: Redesign home and allergen states

**Files:**
- Create: `android/.../ui/screens/HomeScreen.kt`
- Modify: `android/.../ui/AllergenRadarApp.kt`, `ui/viewmodel/ViewModels.kt`
- Test: `android/app/src/test/.../ui/HomeSemanticsTest.kt`, Android Compose tests as needed

- [ ] Write tests for CURRENT/FORECAST/OBSERVATION/ESTIMATE labels, UNKNOWN presentation, provider-valid empty, and offline cache facts.
- [ ] Extract home presentation from the monolithic app file and render real total/Artemisia data through design-system primitives.
- [ ] Wire the Beijing child-location action to the existing location page district section.
- [ ] Run unit/lint/assemble and commit `feat(android): redesign home allergen states`.

### Task 5: Redesign location and history

**Files:**
- Create: `android/.../ui/screens/LocationScreen.kt`, `HistoryScreen.kt`
- Modify: `android/.../ui/AllergenRadarApp.kt`
- Test: existing nearby/location and history tests plus new UI semantics tests

- [ ] Preserve one-shot coarse-location entry and write state tests for candidate, denied, unsupported, and cached list wording.
- [ ] Render grouped locations and horizontal filter groups with focused, factual rows.
- [ ] Run Android unit and connected tests; commit `feat(android): redesign location and history`.

### Task 6: Redesign settings and information

**Files:**
- Create: `android/.../ui/screens/MyScreen.kt`, `InformationScreens.kt`
- Modify: `android/.../ui/AllergenRadarApp.kt`
- Test: notification/permission tests and data-copy tests

- [ ] Add tests for notification capability copy and denied-permission settings handoff.
- [ ] Render settings, privacy, about, and data explanation pages with factual editorial cards and BuildConfig version.
- [ ] Run Android unit/lint/assemble and commit `feat(android): redesign settings and information`.

### Task 7: Device verification and documentation

**Files:**
- Create: `android/artifacts/phase-f/README.md` and final emulator screenshots
- Modify: `android/README.md` if system behavior needs user-facing documentation
- Test: backend and Android full suites

- [ ] Run backend `bun test`, typecheck, and build.
- [ ] Run Android unit, lint, assemble, and connected tests on API 35 and API 36.
- [ ] Capture the required light/dark, state, cache, permission, and information screenshots without coordinates or copied artwork.
- [ ] Run `apksigner verify` and `git diff --check`; commit `test(android): verify premium product semantics` and push the feature branch.
