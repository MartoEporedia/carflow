package com.carflow.app.ui.screens.expense

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LinearProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import com.carflow.app.ui.screens.expense.components.*
import com.carflow.app.ui.screens.expense.viewmodel.ExpenseInputViewModel
import com.carflow.parser.model.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseInputScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicle: () -> Unit = {},
    viewModel: ExpenseInputViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Parser NLP", "Form Manuale")

    val vehicles by viewModel.vehicles.collectAsState()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsState()
    val isVehicleSelected = selectedVehicleId != null

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
            VehicleSelector(
                vehicles = vehicles,
                selectedVehicleId = selectedVehicleId,
                onVehicleSelected = { viewModel.selectVehicle(it) },
                onAddVehicleClicked = onNavigateToVehicle
            )

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
                0 -> NlpTabContent(viewModel, isVehicleSelected)
                1 -> FormTabContent(viewModel, isVehicleSelected)
            }
        }
    }
}

@Composable
private fun NlpTabContent(viewModel: ExpenseInputViewModel, isVehicleSelected: Boolean) {
    val nlpState by viewModel.nlpState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nlpState.inputText,
            onValueChange = { viewModel.updateNlpInput(it) },
            label = { Text("Descrivi la spesa") },
            placeholder = { Text("Es: benzina 50€ 30L 45000km") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        val editedExpense = nlpState.editedExpense
        when {
            editedExpense != null -> EditableExpenseCard(
                expense = editedExpense,
                onExpenseChange = { viewModel.updateEditedExpense(it) },
                onConfirm = { viewModel.confirmParsedExpense() },
                modifier = Modifier.fillMaxWidth(),
                canSave = isVehicleSelected
            )
            nlpState.isParsing -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        nlpState.saveResult?.let { result ->
            SaveResultBanner(
                result = result,
                onDismiss = { viewModel.clearNlpSaveResult() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTabContent(viewModel: ExpenseInputViewModel, isVehicleSelected: Boolean) {
    val formState by viewModel.formState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ExpenseCategory.FUEL to "Carburante",
                ExpenseCategory.MAINTENANCE to "Manutenzione",
                ExpenseCategory.EXTRA to "Extra"
            ).forEach { (cat, label) ->
                FilterChip(
                    selected = formState.selectedCategory == cat,
                    onClick = { viewModel.selectCategory(cat) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (formState.selectedCategory) {
            ExpenseCategory.FUEL -> FuelFormContent(
                state = formState.fuelState,
                onStateChange = { viewModel.updateFuelForm(it) },
                onSave = { viewModel.saveFormExpense() },
                canSave = isVehicleSelected
            )
            ExpenseCategory.MAINTENANCE -> MaintenanceFormContent(
                state = formState.maintenanceState,
                onStateChange = { viewModel.updateMaintenanceForm(it) },
                onSave = { viewModel.saveFormExpense() },
                canSave = isVehicleSelected
            )
            ExpenseCategory.EXTRA -> ExtraFormContent(
                state = formState.extraState,
                onStateChange = { viewModel.updateExtraForm(it) },
                onSave = { viewModel.saveFormExpense() },
                canSave = isVehicleSelected
            )
            else -> {}
        }

        formState.saveResult?.let { result ->
            SaveResultBanner(
                result = result,
                onDismiss = { viewModel.clearFormSaveResult() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
