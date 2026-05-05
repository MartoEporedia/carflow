# UI Contracts: Expense Input Screen

**Feature**: 001-expense-input-ux
**Date**: 2026-04-28

These contracts define the public interface of each Composable and ViewModel
function introduced or modified by this feature. Implementations MUST honour
these signatures.

---

## ExpenseInputScreen (modified)

**File**: `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseInputScreen.kt`

```
ExpenseInputScreen(
  onNavigateBack: () -> Unit,
  viewModel: ExpenseInputViewModel = hiltViewModel()
)
```

**Behaviour**:
- Hosts a `TabRow` with two tabs: "Parser NLP" (index 0) and "Form Manuale" (index 1).
- Tab content is `NlpTabContent` and `FormTabContent` respectively.
- Both tabs share the same `viewModel` instance.

---

## NlpTabContent (modified)

**File**: same as above (private Composable)

```
NlpTabContent(viewModel: ExpenseInputViewModel)
```

**Observes**:
- `viewModel.nlpState: StateFlow<NlpTabState>`

**Renders**:
- Multi-line `OutlinedTextField` bound to `nlpState.inputText`
- "Analizza" `Button` (disabled when inputText blank or isParsing)
- `CircularProgressIndicator` while `isParsing`
- `EditableExpenseCard` when `editedExpense != null`
- `SaveResultBanner` when `saveResult != null`

---

## EditableExpenseCard (new)

**File**: `app/.../expense/components/EditableExpenseCard.kt`

```
EditableExpenseCard(
  expense: EditableExpense,
  onExpenseChange: (EditableExpense) -> Unit,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier
)
```

**Behaviour**:
- Shows category (editable dropdown), amount (editable text field), description
  (editable text field), warnings (read-only), date (read-only label + picker trigger).
- "Conferma e Salva" button enabled only when `amount` is a valid positive number.
- Calls `onExpenseChange` on every field edit.
- Calls `onConfirm` on button tap.

---

## FormTabContent (new — replaces FuelFormTabContent)

**File**: `app/.../expense/ExpenseInputScreen.kt` (private Composable)

```
FormTabContent(viewModel: ExpenseInputViewModel)
```

**Observes**:
- `viewModel.formState: StateFlow<FormTabState>`

**Renders**:
- Category selector chip group or dropdown: Carburante / Manutenzione / Extra
- Conditionally renders one of:
  - `FuelFormContent` when `selectedCategory == FUEL`
  - `MaintenanceFormContent` when `selectedCategory == MAINTENANCE`
  - `ExtraFormContent` when `selectedCategory == EXTRA`
- `SaveResultBanner` when `saveResult != null`

---

## FuelFormContent (modified — extracted)

**File**: `app/.../expense/components/FuelFormContent.kt`

```
FuelFormContent(
  state: FuelFormState,
  onStateChange: (FuelFormState) -> Unit,
  onSave: () -> Unit
)
```

**Behaviour**: identical to current `FuelFormTabContent` minus the outer Column
wrapper and feedback banner (moved to `FormTabContent`).

---

## MaintenanceFormContent (new)

**File**: `app/.../expense/components/MaintenanceFormContent.kt`

```
MaintenanceFormContent(
  state: MaintenanceFormState,
  onStateChange: (MaintenanceFormState) -> Unit,
  onSave: () -> Unit
)
```

**Fields rendered**:
- Date picker (same pattern as FuelFormContent)
- Amount `OutlinedTextField` (mandatory, numeric)
- Description `OutlinedTextField` (optional, multi-line)
- "Salva manutenzione" `Button` (disabled when amount invalid)

---

## ExtraFormContent (new)

**File**: `app/.../expense/components/ExtraFormContent.kt`

```
ExtraFormContent(
  state: ExtraFormState,
  onStateChange: (ExtraFormState) -> Unit,
  onSave: () -> Unit
)
```

**Fields rendered**:
- Date picker
- Amount `OutlinedTextField` (mandatory, numeric)
- Description `OutlinedTextField` (optional; used as sub-type label, e.g., "Assicurazione")
- "Salva spesa extra" `Button` (disabled when amount invalid)

---

## SaveResultBanner (new)

**File**: `app/.../expense/components/SaveResultBanner.kt`

```
SaveResultBanner(
  result: SaveResult,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
)
```

**Behaviour**:
- `SaveResult.Success`: green tinted card with "Spesa salvata!" and auto-dismiss
  after 3 seconds via `LaunchedEffect`.
- `SaveResult.Error(message)`: red tinted card with `message` and a "Riprova"
  `TextButton` that calls `onDismiss`.

---

## ExpenseInputViewModel (modified)

**File**: `app/.../expense/viewmodel/ExpenseInputViewModel.kt`

### Exposed state

```
val nlpState: StateFlow<NlpTabState>
val formState: StateFlow<FormTabState>
```

### Commands

```
fun updateNlpInput(text: String)
fun parseExpense()
fun updateEditedExpense(updated: EditableExpense)
fun confirmParsedExpense()
fun clearNlpSaveResult()

fun selectCategory(category: ExpenseCategory)
fun updateFuelForm(state: FuelFormState)
fun updateMaintenanceForm(state: MaintenanceFormState)
fun updateExtraForm(state: ExtraFormState)
fun saveFormExpense()
fun clearFormSaveResult()
```

### Invariants

- `parseExpense()` MUST NOT be called while `nlpState.isParsing == true`.
- `confirmParsedExpense()` MUST only be called when `nlpState.editedExpense != null`.
- `saveFormExpense()` delegates to the correct repository method based on
  `formState.selectedCategory`.
