package com.carflow.app.data.repository

import com.carflow.app.data.dao.VehicleDao
import com.carflow.app.data.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao
) {
    fun getAllActive(): Flow<List<VehicleEntity>> = vehicleDao.getAllActive()

    suspend fun getById(id: String): VehicleEntity? = vehicleDao.getById(id)

    suspend fun create(
        name: String,
        make: String = "",
        model: String = "",
        year: Int? = null,
        licensePlate: String = "",
        fuelType: String = "",
        odometerKm: Double = 0.0
    ): VehicleEntity {
        val vehicle = VehicleEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            make = make,
            model = model,
            year = year,
            licensePlate = licensePlate,
            odometerKm = odometerKm,
            fuelType = fuelType
        )
        vehicleDao.insert(vehicle)
        return vehicle
    }

    suspend fun update(vehicle: VehicleEntity) {
        vehicleDao.update(vehicle.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(id: String) {
        vehicleDao.softDelete(id)
    }

    suspend fun count(): Int = vehicleDao.count()

    fun getAllVehicles(): Flow<List<VehicleEntity>> = getAllActive()

    suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insert(vehicle)
    }
}
