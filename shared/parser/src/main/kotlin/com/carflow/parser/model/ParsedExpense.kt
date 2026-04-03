package com.carflow.parser.model

import kotlinx.serialization.Serializable

@Serializable
data class ParsedExpense(
    val category: ExpenseCategory,
    val subcategory: String? = null,
    val amount: Double? = null,
    val quantity: Double? = null,
    val quantityUnit: QuantityUnit? = null,
    val description: String = "",
    val confidence: ParseConfidence = ParseConfidence.HIGH,
    val warnings: List<String> = emptyList()
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
enum class ParseConfidence {
    HIGH,
    MEDIUM,
    LOW
}
