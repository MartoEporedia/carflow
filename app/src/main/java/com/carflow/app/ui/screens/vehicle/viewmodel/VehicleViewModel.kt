package com.carflow.app.ui.screens.vehicle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<VehicleEntity>>(emptyList())
    val vehicles: StateFlow<List<VehicleEntity>> = _vehicles.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingVehicle = MutableStateFlow<VehicleEntity?>(null)
    val editingVehicle: StateFlow<VehicleEntity?> = _editingVehicle.asStateFlow()

    private val _showDeleteConfirm = MutableStateFlow<VehicleEntity?>(null)
    val showDeleteConfirm: StateFlow<VehicleEntity?> = _showDeleteConfirm.asStateFlow()

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect { _vehicles.value = it }
        }
    }

    fun openAddDialog() {
        _editingVehicle.value = null
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun openEditDialog(vehicle: VehicleEntity) {
        _showAddDialog.value = false
        _editingVehicle.value = vehicle
    }

    fun closeEditDialog() {
        _editingVehicle.value = null
    }

    fun addVehicle(
        name: String,
        make: String,
        model: String,
        year: Int?,
        licensePlate: String,
        fuelType: String,
        odometerKm: Double
    ) {
        viewModelScope.launch {
            vehicleRepository.create(
                name = name,
                make = make,
                model = model,
                year = year,
                licensePlate = licensePlate,
                fuelType = fuelType,
                odometerKm = odometerKm
            )
            _showAddDialog.value = false
        }
    }

    fun updateVehicle(
        vehicle: VehicleEntity,
        name: String,
        make: String,
        model: String,
        year: Int?,
        licensePlate: String,
        fuelType: String,
        odometerKm: Double
    ) {
        viewModelScope.launch {
            vehicleRepository.update(
                vehicle.copy(
                    name = name,
                    make = make,
                    model = model,
                    year = year,
                    licensePlate = licensePlate,
                    fuelType = fuelType,
                    odometerKm = odometerKm
                )
            )
            _editingVehicle.value = null
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            if (vehicleRepository.hasExpenses(vehicle.id)) {
                _showDeleteConfirm.value = vehicle
            } else {
                vehicleRepository.delete(vehicle.id)
            }
        }
    }

    fun confirmDelete(vehicle: VehicleEntity) {
        viewModelScope.launch {
            vehicleRepository.delete(vehicle.id)
            _showDeleteConfirm.value = null
        }
    }

    fun clearDeleteConfirm() {
        _showDeleteConfirm.value = null
    }
}
