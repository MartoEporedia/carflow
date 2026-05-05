# Data Model: Vehicle Management & Mandatory Vehicle Association

## Entities

### VehicleEntity (unchanged schema)

```
Table: vehicles
─────────────────────────────────────────────────────────
Field          Type        Nullable   Notes
─────────────────────────────────────────────────────────
id             String      No         UUID, PrimaryKey
name           String      No         Nickname (mandatory)
make           String      No         Default ""
model          String      No         Default ""
year           Int         Yes        null if not set
licensePlate   String      No         Default ""
fuelType       String      No         Default ""; values: petrol|diesel|electric|hybrid|lpg|cng
odometerKm     Double      No         Default 0.0
createdAt      Long        No         millis since epoch
updatedAt      Long        No         millis since epoch
isDeleted      Boolean     No         Soft-delete flag; Default false
─────────────────────────────────────────────────────────
```

**No migration needed** — all fields already exist in the current schema.

---

### ExpenseEntity (unchanged schema)

```
Table: expenses
─────────────────────────────────────────────────────────
Field              Type        Nullable   Notes
─────────────────────────────────────────────────────────
id                 String      No         UUID, PrimaryKey
vehicleId          String      No         Must reference an active vehicle
category           String      No         FUEL | MAINTENANCE | EXTRA
...                (other fields unchanged)
─────────────────────────────────────────────────────────
```

**Invariant enforced in ViewModel** (not via DB foreign key): `vehicleId` must be
the id of a non-deleted vehicle. The auto-create fallback in `resolveVehicleId()`
is removed.

---

### VehiclePreferences (new — not a DB table)

A `SharedPreferences`-backed wrapper that persists a single key:

```
File: app/data/settings/VehiclePreferences.kt
─────────────────────────────────────────────
Key                        Type     Default
─────────────────────────────────────────────
last_used_vehicle_id       String?  null
─────────────────────────────────────────────
```

**Lifecycle**: Written immediately after a successful expense save. Read on
`ExpenseInputViewModel` initialisation to pre-populate `selectedVehicleId`.
If the stored id refers to a deleted vehicle, `selectedVehicleId` is set to `null`.

---

## State Flows in ViewModels

### VehicleViewModel (additions)

| StateFlow              | Type                  | Description                          |
|------------------------|-----------------------|--------------------------------------|
| `editingVehicle`       | `VehicleEntity?`      | Null → no edit form; non-null → pre-fill edit form |
| `showAddDialog`        | `Boolean`             | Unchanged                            |
| `vehicles`             | `List<VehicleEntity>` | Unchanged (active only)              |

### ExpenseInputViewModel (changes)

| StateFlow              | Type       | Change                                                    |
|------------------------|------------|-----------------------------------------------------------|
| `selectedVehicleId`    | `String?`  | **New** — replaces `resolveVehicleId()` auto-create logic |
| `vehicles`             | `List<VehicleEntity>` | Already present; drives selector dropdown        |

`resolveVehicleId()` is **deleted**. Save functions receive `vehicleId` from
`selectedVehicleId.value` and throw `IllegalStateException` if null (UI blocks
the path before this point).

---

## Validation Rules

| Field          | Rule                                         | Error message (Italian)           |
|----------------|----------------------------------------------|-----------------------------------|
| name           | Non-blank                                    | "Il nome è obbligatorio"          |
| year           | Null OR in [1900, currentYear+1]             | "Anno non valido"                 |
| fuelType       | One of the 6 canonical values OR empty       | (dropdown prevents invalid input) |
| odometerKm     | ≥ 0                                          | "Km non validi"                   |
| selectedVehicleId | Non-null at save time                     | Save button disabled              |

---

## Entity Relationships

```
Vehicle (1) ──── (N) Expense
  └── isDeleted=true → vehicle hidden, expenses still stored (soft delete)

VehiclePreferences ──── (1) Vehicle   [loose ref, validated at read time]
```
