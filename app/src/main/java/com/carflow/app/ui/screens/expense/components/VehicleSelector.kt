package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.data.entity.VehicleEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleSelector(
    vehicles: List<VehicleEntity>,
    selectedVehicleId: String?,
    onVehicleSelected: (String) -> Unit,
    onAddVehicleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (vehicles.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nessun veicolo registrato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddVehicleClicked) {
                    Text("Aggiungi veicolo")
                }
            }
        }
        return
    }

    val selectedVehicle = vehicles.firstOrNull { it.id == selectedVehicleId }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedVehicle?.name ?: "Seleziona veicolo",
            onValueChange = {},
            readOnly = true,
            label = { Text("Veicolo") },
            leadingIcon = {
                Icon(Icons.Filled.DirectionsCar, contentDescription = null)
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(vehicle.name, style = MaterialTheme.typography.bodyLarge)
                            val detail = "${vehicle.make} ${vehicle.model}".trim()
                            if (detail.isNotBlank()) {
                                Text(
                                    detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onVehicleSelected(vehicle.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "No vehicles")
@Composable
private fun VehicleSelectorNoVehiclesPreview() {
    MaterialTheme {
        VehicleSelector(
            vehicles = emptyList(),
            selectedVehicleId = null,
            onVehicleSelected = {},
            onAddVehicleClicked = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Vehicle selected")
@Composable
private fun VehicleSelectorSelectedPreview() {
    val v = VehicleEntity(id = "1", name = "Fiat 500", make = "Fiat", model = "500", odometerKm = 0.0)
    MaterialTheme {
        VehicleSelector(
            vehicles = listOf(v),
            selectedVehicleId = "1",
            onVehicleSelected = {},
            onAddVehicleClicked = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "No selection")
@Composable
private fun VehicleSelectorNoSelectionPreview() {
    val v = VehicleEntity(id = "1", name = "Fiat 500", make = "Fiat", model = "500", odometerKm = 0.0)
    MaterialTheme {
        VehicleSelector(
            vehicles = listOf(v),
            selectedVehicleId = null,
            onVehicleSelected = {},
            onAddVehicleClicked = {}
        )
    }
}
