package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.ui.screens.expense.viewmodel.MaintenanceFormState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceFormContent(
    state: MaintenanceFormState,
    onStateChange: (MaintenanceFormState) -> Unit,
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

    val amountValid = state.amount.toDoubleOrNull()?.let { it > 0 } ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aggiungi spesa manutenzione", style = MaterialTheme.typography.titleLarge)

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

        OutlinedTextField(
            value = state.amount,
            onValueChange = { v ->
                onStateChange(state.copy(amount = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
            },
            label = { Text("Importo (€)") },
            singleLine = true,
            isError = state.amount.isNotBlank() && !amountValid,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onStateChange(state.copy(description = it)) },
            label = { Text("Descrizione (opzionale)") },
            placeholder = { Text("Es: Tagliando, Cambio gomme...") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = amountValid
        ) { Text("Salva manutenzione") }
    }
}

@Preview(showBackground = true)
@Composable
private fun MaintenanceFormContentPreview() {
    MaintenanceFormContent(
        state = MaintenanceFormState(amount = "150", description = "Cambio olio"),
        onStateChange = {},
        onSave = {}
    )
}
