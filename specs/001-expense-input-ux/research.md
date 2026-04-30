# Research: Expense Input UX (NLP + Classic Form)

**Feature**: 001-expense-input-ux
**Date**: 2026-04-28
**Status**: Complete — all decisions resolved from existing codebase analysis

---

## Decision 1: Inline Editing of Parsed Result Card

**Decision**: Replace the read-only `ParsedResultCard` with an editable version using
`TextField` / `OutlinedTextField` components inside the card for amount, description,
and category fields.

**Rationale**: The card already uses a `Column` of `Row` pairs. Replacing the `Text`
value on the right side of each row with an editable field requires minimal structural
change. The `parsedExpense` state is already held as `MutableStateFlow<ParsedExpense?>` in
the ViewModel, so mutations are propagated reactively.

**Alternatives considered**:
- A separate "edit" screen after parse: rejected — extra navigation step hurts UX.
- Dismiss parse result and pre-fill the form tab: rejected — context switch confuses users.

---

## Decision 2: Category-Adaptive Form

**Decision**: Add a category selector (dropdown/chip group) at the top of the form
tab. Below it, render one of three conditional `@Composable` blocks:
`FuelFormContent`, `MaintenanceFormContent`, `ExtraFormContent`. The current
`FuelFormTabContent` becomes `FuelFormContent` after extraction.

**Rationale**: Compose's `when` expression on a `selectedCategory` state variable
makes conditional rendering straightforward with zero redundant recompositions.
Each sub-form manages its own local `State<FormState>` for fields that only belong
to that category.

**Alternatives considered**:
- A single mega-form with hidden fields per category: rejected — hard to maintain,
  unnecessary validation complexity.
- Separate screens per category: rejected — breaks the single "Nuova Spesa" entry
  point mandated by the spec.

---

## Decision 3: Unified Feedback (Success / Error)

**Decision**: Replace the current ad-hoc Snackbar (NLP path) and inline text label
(form path) with a single `ExpenseSavedBanner` composable that both tabs call via
shared ViewModel state. The ViewModel exposes a `saveResult: StateFlow<SaveResult?>`
(sealed class: `Success`, `Error(message)`). The banner auto-dismisses after 3 seconds
on success.

**Rationale**: One ViewModel is already shared between both tabs. Adding a single
`saveResult` StateFlow centralises feedback logic and removes the duplicated
`showSavedSnackbar` / `saveSuccess` local states.

**Alternatives considered**:
- Each tab managing its own feedback state: rejected — inconsistency was the
  original problem being solved.
- Navigating away on success: rejected — the app's flow should allow rapid sequential
  expense entry without re-navigating.

---

## Decision 4: Tab State Preservation on Tab Switch

**Decision**: Keep `inputText` for the NLP tab and each category's `FormState` in
the shared ViewModel (as `StateFlow`) rather than Compose local `remember` state.
This ensures state survives tab switches.

**Rationale**: Currently `inputText` and `FuelFormState` are Compose `remember`
variables, which are reset when the Composable leaves the composition (tab switch).
Moving them to the ViewModel costs very little and solves FR-009 cleanly.

**Alternatives considered**:
- `rememberSaveable`: survives process death but still resets on tab switch in the
  current `TabRow` pattern (Composables are fully removed from composition on tab
  switch). Rejected.

---

## Decision 5: Auto-Calculation for Fuel Fields

**Decision**: Keep the existing `recalc()` function pattern. Move it into the
ViewModel as a pure function triggered by `onValueChange` of each numeric field.
No change to the calculation logic itself.

**Rationale**: The algorithm already works correctly. Moving it to the ViewModel
(from screen-level local function) fits the MVVM mandate from Constitution Principle V
and enables unit testing.

---

## No NEEDS CLARIFICATION Items

All technical decisions are derivable from the existing multi-module codebase:
- Kotlin 1.9.22 + Jetpack Compose + Material3: already in use
- Room + Repository + Hilt: already in use
- StateFlow + ViewModel: already the pattern in `ExpenseInputViewModel`
- Parser public API (`ExpenseParser.parse()`, `ParsedExpense`): already consumed
