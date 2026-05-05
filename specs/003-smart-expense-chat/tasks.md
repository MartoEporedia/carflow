# Tasks: Smart Expense Chat Input

**Input**: Design documents from `specs/003-smart-expense-chat/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ui-contracts.md ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create directory structure for the new chat feature module.

- [X] T001 Create directory structure `app/src/main/java/com/carflow/app/ui/screens/chat/components/` and `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/` (use `mkdir -p`)
- [X] T002 Create directory `app/src/test/java/com/carflow/app/ui/screens/chat/viewmodel/` for ViewModel unit tests

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models and shared-network extensions that ALL user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 Define all in-memory models in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatUiState.kt`: enums `MessageRole` (USER, SYSTEM), `ContentType` (TEXT, IMAGE, CONFIRMATION_SUMMARY), `RequiredField` (AMOUNT, CATEGORY, VEHICLE, DATE); data classes `ChatMessage(id, role, contentType, text, imageBase64, imageMimeType, timestamp)` and `DraftExpense(amount, category, vehicleId, date, description, fuelType, liters, pricePerLiter, warnings)`; sealed class `ConversationState` with variants `Idle`, `Processing`, `AwaitingAnswer(field: RequiredField, options: List<String>?)`, `Confirming(draft: DraftExpense)`, `Saved`; data class `ChatUiState(messages, conversationState, inputText, isAttachEnabled, errorMessage)`

- [X] T004 Extend `LlmClient` interface in `shared/network/src/main/kotlin/com/carflow/network/llm/LlmClient.kt` by adding: `suspend fun chatWithImage(systemPrompt: String, userPrompt: String, imageBase64: String, mimeType: String): String` alongside the existing `chat()` method

- [X] T005 [P] Implement `chatWithImage()` in `shared/network/src/main/kotlin/com/carflow/network/client/DirectLlmClient.kt`: build an OpenAI-compatible multimodal request body where the user message `content` is a JSON array containing `{"type":"image_url","image_url":{"url":"data:<mimeType>;base64,<imageBase64>"}}` and `{"type":"text","text":"<userPrompt>"}`. Reuse existing Ktor HTTP client and auth headers. Return the response string.

- [X] T006 [P] Implement `chatWithImage()` in `shared/network/src/main/kotlin/com/carflow/network/client/ProxyLlmClient.kt`: same multimodal request structure as T005, using the existing proxy endpoint and auth token. Return the response string.

- [X] T007 Add `CHAT_SYSTEM` constant and `imageChatPrompt(mimeType: String): String` function to `shared/network/src/main/kotlin/com/carflow/network/llm/LlmPrompt.kt`. `CHAT_SYSTEM` is identical to `SYSTEM` but with an additional instruction: "When the user message includes an image, extract expense data from the image content. Apply the same JSON schema and rules." `imageChatPrompt` returns: `"Analyse this ${mimeType.substringAfter('/')} image and extract any car expense data following the JSON schema."`

- [X] T008 Add `Screen.ChatExpense : Screen("chat_expense")` object to `app/src/main/java/com/carflow/app/ui/navigation/Navigation.kt` and register the composable in `CarFlowNavHost` with `onNavigateBack` and `onNavigateToVehicle` callbacks (use a placeholder `ChatExpenseScreen` stub — `TODO("implement in US1")` — so the project compiles before US1 tasks run)

**Checkpoint**: Foundation complete — `LlmClient` has `chatWithImage()`, all data models exist, new route registered. Project must compile cleanly.

---

## Phase 3: User Story 1 — Text-Based Chat Expense Entry (Priority: P1) 🎯 MVP

**Goal**: User types a natural-language expense description, system parses it via the existing LLM/parser pipeline, asks follow-up questions one at a time for missing required fields, shows a confirmation summary, and saves the expense.

**Independent Test**: Open the Chat screen, type "50 euro benzina oggi", answer the vehicle follow-up, tap Save. Verify the expense appears in the expense list with correct amount, category FUEL, and today's date.

### Implementation for User Story 1

- [X] T009 [US1] Implement `ChatExpenseViewModel` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: annotate with `@HiltViewModel`; inject `ExpenseParserStrategy` (existing LLM/parser), `ExpenseRepository`, `VehicleRepository`, `VehiclePreferences`; expose `uiState: StateFlow<ChatUiState>` and `vehicles: StateFlow<List<VehicleEntity>>`; implement `onTextChanged()`, `onSendText()` (triggers parse → state Idle→Processing→AwaitingAnswer or Confirming), `onFollowUpAnswer(answer)` (local resolution — advances state machine without LLM), `onDraftChanged()`, `onSaveConfirmed()` (calls `ExpenseRepository.insertExpense()` then state→Saved, resets to Idle after 1 s), `onDiscardConversation()` (resets to Idle); include `computeMissingFields(draft): List<RequiredField>` returning fields in priority order (AMOUNT→CATEGORY→VEHICLE, skip DATE if null — defaults to today at save time)

