# UI Contracts: Vehicle Management & Mandatory Vehicle Association

## Contract 1 — VehicleScreen

### States

| State              | Condition                            | What user sees                                         |
|--------------------|--------------------------------------|--------------------------------------------------------|
| Empty              | `vehicles.isEmpty() && !showAddDialog && editingVehicle == null` | Centre-aligned "Nessun veicolo" text + FAB |
| List               | `vehicles.isNotEmpty()`              | LazyColumn of VehicleItems + FAB                       |
| Add form open      | `showAddDialog == true`              | Inline card at top with all 7 fields + Save/Cancel     |
| Edit form open     | `editingVehicle != null`             | Same inline card, pre-filled, Save updates the vehicle |

### VehicleItem card — visible fields

- Nickname (titleMedium)
- "Make Model" (bodyMedium, onSurfaceVariant)
- Targa: {licensePlate} (bodySmall, only if non-blank)
- Km: {odometerKm} (bodySmall)
- Anno: {year} (bodySmall, only if non-null)
- Carburante: {fuelTypeLabel} (bodySmall, only if non-blank)
- Edit icon (tap → open edit form for this vehicle)
- Delete icon (error tint)

### Vehicle Add/Edit Form — field list

| Field          | Widget                        | Keyboard      | Optional |
|----------------|-------------------------------|---------------|----------|
| Nome veicolo   | OutlinedTextField             | Text          | No       |
| Marca          | OutlinedTextField             | Text          | Yes      |
| Modello        | OutlinedTextField             | Text          | Yes      |
| Anno           | OutlinedTextField             | Number        | Yes      |
| Targa          | OutlinedTextField             | Text (caps)   | Yes      |
| Carburante     | ExposedDropdownMenuBox        | —             | Yes      |
| Km attuali     | OutlinedTextField             | Number        | Yes      |

Save button enabled when `name.isNotBlank()`.

### Delete confirmation dialog

Shown only when the vehicle has ≥ 1 linked non-deleted expense.

- Title: "Elimina veicolo?"
- Body: "Questo veicolo ha N spese associate. Elimina comunque?"
- Buttons: "Annulla" (dismiss) | "Elimina" (confirm soft-delete)

---

## Contract 2 — VehicleSelector (new composable)

Used inside `ExpenseInputScreen` above the existing tab row.

### Props

```
VehicleSelector(
  vehicles: List<VehicleEntity>,        // active vehicles only
  selectedVehicleId: String?,
  onVehicleSelected: (String) -> Unit,
  onAddVehicleClicked: () -> Unit       // navigates to VehicleScreen
)
```

### States

| State            | Condition                    | What user sees                                          |
|------------------|------------------------------|---------------------------------------------------------|
| No vehicles      | `vehicles.isEmpty()`         | Warning banner: "Nessun veicolo. Aggiungi un veicolo." + CTA button |
| Vehicle selected | `selectedVehicleId != null`  | Row: car icon + vehicle name + dropdown arrow           |
| No selection     | `vehicles.isNotEmpty() && selectedVehicleId == null` | Placeholder "Seleziona veicolo" in dropdown |

When user taps the selector, a `DropdownMenu` opens listing all active vehicles by
nickname. Selecting one calls `onVehicleSelected(vehicleId)`.

---

## Contract 3 — ExpenseInputScreen changes

### Save button guard

The save button (both NLP "Conferma e Salva" and Form "Salva") MUST be disabled
when `selectedVehicleId == null`.

### NLP preview card

The preview card MUST show the selected vehicle's nickname above the parsed fields:

```
┌─────────────────────────────┐
│ Veicolo: Fiat 500           │
│ Categoria: Carburante       │
│ Importo: €50,00             │
│ Quantità: 30 L              │
│ ...                         │
└─────────────────────────────┘
```

### No-vehicle banner placement

When `vehicles.isEmpty()`, the `VehicleSelector` banner occupies the full width
above the tab row. The tab row and its content are still visible (not hidden), but
both save actions are disabled.

---

## Contract 4 — Fuel-Type Dropdown Labels

| Display label (Italian) | Internal value |
|-------------------------|----------------|
| Benzina                 | petrol         |
| Diesel                  | diesel         |
| Elettrico               | electric       |
| Ibrido                  | hybrid         |
| GPL                     | lpg            |
| Metano                  | cng            |
| (non specificato)       | ""             |

This mapping lives in a new `FuelTypeUiModel` sealed class or companion object in
the `app` module — not in `shared:parser`.
