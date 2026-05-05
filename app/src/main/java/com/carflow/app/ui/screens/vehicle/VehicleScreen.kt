package com.carflow.app.ui.screens.vehicle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.ui.screens.vehicle.viewmodel.VehicleViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(
    onNavigateBack: () -> Unit,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingVehicle by viewModel.editingVehicle.collectAsState()
    val showDeleteConfirm by viewModel.showDeleteConfirm.collectAsState()

    showDeleteConfirm?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteConfirm() },
            title = { Text("Elimina veicolo?") },
            text = { Text("Questo veicolo ha spese associate. Eliminarlo comunque?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(vehicle) }) {
                    Text("Elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearDeleteConfirm() }) {
                    Text("Annulla")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Veicoli") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!showAddDialog && editingVehicle == null) {
                FloatingActionButton(onClick = { viewModel.openAddDialog() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Aggiungi veicolo")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showAddDialog) {
                VehicleFormCard(
                    title = "Aggiungi Veicolo",
                    initialVehicle = null,
                    onSave = { name, make, model, year, plate, fuel, km ->
                        viewModel.addVehicle(name, make, model, year, plate, fuel, km)
                    },
                    onCancel = { viewModel.closeAddDialog() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            editingVehicle?.let { vehicle ->
                VehicleFormCard(
                    title = "Modifica Veicolo",
                    initialVehicle = vehicle,
                    onSave = { name, make, model, year, plate, fuel, km ->
                        viewModel.updateVehicle(vehicle, name, make, model, year, plate, fuel, km)
                    },
                    onCancel = { viewModel.closeEditDialog() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (vehicles.isEmpty() && !showAddDialog && editingVehicle == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun veicolo registrato\nTocca + per aggiungere",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            onEdit = { viewModel.openEditDialog(vehicle) },
                            onDelete = { viewModel.deleteVehicle(vehicle) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleFormCard(
    title: String,
    initialVehicle: VehicleEntity?,
    onSave: (name: String, make: String, model: String, year: Int?, licensePlate: String, fuelType: String, odometerKm: Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.name ?: "") }
    var make by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.make ?: "") }
    var model by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.model ?: "") }
    var year by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.year?.toString() ?: "") }
    var licensePlate by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.licensePlate ?: "") }
    var fuelType by remember(initialVehicle?.id) { mutableStateOf(initialVehicle?.fuelType ?: "") }
    var odometerKm by remember(initialVehicle?.id) {
        mutableStateOf(if (initialVehicle != null && initialVehicle.odometerKm > 0) initialVehicle.odometerKm.toLong().toString() else "")
    }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearError = year.isNotBlank() && (year.toIntOrNull() == null || year.toInt() !in 1900..(currentYear + 1))
    var fuelExpanded by remember { mutableStateOf(false) }
    val selectedFuelLabel = FuelTypeUiModel.entries.firstOrNull { it.second == fuelType }?.first ?: "(non specificato)"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome veicolo *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Marca") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modello") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Anno") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = yearError,
                    supportingText = if (yearError) {{ Text("Anno non valido") }} else null,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text("Targa") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            ExposedDropdownMenuBox(
                expanded = fuelExpanded,
                onExpandedChange = { fuelExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFuelLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Carburante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fuelExpanded,
                    onDismissRequest = { fuelExpanded = false }
                ) {
                    FuelTypeUiModel.entries.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                fuelType = value
                                fuelExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = odometerKm,
                onValueChange = { odometerKm = it },
                label = { Text("Km attuali") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) { Text("Annulla") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSave(
                            name.trim(),
                            make.trim(),
                            model.trim(),
                            year.toIntOrNull(),
                            licensePlate.trim(),
                            fuelType,
                            odometerKm.toDoubleOrNull() ?: 0.0
                        )
                    },
                    enabled = name.isNotBlank() && !yearError
                ) {
                    Text("Salva")
                }
            }
        }
    }
}

@Composable
fun VehicleItem(
    vehicle: VehicleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = vehicle.name, style = MaterialTheme.typography.titleMedium)
                val makeModel = "${vehicle.make} ${vehicle.model}".trim()
                if (makeModel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = makeModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (vehicle.licensePlate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Targa: ${vehicle.licensePlate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (vehicle.year != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Anno: ${vehicle.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val fuelLabel = FuelTypeUiModel.labelFor(vehicle.fuelType)
                if (fuelLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Carburante: $fuelLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Km: ${vehicle.odometerKm.toLong()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Elimina",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Empty state")
@Composable
private fun VehicleScreenEmptyPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Nessun veicolo registrato\nTocca + per aggiungere",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true, name = "Vehicle item")
@Composable
private fun VehicleItemPreview() {
    MaterialTheme {
        VehicleItem(
            vehicle = VehicleEntity(
                id = "1",
                name = "Fiat 500",
                make = "Fiat",
                model = "500",
                year = 2020,
                licensePlate = "AB123CD",
                fuelType = "petrol",
                odometerKm = 45000.0
            ),
            onEdit = {},
            onDelete = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Add form")
@Composable
private fun VehicleFormAddPreview() {
    MaterialTheme {
        VehicleFormCard(
            title = "Aggiungi Veicolo",
            initialVehicle = null,
            onSave = { _, _, _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Edit form")
@Composable
private fun VehicleFormEditPreview() {
    MaterialTheme {
        VehicleFormCard(
            title = "Modifica Veicolo",
            initialVehicle = VehicleEntity(
                id = "1",
                name = "Fiat 500",
                make = "Fiat",
                model = "500",
                year = 2020,
                licensePlate = "AB123CD",
                fuelType = "diesel",
                odometerKm = 45000.0
            ),
            onSave = { _, _, _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}