- [X] T010 [P] [US1] Implement `ChatBubble` composable in `app/src/main/java/com/carflow/app/ui/screens/chat/components/ChatBubble.kt`: render `USER TEXT` as right-aligned `Card` with `primaryContainer` background; render `SYSTEM TEXT` as left-aligned `Card` with `surfaceVariant` background; render `SYSTEM CONFIRMATION_SUMMARY` as a full-width container (placeholder — `ExpenseConfirmationCard` wired in T012); add 4 `@Preview` variants (user-text, system-text, system-confirmation-summary stub, system-text with long content)

- [X] T011 [P] [US1] Implement `ChatInputRow` composable in `app/src/main/java/com/carflow/app/ui/screens/chat/components/ChatInputRow.kt`: `OutlinedTextField` for text, Send `IconButton` (enabled when text not blank and `isEnabled`), Attach `IconButton` (always visible, disabled when `isEnabled == false`); props: `text, onTextChange, onSendClick, onAttachClick, isEnabled`; add 2 `@Preview` variants (enabled with text, disabled)

- [X] T012 [P] [US1] Implement `SuggestionChips` composable in `app/src/main/java/com/carflow/app/ui/screens/chat/components/SuggestionChips.kt`: horizontally scrollable `LazyRow` of `SuggestionChip` items; props: `options: List<String>, onOptionSelected: (String) -> Unit`; hidden/not shown when `options` is empty; add 1 `@Preview` with 3 options

- [X] T013 [US1] Implement `ExpenseConfirmationCard` composable in `app/src/main/java/com/carflow/app/ui/screens/chat/components/ExpenseConfirmationCard.kt` per the UI contract: show `OutlinedTextField` for amount and description; `ExposedDropdownMenuBox` for category (FUEL/MAINTENANCE/EXTRA) and vehicle (from `vehicles: List<VehicleEntity>`); `OutlinedTextField` + calendar icon for date (show `DatePickerDialog`); conditionally show fuel-type dropdown, liters field, and price-per-liter field only when `draft.category == FUEL`; Save button enabled when `amount != null && amount > 0 && category != null && vehicleId != null`; Discard `TextButton`; add 3 `@Preview` variants (complete FUEL draft, partial MAINTENANCE draft, no vehicle selected)

- [X] T014 [US1] Replace the stub in `ChatExpenseScreen` and implement the full `ChatExpenseScreen` composable in `app/src/main/java/com/carflow/app/ui/screens/chat/ChatExpenseScreen.kt`: collect `uiState` and `vehicles` from `ChatExpenseViewModel`; show `LazyColumn` of `ChatBubble` items (auto-scroll to bottom on list change); show `SuggestionChips` above input when `ConversationState` is `AwaitingAnswer` with non-empty options; show `CircularProgressIndicator` when `Processing`; show `ChatInputRow` at bottom (hidden when `Confirming` — show Save/Discard from `ExpenseConfirmationCard` instead); show `ExpenseConfirmationCard` as the last item when `Confirming`; show transient success `Snackbar` when `Saved`; add `@Preview` for each `ConversationState` variant (5 previews)

- [X] T015 [US1] Wire `onNavigateToChat` in `app/src/main/java/com/carflow/app/ui/screens/expense/ExpenseListScreen.kt`: add `onNavigateToChat: () -> Unit` parameter; add a "Chat" action (FAB or top-bar icon using `Icons.Filled.Chat`) that calls the callback; update `CarFlowNavHost` in Navigation.kt to pass `onNavigateToChat = { navController.navigate(Screen.ChatExpense.route) }` to `ExpenseListScreen`

**Checkpoint**: User Story 1 fully functional — text expense logging via chat works end-to-end. Test Scenario 1 from quickstart.md passes.

---

## Phase 4: User Story 2 — Photo-Based Expense Entry (Priority: P2)

**Goal**: User attaches a photo of a receipt or invoice; the system analyses it via `chatWithImage()`, extracts expense data, and continues the same follow-up flow from US1.

**Independent Test**: Attach a clear receipt photo. Verify the system shows extracted amount and category in the chat, asks for the vehicle, and saves the expense with correct values.

### Implementation for User Story 2

