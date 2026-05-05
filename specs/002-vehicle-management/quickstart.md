# Quickstart: Vehicle Management & Mandatory Vehicle Association

## Context

Branch: `002-vehicle-management`
Spec: `specs/002-vehicle-management/spec.md`
Plan: `specs/002-vehicle-management/plan.md`

## What changes and where

### New file

| File | Purpose |
|------|---------|
| `app/src/main/java/com/carflow/app/data/settings/VehiclePreferences.kt` | SharedPreferences wrapper for `last_used_vehicle_id` |
| `app/src/main/java/com/carflow/app/ui/screens/expense/components/VehicleSelector.kt` | Reusable vehicle-selector composable used on the expense screen |

### Modified files

| File | Change summary |
|------|----------------|
| `app/.../ui/screens/vehicle/VehicleScreen.kt` | Add edit icon per card; add year field + fuel-type dropdown to add/edit form; delete confirmation dialog for vehicles with expenses |
| `app/.../ui/screens/vehicle/viewmodel/VehicleViewModel.kt` | Add `editingVehicle` StateFlow; add `editVehicle()` / `updateVehicle()` functions; pass year + fuelType through `addVehicle()` |
| `app/.../ui/screens/expense/ExpenseInputScreen.kt` | Embed `VehicleSelector` above tab row; wire `selectedVehicleId` to save button enabled state |
| `app/.../ui/screens/expense/viewmodel/ExpenseInputViewModel.kt` | Delete `resolveVehicleId()`; add `selectedVehicleId: MutableStateFlow<String?>`; persist selection to `VehiclePreferences` on save; init from `VehiclePreferences` |
| `app/.../di/DatabaseModule.kt` | Provide `VehiclePreferences` as a `@Singleton` |

### No changes required

- `VehicleEntity`, `ExpenseEntity` — schemas are already correct
- `VehicleDao`, `ExpenseDao` — no new queries needed
- `VehicleRepository` — `create()` already accepts year + fuelType params (just not used by VM)
- `shared:parser` — no changes; fuel-type strings are already defined there

## Build & test

```bash
# Build the app module
./gradlew :app:assembleDebug

# Run parser tests (must remain ≥95% coverage)
./gradlew :shared:parser:test

# Install on device/emulator for manual golden-path test
./gradlew :app:installDebug
```

## Golden-path manual test checklist

1. Open app → navigate to Vehicles → create a vehicle with all 7 fields → verify card shows all data
2. Tap vehicle card edit icon → change odometer → save → verify updated km on card
3. Navigate to Expense Input → verify vehicle selector shows the newly created vehicle pre-selected
4. Save an NLP expense → verify vehicle name appears on preview card → confirm → verify expense saved
5. Go back to Vehicles → delete the vehicle (with linked expense) → verify confirmation dialog appears
6. After deletion → go to Expense Input → verify save button is disabled and no-vehicle banner appears
