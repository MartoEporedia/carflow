# Data Model: Expense Input UX

**Feature**: 001-expense-input-ux
**Date**: 2026-04-28

---

## Existing Entities (unchanged)

### ExpenseEntity (Room)

Persisted in the database. No schema changes required for this feature.

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | String (UUID) | ✅ | Primary key |
| vehicleId | String | ✅ | FK → VehicleEntity |
| category | String | ✅ | "FUEL" / "MAINTENANCE" / "EXTRA" |
| subcategory | String? | ❌ | e.g., fuelType for FUEL |
| amount | Double | ✅ | Total cost in euros |
| quantity | Double? | ❌ | Litres for fuel; generic quantity otherwise |
| quantityUnit | String? | ❌ | "LITERS" or domain-specific unit name |
| description | String? | ❌ | Free text |
| date | Long | ✅ | Unix epoch ms |
| odometerKm | Double? | ❌ | FUEL only |
| isFullTank | Boolean | ❌ | FUEL only; defaults to false |
| gasStationName | String? | ❌ | FUEL only |
| gasStationLocation | String? | ❌ | FUEL only |
| pricePerLiter | Double? | ❌ | FUEL only |
| createdAt | Long | ✅ | Auto-set on insert |
| updatedAt | Long | ✅ | Auto-set on insert |

### ParsedExpense (parser public API — read-only)

Returned by `ExpenseParser.parse()` and `LlmParser.parse()`. Fields not changed.

| Field | Type | Notes |
|-------|------|-------|
| category | ExpenseCategory | FUEL / MAINTENANCE / EXTRA |
| amount | Double? | Recognised amount |
| quantity | Double? | Recognised quantity |
| quantityUnit | QuantityUnit? | LITERS / GENERIC |
| description | String | Extracted or raw input |
| date | Long? | Recognised date or null |
| fuelType | FuelType? | PETROL / DIESEL / etc. |
| warnings | List\<String\> | Low-confidence signals from parser |

---

## New ViewModel State Models

These replace the current screen-local `remember` variables. All state lives in
`ExpenseInputViewModel` as `StateFlow`.

### NlpTabState

```
NlpTabState {
  inputText: String          // bound to the NLP text field
  parsedExpense: ParsedExpense?  // result from parser; null = no result yet
  editedExpense: EditableExpense?  // user-modified copy of parsedExpense
  isParsing: Boolean
  saveResult: SaveResult?
}
```

### EditableExpense

A mutable view-layer representation of ParsedExpense that the user can tweak
before confirming. Mirrors ParsedExpense fields but with String-typed numeric
fields to allow partial/in-progress editing without parse errors.

```
EditableExpense {
  category: ExpenseCategory     // editable via dropdown
  amount: String                // "" or numeric string
  quantity: String              // "" or numeric string
  quantityUnit: QuantityUnit?
  description: String
  date: Long
  fuelType: FuelType?           // only relevant when category == FUEL
  warnings: List<String>        // surfaced to user as-is from parser
}
```

**Validation rule**: `amount` must be parseable as a positive Double before save.

### FuelFormState (existing — moved to ViewModel)

Identical fields to current screen-local `FuelFormState` data class. No field
changes; only moved from Compose `remember` to ViewModel `StateFlow`.

### MaintenanceFormState (new)

```
MaintenanceFormState {
  date: Long                    // defaults to now
  amount: String                // mandatory
  description: String           // optional
}
```

**Validation rule**: `amount` must be parseable as positive Double.

### ExtraFormState (new)

```
ExtraFormState {
  date: Long                    // defaults to now
  amount: String                // mandatory
  description: String           // optional; serves as sub-type label
}
```

**Validation rule**: `amount` must be parseable as positive Double.

### FormTabState

Container for the form tab's shared state.

```
FormTabState {
  selectedCategory: ExpenseCategory   // defaults to FUEL
  fuelState: FuelFormState
  maintenanceState: MaintenanceFormState
  extrState: ExtraFormState
  saveResult: SaveResult?
}
```

### SaveResult (sealed)

```
SaveResult {
  Success
  Error(message: String)
}
```

---

## State Transitions

```
NLP tab:
  idle → (user types) → inputText set
  inputText set → (tap "Analizza") → isParsing = true
  isParsing = true → (parse succeeds) → parsedExpense set, editedExpense populated
  isParsing = true → (parse fails) → saveResult = Error(...)
  editedExpense set → (user edits fields) → editedExpense updated
  editedExpense set → (tap "Conferma e Salva") → saveResult = Success / Error
  saveResult = Success → (auto-clear after 3s) → NlpTabState reset to idle

Form tab:
  idle → (select category) → selectedCategory updated, relevant sub-form shown
  sub-form → (user fills fields) → respective FormState updated
  sub-form → (auto-calc trigger) → derived field populated (FUEL only)
  sub-form → (tap "Salva") → saveResult = Success / Error
  saveResult = Success → (auto-clear after 3s) → FormTabState reset to idle
```
