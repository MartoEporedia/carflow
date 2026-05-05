package com.carflow.app.ui.screens.vehicle

object FuelTypeUiModel {
    val entries: List<Pair<String, String>> = listOf(
        "(non specificato)" to "",
        "Benzina" to "petrol",
        "Diesel" to "diesel",
        "Elettrico" to "electric",
        "Ibrido" to "hybrid",
        "GPL" to "lpg",
        "Metano" to "cng"
    )

    fun labelFor(value: String): String =
        entries.firstOrNull { it.second == value }?.first ?: ""
}