- [X] T016 [US2] Add image attachment handling to `app/src/main/java/com/carflow/app/ui/screens/chat/ChatExpenseScreen.kt`: register two `ActivityResultLauncher`s using `rememberLauncherForActivityResult` — one for `ActivityResultContracts.TakePicture()` (camera) and one for `ActivityResultContracts.GetContent()` (gallery, accepts `"image/*"`); on result, read the `Uri` via `ContentResolver.openInputStream()` on the IO dispatcher, Base64-encode the bytes, detect MIME type, call `viewModel.onImageSelected(base64, mimeType)`; enable the Attach `IconButton` in `ChatInputRow` (show a `DropdownMenu` or `ModalBottomSheet` with "Camera" and "Gallery" options); request `CAMERA` runtime permission before launching the camera launcher

- [X] T017 [US2] Implement `onImageSelected(base64: String, mimeType: String)` in `ChatExpenseViewModel` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: append a `ChatMessage(role=USER, contentType=IMAGE, imageBase64=base64)` to the message list; transition to `Processing`; call `chatWithImage(LlmPrompt.CHAT_SYSTEM, LlmPrompt.imageChatPrompt(mimeType), base64, mimeType)` on the IO dispatcher; parse the response with the same `parseLlmResponse()` logic used for text; build `DraftExpense` from result; call `computeMissingFields()` and advance the state machine (same as text path)

- [X] T018 [US2] Add offline detection to `ChatExpenseViewModel` image path in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: catch network exceptions in `onImageSelected()` and transition to `AwaitingAnswer` state with a system message: "Analisi immagine non disponibile. Puoi descrivere la spesa a parole?"; do NOT set `errorMessage` (show it conversationally as a system chat bubble, not a transient error)

- [X] T019 [P] [US2] Add image thumbnail rendering to `ChatBubble` in `app/src/main/java/com/carflow/app/ui/screens/chat/components/ChatBubble.kt`: when `message.contentType == IMAGE`, decode `imageBase64` to a `Bitmap`, render it in a fixed-height (120 dp) `Image` composable with `contentScale = ContentScale.Crop` inside the right-aligned user bubble; add a loading skeleton while decoding; add 1 new `@Preview` for the image bubble

- [X] T020 [US2] Add `CAMERA` permission to `app/src/main/AndroidManifest.xml` under `<manifest>`: `<uses-permission android:name="android.permission.CAMERA" />`; add `<uses-feature android:name="android.hardware.camera" android:required="false" />` so the app is not filtered out on devices without a camera

**Checkpoint**: User Story 2 functional — Quickstart Scenario 3 (photo happy path) and Scenario 4 (unreadable photo) pass.

---

## Phase 5: User Story 3 — Guided Follow-Up for Missing Information (Priority: P3)

**Goal**: The system always identifies the next missing required field and asks for it exactly once with contextual options, in priority order, until the expense is complete. FUEL expenses get optional follow-up for liters and price-per-liter.

**Independent Test**: Send "spesa auto" (amount missing, category missing, vehicle missing). Verify the system asks for amount first, then category with chips, then vehicle with chips, then shows confirmation (date defaults to today).

### Implementation for User Story 3

- [X] T021 [US3] Refine `computeMissingFields()` in `ChatExpenseViewModel` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: ensure strict priority order AMOUNT→CATEGORY→VEHICLE→DATE; add FUEL-specific optional fields (LITERS, PRICE_PER_LITER) as separate `RequiredField` enum entries — extend the enum; only include them in the returned list when `draft.category == FUEL` and the respective field is null; mark them optional (the system asks but accepts "skip" or empty answer)

- [X] T022 [US3] Implement `buildFollowUpMessage(field: RequiredField, vehicles: List<VehicleEntity>): Pair<String, List<String>?>` in `ChatExpenseViewModel` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: returns the question text and the pre-filled options list. AMOUNT → ("Quanto hai speso? (€)", null). CATEGORY → ("Che tipo di spesa è?", listOf("Carburante", "Manutenzione", "Extra")). VEHICLE → ("A quale veicolo?", vehicles.map { it.name }). DATE → ("Che data? (lascia vuoto per oggi)", null). LITERS → ("Quanti litri? (opzionale)", null). PRICE_PER_LITER → ("Prezzo al litro? (opzionale)", null)

- [X] T023 [US3] Implement `resolveFollowUpAnswer(field: RequiredField, answer: String, draft: DraftExpense, vehicles: List<VehicleEntity>): DraftExpense` in `ChatExpenseViewModel` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: AMOUNT → `answer.replace(',', '.').toDoubleOrNull()?.let { draft.copy(amount = it) } ?: draft`. CATEGORY → map "Carburante"→FUEL, "Manutenzione"→MAINTENANCE, "Extra"→EXTRA (case-insensitive). VEHICLE → find `vehicles.firstOrNull { it.name.equals(answer, ignoreCase = true) }?.id`. DATE → parse with existing `DateParser` or default to today. LITERS / PRICE_PER_LITER → `answer.toDoubleOrNull()` (null if blank or invalid — optional fields)

