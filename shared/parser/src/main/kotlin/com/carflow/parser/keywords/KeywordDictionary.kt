package com.carflow.parser.keywords

import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.FuelType

/**
 * Extensible keyword dictionary for expense classification.
 *
 * Supports Italian and English keywords out of the box.
 * Users can add custom keywords via [addKeywords].
 */
class KeywordDictionary {

    private val categoryKeywords: MutableMap<ExpenseCategory, MutableSet<String>> = mutableMapOf(
        ExpenseCategory.FUEL to mutableSetOf(
            // Italian
            "benzina", "diesel", "gasolio", "ricarica", "elettrico", "elettrica",
            "carburante", "rifornimento", "pieno", "gpl", "metano", "nafta",
            "super", "verde", "ibrido", "ibrida",
            // English
            "fuel", "gas", "gasoline", "petrol", "charging", "charge",
            "electric", "hybrid", "unleaded", "lpg", "cng"
        ),
        ExpenseCategory.MAINTENANCE to mutableSetOf(
            // Italian
            "tagliando", "gomme", "revisione", "freni", "olio", "filtro",
            "cambio", "pneumatici", "batteria", "cinghia", "candele",
            "ammortizzatori", "scarico", "marmitta", "frizione",
            "convergenza", "bilanciatura", "pastiglie", "dischi",
            "officina", "meccanico", "carrozziere", "carrozzeria",
            "riparazione", "riparaz", "lavaggio", "autolavaggio",
            // English
            "service", "maintenance", "tires", "tyres", "brakes", "oil",
            "filter", "battery", "clutch", "exhaust", "repair", "mechanic",
            "workshop", "car wash", "inspection"
        ),
        ExpenseCategory.EXTRA to mutableSetOf(
            // Italian
            "assicurazione", "bollo", "parcheggio", "multa", "pedaggio",
            "autostrada", "telepass", "area di sosta", "sosta", "ztl",
            "patente", "collaudo", "accessori", "accessorio",
            "tassa", "imposta",
            // English
            "insurance", "tax", "parking", "fine", "toll", "highway",
            "license", "accessories", "accessory", "registration"
        )
    )

    fun getCategory(keyword: String): ExpenseCategory {
        val lower = keyword.lowercase().trim()
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { lower.contains(it) || it.contains(lower) }) {
                return category
            }
        }
        return ExpenseCategory.UNKNOWN
    }

    fun getCategoryExact(keyword: String): ExpenseCategory {
        val lower = keyword.lowercase().trim()
        for ((category, keywords) in categoryKeywords) {
            if (lower in keywords) {
                return category
            }
        }
        return ExpenseCategory.UNKNOWN
    }

    fun getFuelType(keyword: String): FuelType? {
        return FuelType.fromKeyword(keyword)
    }

    fun addKeywords(category: ExpenseCategory, keywords: Collection<String>) {
        categoryKeywords.getOrPut(category) { mutableSetOf() }
            .addAll(keywords.map { it.lowercase().trim() })
    }

    fun getAllKeywords(category: ExpenseCategory): Set<String> {
        return categoryKeywords[category]?.toSet() ?: emptySet()
    }

    fun getAllKeywords(): Map<ExpenseCategory, Set<String>> {
        return categoryKeywords.mapValues { it.value.toSet() }
    }
}
