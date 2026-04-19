package com.carflow.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carflow.app.data.entity.ExpenseEntity
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.ExpenseRepository
import com.carflow.app.data.repository.VehicleRepository
import com.carflow.parser.ExpenseParser
import com.carflow.parser.model.ParsedExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CarflowViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val vehicleRepository: VehicleRepository,
    private val parser: ExpenseParser,
) : ViewModel() {

    val vehicles: StateFlow<List<VehicleEntity>> = vehicleRepository
        .getAllActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = expenseRepository
        .getAllActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentExpenses: StateFlow<List<ExpenseEntity>> = expenseRepository
        .getRecent(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedVehicleId = MutableStateFlow<String?>(null)
    val selectedVehicleId: StateFlow<String?> = _selectedVehicleId.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _parsedExpense = MutableStateFlow<ParsedExpense?>(null)
    val parsedExpense: StateFlow<ParsedExpense?> = _parsedExpense.asStateFlow()

    fun selectVehicle(id: String?) {
        _selectedVehicleId.value = id
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
        _parsedExpense.value = if (text.isBlank()) null else parser.parse(text)
    }

    fun saveExpense(vehicleId: String) {
        val parsed = _parsedExpense.value ?: return
        viewModelScope.launch {
            expenseRepository.create(
                vehicleId = vehicleId,
                category = parsed.category.name,
                subcategory = parsed.subcategory ?: "",
                amount = parsed.amount ?: 0.0,
                quantity = parsed.quantity,
                quantityUnit = parsed.quantityUnit?.name,
                description = parsed.description,
            )
            _inputText.value = ""
            _parsedExpense.value = null
        }
    }

    fun seedDemoDataIfEmpty() {
        viewModelScope.launch {
            if (vehicles.value.isEmpty()) {
                val v1 = VehicleEntity(
                    id = "v1",
                    name = "Nina",
                    make = "Fiat",
                    model = "500",
                    year = 2019,
                    licensePlate = "GE 842 RK",
                    fuelType = "petrol",
                    odometerKm = 64280.0,
                )
                val v2 = VehicleEntity(
                    id = "v2",
                    name = "Il Bombardone",
                    make = "Volkswagen",
                    model = "Golf GTI",
                    year = 2016,
                    licensePlate = "MI 117 ZT",
                    fuelType = "petrol",
                    odometerKm = 128450.0,
                )
                vehicleRepository.create(v1.name, v1.make, v1.model, v1.year, v1.licensePlate, v1.fuelType)
                vehicleRepository.create(v2.name, v2.make, v2.model, v2.year, v2.licensePlate, v2.fuelType)
            }
        }
    }
}
