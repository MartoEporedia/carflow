package com.carflow.parser.model

import kotlinx.serialization.Serializable

@Serializable
enum class FuelType(val keywords: List<String>) {
    PETROL(listOf("benzina", "super", "verde", "unleaded", "petrol", "gasoline")),
    DIESEL(listOf("diesel", "gasolio", "nafta")),
    ELECTRIC(listOf("ricarica", "elettrico", "elettrica", "electric", "charging", "charge")),
    HYBRID(listOf("ibrido", "ibrida", "hybrid")),
    LPG(listOf("gpl", "lpg")),
    CNG(listOf("metano", "cng", "gnc"));

    companion object {
        fun fromKeyword(keyword: String): FuelType? {
            val lower = keyword.lowercase().trim()
            return entries.firstOrNull { type ->
                type.keywords.any { it == lower }
            }
        }
    }
}
