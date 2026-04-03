package com.carflow.app.ui.screens.expense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.ExpenseRepository
import com.carflow.app.data.repository.VehicleRepository
import com.carflow.app.data.settings.LlmSettings
import com.carflow.network.llm.ExpenseParserStrategy
import com.carflow.network.llm.LlmMode
import com.carflow.parser.ExpenseParser
import com.carflow.parser.model.ParsedExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ExpenseInputViewModel @Inject constructor(
    @Named("default") private val defaultParser: ExpenseParser,
    @Named("llm") private val llmParser: ExpenseParserStrategy,
    private val llmSettings: LlmSettings,
    private val expenseRepository: ExpenseRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _parsedExpense = MutableStateFlow<ParsedExpense?>(null)
    val parsedExpense: StateFlow<ParsedExpense?> = _parsedExpense

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing

    private val _vehicles = MutableStateFlow<List<VehicleEntity>>(emptyList())
    val vehicles: StateFlow<List<VehicleEntity>> = _vehicles.asStateFlow()

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect { vehicleList ->
                _vehicles.value = vehicleList
            }
        }
    }

    fun parseExpense(input: String) {
        viewModelScope.launch {
            _isParsing.value = true
            try {
                val parser = getActiveParser()
                val result = parser.parse(input)
                _parsedExpense.value = result
            } finally {
                _isParsing.value = false
            }
        }
    }

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

    fun clearParsedExpense() {
        _parsedExpense.value = null
    }

    fun saveExpense(parsed: ParsedExpense) {
        viewModelScope.launch {
            val vehiclesList = vehicleRepository.getAllVehicles().first()
            val vehicleId = if (vehiclesList.isNotEmpty()) {
                vehiclesList.first().id
            } else {
                val defaultVehicle = com.carflow.app.data.entity.VehicleEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Veicolo principale",
                    make = "",
                    model = "",
                    year = null,
                    licensePlate = "",
                    odometerKm = 0.0,
                    fuelType = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                vehicleRepository.insertVehicle(defaultVehicle)
                defaultVehicle.id
            }

            val expense = com.carflow.app.data.entity.ExpenseEntity(
                id = UUID.randomUUID().toString(),
                vehicleId = vehicleId,
                category = parsed.category.name,
                amount = parsed.amount ?: 0.0,
                quantity = parsed.quantity,
                quantityUnit = parsed.quantityUnit?.name,
                description = parsed.description,
                date = parsed.date ?: System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            expenseRepository.insertExpense(expense)
        }
    }

    fun saveFuelExpense(
        vehicleId: String?,
        fuelType: String,
        totalPrice: Double,
        pricePerLiter: Double?,
        quantity: Double?,
        odometerKm: Double?,
        isFullTank: Boolean,
        gasStationName: String?,
        gasStationLocation: String?,
        description: String,
        date: Long
    ) {
        viewModelScope.launch {
            val actualVehicleId = vehicleId ?: run {
                val vehiclesList = vehicleRepository.getAllVehicles().first()
                if (vehiclesList.isNotEmpty()) {
                    vehiclesList.first().id
                } else {
                    val defaultVehicle = com.carflow.app.data.entity.VehicleEntity(
                        id = UUID.randomUUID().toString(),
                        name = "Veicolo principale",
                        make = "",
                        model = "",
                        year = null,
                        licensePlate = "",
                        odometerKm = 0.0,
                        fuelType = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    vehicleRepository.insertVehicle(defaultVehicle)
                    defaultVehicle.id
                }
            }

            expenseRepository.create(
                vehicleId = actualVehicleId,
                category = "FUEL",
                subcategory = fuelType,
                amount = totalPrice,
                quantity = quantity,
                quantityUnit = "LITERS",
                description = description,
                date = date,
                odometerKm = odometerKm,
                isFullTank = isFullTank,
                gasStationName = gasStationName,
                gasStationLocation = gasStationLocation,
                pricePerLiter = pricePerLiter
            )
        }
    }
}
