package com.carflow.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey
    val id: String, // UUID
    val name: String,
    val make: String = "",
    val model: String = "",
    val year: Int? = null,
    val licensePlate: String = "",
    val odometerKm: Double = 0.0,
    val fuelType: String = "", // petrol, diesel, electric, hybrid, lpg, cng
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
