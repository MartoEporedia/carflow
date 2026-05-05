# Research: Vehicle Management & Mandatory Vehicle Association

## Decision 1 — Persistence for Last-Used Vehicle ID

**Decision**: Use `SharedPreferences` via a new `VehiclePreferences` wrapper class.

**Rationale**: The app already uses `EncryptedSharedPreferences` for `LlmSettings`
(app/src/main/java/com/carflow/app/data/settings/LlmSettings.kt). Adding plain
`SharedPreferences` for a non-sensitive preference (a vehicle UUID) is consistent
with the existing pattern and requires no new Gradle dependencies. DataStore
Preferences would need a new dependency (`androidx.datastore:datastore-preferences`)
and additional coroutine setup — disproportionate for storing a single String.

**Alternatives considered**:
- DataStore Preferences: cleaner coroutine API, but adds a dependency for minimal gain.
- Room table: overkill for a single scalar preference.
- In-memory ViewModel state: lost on process death.

---

## Decision 2 — Vehicle Selector UI Component

**Decision**: Use Material3 `ExposedDropdownMenuBox` / `DropdownMenu` for the
vehicle selector on the expense input screen.

**Rationale**: Material3 `ExposedDropdownMenuBox` is the canonical M3 pattern for
selection from a short list (1–10 vehicles is the expected range). It is already
available via the Material3 BOM in the project; no new dependency is needed. For
>10 vehicles a bottom-sheet picker would be better, but this is not an anticipated
scenario for a personal car-expense app.

**Alternatives considered**:
- Bottom sheet: more discoverable for long lists, but adds complexity for the
  common single-vehicle case.
- Radio button list: takes too much vertical space inline.

---

## Decision 3 — Removing the Auto-Create Vehicle Fallback

**Decision**: Delete `resolveVehicleId()` from `ExpenseInputViewModel` entirely.
Replace with a nullable `selectedVehicleId: StateFlow<String?>` that defaults to
the last-used vehicle id from `VehiclePreferences`, or `null` if none exists or
the last-used vehicle was deleted.

**Rationale**: The current `resolveVehicleId()` silently creates a phantom vehicle
named "Default Vehicle" when no vehicle exists, violating FR-006 and corrupting the
vehicle list. The replacement makes the invariant explicit: save functions must check
`selectedVehicleId != null` before proceeding; the save button is disabled when it
is null.

**Alternatives considered**:
- Keep the function but throw instead of auto-creating: still misleading; the UI
  must already block the save path.

---

## Decision 4 — Edit Vehicle Flow

**Decision**: Tap on a vehicle card in `VehicleScreen` opens an inline edit form
(same Card pattern as the existing add form), pre-filled with the vehicle's current
values. No separate screen or dialog is needed.

**Rationale**: The existing add form is already an inline Card that slides in at
the top of the list. Reusing the same pattern for editing avoids a new navigation
destination and is consistent with the current UX. The `VehicleViewModel` gains an
`editingVehicle: StateFlow<VehicleEntity?>` to track which vehicle (if any) is
being edited.

**Alternatives considered**:
- Separate detail screen: better for many fields, but 7 fields fit comfortably inline.
- Dialog: less space for field labels; harder to scroll on small screens.

---

## Decision 5 — Fuel-Type Selector in Vehicle Form

**Decision**: A `DropdownMenu`-based selector showing the same fuel-type labels
as the expense parser: Benzina (petrol), Diesel, Elettrico, Ibrido, GPL, Metano.
Internal values use the parser's canonical strings: "petrol", "diesel", "electric",
"hybrid", "lpg", "cng".

**Rationale**: The parser already defines these constants in `TokenExtractor`
(shared/parser). The vehicle's `fuelType` field uses the same string values.
Keeping label → value mapping in the UI layer (not the parser module) preserves
the Clean Architecture separation (Principle IV).

**Alternatives considered**:
- Free-text field: allows user errors and mismatches with parser values.
- Enum in shared module: would couple `app` UI to `shared:parser` internals.

---

## Decision 6 — Year Field Validation

**Decision**: Accept years in the range [1900, currentYear + 1] via a numeric
`OutlinedTextField` with `KeyboardType.Number`. Validation fires on field blur;
out-of-range values show an inline error label.

**Rationale**: This covers all plausible production vehicles (oldest collectibles)
and allows pre-registering a car ordered but not yet delivered (currentYear + 1).
The field is optional — an empty value is stored as `null` in `VehicleEntity.year`.

---

## Decision 7 — No Schema Migration Required

**Decision**: The `VehicleEntity` and `ExpenseEntity` schemas are unchanged.
`VehicleEntity` already has `year` (nullable Int) and `fuelType` (String with
default ""). `ExpenseEntity.vehicleId` is already a non-nullable String — the
auto-create workaround in the ViewModel was the only source of phantom vehicleIds.

**Rationale**: No Room migration script is needed, which avoids version bumps and
migration test overhead.
