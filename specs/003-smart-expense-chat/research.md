# Research: Smart Expense Chat Input

**Feature**: 003-smart-expense-chat | **Date**: 2026-04-30

---

## Decision 1: Multimodal Image Support in LlmClient

**Decision**: Extend the `LlmClient` interface with a separate `chatWithImage(systemPrompt, userPrompt, imageBase64, mimeType)` method. The existing `chat()` method stays unchanged to avoid breaking the parser pipeline.

**Rationale**: The existing `LlmClient.chat()` accepts only string prompts. OpenAI-compatible vision APIs embed images as base64-encoded data URLs inside the `messages[].content` array. Adding a dedicated method keeps the interface clean and allows `DirectLlmClient` / `ProxyLlmClient` to add vision-specific JSON serialisation without touching the text path. Both `DirectLlmClient` and `ProxyLlmClient` already use Ktor; adding multipart or JSON array content for vision is a small incremental change.

**Alternatives considered**:
- Unified method with nullable `imageBase64` param — rejected: optional params on interfaces make every implementor handle nullability; cleaner to keep methods separate.
- Separate `VisionLlmClient` interface — rejected: over-engineering; the same client instance handles both text and vision in every major provider.

---

## Decision 2: Follow-Up Answer Resolution Strategy

**Decision**: Resolve follow-up answers locally (no additional LLM call) using structured matching. The system presents a finite list of options (vehicle names, category names) and parses the user's reply by simple string comparison or `toDoubleOrNull()`. No second LLM round-trip per follow-up.

**Rationale**: Follow-up questions are structured — the system knows exactly what it is asking for (a vehicle name from a fixed list, a number, a date). Local resolution is deterministic, instant, and works offline. Each extra LLM call adds 1-3 s latency and API cost; for a single yes/no or pick-from-list answer that overhead is unacceptable UX.

**Alternatives considered**:
- Send the entire conversation to the LLM again on every answer — rejected: high latency, high cost, unnecessary for structured answers.
- Use the rule-based `ExpenseParser` to re-parse the answer — rejected: the parser targets full expense sentences, not isolated field answers.

---

## Decision 3: Conversation State Machine

**Decision**: Model conversation state as a sealed class `ConversationState` with five variants: `Idle`, `Processing`, `AwaitingAnswer(field: RequiredField, options: List<String>?)`, `Confirming(draft: DraftExpense)`, `Saved`. The `ChatExpenseViewModel` holds a `MutableStateFlow<ChatUiState>` that wraps the state and the ordered message list.

**Rationale**: A sealed class makes every UI state explicit and exhaustive, enabling the Compose UI to `when`-match without an `else` branch. Five states cover the full lifecycle without over-modelling. The `AwaitingAnswer` state carries the exact field being collected and optional pre-filled options (e.g., the vehicle list), so the UI can render contextual chips.

**Alternatives considered**:
- Boolean flags (isProcessing, isConfirming, …) — rejected: combinatorial explosion of invalid states (e.g., both flags true simultaneously).
- Pure UDF event/reducer pattern — considered valid but adds boilerplate for a single-screen flow; sealed class + StateFlow achieves the same guarantees with less code.

---

## Decision 4: Required Field Priority Order for Follow-Up Questions

**Decision**: Ask missing required fields in this order: **amount → category → vehicle → date** (date defaults to today silently if never provided by the time of confirmation). For FUEL category, additionally ask: **liters → price-per-liter** (if both still missing after initial parse, ask them as optional after vehicle).

**Rationale**: Amount is the most critical data point — without it the expense cannot be validated. Category determines which additional fields are relevant (no need to ask about liters for a MAINTENANCE expense). Vehicle is always required (existing FR). Date defaults to today in 90%+ of fuel/maintenance cases, so it is the lowest-priority explicit ask.

**Alternatives considered**:
- Ask all missing fields at once — rejected: breaks the "one question at a time" UX requirement; overwhelming for users with multiple missing fields.
- Ask category before amount — rejected: amount is universally required and category may be inferrable from other context already in the conversation.

---

## Decision 5: Image Handling — URI → Base64 in Composable Layer

**Decision**: The Compose UI layer reads the image `Uri` into bytes (via `ContentResolver`) and Base64-encodes it before passing the encoded string and MIME type to the ViewModel. The ViewModel never holds a `Uri` (not lifecycle-safe); it receives only the plain string.

**Rationale**: `Uri` references are tied to Activity context; passing them into a ViewModel introduces lifecycle coupling. Base64 strings are plain data, safe to store in `StateFlow`, and directly usable in the LLM API call. The encoding step is done in a `LaunchedEffect` / coroutine on the IO dispatcher.

**Alternatives considered**:
- Store `Uri` in ViewModel and read it there — rejected: ViewModel must not hold context references (Android lifecycle violation).
- Save image to a temp file and pass the path — rejected: unnecessary disk I/O, complicates cleanup.

---

## Decision 6: Chat Screen as New Top-Level Navigation Route

**Decision**: `ChatExpenseScreen` is a new top-level route (`Screen.ChatExpense`). The existing `ExpenseListScreen` navigation bar gains a "Chat" action that navigates to this new route. It is NOT a tab inside the existing `ExpenseInputScreen`.

**Rationale**: The existing `ExpenseInputScreen` has its own `TabRow` for form-mode tabs (Text/NLP, Fuel, Maintenance, Extra). Adding a full chat UI as another tab would overload that screen and require significant layout changes. A dedicated screen is cleaner, matches the spec's "tab nuovo" intent as a primary navigation destination, and is independently dismissible.

**Alternatives considered**:
- Tab within `ExpenseInputScreen` — rejected: mixing a chat flow with a structured form tab row creates confusing UX and layout constraints.
- Bottom navigation bar (permanent) — considered but out of scope; existing nav uses push navigation with back stacks, not a persistent bottom bar.

---

## Decision 7: Offline Degradation for Photo Analysis

**Decision**: If the network is unavailable when the user sends a photo, the system immediately shows an error message in the chat and invites the user to type the expense manually instead. Text input always falls back to the local rule-based `ExpenseParser`. This is a documented Constitution Principle I partial exception, justified below.

**Rationale**: Local image analysis (on-device vision models) is not available on Android API 26+ without large model downloads (100 MB+), which is out of scope. The exception is narrow: only photo analysis requires network; the text path (which is the primary input) degrades gracefully to the local parser.

**Constitution I Exception Justification**: Vision analysis is an enhancement, not a core flow. The core expense-saving user journey (text → parse → follow-up → save) remains fully functional offline. This exception is scoped solely to the photo attachment path and MUST be documented in the plan's Complexity Tracking table.

---

## Decision 8: LlmPrompt Extensions for Chat

**Decision**: Add two new prompt templates alongside the existing `SYSTEM` / `userPrompt()`:
1. `CHAT_SYSTEM` — identical to `SYSTEM` but instructs the model to also accept and extract data from image descriptions.
2. `imageChatPrompt(mimeType)` — constructs the user-facing content for a vision request, describing the image role.

These live in the existing `LlmPrompt` object in `shared:network`, keeping all prompt logic centralised.

**Alternatives considered**:
- A separate `ChatLlmPrompt` object — rejected: duplicates the category/schema documentation already in `SYSTEM`; single source of truth is preferable.
