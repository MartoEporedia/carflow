# Tasks: Vehicle Management & Mandatory Vehicle Association

**Input**: Design documents from `specs/002-vehicle-management/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no shared state)
- **[Story]**: User story this task belongs to (US1, US2, US3)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Wire the new `VehiclePreferences` dependency into Hilt before any feature work begins.

- [x] T001 Provide `VehiclePreferences` as a `@Singleton` via Hilt in `app/src/main/java/com/carflow/app/di/DatabaseModule.kt` — add `@Provides @Singleton fun provideVehiclePreferences(@ApplicationContext ctx: Context): VehiclePreferences`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T002 Create `app/src/main/java/com/carflow/app/data/settings/VehiclePreferences.kt` — `@Singleton` class injected with `@ApplicationContext Context`; wraps `SharedPreferences("carflow_vehicle_prefs", MODE_PRIVATE)`; exposes `fun getLastUsedVehicleId(): String?` and `fun setLastUsedVehicleId(id: String)`
- [x] T003 [P] Create `app/src/main/java/com/carflow/app/ui/screens/vehicle/FuelTypeUiModel.kt` — `object FuelTypeUiModel` with a `val entries: List<Pair<String, String>>` mapping display label → internal value: `"Benzina"→"petrol"`, `"Diesel"→"diesel"`, `"Elettrico"→"electric"`, `"Ibrido"→"hybrid"`, `"GPL"→"lpg"`, `"Metano"→"cng"`, `"(non specificato)"→""`; add `fun labelFor(value: String): String` helper
- [x] T004 [P] Add `@Query("SELECT COUNT(*) FROM expenses WHERE vehicleId = :vehicleId AND isDeleted = 0") suspend fun countByVehicleId(vehicleId: String): Int` to `app/src/main/java/com/carflow/app/data/dao/ExpenseDao.kt`

**Checkpoint**: Foundation ready — T002, T003, T004 must all pass before starting Phase 3.

---

## Phase 3: User Story 1 — Vehicle CRUD with Complete Base Data (Priority: P1) 🎯 MVP

**Goal**: Users can create, view, edit, and delete vehicles with all 7 base fields (nickname, make, model, year, plate, fuel type, odometer). Vehicles are shown with all data on their card.

**Independent Test**: Open Vehicles screen → tap "+" → fill all 7 fields → save → verify card shows all values → tap edit icon → change odometer → save → verify updated value. Delete a vehicle with linked expenses → verify confirmation dialog appears.

### Implementation for User Story 1

- [x] T005 [P] [US1] Add `suspend fun hasExpenses(vehicleId: String): Boolean` to `app/src/main/java/com/carflow/app/data/repository/VehicleRepository.kt` — delegates to `expenseDao.countByVehicleId(vehicleId) > 0`; inject `ExpenseDao` via constructor (add it to the `@Inject constructor`)
- [x] T006 [P] [US1] Update `app/src/main/java/com/carflow/app/ui/screens/vehicle/viewmodel/VehicleViewModel.kt`:
  - Add `private val _editingVehicle = MutableStateFlow<VehicleEntity?>(null)` and expose as `val editingVehicle: StateFlow<VehicleEntity?>`
  - Add `fun openEditDialog(vehicle: VehicleEntity)` and `fun closeEditDialog()`
  - Add `fun updateVehicle(vehicle: VehicleEntity, name: String, make: String, model: String, year: Int?, licensePlate: String, fuelType: String, odometerKm: Double)` that calls `vehicleRepository.update(vehicle.copy(...))`
  - Update `fun addVehicle(...)` signature to include `year: Int?` and `fuelType: String` parameters
  - Add `private val _showDeleteConfirm = MutableStateFlow<VehicleEntity?>(null)` and expose as `val showDeleteConfirm`; update `deleteVehicle()` to check `hasExpenses()` — if true, set `_showDeleteConfirm`; add `fun confirmDelete(vehicle: VehicleEntity)` that calls the actual soft-delete
- [x] T007 [US1] Update add-form Card in `app/src/main/java/com/carflow/app/ui/screens/vehicle/VehicleScreen.kt`:
  - Add `var year by remember { mutableStateOf("") }` and `var fuelType by remember { mutableStateOf("") }` local state
  - Add `OutlinedTextField` for year with `KeyboardType.Number`; show inline error if value is non-empty and outside [1900, currentYear+1]
  - Add `ExposedDropdownMenuBox` for fuel type using `FuelTypeUiModel.entries`; selected value shows label, stores internal value in `fuelType` state
  - Pass `year.toIntOrNull()` and `fuelType` to `viewModel.addVehicle(...)`
- [x] T008 [US1] Update `VehicleItem` composable in `app/src/main/java/com/carflow/app/ui/screens/vehicle/VehicleScreen.kt`:
  - Add `onEdit: (VehicleEntity) -> Unit` parameter
  - Add edit `IconButton` (use `Icons.Filled.Edit`) before the delete icon
  - Add display of `anno` (if non-null) and `carburante` label (if non-blank) using `FuelTypeUiModel.labelFor()`
  - In `VehicleScreen`, collect `editingVehicle` and call `viewModel.openEditDialog(vehicle)` in the `onEdit` lambda
- [x] T009 [US1] Add inline edit form Card to `app/src/main/java/com/carflow/app/ui/screens/vehicle/VehicleScreen.kt`:
  - Render when `editingVehicle != null` (below add-form area, above the list)
  - Pre-fill local state from `editingVehicle` fields on first composition (use `LaunchedEffect(editingVehicle)`)
  - Same 7 fields as the add form; Save calls `viewModel.updateVehicle(editingVehicle!!, ...)` then `viewModel.closeEditDialog()`; Cancel calls `viewModel.closeEditDialog()`
  - Add `@Preview` for edit-form-open state
- [x] T010 [US1] Add delete confirmation `AlertDialog` to `app/src/main/java/com/carflow/app/ui/screens/vehicle/VehicleScreen.kt`:
  - Collect `showDeleteConfirm` from ViewModel
  - When non-null, show dialog: title "Elimina veicolo?", body "Questo veicolo ha spese associate. Eliminarlo comunque?", buttons "Annulla" (dismiss → `viewModel.clearDeleteConfirm()`) and "Elimina" (→ `viewModel.confirmDelete(vehicle)`)
  - Add `fun clearDeleteConfirm()` to ViewModel

**Checkpoint**: User Story 1 fully functional — all 7 fields captured, edit works, delete confirmation works.

---

## Phase 4: User Story 2 — Mandatory Vehicle Selection on Every Expense (Priority: P2)

**Goal**: Every expense (NLP or form) requires an explicit vehicle selection. No auto-create fallback. If no vehicle exists, save is blocked with a clear CTA.

**Independent Test**: Delete all vehicles → go to Expense Input → verify save button is disabled and no-vehicle banner appears → create a vehicle → return to Expense Input → verify vehicle selector shows it pre-selected → save an expense → verify it is stored with the correct vehicleId.

### Implementation for User Story 2

- [x] T011 [P] [US2] Create `app/src/main/java/com/carflow/app/ui/screens/expense/components/VehicleSelector.kt`:
  - `@Composable fun VehicleSelector(vehicles: List<VehicleEntity>, selectedVehicleId: String?, onVehicleSelected: (String) -> Unit, onAddVehicleClicked: () -> Unit)`
  - State 1 (no vehicles): warning `Card` with text "Nessun veicolo registrato" and `Button("Aggiungi veicolo") { onAddVehicleClicked() }`
  - State 2 (vehicles exist, none selected): `ExposedDropdownMenuBox` showing placeholder "Seleziona veicolo"
  - State 3 (vehicles exist, one selected): `ExposedDropdownMenuBox` showing selected vehicle's `name`; dropdown lists all vehicles by name
  - Add `@Preview` for each of the 3 states
- [x] T012 [US2] Update `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`:
  - **Delete** `resolveVehicleId()` entirely
  - Inject `VehiclePreferences` via constructor
  - Add `private val _selectedVehicleId = MutableStateFlow<String?>(null)` and expose as `val selectedVehicleId: StateFlow<String?>`
  - In `init {}`: read `vehiclePreferences.getLastUsedVehicleId()`; validate it exists in the current `vehicles` list (collect first emission); set `_selectedVehicleId.value` to the validated id or `null`
  - Add `fun selectVehicle(id: String)` that sets `_selectedVehicleId.value = id`
  - Update all save functions (`saveFuelFromNlp`, `saveFuelFromForm`, `saveMaintenanceFromForm`, `saveExtraFromForm`) to receive `vehicleId: String` from `_selectedVehicleId.value ?: error("no vehicle selected")`
  - After each successful save, call `vehiclePreferences.setLastUsedVehicleId(vehicleId)`
- [x] T013 [US2] Update `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`:
  - Collect `vehicles` and `selectedVehicleId` from ViewModel
  - Embed `VehicleSelector(vehicles, selectedVehicleId, onVehicleSelected = { viewModel.selectVehicle(it) }, onAddVehicleClicked = { onNavigateToVehicle() })` above the tab row (requires `onNavigateToVehicle: () -> Unit` parameter added to `ExpenseInputScreen`)
  - Wire `onNavigateToVehicle` in `Navigation.kt` to `navController.navigate(Screen.Vehicle.route)`
  - Disable the NLP "Conferma e Salva" button and Form "Salva" button when `selectedVehicleId == null`
  - Add selected vehicle's `name` to the NLP preview `EditableExpenseCard` display (new read-only row above category)

**Checkpoint**: User Stories 1 and 2 both independently functional. All expenses now require a vehicle.

---

## Phase 5: User Story 3 — Active Vehicle & Quick Switch (Priority: P3)

**Goal**: Last-used vehicle is remembered across app restarts. Single-vehicle users incur zero extra taps. Multi-vehicle users can switch in 2 taps.

**Note**: The persistence mechanism (T002 `VehiclePreferences`) and the init/save wiring (T012) already implement US3 completely. This phase validates the end-to-end behaviour and handles the deleted-vehicle edge case.

**Independent Test**: Save expense for vehicle A → force-stop and reopen app → open Expense Input → verify vehicle A is still pre-selected. Delete vehicle A → open Expense Input → verify selector shows no pre-selection and save is disabled.

### Implementation for User Story 3

- [x] T014 [US3] Verify and harden the deleted-vehicle guard in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`:
  - In `init {}`, after restoring `lastUsedVehicleId` from `VehiclePreferences`, collect the first emission of `vehicleRepository.getAllVehicles()` and check that the restored id is present in the list; if not found (vehicle was deleted), set `_selectedVehicleId.value = null` and call `vehiclePreferences.setLastUsedVehicleId("")` to clear the stale preference
  - Add `combine(_vehicles, _selectedVehicleId)` derivation so that if the selected vehicle is soft-deleted while the app is running, `selectedVehicleId` automatically resets to `null`

