package com.carflow.app.ui.screens.expense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.ExpenseRepository
import com.carflow.app.data.repository.VehicleRepository
import com.carflow.app.data.settings.LlmSettings
import com.carflow.app.data.settings.VehiclePreferences
import com.carflow.network.llm.ExpenseParserStrategy
import com.carflow.network.llm.LlmMode
import com.carflow.parser.ExpenseParser
import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.FuelType
import com.carflow.parser.model.ParsedExpense
import com.carflow.parser.model.QuantityUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

// --- Supporting types ---

sealed class SaveResult {
    object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}

data class EditableExpense(
    val category: ExpenseCategory,
    val amount: String,
    val description: String,
    val date: Long,
    val fuelType: FuelType?,
    val quantityUnit: QuantityUnit?,
    val quantity: String,
    val odometerKm: String = "",
    val warnings: List<String>
)

data class NlpTabState(
    val inputText: String = "",
    val editedExpense: EditableExpense? = null,
    val isParsing: Boolean = false,
    val saveResult: SaveResult? = null
)

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

data class MaintenanceFormState(
    val date: Long = System.currentTimeMillis(),
    val amount: String = "",
    val description: String = ""
)

data class ExtraFormState(
    val date: Long = System.currentTimeMillis(),
    val amount: String = "",
    val description: String = ""
)

data class FormTabState(
    val selectedCategory: ExpenseCategory = ExpenseCategory.FUEL,
    val fuelState: FuelFormState = FuelFormState(),
    val maintenanceState: MaintenanceFormState = MaintenanceFormState(),
    val extraState: ExtraFormState = ExtraFormState(),
    val saveResult: SaveResult? = null
)

