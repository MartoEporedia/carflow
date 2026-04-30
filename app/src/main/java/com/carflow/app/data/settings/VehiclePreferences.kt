package com.carflow.app.data.settings

import android.content.Context

class VehiclePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("carflow_vehicle_prefs", Context.MODE_PRIVATE)

    fun getLastUsedVehicleId(): String? = prefs.getString("last_used_vehicle_id", null)

    fun setLastUsedVehicleId(id: String) {
        if (id.isBlank()) {
            prefs.edit().remove("last_used_vehicle_id").apply()
        } else {
            prefs.edit().putString("last_used_vehicle_id", id).apply()
        }
    }
}