**Checkpoint**: All 3 user stories are independently functional and verified.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [x] T015 [P] Add `@Preview` annotations to `app/src/main/java/com/carflow/app/ui/screens/vehicle/VehicleScreen.kt` covering: empty state, list with 2 vehicles, add-form-open state, edit-form-open state, delete-confirm-dialog state
- [x] T016 [P] Update `app/src/main/java/com/carflow/app/ui/navigation/Navigation.kt` — verify `ExpenseInputScreen` composable receives the `onNavigateToVehicle` lambda (added in T013); confirm navigation wiring is complete
- [x] T017 Run the golden-path manual test checklist from `specs/002-vehicle-management/quickstart.md` and fix any regressions found

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — **BLOCKS all user stories**
- **Phase 3 (US1)**: Depends on Phase 2 — T005 and T006 can start together [P]; T007–T010 are sequential (same file)
- **Phase 4 (US2)**: Depends on Phase 2 — T011 can start as soon as Phase 2 is done; T012 requires T002 (VehiclePreferences); T013 requires T011 and T012
- **Phase 5 (US3)**: Depends on T012 — hardens existing logic
- **Phase 6 (Polish)**: Depends on all story phases

### User Story Dependencies

- **US1 (P1)**: No dependency on US2 or US3 — fully independent
- **US2 (P2)**: No dependency on US1 at the code level (separate ViewModels/screens); integrates at the UX level (user creates vehicle in US1 flow, then uses it in US2 flow)
- **US3 (P3)**: Depends on T002 (VehiclePreferences) and T012 (init/save wiring) — no new files

