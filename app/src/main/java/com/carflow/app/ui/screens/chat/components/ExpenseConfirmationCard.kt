package com.carflow.app.ui.screens.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.ui.screens.chat.viewmodel.DraftExpense
import com.carflow.parser.model.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseConfirmationCard(
    draft: DraftExpense,
    vehicles: List<VehicleEntity>,
    onDraftChange: (DraftExpense) -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSaveEnabled = (draft.amount ?: 0.0) > 0 &&
            draft.category != null &&
            draft.vehicleId != null

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Riepilogo spesa", style = MaterialTheme.typography.titleMedium)

            // Amount
            OutlinedTextField(
                value = draft.amount?.toString() ?: "",
                onValueChange = { v ->
                    onDraftChange(draft.copy(amount = v.replace(',', '.').toDoubleOrNull()))
                },
                label = { Text("Importo (€)") },
                singleLine = true,
                isError = (draft.amount ?: 0.0) <= 0,
                modifier = Modifier.fillMaxWidth()
            )

            // Category dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = draft.category?.displayName() ?: "Seleziona categoria",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    listOf(ExpenseCategory.FUEL, ExpenseCategory.MAINTENANCE, ExpenseCategory.EXTRA).forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName()) },
                            onClick = {
                                onDraftChange(draft.copy(category = cat))
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Vehicle dropdown
            var vehicleExpanded by remember { mutableStateOf(false) }
            val selectedVehicleName = vehicles.firstOrNull { it.id == draft.vehicleId }?.name ?: "Seleziona veicolo"
            ExposedDropdownMenuBox(
                expanded = vehicleExpanded,
                onExpandedChange = { vehicleExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedVehicleName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Veicolo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = vehicleExpanded,
                    onDismissRequest = { vehicleExpanded = false }
                ) {
                    vehicles.forEach { v ->
                        DropdownMenuItem(
                            text = { Text(v.name) },
                            onClick = {
                                onDraftChange(draft.copy(vehicleId = v.id))
                                vehicleExpanded = false
                            }
                        )
                    }
                }
            }

            // Date
            var showDatePicker by remember { mutableStateOf(false) }
            val displayDate = draft.date?.let {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
            } ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(draft.date)
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDraftChange(draft.copy(date = it))
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
                    }
                ) { DatePicker(state = datePickerState) }
            }

            OutlinedTextField(
                value = displayDate,
                onValueChange = {},
                label = { Text("Data") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Seleziona data")
                    }
                }
            )

            // Description
            OutlinedTextField(
                value = draft.description ?: "",
                onValueChange = { onDraftChange(draft.copy(description = it)) },
                label = { Text("Descrizione (opzionale)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
                maxLines = 3
            )

            // FUEL-specific fields
            if (draft.category == ExpenseCategory.FUEL) {
                var fuelTypeExpanded by remember { mutableStateOf(false) }
                val fuelOptions = listOf("" to "Non specificato", "PETROL" to "Benzina", "DIESEL" to "Diesel",
                    "ELECTRIC" to "Elettrico", "HYBRID" to "Ibrido", "LPG" to "GPL", "CNG" to "Metano")
                val fuelLabel = fuelOptions.firstOrNull { it.first == draft.fuelType }?.second ?: "Non specificato"

                ExposedDropdownMenuBox(
                    expanded = fuelTypeExpanded,
                    onExpandedChange = { fuelTypeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fuelLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo carburante") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelTypeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = fuelTypeExpanded,
                        onDismissRequest = { fuelTypeExpanded = false }
                    ) {
                        fuelOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onDraftChange(draft.copy(fuelType = value.ifBlank { null }))
                                    fuelTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = draft.liters?.toString() ?: "",
                    onValueChange = { v ->
                        onDraftChange(draft.copy(liters = v.replace(',', '.').toDoubleOrNull()))
                    },
                    label = { Text("Litri (opzionale)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = draft.pricePerLiter?.toString() ?: "",
                    onValueChange = { v ->
                        onDraftChange(draft.copy(pricePerLiter = v.replace(',', '.').toDoubleOrNull()))
                    },
                    label = { Text("Prezzo al litro €/L (opzionale)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f)
                ) { Text("Annulla") }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = isSaveEnabled
                ) { Text("Salva spesa") }
            }
        }
    }
}

private fun ExpenseCategory.displayName() = when (this) {
    ExpenseCategory.FUEL -> "Carburante"
    ExpenseCategory.MAINTENANCE -> "Manutenzione"
    ExpenseCategory.EXTRA -> "Extra"
    ExpenseCategory.UNKNOWN -> "Sconosciuto"
}

@Preview(showBackground = true, name = "Confirmation card — FUEL complete")
@Composable
private fun ConfirmationCardFuelPreview() {
    val vehicles = listOf(VehicleEntity("v1", "Fiat Panda"))
    ExpenseConfirmationCard(
        draft = DraftExpense(
            amount = 55.40,
            category = ExpenseCategory.FUEL,
            vehicleId = "v1",
            fuelType = "PETROL",
            liters = 35.0,
            pricePerLiter = 1.58
        ),
        vehicles = vehicles,
        onDraftChange = {},
        onSave = {},
        onDiscard = {}
    )
}

@Preview(showBackground = true, name = "Confirmation card — MAINTENANCE partial")
@Composable
private fun ConfirmationCardMaintenancePreview() {
    ExpenseConfirmationCard(
        draft = DraftExpense(amount = 120.0, category = ExpenseCategory.MAINTENANCE),
        vehicles = listOf(VehicleEntity("v1", "Fiat Panda")),
        onDraftChange = {},
        onSave = {},
        onDiscard = {}
    )
}

@Preview(showBackground = true, name = "Confirmation card — no vehicle")
@Composable
private fun ConfirmationCardNoVehiclePreview() {
    ExpenseConfirmationCard(
        draft = DraftExpense(amount = 80.0, category = ExpenseCategory.EXTRA),
        vehicles = emptyList(),
        onDraftChange = {},
        onSave = {},
        onDiscard = {}
    )
}
