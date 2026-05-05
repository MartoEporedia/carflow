package com.carflow.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("vehicleId"),
        Index("category"),
        Index("date"),
        Index("isDeleted")
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String, // UUID
    val vehicleId: String,
    val category: String, // FUEL, MAINTENANCE, EXTRA, or custom
    val subcategory: String = "",
    val amount: Double,
    val quantity: Double? = null,
    val quantityUnit: String? = null, // LITERS, KWH
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val odometerKm: Double? = null,
    val isFullTank: Boolean = false,
    val gasStationName: String? = null,
    val gasStationLocation: String? = null,
    val pricePerLiter: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