### Within Phase 3 (US1)

- T005 [P] and T006 [P] can run in parallel (different files)
- T007 depends on T003 (FuelTypeUiModel)
- T008 depends on T003 (FuelTypeUiModel.labelFor)
- T009 depends on T006 (editingVehicle StateFlow)
- T010 depends on T006 (showDeleteConfirm StateFlow)
- T007 → T008 → T009 → T010 should be done sequentially (all touch VehicleScreen.kt)

### Parallel Opportunities

```bash
# Phase 2 — run together:
Task T002: Create VehiclePreferences.kt
Task T003: Create FuelTypeUiModel.kt
Task T004: Add countByVehicleId to ExpenseDao.kt

# Phase 3 — run together first:
Task T005: Add hasExpenses to VehicleRepository.kt
Task T006: Update VehicleViewModel.kt

# Phase 4 — run T011 while T012 is being written:
Task T011: Create VehicleSelector.kt
Task T012: Update ExpenseInputViewModel.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002, T003, T004)
3. Complete Phase 3: User Story 1 (T005–T010)
4. **STOP and VALIDATE**: Open Vehicles screen, create/edit/delete vehicles with all 7 fields
5. Demo vehicle management independently of expenses

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. US1 → Full vehicle CRUD with complete base data (**MVP**)
3. US2 → Expenses require explicit vehicle → **zero orphan expenses**
4. US3 → Persistence across restarts → **seamless returning-user experience**
5. Polish → Previews, navigation wiring, regression check

---

## Notes

- [P] tasks = different files, no shared state dependencies
- [Story] label maps each task to its user story for traceability
- No test tasks generated — spec does not request TDD approach for UI layer
- `shared:parser` is untouched — no parser changes in this feature
- No Room migrations required — `VehicleEntity` schema is already complete
- Commit after each logical group (e.g., after T004, after T010, after T013)
