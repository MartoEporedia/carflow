# Implementation Plan: Vehicle Management & Mandatory Vehicle Association

**Branch**: `002-vehicle-management` | **Date**: 2026-04-29 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/002-vehicle-management/spec.md`

## Summary

Improve vehicle management so every vehicle carries all base data (nickname, make,
model, year, licence plate, fuel type, odometer), support editing existing vehicles,
and enforce that every expense is explicitly linked to a user-selected vehicle —
removing the existing auto-create default-vehicle fallback. A `VehicleSelector`
composable is added to the expense input screen; the last-used vehicle is persisted
across sessions via `SharedPreferences`.

## Technical Context

**Language/Version**: Kotlin 1.9.22 / JVM 17  
**Primary Dependencies**: Jetpack Compose + Material3 BOM 2024.01.00, Room 2.6.1, Hilt 2.50, Kotlin Coroutines/Flow  
**Storage**: Room (canonical source of truth) + SharedPreferences (last-used vehicle preference)  
**Testing**: JUnit for `shared:parser` (≥95% coverage enforced); `@Preview` for new composables  
**Target Platform**: Android 8.0+ (minSdk 26)  
**Project Type**: Android mobile app  
**Performance Goals**: Vehicle list refresh < 1 s; expense save < 500 ms (all local)  
**Constraints**: Offline-first — no network calls introduced; no new Gradle modules  
**Scale/Scope**: Personal use; 1–N vehicles (N expected < 10 for typical user)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Offline-First | ✅ PASS | All changes use Room + SharedPreferences; no network dependency |
| II. NLP-Powered Input | ✅ PASS | VehicleSelector is additive; NLP pipeline unchanged |
| III. Test-First | ✅ PASS | No parser changes; existing ≥95% coverage unaffected |
| IV. Multi-Module Clean Architecture | ✅ PASS | All changes in `app` module; `shared:parser` untouched |
| V. Reactive Architecture | ✅ PASS | New StateFlows use Coroutines/Flow; MVVM+Repository enforced; Hilt provides all new dependencies |

**Post-design re-check**: No violations introduced. `VehiclePreferences` is provided
as a `@Singleton` via Hilt; `VehicleViewModel` and `ExpenseInputViewModel` receive
it through constructor injection.

## Project Structure

### Documentation (this feature)

```text
specs/002-vehicle-management/
├── plan.md              ← this file
├── research.md          ← Phase 0 decisions
├── data-model.md        ← Phase 1 entity & state design
├── quickstart.md        ← Phase 1 build & test guide
├── contracts/
│   └── ui-contracts.md  ← Phase 1 UI component contracts
└── tasks.md             ← Phase 2 output (/speckit-tasks — not yet generated)
```

### Source Code (repository root)

```text
app/src/main/java/com/carflow/app/
├── data/
│   ├── entity/
│   │   ├── VehicleEntity.kt          (unchanged)
│   │   └── ExpenseEntity.kt          (unchanged)
│   ├── dao/
│   │   └── VehicleDao.kt             (unchanged)
│   ├── repository/
│   │   └── VehicleRepository.kt      (unchanged)
│   └── settings/
│       ├── LlmSettings.kt            (unchanged)
│       └── VehiclePreferences.kt     ← NEW
├── di/
│   └── DatabaseModule.kt             ← add VehiclePreferences @Provides
└── ui/
    └── screens/
        ├── vehicle/
        │   ├── VehicleScreen.kt       ← edit icon, year/fuelType fields, delete confirm dialog
        │   └── viewmodel/
        │       └── VehicleViewModel.kt ← editingVehicle StateFlow, edit/update functions
        └── expense/
            ├── ExpenseInputScreen.kt  ← embed VehicleSelector
            ├── components/
            │   └── VehicleSelector.kt ← NEW composable
            └── viewmodel/
                └── ExpenseInputViewModel.kt ← remove resolveVehicleId(), add selectedVehicleId StateFlow
```

**Structure Decision**: All changes stay within the existing `app` module following
the current package layout. No new modules or navigation routes are introduced.
