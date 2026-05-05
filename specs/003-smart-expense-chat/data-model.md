# Data Model: Smart Expense Chat Input

**Feature**: 003-smart-expense-chat | **Date**: 2026-04-30

---

## Overview

This feature introduces an ephemeral, in-memory conversation model layered on top of the existing `ExpenseEntity` and `VehicleEntity`. No new Room entities are required — chat session data lives exclusively in ViewModel `StateFlow`s and is discarded when the session ends (navigation away, successful save, or 10-minute background timeout).

---

## New In-Memory Models (`app` module)

### ChatMessage

Represents a single turn in the conversation.

```
ChatMessage
├── id: String              // UUID, unique per message
├── role: MessageRole       // USER | SYSTEM
├── contentType: ContentType // TEXT | IMAGE | CONFIRMATION_SUMMARY
├── text: String?           // Non-null for TEXT and CONFIRMATION_SUMMARY
├── imageBase64: String?    // Non-null for IMAGE messages only
├── imageMimeType: String?  // "image/jpeg" | "image/png", non-null when imageBase64 present
└── timestamp: Long         // System.currentTimeMillis()
```

**Enums**:
- `MessageRole`: `USER`, `SYSTEM`
- `ContentType`: `TEXT`, `IMAGE`, `CONFIRMATION_SUMMARY`

**Validation**: A `SYSTEM` message MUST have `contentType = TEXT` or `CONFIRMATION_SUMMARY`. A `USER` message with `contentType = IMAGE` MUST have non-null `imageBase64`.

---

### DraftExpense

Accumulates extracted expense fields across the conversation. Fields are `null` until filled by the LLM or by a follow-up answer.

```
DraftExpense
├── amount: Double?                // Euros; must be > 0 when non-null
├── category: ExpenseCategory?     // FUEL | MAINTENANCE | EXTRA | null
├── vehicleId: String?             // FK to VehicleEntity.id
├── date: Long?                    // Unix millis; defaults to today at confirmation time
├── description: String?
├── fuelType: String?              // "PETROL" | "DIESEL" | "LPG" | "CNG" | "ELECTRIC" | "HYBRID"
├── liters: Double?
├── pricePerLiter: Double?
├── totalCost: Double?             // Alias for amount when category is FUEL
└── warnings: List<String>         // Non-critical extraction notes
```

**State transitions**: fields move from `null` → non-null as the conversation progresses. Fields cannot revert to `null` once set (they can be overwritten by a subsequent user correction).

---

### RequiredField (enum)

Enumerates fields that must be populated before an expense can be saved. Drives follow-up question ordering.

```
RequiredField
├── AMOUNT      // Priority 1 — always required
├── CATEGORY    // Priority 2 — always required
├── VEHICLE     // Priority 3 — always required
└── DATE        // Priority 4 — auto-fills to today if never answered
```

---

### ConversationState (sealed class)

The finite state machine governing the chat session lifecycle.

```
ConversationState
├── Idle                           // No conversation in progress
├── Processing                     // Awaiting LLM response
├── AwaitingAnswer(
│     field: RequiredField,
│     options: List<String>?       // Pre-filled options (vehicle names, category list); null = free text
│   )
├── Confirming(draft: DraftExpense) // All required fields collected; show summary
└── Saved                          // Expense written to Room; conversation complete
```

---

### ChatUiState

Top-level state object exposed by `ChatExpenseViewModel` via `StateFlow<ChatUiState>`.

```
ChatUiState
├── messages: List<ChatMessage>     // Ordered oldest-first
├── conversationState: ConversationState
├── inputText: String               // Current text in the input field
├── isAttachEnabled: Boolean        // True when state is Idle or AwaitingAnswer(free-text)
└── errorMessage: String?           // Transient error (network failure, parse error)
```

---

## Extended Shared Network Models

### LlmClient (interface — extended)

Adds a vision-capable overload alongside the existing `chat()` method.

```
LlmClient
├── suspend fun chat(systemPrompt: String, userPrompt: String): String           // existing
└── suspend fun chatWithImage(
        systemPrompt: String,
        userPrompt: String,
        imageBase64: String,
        mimeType: String
    ): String                                                                     // new
```

---

## Relationships to Existing Entities

```
DraftExpense.vehicleId ──FK──► VehicleEntity.id   (validated at confirmation time)
DraftExpense  ──on save──►  ExpenseEntity           (same mapping as existing save flows)
```

The `DraftExpense` fields map directly to `ExpenseEntity` columns. No schema migration is required.

---

## State Transition Diagram

```
Idle
  │  user sends text or image
  ▼
Processing
  │  LLM returns parsed data
  ├──► AwaitingAnswer(field)   ← if required fields missing
  │         │  user answers
  │         ├──► AwaitingAnswer(next field)   ← if more fields missing
  │         └──► Confirming(draft)            ← all required fields filled
  └──► Confirming(draft)       ← if all fields extracted in one shot
             │  user taps Save
             ▼
           Saved
             │  auto-reset after 1s
             ▼
           Idle  (new session)

Any state ──► Idle   (user navigates away or session times out)
```
