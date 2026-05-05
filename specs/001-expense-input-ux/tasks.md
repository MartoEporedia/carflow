# Tasks: Expense Input UX (NLP + Classic Form)

**Input**: Design documents from `specs/001-expense-input-ux/`
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅, research.md ✅

**Tests**: Not requested in specification — no test tasks generated.

**Organization**: Tasks are grouped by user story to enable independent implementation
and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- File paths relative to repo root

---

## Phase 1: Setup

**Purpose**: Verify file locations and confirm no structural changes to project are needed.

- [x] T001 Confirm that `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`, `viewmodel/ExpenseInputViewModel.kt`, and `components/` directory all exist and match the source structure in plan.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Define new data classes and refactor `ExpenseInputViewModel` to hold all
tab state as `StateFlow`. MUST be complete before any user story phase begins.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T002 [P] Define `sealed class SaveResult` (subclasses: `Success`, `Error(message: String)`) at the bottom of `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`
- [x] T003 [P] Define `data class EditableExpense` (fields: `category: ExpenseCategory`, `amount: String`, `description: String`, `date: Long`, `fuelType: FuelType?`, `quantityUnit: QuantityUnit?`, `quantity: String`, `warnings: List<String>`) in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`
- [x] T004 [P] Define `data class NlpTabState` (fields: `inputText: String = ""`, `editedExpense: EditableExpense? = null`, `isParsing: Boolean = false`, `saveResult: SaveResult? = null`) in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`
- [x] T005 [P] Define `data class MaintenanceFormState` (fields: `date: Long`, `amount: String = ""`, `description: String = ""`), `data class ExtraFormState` (same fields), and `data class FormTabState` (fields: `selectedCategory: ExpenseCategory = FUEL`, `fuelState: FuelFormState`, `maintenanceState: MaintenanceFormState`, `extraState: ExtraFormState`, `saveResult: SaveResult? = null`) in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`
- [x] T006 Replace `_parsedExpense: MutableStateFlow<ParsedExpense?>` and `_isParsing: MutableStateFlow<Boolean>` with `_nlpState: MutableStateFlow<NlpTabState>` and `_formState: MutableStateFlow<FormTabState>` in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`; expose as `val nlpState` and `val formState`
- [x] T007 Implement NLP command functions (`updateNlpInput`, `parseExpense`, `updateEditedExpense`, `confirmParsedExpense`, `clearNlpSaveResult`) in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt` per contracts/ui-contracts.md; `parseExpense` populates `editedExpense` from parser result; `confirmParsedExpense` maps `EditableExpense` → `ExpenseEntity` and calls `expenseRepository`
- [x] T008 Implement form command functions (`selectCategory`, `updateFuelForm`, `updateMaintenanceForm`, `updateExtraForm`, `saveFormExpense`, `clearFormSaveResult`) in `app/src/main/java/com/carflow/app/ui/screens/expense/viewmodel/ExpenseInputViewModel.kt`; `saveFormExpense` dispatches to correct `expenseRepository` method based on `formState.selectedCategory`
- [x] T009 Move `private fun recalc(state: FuelFormState): FuelFormState` from `ExpenseInputScreen.kt` into `ExpenseInputViewModel.kt` and call it inside `updateFuelForm()` before updating `_formState`

**Checkpoint**: All ViewModel state and commands are in place — user story implementation can now begin.

---

## Phase 3: User Story 1 — NLP Input with Editable Preview (Priority: P1) 🎯 MVP

**Goal**: User types free-text expense, sees an editable preview card, corrects any
field, then saves. All categories supported (not just fuel).

**Independent Test**: Type `benzina 50€ 30L` → tap "Analizza" → verify preview card
shows Carburante / €50.00 / 30 L → change amount to €45 → tap "Conferma e Salva" →
verify success banner appears and expense shows in list with €45.

### Implementation for User Story 1

- [x] T010 [P] [US1] Create `SaveResultBanner` composable in `app/src/main/java/com/carflow/app/ui/screens/expense/components/SaveResultBanner.kt`: for `SaveResult.Success` render a green-tinted `Card` with "Spesa salvata!" text and auto-dismiss via `LaunchedEffect(Unit) { delay(3000); onDismiss() }`; for `SaveResult.Error(message)` render a red-tinted `Card` with `message` text and a "Riprova" `TextButton` calling `onDismiss`
- [x] T011 [P] [US1] Create `EditableExpenseCard` composable in `app/src/main/java/com/carflow/app/ui/screens/expense/components/EditableExpenseCard.kt` per contracts/ui-contracts.md: editable amount `OutlinedTextField`, editable description `OutlinedTextField`, category `ExposedDropdownMenuBox`, read-only warnings section (only shown when `expense.warnings.isNotEmpty()`), "Conferma e Salva" `Button` enabled only when `amount.toDoubleOrNull()?.let { it > 0 } == true`
- [x] T012 [US1] Rename `ParserTabContent` to `NlpTabContent` in `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`; replace all `remember`/`collectAsState` locals with `collectAsState` on `viewModel.nlpState`; bind input field to `viewModel.updateNlpInput()`; show `EditableExpenseCard` when `nlpState.editedExpense != null`; wire `onExpenseChange` to `viewModel.updateEditedExpense()` and `onConfirm` to `viewModel.confirmParsedExpense()`; show `SaveResultBanner` when `nlpState.saveResult != null`, wire `onDismiss` to `viewModel.clearNlpSaveResult()`
- [x] T013 [US1] Delete `app/src/main/java/com/carflow/app/ui/screens/expense/components/ParsedResultCard.kt` (fully superseded by `EditableExpenseCard`; verify no remaining imports)

**Checkpoint**: User Story 1 is fully functional and independently testable.

---

## Phase 4: User Story 2 — Classic Form for All Expense Categories (Priority: P2)

**Goal**: The "Form Manuale" tab shows a category selector; selecting Manutenzione or
Extra shows relevant fields; all three categories can be saved.

**Independent Test**: Select form tab → choose "Manutenzione" → fill amount €150,
description "Cambio olio" → tap "Salva manutenzione" → verify success banner and
expense appears in list under Maintenance category.

### Implementation for User Story 2

- [x] T014 [P] [US2] Extract `FuelFormContent` to `app/src/main/java/com/carflow/app/ui/screens/expense/components/FuelFormContent.kt` with signature `FuelFormContent(state: FuelFormState, onStateChange: (FuelFormState) -> Unit, onSave: () -> Unit)`; move the body of `FuelFormTabContent` verbatim (date picker, fuel type dropdown, price/litre/km fields, checkbox, station fields, description, save button) removing only the outer feedback logic
- [x] T015 [P] [US2] Create `MaintenanceFormContent` in `app/src/main/java/com/carflow/app/ui/screens/expense/components/MaintenanceFormContent.kt` with signature `MaintenanceFormContent(state: MaintenanceFormState, onStateChange: (MaintenanceFormState) -> Unit, onSave: () -> Unit)`; fields: date picker (same pattern as FuelFormContent), amount `OutlinedTextField` (mandatory, digits + decimal), description `OutlinedTextField` (optional, multi-line), "Salva manutenzione" `Button` disabled when amount is blank or non-positive
- [x] T016 [P] [US2] Create `ExtraFormContent` in `app/src/main/java/com/carflow/app/ui/screens/expense/components/ExtraFormContent.kt` with signature `ExtraFormContent(state: ExtraFormState, onStateChange: (ExtraFormState) -> Unit, onSave: () -> Unit)`; same fields as `MaintenanceFormContent` with button label "Salva spesa extra"
- [x] T017 [US2] Implement `FormTabContent` as a private `@Composable` in `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`; collect `viewModel.formState`; render category `SingleChoiceSegmentedButtonRow` or chip row (Carburante / Manutenzione / Extra) wired to `viewModel.selectCategory()`; use `when(formState.selectedCategory)` to render `FuelFormContent`, `MaintenanceFormContent`, or `ExtraFormContent`, each bound to the corresponding sub-state and `viewModel.updateXxxForm()` / `viewModel.saveFormExpense()`; show `SaveResultBanner` when `formState.saveResult != null`, wired to `viewModel.clearFormSaveResult()`
- [x] T018 [US2] Remove `FuelFormTabContent`, `FuelFormState` data class, and `recalc()` from `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`; update the `when(selectedTabIndex)` block in `ExpenseInputScreen` to call `FormTabContent(viewModel)` for tab index 1

**Checkpoint**: User Stories 1 AND 2 are both independently functional.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Tab state preservation, UX polish, and `@Preview` annotations.

- [x] T019 [P] Verify tab state preservation in `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`: confirm `NlpTabContent` reads `inputText` from `nlpState` StateFlow only (no `remember` for input text) and `FormTabContent` reads all form fields from `formState` StateFlow only; fix any remaining `remember` usage that would reset on tab switch
- [x] T020 [P] Add `@Preview` annotations (default state + filled state) to `EditableExpenseCard`, `SaveResultBanner`, `MaintenanceFormContent`, and `ExtraFormContent` in their respective component files
- [x] T021 Run manual validation from `specs/001-expense-input-ux/quickstart.md` (all three user story checklists); fix any UI layout, accessibility, or interaction issues found

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — **BLOCKS** all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase — no dependency on US2
- **User Story 2 (Phase 4)**: Depends on Foundational phase — T014 depends on T010 (`SaveResultBanner`) being available; US2 can otherwise proceed in parallel with US1
- **Polish (Phase 5)**: Depends on all user story phases complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational (Phase 2) — no dependency on US2
- **US2 (P2)**: Can start after Foundational (Phase 2) — T017 depends on T010 (`SaveResultBanner` from US1); T014–T016 are parallel and can start with US1
- **US3 (polish)**: Depends on US1 and US2 completion

### Within Each User Story

- Components (T010, T011, T014–T016) before screen wiring (T012, T017)
- Screen wiring before cleanup (T013, T018)
- All [P] tasks within a story can run in parallel

### Parallel Opportunities

- T002–T005 (data class definitions): all fully parallel
- T010 + T011 (SaveResultBanner + EditableExpenseCard): parallel
- T014 + T015 + T016 (FuelFormContent + MaintenanceFormContent + ExtraFormContent): parallel
- T019 + T020 (polish tasks): parallel

---

## Parallel Example: User Story 1

```bash
# Run in parallel — different files, no mutual dependency:
Task T010: "Create SaveResultBanner in .../components/SaveResultBanner.kt"
Task T011: "Create EditableExpenseCard in .../components/EditableExpenseCard.kt"

