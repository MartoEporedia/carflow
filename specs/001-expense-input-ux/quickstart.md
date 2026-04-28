# Quickstart: Expense Input UX — Build & Validate

**Feature**: 001-expense-input-ux
**Date**: 2026-04-28

---

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 34 (Android 14)
- A running Android emulator or device (API 26+)

---

## Build

```bash
# Full debug build
./gradlew assembleDebug

# Build only the parser library (fast check — no Android toolchain needed)
./gradlew :shared:parser:build
```

---

## Run Tests

```bash
# Parser unit tests (JVM — fast, no emulator)
./gradlew :shared:parser:test

# App unit tests (JVM)
./gradlew :app:test

# Instrumented tests (requires connected emulator/device)
./gradlew :app:connectedAndroidTest
```

---

## Manual Validation Checklist (User Story 1 — NLP)

1. Launch the app and tap the FAB / "+" to open "Nuova Spesa".
2. Confirm the "Parser NLP" tab is selected by default.
3. Type `benzina 50€ 30L` → tap "Analizza".
   - ✅ Preview card shows: category = Carburante, amount = €50.00, quantity = 30 L.
4. Change the amount field in the card to `45` → tap "Conferma e Salva".
   - ✅ Success banner "Spesa salvata!" appears and auto-dismisses.
   - ✅ Input field is cleared.
5. Navigate to the expense list and confirm the entry appears with amount €45.
6. Type `pippo` (unrecognisable) → tap "Analizza".
   - ✅ Error message is shown; no broken preview card appears.

## Manual Validation Checklist (User Story 2 — Form)

1. On "Nuova Spesa", tap the "Form Manuale" tab.
2. Confirm the category selector shows "Carburante" by default.
3. Select "Manutenzione" → confirm fuel-specific fields disappear.
4. Fill in amount = `150`, description = `Cambio olio` → tap "Salva manutenzione".
   - ✅ Success banner appears; form resets.
5. Navigate to expense list and confirm the maintenance entry.
6. Return to form tab — confirm the form is empty (reset after save).
7. Leave amount blank on any category → confirm "Salva" button is disabled.

## Manual Validation Checklist (User Story 3 — Feedback)

1. Save via NLP → confirm banner style matches save via form.
2. Switch tabs mid-entry (NLP: type some text; switch to form; switch back).
   - ✅ NLP input text is preserved.
   - ✅ Form fields are preserved.

---

## Constitution Compliance Check

| Principle | Status | Evidence |
|-----------|--------|---------|
| I. Offline-First | ✅ | All saves go through Room; no network calls |
| II. NLP-Powered Input | ✅ | NLP tab is default (index 0); form is secondary |
| III. Test-First | ✅ | Parser tests must pass before ViewModel changes land |
| IV. Multi-Module Clean Architecture | ✅ | `app` calls only `ExpenseParser.parse()` and `ParsedExpense` |
| V. Reactive Architecture | ✅ | All state in ViewModel as StateFlow; no direct DAO refs from UI |
