# Feature Specification: Smart Expense Chat Input

**Feature Branch**: `003-smart-expense-chat`
**Created**: 2026-04-30
**Status**: Draft
**Input**: User description: "Voglio che il modo per aggiungere una spesa sia più smart possibile, anche tramite foto di una fattura, scontrino o simili. L'app in ogni caso dovrebbe chiedere le info aggiuntive che non ho mandato. Il tutto in un tab nuovo con una specie di chat (va benissimo asincrona, senza websocket o cose complesse)"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Text-Based Chat Expense Entry (Priority: P1)

A user wants to log an expense by typing a natural-language message in the chat tab. They might write something like "50 euro di benzina oggi da ENI, 35 litri" or "tagliando fatto ieri, 180€". The system reads their message, extracts what it can (amount, category, date, fuel type, etc.), and then asks one follow-up question at a time for each piece of required information it could not determine. Once all required data is collected, the user confirms and the expense is saved.

**Why this priority**: This is the core differentiating interaction — a chat-first input flow that is faster and more natural than filling out a form. It unlocks the value of the feature with zero dependency on camera or image processing.

**Independent Test**: Can be fully tested by opening the chat tab, typing a textual expense description, answering the follow-up questions, and verifying the saved expense appears in the app's expense list.

**Acceptance Scenarios**:

1. **Given** the user is on the Chat tab and has at least one vehicle registered, **When** they type "benzina 50 euro" and send, **Then** the system replies with the extracted data and asks which vehicle to assign the expense to.
2. **Given** the system has asked "Which vehicle?", **When** the user replies with the vehicle name, **Then** the system confirms the complete expense details and offers a "Save" action.
3. **Given** the user confirms, **When** they tap "Save", **Then** the expense is stored and the chat shows a success message.
4. **Given** the system extracts date, amount, and category from the user's message, **When** no required fields are missing, **Then** the system moves directly to confirmation without any follow-up questions.
5. **Given** the user sends a message that cannot be parsed as an expense at all, **When** the system cannot extract an amount or category, **Then** it replies explaining it didn't understand and asks the user to rephrase or provide more detail.

---

### User Story 2 - Photo-Based Expense Entry (Priority: P2)

A user takes a photo (or selects one from their gallery) of a petrol receipt, an invoice, or a service ticket and sends it in the chat. The system analyses the image, extracts recognisable fields (total amount, date, vendor name, items), and continues the same follow-up conversation to fill in anything it could not determine from the image. The result is the same confirmed-and-saved expense.

**Why this priority**: Photo input eliminates manual re-typing of printed figures, reducing errors and time. It requires the text-entry flow (P1) to work first, since the follow-up question mechanism is shared.

**Independent Test**: Can be tested by attaching a photo of any receipt in the chat, verifying the system extracts at least the total amount, answering any follow-up questions, and confirming the saved expense.

**Acceptance Scenarios**:

1. **Given** the user taps the attachment button and selects a petrol receipt photo, **When** the photo is sent, **Then** the system shows a processing indicator and then replies with the data it extracted (amount, date, vendor if legible).
2. **Given** the extracted data is missing the expense category, **When** the system presents what it found, **Then** it asks the user to confirm or specify the category before proceeding.
3. **Given** the photo is too blurry or unreadable, **When** the system cannot extract any useful data, **Then** it notifies the user that the image was not readable and invites them to type the details manually or retry with a clearer photo.
4. **Given** the extracted amount from the photo differs from what the user typed in the same message, **When** the system detects the discrepancy, **Then** it presents both values and asks the user which one to use.

---

### User Story 3 - Guided Follow-Up for Missing Information (Priority: P3)

Regardless of whether input came from text or photo, the system systematically identifies which required fields are still missing after extraction and asks for them one question at a time in a conversational manner. The user never has to know which fields are required — the system manages the conversation until the expense is complete.

**Why this priority**: This is the "smart" aspect of the feature. Without it, the user would still need to notice and fill in gaps manually. It builds on P1 and P2.

**Independent Test**: Can be tested by sending an intentionally incomplete expense description (e.g., only the amount, with no date, category, or vehicle) and verifying the system asks for each missing piece sequentially, one at a time.

**Acceptance Scenarios**:

1. **Given** only the amount was recognised, **When** the system prepares follow-up questions, **Then** it asks for the category first (highest-value missing field), then vehicle, then date (defaulting to today if the user does not answer).
2. **Given** the system has asked a follow-up question, **When** the user responds with a valid answer, **Then** the system acknowledges it, updates the in-progress expense, and either asks the next question or moves to confirmation.
3. **Given** the user's answer to a follow-up question is ambiguous, **When** the system cannot resolve it, **Then** it rephrases the question and provides explicit options (e.g., a list of registered vehicles).
4. **Given** the user has answered all required questions, **When** the confirmation step is shown, **Then** all collected data is displayed in a structured summary before the user taps "Save".

