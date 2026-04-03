package com.carflow.app.ui.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.carflow.app.ui.screens.expense.components.ParsedResultCard
import com.carflow.app.ui.screens.expense.viewmodel.ExpenseInputViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseInputViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Parser NLP", "Form Manuale")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuova Spesa") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> ParserTabContent(viewModel)
                1 -> FuelFormTabContent(viewModel)
            }
        }
    }
}

@Composable
fun ParserTabContent(
    viewModel: ExpenseInputViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var showSavedSnackbar by remember { mutableStateOf(false) }
    val parsedExpense by viewModel.parsedExpense.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Descrivi la spesa") },
            placeholder = { Text("Es: benzina 50€ 30L o tagliando 200€") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                viewModel.parseExpense(inputText)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputText.isNotBlank() && !isParsing
        ) {
            if (isParsing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analisi in corso...")
            } else {
                Text("Analizza")
            }
        }

        parsedExpense?.let { parsed ->
            ParsedResultCard(
                parsedExpense = parsed,
                onConfirm = {
                    coroutineScope.launch {
                        viewModel.saveExpense(parsed)
                        showSavedSnackbar = true
                        inputText = ""
                        viewModel.clearParsedExpense()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showSavedSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSavedSnackbar = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text("Spesa salvata!")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelFormTabContent(viewModel: ExpenseInputViewModel) {
    var formState by remember { mutableStateOf(FuelFormState()) }
    var saveSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (saveSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            saveSuccess = false
            formState = FuelFormState()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Spesa carburante salvata!", color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(formState.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        formState = formState.copy(date = it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annulla")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Aggiungi spesa carburante", style = MaterialTheme.typography.titleLarge)

        // Data
        OutlinedTextField(
            value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(formState.date)),
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

        // Tipo carburante
        var expanded by remember { mutableStateOf(false) }
        Column {
            Text(text = "Tipo carburante", style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(formState.fuelType)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("PETROL", "DIESEL", "HYBRID", "ELECTRIC", "LPG", "CNG").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                formState = formState.copy(fuelType = option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Helper for numeric fields
        val numericModifier = Modifier.fillMaxWidth()

        // Prezzo totale
        OutlinedTextField(
            value = formState.totalPrice,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                formState = formState.copy(totalPrice = filtered)
                formState = recalc(formState)
            },
            label = { Text("Prezzo totale (€)") },
            singleLine = true,
            modifier = numericModifier
        )

        // Prezzo al litro
        OutlinedTextField(
            value = formState.pricePerLiter,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                formState = formState.copy(pricePerLiter = filtered)
                formState = recalc(formState)
            },
            label = { Text("Prezzo al litro (€/L)") },
            singleLine = true,
            modifier = numericModifier
        )

        // Litri
        OutlinedTextField(
            value = formState.liters,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' }
                formState = formState.copy(liters = filtered)
                formState = recalc(formState)
            },
            label = { Text("Litri (L)") },
            singleLine = true,
            modifier = numericModifier
        )

        // Km
        OutlinedTextField(
            value = formState.odometerKm,
            onValueChange = { formState = formState.copy(odometerKm = it.filter { ch -> ch.isDigit() || ch == '.' }) },
            label = { Text("Km al rifornimento") },
            singleLine = true,
            modifier = numericModifier
        )

        // Checkbox pieno
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = formState.isFullTank,
                onCheckedChange = { formState = formState.copy(isFullTank = it) }
            )
            Text("Rifornimento completo (pieno)")
        }

        // Distributore nome
        OutlinedTextField(
            value = formState.gasStationName,
            onValueChange = { formState = formState.copy(gasStationName = it) },
            label = { Text("Nome distributore (opzionale)") },
            singleLine = true,
            modifier = numericModifier
        )

        // Distributore location
        OutlinedTextField(
            value = formState.gasStationLocation,
            onValueChange = { formState = formState.copy(gasStationLocation = it) },
            label = { Text("Località distributore (opzionale)") },
            singleLine = true,
            modifier = numericModifier
        )

        // Descrizione
        OutlinedTextField(
            value = formState.description,
            onValueChange = { formState = formState.copy(description = it) },
            label = { Text("Descrizione (opzionale)") },
            minLines = 2,
            maxLines = 3,
            modifier = numericModifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Validazione: richiedi almeno due valori tra totalPrice, pricePerLiter, liters
        val totalPriceValid = formState.totalPrice.toDoubleOrNull()?.let { it > 0 } ?: false
        val pricePerLiterValid = formState.pricePerLiter.toDoubleOrNull()?.let { it > 0 } ?: false
        val litersValid = formState.liters.toDoubleOrNull()?.let { it > 0 } ?: false
        val validCount = listOf(totalPriceValid, pricePerLiterValid, litersValid).count { it }

        val isValid = validCount >= 2

        Button(
            onClick = {
                if (isValid) {
                    val totalPrice = formState.totalPrice.toDoubleOrNull() ?: 0.0
                    val pricePerLiter = if (formState.pricePerLiter.isNotBlank()) formState.pricePerLiter.toDoubleOrNull() else null
                    val liters = if (formState.liters.isNotBlank()) formState.liters.toDoubleOrNull() else null
                    val odometerKm = if (formState.odometerKm.isNotBlank()) formState.odometerKm.toDoubleOrNull() else null

                    viewModel.saveFuelExpense(
                        fuelType = formState.fuelType,
                        totalPrice = totalPrice,
                        pricePerLiter = pricePerLiter,
                        quantity = liters,
                        odometerKm = odometerKm,
                        isFullTank = formState.isFullTank,
                        gasStationName = formState.gasStationName.ifBlank { null },
                        gasStationLocation = formState.gasStationLocation.ifBlank { null },
                        description = formState.description,
                        date = formState.date
                    )
                    saveSuccess = true
                    errorMessage = null
                } else {
                    errorMessage = "Inserisci almeno due tra Prezzo totale, Prezzo al litro e Litri."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isValid
        ) {
            Text("Salva carburante")
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

private fun recalc(state: FuelFormState): FuelFormState {
    val total = state.totalPrice.toDoubleOrNull()
    val price = state.pricePerLiter.toDoubleOrNull()
    val liters = state.liters.toDoubleOrNull()

    val validTotal = total != null && total > 0
    val validPrice = price != null && price > 0
    val validLiters = liters != null && liters > 0

    val validCount = listOf(validTotal, validPrice, validLiters).count { it }

    if (validCount == 2) {
        return when {
            validTotal && validPrice && !validLiters -> {
                val newLiters = total!! / price!!
                state.copy(liters = "%.2f".format(newLiters))
            }
            validTotal && validLiters && !validPrice -> {
                val newPrice = total!! / liters!!
                state.copy(pricePerLiter = "%.2f".format(newPrice))
            }
            validPrice && validLiters && !validTotal -> {
                val newTotal = price!! * liters!!
                state.copy(totalPrice = "%.2f".format(newTotal))
            }
            else -> state
        }
    }
    return state
}

data class FuelFormState(
    val date: Long = System.currentTimeMillis(),
    val fuelType: String = "PETROL",
    val totalPrice: String = "",
    val pricePerLiter: String = "",
    val liters: String = "",
    val odometerKm: String = "",
    val isFullTank: Boolean = false,
    val gasStationName: String = "",
    val gasStationLocation: String = "",
    val description: String = ""
)
