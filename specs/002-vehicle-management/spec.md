# Feature Specification: Vehicle Management & Mandatory Vehicle Association

**Feature Branch**: `002-vehicle-management`
**Created**: 2026-04-29
**Status**: Draft
**Input**: User description: "Migliora tutta la parte di gestione delle auto, con dati base e fai in modo che venga sempre associata una spesa ad un veicolo."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Vehicle CRUD with Complete Base Data (Priority: P1)

The user can create, view, edit, and delete vehicles. Each vehicle captures all
base identifying fields: nickname, make, model, year, licence plate, default fuel
type, and initial odometer reading. All fields except nickname are optional.

**Why this priority**: Without a proper vehicle registry, the vehicle-association
requirement (P2) cannot be enforced. This is the foundation for everything else.

**Independent Test**: Open the Vehicles screen → tap "+" → fill in name "Fiat 500",
make "Fiat", model "500", year 2020, plate "AB123CD", fuel type "Benzina", km 45000
→ save → verify the vehicle card shows all entered values. Tap the card → verify
an edit form opens pre-filled → change the km to 46000 → save → verify the updated
value appears.

**Acceptance Scenarios**:

1. **Given** the Vehicles screen is open, **When** the user taps "+" and fills in
   only the nickname field, **Then** the vehicle is saved and appears in the list.
2. **Given** the add-vehicle form is open, **When** the user fills in make, model,
   year, plate, fuel type, and odometer, **Then** all values are persisted and
   displayed on the vehicle card.
3. **Given** a vehicle exists, **When** the user opens its detail and changes any
   field, **Then** the updated values are saved and reflected immediately.
4. **Given** a vehicle exists with no linked expenses, **When** the user deletes it,
   **Then** it is removed from the list without a confirmation dialog.
5. **Given** a vehicle exists with linked expenses, **When** the user attempts to
   delete it, **Then** a confirmation dialog warns that its expenses will also be
   hidden, and the user must explicitly confirm.

---

### User Story 2 — Mandatory Vehicle Selection on Every Expense (Priority: P2)

Whenever the user saves an expense (via NLP or classic form), they must select the
target vehicle. There is no auto-selection or auto-creation of a default vehicle.
If no vehicle exists, the user is guided to create one before the expense can be saved.

**Why this priority**: Every expense MUST be traceable to a specific vehicle for
per-vehicle statistics, cost-per-km calculations, and fuel efficiency reports.
The existing "auto-create default" fallback silently bypasses this invariant.

**Independent Test**: Delete all vehicles → go to Expense Input → type "benzina 50€"
→ tap "Analizza" → verify a banner appears saying "Nessun veicolo registrato" with a
CTA to create one → create a vehicle → verify the expense input screen now shows a
vehicle selector → select the vehicle → confirm → verify the saved expense is linked
to that vehicle in the expense list.

**Acceptance Scenarios**:

1. **Given** at least one vehicle exists, **When** the user opens the expense input
   screen, **Then** a vehicle selector is visible showing the last-used vehicle
   pre-selected.
2. **Given** multiple vehicles exist, **When** the user taps the vehicle selector,
   **Then** a picker shows all active vehicles and the user can choose one.
3. **Given** no vehicle exists, **When** the user reaches the expense input screen,
   **Then** the save button is disabled and a clear call-to-action to add a vehicle
   is shown.
4. **Given** the user has selected a vehicle and fills in an expense, **When** they
   save it, **Then** the expense is stored with the selected vehicleId.
5. **Given** an NLP parse produces a result, **When** the preview card is shown,
   **Then** the selected vehicle is displayed on the card and included in the save
   payload.

---

### User Story 3 — Active Vehicle Indicator & Quick Switch (Priority: P3)

A persistent "active vehicle" concept lets users switch the current vehicle once
and have all subsequent expenses automatically pre-filled with it, avoiding
repeated selection.

**Why this priority**: Users who own one vehicle (the common case) should not be
forced to pick from a dropdown on every expense. Stickiness of the last-used
vehicle reduces friction without violating the mandatory-vehicle invariant.