---

### Edge Cases

- What happens when the user sends a photo that is not a receipt (e.g., a selfie)? The system gracefully reports it found no expense data and prompts the user to try again.
- What happens if the user abandons the conversation mid-flow (navigates away)? The partial conversation is discarded; no incomplete expense is saved.
- What happens if no vehicles are registered? The system informs the user they must add a vehicle first and provides a shortcut to the Vehicle screen.
- What happens if the user sends multiple separate amounts in one message? The system asks the user to confirm which amount represents the total expense, or to split them into separate messages.
- What happens if there is no internet connection when a photo is sent? The system informs the user that image analysis is unavailable offline and falls back to manual text entry.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The app MUST provide a dedicated "Chat" tab accessible from the main navigation bar, separate from the existing expense input tabs.
- **FR-002**: Users MUST be able to send a free-text message in the chat to describe an expense.
- **FR-003**: Users MUST be able to attach a single photo (taken with the camera or selected from the gallery) per message to represent a receipt, invoice, or ticket.
- **FR-004**: The system MUST attempt to extract expense fields (amount, category, date, fuel type, quantity, vendor) from each user message or photo.
- **FR-005**: After extraction, the system MUST identify which required fields (amount, category, vehicle) are still missing.
- **FR-006**: The system MUST ask the user one follow-up question at a time for each missing required field, in order of importance (amount → category → vehicle → date).
- **FR-007**: The system MUST accept the user's plain-text reply to each follow-up question and use it to populate the corresponding field.
- **FR-008**: When the user's reply to a follow-up is ambiguous, the system MUST rephrase the question and offer a concrete list of options (e.g., registered vehicles, expense categories).
- **FR-009**: Date MUST default to today if the user never provides one and does not contradict the default.
- **FR-010**: Before saving, the system MUST display a structured confirmation summary of all collected expense fields for the user to review.
- **FR-011**: Users MUST be able to edit any field in the confirmation summary before saving.
- **FR-012**: The system MUST save the confirmed expense with the associated vehicle to the app database.
- **FR-013**: The chat tab MUST show a processing/loading indicator while the system analyses a message or photo.
- **FR-014**: The chat tab MUST display the conversation history within the current session (messages are cleared when the user leaves the tab or after a successful save).
- **FR-015**: If no vehicles are registered, the system MUST block saving and provide a direct navigation path to the Vehicle management screen.

### Key Entities

- **ChatMessage**: A single turn in the conversation. Has a sender role (user or system), a content type (text, image, system-prompt, or confirmation-summary), a timestamp, and optional structured expense data attached by the system.
- **ConversationSession**: The active in-progress conversation. Holds the ordered list of messages, the partially-built expense data collected so far, the list of fields still needed, and the current conversation state (collecting → confirming → saved → abandoned).
- **ExtractedExpenseData**: The partial or complete set of expense fields the system has gathered across the conversation — mirrors the fields of an expense record (amount, category, date, vehicle, fuel type, quantity, vendor, description).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can log a typical fuel expense (all key details provided in one text message) in under 60 seconds from opening the chat tab to seeing the success confirmation.
- **SC-002**: The system correctly extracts amount and category from at least 85% of clearly written Italian or English expense descriptions.
- **SC-003**: The system correctly extracts at least the total amount from at least 75% of clear, well-lit receipt photos.
- **SC-004**: The system requires no more than 3 follow-up questions per expense entry for messages that include at least the amount and a recognisable category.
- **SC-005**: At least 80% of users who start a chat expense entry successfully complete and save the expense within the same session.
- **SC-006**: The system never silently drops a user message — every input receives a visible response within a reasonable waiting time.

## Assumptions

- The app already has network infrastructure for communicating with an AI/LLM backend; photo analysis will use the same channel.
- Image recognition quality depends on the quality of the backend service; the app's responsibility is to send the image and handle the response.
- Chat history is ephemeral: it is stored only in memory for the duration of the session and is not persisted to the database.
- A "session" ends when the user navigates away from the chat tab, the app is backgrounded for more than 10 minutes, or a successful save occurs.
- Only one expense is processed per conversation session; users who want to log multiple expenses start a new conversation each time.
- The vehicle selector in the follow-up step presents the list of registered vehicles, consistent with the existing vehicle-association requirement.
- The feature is Italian-primary but the system should handle English input gracefully without explicit language-switching UI.