@HiltViewModel
class ExpenseInputViewModel @Inject constructor(
    @Named("default") private val defaultParser: ExpenseParser,
    @Named("llm") private val llmParser: ExpenseParserStrategy,
    private val llmSettings: LlmSettings,
    private val expenseRepository: ExpenseRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehiclePreferences: VehiclePreferences
) : ViewModel() {

    private val _nlpState = MutableStateFlow(NlpTabState())
    val nlpState: StateFlow<NlpTabState> = _nlpState.asStateFlow()

    private val _formState = MutableStateFlow(FormTabState())
    val formState: StateFlow<FormTabState> = _formState.asStateFlow()

    private val _vehicles = MutableStateFlow<List<VehicleEntity>>(emptyList())
    val vehicles: StateFlow<List<VehicleEntity>> = _vehicles.asStateFlow()

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    init {
        loadVehicles()
        restoreLastUsedVehicle()
        viewModelScope.launch {
            _nlpState
                .map { it.inputText }
                .distinctUntilChanged()
                .debounce(500L)
                .collect { input ->
                    if (input.isNotBlank()) autoParse(input)
                }
        }
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect { list ->
                _vehicles.value = list
                // Guard: reset selection if the selected vehicle was deleted while the app is running
                val currentId = _selectedVehicleId.value
                if (currentId != null && list.none { it.id == currentId }) {
                    _selectedVehicleId.value = null
                    vehiclePreferences.setLastUsedVehicleId("")
                }
            }
        }
    }

    private fun restoreLastUsedVehicle() {
        viewModelScope.launch {
            val storedId = vehiclePreferences.getLastUsedVehicleId() ?: return@launch
            val list = vehicleRepository.getAllVehicles().first()
            if (list.any { it.id == storedId }) {
                _selectedVehicleId.value = storedId
            }
        }
    }

    fun selectVehicle(id: String) {
        _selectedVehicleId.value = id
    }

    private suspend fun autoParse(input: String) {
        _nlpState.value = _nlpState.value.copy(isParsing = true)
        try {
            val result = getActiveParser().parse(input)
            _nlpState.value = _nlpState.value.copy(isParsing = false, editedExpense = result.toEditable())
        } catch (e: Exception) {
            _nlpState.value = _nlpState.value.copy(isParsing = false)
        }
    }

    // --- NLP commands ---

    fun updateNlpInput(text: String) {
        _nlpState.value = _nlpState.value.copy(
            inputText = text,
            editedExpense = if (text.isBlank()) null else _nlpState.value.editedExpense
        )
    }

    fun parseExpense() {
        val input = _nlpState.value.inputText
        if (input.isBlank()) return
        viewModelScope.launch {
            _nlpState.value = _nlpState.value.copy(isParsing = true, editedExpense = null)
            try {
                val result = getActiveParser().parse(input)
                _nlpState.value = _nlpState.value.copy(
                    isParsing = false,
                    editedExpense = result.toEditable()
                )
            } catch (e: Exception) {
                _nlpState.value = _nlpState.value.copy(
                    isParsing = false,
                    saveResult = SaveResult.Error("Impossibile analizzare l'input. Riprova.")
                )
            }
        }
    }

    fun updateEditedExpense(updated: EditableExpense) {
        _nlpState.value = _nlpState.value.copy(editedExpense = updated)
    }

    fun confirmParsedExpense() {
        val expense = _nlpState.value.editedExpense ?: return
        val amount = expense.amount.toDoubleOrNull() ?: return
        val vehicleId = _selectedVehicleId.value ?: return
        viewModelScope.launch {
            try {
                expenseRepository.create(
                    vehicleId = vehicleId,
                    category = expense.category.name,
                    subcategory = expense.fuelType?.name ?: "",
                    amount = amount,
                    quantity = expense.quantity.toDoubleOrNull(),
                    quantityUnit = expense.quantityUnit?.name,
                    description = expense.description,
                    date = expense.date,
                    odometerKm = expense.odometerKm.toDoubleOrNull()
                )
                vehiclePreferences.setLastUsedVehicleId(vehicleId)
                _nlpState.value = NlpTabState(saveResult = SaveResult.Success)
            } catch (e: Exception) {
                _nlpState.value = _nlpState.value.copy(
                    saveResult = SaveResult.Error("Errore nel salvataggio. Riprova.")
                )
            }
        }
    }

    fun clearNlpSaveResult() {
        _nlpState.value = _nlpState.value.copy(saveResult = null)
    }

    // --- Form commands ---

    fun selectCategory(category: ExpenseCategory) {
        _formState.value = _formState.value.copy(selectedCategory = category)
    }

    fun updateFuelForm(state: FuelFormState) {
        _formState.value = _formState.value.copy(fuelState = recalcFuelState(state))
    }

    fun updateMaintenanceForm(state: MaintenanceFormState) {
        _formState.value = _formState.value.copy(maintenanceState = state)
    }

    fun updateExtraForm(state: ExtraFormState) {
        _formState.value = _formState.value.copy(extraState = state)
    }

    fun saveFormExpense() {
        val vehicleId = _selectedVehicleId.value ?: return
        viewModelScope.launch {
            try {
                when (_formState.value.selectedCategory) {
                    ExpenseCategory.FUEL -> saveFuelFromForm(vehicleId)
                    ExpenseCategory.MAINTENANCE -> saveMaintenanceFromForm(vehicleId)
                    ExpenseCategory.EXTRA -> saveExtraFromForm(vehicleId)
                    ExpenseCategory.UNKNOWN -> return@launch
                }
                vehiclePreferences.setLastUsedVehicleId(vehicleId)
                _formState.value = FormTabState(saveResult = SaveResult.Success)
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    saveResult = SaveResult.Error("Errore nel salvataggio. Riprova.")
                )
            }
        }
    }

    fun clearFormSaveResult() {
        _formState.value = _formState.value.copy(saveResult = null)
    }

    // --- Private helpers ---

    private suspend fun getActiveParser(): ExpenseParserStrategy {
        return if (llmSettings.getMode() == LlmMode.DIRECT && llmSettings.hasDirectConfig()) {
            llmParser
        } else {
            object : ExpenseParserStrategy {
                override suspend fun parse(input: String): ParsedExpense =
                    defaultParser.parse(input)
            }
        }
    }

    private suspend fun saveFuelFromForm(vehicleId: String) {
        val s = _formState.value.fuelState
        val total = s.totalPrice.toDoubleOrNull() ?: return
        expenseRepository.create(
            vehicleId = vehicleId,
            category = "FUEL",
            subcategory = s.fuelType,
            amount = total,
            quantity = s.liters.toDoubleOrNull(),
            quantityUnit = "LITERS",
            description = s.description,
            date = s.date,
            odometerKm = s.odometerKm.toDoubleOrNull(),
            isFullTank = s.isFullTank,
            gasStationName = s.gasStationName.ifBlank { null },
            gasStationLocation = s.gasStationLocation.ifBlank { null },
            pricePerLiter = s.pricePerLiter.toDoubleOrNull()
        )
    }

    private suspend fun saveMaintenanceFromForm(vehicleId: String) {
        val s = _formState.value.maintenanceState
        val amount = s.amount.toDoubleOrNull() ?: return
        expenseRepository.create(
            vehicleId = vehicleId,
            category = "MAINTENANCE",
            amount = amount,
            description = s.description,
            date = s.date
        )
    }

    private suspend fun saveExtraFromForm(vehicleId: String) {
        val s = _formState.value.extraState
        val amount = s.amount.toDoubleOrNull() ?: return
        expenseRepository.create(
            vehicleId = vehicleId,
            category = "EXTRA",
            amount = amount,
            description = s.description,
            date = s.date
        )
    }
}

internal fun recalcFuelState(state: FuelFormState): FuelFormState {
    val total = state.totalPrice.toDoubleOrNull()
    val price = state.pricePerLiter.toDoubleOrNull()
    val liters = state.liters.toDoubleOrNull()

    val hasTotal = total != null && total > 0
    val hasPrice = price != null && price > 0
    val hasLiters = liters != null && liters > 0

    return when (listOf(hasTotal, hasPrice, hasLiters).count { it }) {
        2 -> when {
            hasTotal && hasPrice && !hasLiters ->
                state.copy(liters = String.format(Locale.US, "%.2f", total!! / price!!))
            hasTotal && hasLiters && !hasPrice ->
                state.copy(pricePerLiter = String.format(Locale.US, "%.2f", total!! / liters!!))
            hasPrice && hasLiters && !hasTotal ->
                state.copy(totalPrice = String.format(Locale.US, "%.2f", price!! * liters!!))
            else -> state
        }
        else -> state
    }
}

private fun ParsedExpense.toEditable() = EditableExpense(
    category = category,
    amount = amount?.toString() ?: "",
    description = description,
    date = date ?: System.currentTimeMillis(),
    fuelType = fuelType,
    quantityUnit = quantityUnit,
    quantity = quantity?.toString() ?: "",
    warnings = warnings
)
