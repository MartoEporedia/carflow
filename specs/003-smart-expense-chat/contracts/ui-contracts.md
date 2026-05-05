# UI Contracts: Smart Expense Chat Input

**Feature**: 003-smart-expense-chat | **Date**: 2026-04-30

---

## Screen: ChatExpenseScreen

**Route**: `Screen.ChatExpense` (`"chat_expense"`)  
**ViewModel**: `ChatExpenseViewModel`  
**Entry points**: Navigation button from `ExpenseListScreen`

### Props / Callbacks

```
ChatExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicle: () -> Unit
)
```

### States

| `ConversationState`   | UI behaviour                                                                      |
|-----------------------|-----------------------------------------------------------------------------------|
| `Idle`                | Empty message list. Input row enabled. Attachment button enabled.                 |
| `Processing`          | All input disabled. `CircularProgressIndicator` shown above input row.            |
| `AwaitingAnswer`      | System message shows question + optional suggestion chips. Input row enabled.     |
| `Confirming(draft)`   | `ExpenseConfirmationCard` shown as last message. Input row hidden. Save button visible. |
| `Saved`               | Success banner shown. Conversation resets to `Idle` after 1 s.                   |

### Invariants

- When `ConversationState` is `Idle` or `AwaitingAnswer` and no vehicles exist: input is enabled, but on send the system immediately responds with a "no vehicles" message and shows a CTA to the Vehicle screen.
- The attachment button is visible only when `ConversationState` is `Idle` or `AwaitingAnswer` with free-text expected (no options list).
- The message list auto-scrolls to the latest message on each state update.

---

## Component: ChatBubble

Renders a single `ChatMessage`.

```
ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
)
```

| `MessageRole` | `ContentType`          | Visual                                                               |
|---------------|------------------------|----------------------------------------------------------------------|
| `USER`        | `TEXT`                 | Right-aligned bubble, `primaryContainer` colour                      |
| `USER`        | `IMAGE`                | Right-aligned card with thumbnail and file-size label                |
| `SYSTEM`      | `TEXT`                 | Left-aligned bubble, `surfaceVariant` colour                         |
| `SYSTEM`      | `CONFIRMATION_SUMMARY` | Full-width `ExpenseConfirmationCard` (not a bubble)                  |

**Previews required**: one per Role × ContentType combination (4 previews minimum).

---

## Component: ChatInputRow

Bottom bar with text field and send/attach actions.

```
ChatInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
)
```

### Invariants

- Send button is enabled only when `text.isNotBlank()` AND `isEnabled`.
- Attach button is always visible but disabled when `isEnabled == false`.
- The text field shows a `keyboardType = Text` input. No numeric keyboard (the user types natural language).

---

## Component: SuggestionChips

Displays pre-filled answer options from `AwaitingAnswer.options`.

```
SuggestionChips(
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

- Renders as a horizontally scrollable `LazyRow` of `SuggestionChip` items.
- Tapping a chip calls `onOptionSelected` with the chip label, equivalent to the user typing that answer and sending it.
- Hidden when `options` is null or empty (free-text answer expected).

---

## Component: ExpenseConfirmationCard

Editable summary of the `DraftExpense` before saving.

```
ExpenseConfirmationCard(
    draft: DraftExpense,
    vehicles: List<VehicleEntity>,
    onDraftChange: (DraftExpense) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Fields shown

| Field            | Widget                            | Editable |
|------------------|-----------------------------------|----------|
| Amount (€)       | `OutlinedTextField` (numeric)     | Yes      |
| Category         | `ExposedDropdownMenuBox`          | Yes      |
| Vehicle          | `ExposedDropdownMenuBox`          | Yes      |
| Date             | `OutlinedTextField` + DatePicker  | Yes      |
| Description      | `OutlinedTextField`               | Yes      |
| Fuel type        | `ExposedDropdownMenuBox`          | Yes (FUEL only) |
| Liters           | `OutlinedTextField` (numeric)     | Yes (FUEL only) |
| Price/L          | `OutlinedTextField` (numeric)     | Yes (FUEL only) |

### Invariants

- Save button enabled only when `amount != null && amount > 0 && category != null && vehicleId != null`.
- Fuel-specific fields shown only when `category == FUEL`.
- Discard button resets to `Idle` (does not save, does not navigate away).

**Previews required**: complete draft (all fields), partial draft (amount + category only), FUEL category showing fuel-specific fields.

---

## ViewModel Contract: ChatExpenseViewModel

```kotlin
// Inputs (functions)
fun onTextChanged(text: String)
fun onSendText()
fun onImageSelected(base64: String, mimeType: String)
fun onFollowUpAnswer(answer: String)
fun onDraftChanged(draft: DraftExpense)
fun onSaveConfirmed()
fun onDiscardConversation()

// Outputs (StateFlow)
val uiState: StateFlow<ChatUiState>
val vehicles: StateFlow<List<VehicleEntity>>
```

### Behaviour

- `onSendText()` triggers LLM parsing when `conversationState == Idle`.
- `onFollowUpAnswer(answer)` resolves the current `AwaitingAnswer` field locally (no LLM call) and advances the state machine.
- `onImageSelected()` triggers `chatWithImage()` LLM call; state moves to `Processing`.
- `onSaveConfirmed()` calls `ExpenseRepository.save()` with the confirmed `DraftExpense`; state moves to `Saved`.
- `vehicles` is collected from `VehicleRepository.getActiveVehicles()` on init.

---

## Navigation Contract

### New route

```kotlin
object ChatExpense : Screen("chat_expense")
```

### Updated `CarFlowNavHost`

```kotlin
composable(Screen.ChatExpense.route) {
    ChatExpenseScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToVehicle = { navController.navigate(Screen.Vehicle.route) }
    )
}
```

### Updated `ExpenseListScreen`

Add a "Chat" navigation action (FAB secondary or toolbar icon) that calls `onNavigateToChat: () -> Unit`.

```kotlin
composable(Screen.ExpenseList.route) {
    ExpenseListScreen(
        onNavigateToInput = { … },
        onNavigateToStats = { … },
        onNavigateToVehicle = { … },
        onNavigateToChat = { navController.navigate(Screen.ChatExpense.route) }   // new
    )
}
```