**Independent Test**: Add two vehicles (A and B) → save an expense for vehicle A
→ open a new expense → verify vehicle A is still pre-selected → switch to B →
save → open another new expense → verify vehicle B is now pre-selected.

**Acceptance Scenarios**:

1. **Given** the user saves an expense for vehicle X, **When** the next expense
   input screen opens, **Then** vehicle X is pre-selected.
2. **Given** the pre-selected vehicle is deleted, **When** the user opens the
   expense input screen, **Then** the selector shows no pre-selection and the save
   button is disabled until a vehicle is chosen.

---

### Edge Cases

- What happens if the user starts typing an expense and then navigates to add a
  vehicle? (in-progress expense input must survive navigation and resume with the
  new vehicle available in the selector)
- What if two vehicles share the same licence plate? (warn but allow — duplicates
  may be legitimate in edge cases)
- What if the user removes the year field while editing a vehicle? (allowed; year
  is optional)
- What if the device has no vehicles and the user deep-links directly to expense
  input? (must show the no-vehicle banner)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The vehicle management screen MUST support Create, Read, Update, and
  Delete operations for vehicles.
- **FR-002**: A vehicle MUST capture at minimum: nickname (mandatory), make, model,
  year, licence plate, default fuel type, and initial odometer reading.
- **FR-003**: The vehicle add/edit form MUST include a fuel-type selector with the
  same fuel types supported by the expense parser (petrol, diesel, electric, hybrid,
  LPG, CNG).
- **FR-004**: The vehicle add/edit form MUST include a year field that accepts only
  plausible years (1900 – current year + 1).
- **FR-005**: The vehicle list MUST display nickname, make+model, licence plate, and
  current odometer for each entry at a glance.
- **FR-006**: Every expense save operation — via NLP and via classic form — MUST
  require an explicit vehicleId; no default or auto-created vehicle is permitted.
- **FR-007**: The expense input screen MUST display a vehicle selector; it MUST
  pre-select the last-used vehicle when one exists.
- **FR-008**: When no vehicle exists, the expense input screen MUST disable the save
  button and show a prominent call-to-action to create a vehicle.
- **FR-009**: The last-used vehicle MUST be persisted across app restarts.
- **FR-010**: Deleting a vehicle that has linked expenses MUST require an explicit
  confirmation step; soft-deletion MUST be used (existing `isDeleted` flag).
- **FR-011**: The vehicle selector on the expense screen MUST list only non-deleted
  vehicles.

### Key Entities

- **Vehicle**: nickname (mandatory), make, model, year (optional), licensePlate,
  defaultFuelType, odometerKm, createdAt, updatedAt, isDeleted.
- **Expense**: vehicleId (mandatory, non-nullable, no default). All other fields as
  currently defined.
- **LastUsedVehicle**: A persisted preference (device-local) storing the id of the
  most recently used vehicle for expense pre-selection.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can create a fully-detailed vehicle (all 7 fields) in under
  60 seconds.
- **SC-002**: 100% of expenses saved through the app carry a valid, non-null
  vehicleId — zero "orphan" expenses after this feature ships.
- **SC-003**: A user with a single vehicle can save a new expense with zero
  additional taps for vehicle selection (pre-selection covers it).
- **SC-004**: Switching from one vehicle to another takes at most 2 taps from the
  expense input screen.
- **SC-005**: The vehicle list reflects create, update, and delete operations within
  1 second of the action completing (reactive update).

## Assumptions

- Multi-vehicle support is in scope; the app must handle 1–N vehicles gracefully.
- The "active vehicle" preference is stored locally on the device; no cloud sync
  is needed for this feature.
- Existing expenses with an empty vehicleId (if any) are out of scope for
  migration; they will remain as-is.
- The fuel-type list is the same as the one already defined in the parser:
  petrol, diesel, electric, hybrid, LPG, CNG.
- A vehicle's odometer is updated only manually by the user; automatic odometer
  tracking from expense entries is out of scope for this feature.
- The VehicleScreen is already accessible from the main navigation; no new
  navigation entry points are added by this feature.