# Then sequentially (T012 depends on T010 + T011):
Task T012: "Update NlpTabContent in ExpenseInputScreen.kt"
Task T013: "Delete ParsedResultCard.kt"
```

## Parallel Example: User Story 2

```bash
# Run in parallel (can also overlap with T010/T011 from US1):
Task T014: "Extract FuelFormContent"
Task T015: "Create MaintenanceFormContent"
Task T016: "Create ExtraFormContent"

# Then sequentially (T017 depends on T010 for SaveResultBanner + T014–T016):
Task T017: "Implement FormTabContent"
Task T018: "Remove FuelFormTabContent from ExpenseInputScreen.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001)
2. Complete Phase 2: Foundational (T002–T009) — CRITICAL
3. Complete Phase 3: User Story 1 (T010–T013)
4. **STOP and VALIDATE**: Follow quickstart.md US1 checklist
5. Demo NLP path; user can save any-category expense via text

### Incremental Delivery

1. Setup + Foundational → ViewModel ready
2. US1 complete → NLP with editable preview working (MVP)
3. US2 complete → Form covers all categories
4. Polish → tab state, previews, final validation

---

## Notes

- [P] tasks = different files, no unmet dependencies
- `FuelFormState` moves from `ExpenseInputScreen.kt` to `ExpenseInputViewModel.kt` in T005; ensure no duplicate definition after T018
- `recalc()` moves in T009; ensure it is deleted from `ExpenseInputScreen.kt` in T018
- The `tabs` list in `ExpenseInputScreen` stays as `["Parser NLP", "Form Manuale"]` — no tab label changes needed
