# Quickstart: Smart Expense Chat Input

**Feature**: 003-smart-expense-chat | **Date**: 2026-04-30

This document describes the end-to-end integration scenarios used to validate the feature. Each scenario maps to a User Story in the spec and drives the acceptance test criteria.

---

## Scenario 1: Text-only expense — all fields in one message

**User Story**: P1 — Text-Based Chat Expense Entry

**Setup**: At least one vehicle registered ("Fiat Panda").

**Flow**:
1. User navigates to the Chat tab.
2. User types: `"50 euro benzina oggi da ENI, 35 litri"` and taps Send.
3. System shows `Processing` indicator.
4. System responds: `"Ho trovato: Carburante, €50.00, 35L, oggi. A quale veicolo associo la spesa?"`
5. System shows suggestion chip: `"Fiat Panda"`.
6. User taps the chip.
7. System shows `ExpenseConfirmationCard` with all fields pre-filled.
8. User taps Save.
9. System shows success message. Chat resets.

**Validation**: The saved expense appears in the expense list with correct amount, category FUEL, vehicle "Fiat Panda", today's date, 35 litres.

---

## Scenario 2: Incomplete text — multiple follow-up questions

**User Story**: P3 — Guided Follow-Up for Missing Information

**Setup**: Two vehicles registered ("Fiat Panda", "Tesla Model 3").

**Flow**:
1. User types: `"spesa auto"` and taps Send.
2. System responds: `"Quanto hai speso? (inserisci l'importo in €)"`.
3. User types: `"120"` and taps Send.
4. System responds: `"Che tipo di spesa è? Carburante, Manutenzione o Extra?"` with suggestion chips.
5. User taps `"Manutenzione"`.
6. System responds: `"A quale veicolo?"` with chips `["Fiat Panda", "Tesla Model 3"]`.
7. User taps `"Fiat Panda"`.
8. System shows `ExpenseConfirmationCard` (date defaults to today).
9. User taps Save.

**Validation**: Saved expense: €120, MAINTENANCE, "Fiat Panda", today.

---

## Scenario 3: Photo receipt — happy path

**User Story**: P2 — Photo-Based Expense Entry

**Setup**: One vehicle registered. A clear photo of a petrol receipt (€55.40 total, ENI, today).

**Flow**:
1. User taps the attachment icon.
2. User selects a photo from gallery.
3. System shows the image as a user message and a `Processing` indicator.
4. System responds: `"Ho letto dallo scontrino: Carburante, €55.40, oggi, ENI. A quale veicolo?"`.
5. User picks the vehicle from the chip.
6. System shows `ExpenseConfirmationCard`.
7. User taps Save.

**Validation**: Saved expense matches the receipt data. No manual field entry required.

---

## Scenario 4: Unreadable photo — graceful fallback

**User Story**: P2 edge case.

**Flow**:
1. User attaches a blurry photo.
2. System responds: `"Non sono riuscito a leggere l'immagine. Puoi descrivere la spesa a parole?"`.
3. User types the expense manually.
4. Normal follow-up flow continues.

**Validation**: Conversation continues without crashing; expense is eventually saved via text.

---

## Scenario 5: No vehicles registered

**User Story**: All stories — vehicle guard.

**Setup**: No vehicles in the database.

**Flow**:
1. User opens Chat tab.
2. User types any expense message.
3. System responds: `"Non hai ancora aggiunto nessun veicolo. Vai alla sezione Veicoli per aggiungerne uno."` with a CTA button.
4. User taps CTA → navigates to Vehicle screen.

**Validation**: No expense is created. Navigation to Vehicle screen is functional.

---

## Scenario 6: Offline — photo fails, text succeeds

**User Story**: P2 edge case + Constitution Principle I degradation.

**Setup**: Airplane mode on.

**Flow** (photo):
1. User attaches a photo while offline.
2. System responds: `"Analisi immagine non disponibile offline. Descrivi la spesa a parole."`.

**Flow** (text, continued):
3. User types `"tagliando 180 euro"`.
4. System uses local rule-based parser (no network required), extracts MAINTENANCE, €180.
5. Normal follow-up resumes (vehicle selection, confirmation).
6. Expense is saved locally.

**Validation**: Text-to-save works without network. Photo path shows a clear error, not a crash.