- [X] T024 [US3] Add "no vehicles" guard in `ChatExpenseViewModel.onSendText()` and `onFollowUpAnswer()` in `app/src/main/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModel.kt`: after any attempt to resolve VEHICLE field — if `vehicles.value.isEmpty()` — emit a system message "Non hai ancora aggiunto nessun veicolo." and post a one-shot `navigateToVehicle` `SharedFlow` event; keep the draft intact so the user can return and continue

- [X] T025 [US3] Consume the `navigateToVehicle` event in `ChatExpenseScreen` in `app/src/main/java/com/carflow/app/ui/screens/chat/ChatExpenseScreen.kt`: collect it with `LaunchedEffect` and call `onNavigateToVehicle()`; this wires the "no vehicles" CTA correctly without coupling the ViewModel to navigation

**Checkpoint**: All 3 user stories functional. All 6 quickstart.md scenarios pass.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: ViewModel tests, preview coverage, minor UX polish.

- [X] T026 [P] Write unit tests for `ChatExpenseViewModel` in `app/src/test/java/com/carflow/app/ui/screens/chat/viewmodel/ChatExpenseViewModelTest.kt` using `kotlinx-coroutines-test` and `turbine`: test state transitions Idle→Processing→AwaitingAnswer(AMOUNT)→AwaitingAnswer(CATEGORY)→AwaitingAnswer(VEHICLE)→Confirming→Saved; test "no vehicles" guard emits navigateToVehicle event; test `onDiscardConversation()` resets to Idle; mock `ExpenseParserStrategy` and `VehicleRepository`

- [X] T027 [P] Add `@Preview` annotations for the `Idle` state of `ChatExpenseScreen` with an empty message list and for the `AwaitingAnswer` state with a system question and a `SuggestionChips` row visible

- [X] T028 Verify the `ExpenseInputViewModel` (existing) is not affected: the `ExpenseInputScreen` NLP tab and manual form tabs must still work correctly after the new `chatWithImage()` method is added to `LlmClient` — confirm `LlmExpenseParser` still compiles and its `parse()` method remains unchanged

- [ ] T029 [P] Run the quickstart.md manual validation checklist: execute all 6 scenarios on the device/emulator and confirm each passes before marking this phase done

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 — MVP; can be delivered alone
- **US2 (Phase 4)**: Depends on Phase 2 + Phase 3 (shares `ChatExpenseScreen`)
- **US3 (Phase 5)**: Depends on Phase 2 + Phase 3 (extends ViewModel); can start alongside US2
- **Polish (Phase 6)**: Depends on all story phases complete

### Within Each User Story

- T009 (ViewModel) must complete before T014 (Screen) — Screen collects from ViewModel
- T010, T011, T012 (components) can run in parallel once T003 (data models) is done
- T013 (ExpenseConfirmationCard) depends on T003 data models only — parallelisable with T010–T012
- T014 (Screen assembly) depends on T009–T013

### Parallel Opportunities

```
Phase 2 parallel group: T005, T006 (after T004 LlmClient interface change)
Phase 3 parallel group: T010, T011, T012, T013 (all components, after T003 + T009)
Phase 4 parallel group: T019 (ChatBubble image), T020 (manifest) — independent of T016–T018
Phase 5 tasks T021–T025 are sequential (each builds on the ViewModel method from the previous)
Phase 6 parallel group: T026, T027, T028, T029
```

---

## Parallel Example: Phase 3 (User Story 1) Components

```
# After T003 (data models) and T009 (ViewModel) are done, launch in parallel:
Task T010: ChatBubble composable
Task T011: ChatInputRow composable
Task T012: SuggestionChips composable
Task T013: ExpenseConfirmationCard composable

# Then, after all four complete:
Task T014: Assemble ChatExpenseScreen
Task T015: Wire navigation in ExpenseListScreen
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001–T002)
2. Complete Phase 2: Foundational (T003–T008)
3. Complete Phase 3: User Story 1 (T009–T015)
4. **STOP and VALIDATE**: Quickstart Scenario 1 and 2 pass → MVP shipped

### Incremental Delivery

1. Setup + Foundational → compiles cleanly, new route registered
2. US1 → text chat works end-to-end → MVP demo
3. US2 → photo attachment works → enhanced demo
4. US3 → full follow-up logic + no-vehicle guard → production-ready
5. Polish → tests + manual validation

---

## Notes

- [P] tasks = different files, no shared-state dependencies
- [Story] label maps each task to its user story for traceability
- `ConversationState` sealed class MUST be complete (T003) before any ViewModel or Screen work
- `chatWithImage()` interface change (T004) MUST be committed before T005/T006 start — both clients need the interface updated first
- The navigation stub in T008 keeps the project compiling during incremental US1 implementation
- Commit after each task group or phase checkpoint
