# Feature Specification: Expense Input — NLP & Classic Form

**Feature Branch**: `001-expense-input-ux`
**Created**: 2026-04-28
**Status**: Draft
**Input**: User description: "Controlla che l'applicazione rispecchi i principi, deve diventare user friendly. L'inserimento di una spesa va fatta tramite form classico o tramite NLP"

## User Scenarios & Testing *(mandatory)*

### User Story 1 — NLP Input with Preview & Confirm (Priority: P1)

The user types a free-text description of an expense (in Italian or English), the app
parses it and shows a preview card with the recognised fields. The user can correct any
field inline before confirming and saving the expense.

**Why this priority**: NLP input is the core differentiator of CarFlow (Constitution
Principle II). It must work reliably for any expense category, not just fuel.

**Independent Test**: Open the app → tap "Nuova Spesa" → type "benzina 50€ 30L" →
verify a preview card appears showing category Fuel, amount €50, quantity 30 L →
tap "Conferma e Salva" → verify expense appears in the expense list.

**Acceptance Scenarios**:

1. **Given** the user is on the "Nuova Spesa" screen, **When** they type "tagliando 200€"
   and tap "Analizza", **Then** a preview card appears showing category Maintenance,
   amount €200, and a "Conferma e Salva" button.
2. **Given** a preview card is shown, **When** the user changes the amount to €180,
   **Then** the updated amount is reflected before saving.
3. **Given** a preview card is shown, **When** the user taps "Conferma e Salva",
   **Then** the expense is saved and the input field is cleared with a confirmation
   message.
4. **Given** the NLP parser cannot recognise the input, **When** the user taps
   "Analizza", **Then** the app shows a clear error message and does not show a
   broken preview card.

---

### User Story 2 — Classic Form for All Expense Categories (Priority: P2)

The user can insert any expense (fuel, maintenance, extra) through a structured form
that adapts its fields based on the chosen category.

**Why this priority**: The current form only supports fuel. All other categories
(maintenance, extras) can only be added via NLP, blocking users who prefer structured
input. This is a usability gap.

**Independent Test**: Select the form tab → choose category "Manutenzione" → fill in
amount €150 and description "Cambio olio" → tap "Salva" → verify expense appears
in the expense list under Maintenance.

**Acceptance Scenarios**:

1. **Given** the user is on the form tab, **When** they select "Carburante" as
   category, **Then** fuel-specific fields appear (tipo carburante, litri, prezzo al
   litro, km, pieno completo, distributore).
2. **Given** the user selects "Manutenzione", **When** they fill in amount and
   description, **Then** only maintenance-relevant fields are shown and the expense
   can be saved.
3. **Given** the user selects "Extra" (insurance, fine, etc.), **When** they fill in
   amount and description, **Then** only generic fields are shown and the expense
   can be saved.
4. **Given** the user leaves the mandatory amount field empty, **When** they tap
   "Salva", **Then** the button stays disabled and an inline validation hint is
   shown next to the empty field.
5. **Given** two of the three fuel fields (total price, price/litre, litres) are
   filled, **When** the user moves focus away, **Then** the third field is
   auto-calculated and shown as a read-only derived value.

---

### User Story 3 — Clear Feedback on Save and Error States (Priority: P3)

After saving (from either input method), the user receives unambiguous confirmation.
If saving fails, a clear actionable message is shown.

**Why this priority**: Current feedback uses a Snackbar only on the NLP path and
a simple text label on the form path; inconsistency hurts trust.

**Independent Test**: Save an expense via each method and verify a consistent
success message appears in both cases. Simulate an offline save (no network needed
for local storage) and verify no silent failure occurs.

**Acceptance Scenarios**:

1. **Given** an expense is saved successfully via NLP, **When** the save completes,
   **Then** a dismissible success banner appears and the input is reset.
2. **Given** an expense is saved successfully via form, **When** the save completes,
   **Then** the same dismissible success banner appears and the form is reset.
3. **Given** saving fails for any reason, **When** the failure occurs, **Then** an
   error message with a "Riprova" option is shown; no data is silently lost.

---

### Edge Cases

- What happens when the user switches tabs mid-entry? (pending input should not be lost)
- How does the form handle decimal separators in different locales (comma vs dot)?
- What if the NLP parser returns a low-confidence result? (parser warnings must be surfaced)
- What if the user submits the NLP field while a previous parse is still running?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The expense input screen MUST offer two modes: NLP text input and
  classic structured form, accessible via clearly labelled tabs.
- **FR-002**: The NLP input MUST support all expense categories (Fuel, Maintenance,
  Extra), not only fuel.
- **FR-003**: After NLP parsing, the user MUST be able to edit any recognised field
  in the preview card before confirming.
- **FR-004**: The classic form MUST present a category selector as the first step;
  the visible fields MUST adapt to the selected category.
- **FR-005**: The Fuel form MUST auto-calculate the missing value when exactly two
  of (total price, price per litre, litres) are provided.
- **FR-006**: Mandatory fields (at minimum: amount and category) MUST be validated
  inline; the save button MUST remain disabled until validation passes.
- **FR-007**: Both input modes MUST show the same style of success confirmation
  after a successful save and reset to an empty state.
- **FR-008**: Any parsing or save error MUST be surfaced to the user with an
  actionable message; silent failures are not allowed.
- **FR-009**: Switching between NLP and form tabs MUST preserve in-progress input
  in each tab until the user explicitly clears or saves.

### Key Entities

- **Expense**: Amount (mandatory), category (mandatory), description, date,
  quantity, quantityUnit, vehicleId. Fuel expenses additionally carry: fuelType,
  pricePerLiter, isFullTank, gasStationName, gasStationLocation, odometerKm.
- **ParsedExpense**: The intermediate representation produced by the NLP parser;
  contains the same fields as Expense plus confidence warnings.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A first-time user can successfully save a fuel expense via NLP in
  under 30 seconds from opening the input screen.
- **SC-002**: A first-time user can successfully save a maintenance expense via the
  classic form in under 60 seconds.
- **SC-003**: 100% of save operations produce a visible confirmation or error
  message — no silent outcomes.
- **SC-004**: All three expense categories (Fuel, Maintenance, Extra) are
  reachable and saveable via both input modes.
- **SC-005**: Parser warning messages (low confidence, ambiguous input) are
  displayed in 100% of cases where the parser emits them.

## Assumptions

- The app is used in Italian as the primary locale; English NLP input is supported
  as a secondary language (consistent with the existing parser).
- Vehicle selection is out of scope for this feature: the app continues to use the
  first available vehicle or auto-creates a default one (existing behaviour).
- Multi-currency support is out of scope; all amounts are in euros.
- The date defaults to today; the user can override it via a date picker (already
  present in the form).
- The NLP model (local regex parser or LLM, depending on settings) is already
  integrated; this feature improves the UX layer, not the parser itself.
