# Implementation Plan: Expense Input UX (NLP + Classic Form)

**Branch**: `001-expense-input-ux` | **Date**: 2026-04-28 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/001-expense-input-ux/spec.md`

## Summary

Make the expense input screen user-friendly by (1) allowing inline editing of NLP
parse results before saving, (2) expanding the classic form to cover all three
expense categories (Fuel, Maintenance, Extra) instead of fuel-only, and (3)
providing unified success/error feedback across both input modes. All ViewModel
state is moved from Compose local `remember` to `StateFlow` to survive tab switches.

## Technical Context

**Language/Version**: Kotlin 1.9.22 (Java 17)
**Primary Dependencies**: Jetpack Compose + Material3 BOM 2024.01.00, Hilt 2.50,
Room 2.6.1, Navigation Compose 2.7.6, Kotlinx Coroutines
**Storage**: Room 2.6.1 — no schema changes; existing `ExpenseEntity` covers all
required fields for Fuel, Maintenance, and Extra categories
**Testing**: JUnit (JVM) for ViewModel and parser unit tests; Android instrumented
tests for UI integration
**Target Platform**: Android API 26+ (Android 8.0)
**Project Type**: Multi-module Android app (app + shared:parser library)
**Performance Goals**: NLP parse and UI response under 500 ms on mid-range hardware;
Room save under 100 ms
**Constraints**: Offline-capable (no network required for any save path); Kotlin DSL
build; Material3 design language; Hilt DI only
**Scale/Scope**: Single-user app; up to 3 expense categories; ~5 new/modified
Composable files and 1 ViewModel refactor

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Pre-Design | Post-Design | Notes |
|-----------|-----------|-------------|-------|
| I. Offline-First | ✅ PASS | ✅ PASS | All saves route through Room; no network dependency added |
| II. NLP-Powered Input | ✅ PASS | ✅ PASS | NLP tab remains index 0 (default); form is secondary |
| III. Test-First | ✅ PASS | ✅ PASS | ViewModel auto-calc logic must have unit tests before implementation |
| IV. Multi-Module Clean Architecture | ✅ PASS | ✅ PASS | Only `ExpenseParser.parse()` and `ParsedExpense` are imported from `shared:parser` |
| V. Reactive Architecture | ✅ PASS | ✅ PASS | All new state in `StateFlow`; ViewModels never reference DAOs directly |

**No violations. No Complexity Tracking entries required.**

## Project Structure

### Documentation (this feature)

```text
specs/001-expense-input-ux/
├── plan.md              ← this file
├── research.md          ← Phase 0 output
├── data-model.md        ← Phase 1 output
├── quickstart.md        ← Phase 1 output
├── contracts/
│   └── ui-contracts.md  ← Phase 1 output
└── tasks.md             ← Phase 2 output (/speckit-tasks — NOT created here)
```

### Source Code (repository root)

```text
app/src/main/java/com/carflow/app/
└── ui/
    └── screens/
        └── expense/
            ├── ExpenseInputScreen.kt              (modify — NlpTabContent + FormTabContent)
            ├── ExpenseListScreen.kt               (no change)
            ├── components/
            │   ├── EditableExpenseCard.kt         (new — replaces read-only ParsedResultCard)
            │   ├── ParsedResultCard.kt            (remove — superseded by EditableExpenseCard)
            │   ├── FuelFormContent.kt             (new — extracted from FuelFormTabContent)
            │   ├── MaintenanceFormContent.kt      (new)
            │   ├── ExtraFormContent.kt            (new)
            │   ├── SaveResultBanner.kt            (new — unified feedback)
            │   └── ExpenseItem.kt                 (no change)
            └── viewmodel/
                └── ExpenseInputViewModel.kt       (modify — add NlpTabState, FormTabState,
                                                    EditableExpense; move state from UI)

shared/parser/src/main/kotlin/com/carflow/parser/
└── model/
    └── ParsedExpense.kt                           (no change)
```

**Structure Decision**: Mobile + multi-module (Option 3 variant). All UI changes are
confined to `app/`. The `shared:parser` module is consumed read-only. Room schema is
unchanged.

## Complexity Tracking

No Constitution Check violations were found. This section is intentionally empty.
