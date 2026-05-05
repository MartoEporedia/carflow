package com.carflow.parser.model

import kotlinx.serialization.Serializable

@Serializable
data class ParsedExpense(
    val category: ExpenseCategory,
    val subcategory: String? = null,
    val amount: Double? = null,
    val quantity: Double? = null,
    val quantityUnit: QuantityUnit? = null,
    val pricePerLiter: Double? = null,
    val description: String = "",
    val confidence: ParseConfidence = ParseConfidence.HIGH,
    val warnings: List<String> = emptyList(),
    val rawInput: String = "",
    val date: Long? = null,
    val fuelType: FuelType? = null
)

@Serializable
enum class ExpenseCategory {
    FUEL,
    MAINTENANCE,
    EXTRA,
    UNKNOWN
}

@Serializable
enum class QuantityUnit {
    LITERS,
    KWH
}

@Serializable
@Serializable
enum class FuelType {
    PETROL,
    DIESEL,
    LPG,
    CNG,
    ELECTRIC,
    HYBRID
}

@Serializable
enum class ParseConfidence {
    HIGH,
    MEDIUM,
    LOW
}
