package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carflow.app.ui.screens.expense.viewmodel.FuelFormState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelFormContent(
    state: FuelFormState,
    onStateChange: (FuelFormState) -> Unit,
    onSave: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(state.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { onStateChange(state.copy(date = it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    val totalValid = state.totalPrice.toDoubleOrNull()?.let { it > 0 } ?: false
    val priceValid = state.pricePerLiter.toDoubleOrNull()?.let { it > 0 } ?: false
    val litersValid = state.liters.toDoubleOrNull()?.let { it > 0 } ?: false
    val isSaveEnabled = listOf(totalValid, priceValid, litersValid).count { it } >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aggiungi spesa carburante", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(state.date)),
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

        var fuelExpanded by remember { mutableStateOf(false) }
        Column {
            Text("Tipo carburante", style = MaterialTheme.typography.bodyMedium)
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedButton(
                    onClick = { fuelExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(state.fuelType) }
                DropdownMenu(
                    expanded = fuelExpanded,
                    onDismissRequest = { fuelExpanded = false }
                ) {
                    listOf("PETROL", "DIESEL", "HYBRID", "ELECTRIC", "LPG", "CNG").forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { onStateChange(state.copy(fuelType = opt)); fuelExpanded = false }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.totalPrice,
            onValueChange = { v ->
                onStateChange(state.copy(totalPrice = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
            },
            label = { Text("Prezzo totale (€)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.pricePerLiter,
            onValueChange = { v ->
                onStateChange(state.copy(pricePerLiter = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
            },
            label = { Text("Prezzo al litro (€/L)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.liters,
            onValueChange = { v ->
                onStateChange(state.copy(liters = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
            },
            label = { Text("Litri (L)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.odometerKm,
            onValueChange = { v ->
                onStateChange(state.copy(odometerKm = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
            },
            label = { Text("Km al rifornimento") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.isFullTank,
                onCheckedChange = { onStateChange(state.copy(isFullTank = it)) }
            )
            Text("Rifornimento completo (pieno)")
        }

        OutlinedTextField(
            value = state.gasStationName,
            onValueChange = { onStateChange(state.copy(gasStationName = it)) },
            label = { Text("Nome distributore (opzionale)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.gasStationLocation,
            onValueChange = { onStateChange(state.copy(gasStationLocation = it)) },
            label = { Text("Località distributore (opzionale)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("Descrizione (opzionale)") },
            minLines = 2,
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = isSaveEnabled
        ) { Text("Salva carburante") }
    }
}
