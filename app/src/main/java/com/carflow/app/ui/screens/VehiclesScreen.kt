package com.carflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.ui.components.CarSilhouette
import com.carflow.app.ui.components.MonoEyebrow
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

@Composable
fun VehiclesScreen(
    vm: CarflowViewModel,
    onOpenVehicle: (String) -> Unit,
) {
    val c = carflowColors
    val vehicles by vm.vehicles.collectAsState()
    val expenses by vm.expenses.collectAsState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        MonoEyebrow("carflow / veicoli")
        Spacer(Modifier.height(6.dp))
        Text(
            "Il tuo garage",
            fontSize = 34.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = (-1).sp,
            color = c.fg,
        )

        Spacer(Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            vehicles.forEach { v ->
                val vehicleExpenses = expenses.filter { it.vehicleId == v.id }
                val totalSpend = vehicleExpenses.sumOf { it.amount }
                VehicleCard(
                    vehicle = v,
                    totalSpend = totalSpend,
                    onClick = { onOpenVehicle(v.id) },
                )
            }

            // Add vehicle button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, c.line2, RoundedCornerShape(18.dp))
                    .padding(18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = c.fg2, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Aggiungi veicolo", fontSize = 14.sp, color = c.fg2)
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: VehicleEntity,
    totalSpend: Double,
    onClick: () -> Unit,
) {
    val c = carflowColors
    val accentColor = vehicleAccentColor(vehicle)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(c.card)
            .border(0.5.dp, c.line, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                accentColor.copy(alpha = 0.22f),
                                c.card,
                            ),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Column {
                    Text(
                        vehicle.licensePlate.uppercase(),
                        fontFamily = MonoFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = c.fg2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        vehicle.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W500,
                        letterSpacing = (-0.6).sp,
                        color = c.fg,
                    )
                    Text(
                        "${vehicle.make} ${vehicle.model} · ${vehicle.year ?: ""}",
                        fontSize = 13.sp,
                        color = c.fg2,
                    )
                    Spacer(Modifier.height(6.dp))
                    CarSilhouette(
                        accentColor = accentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.card)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "KM · TOTALE 2026",
                        fontFamily = MonoFamily,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp,
                        color = c.fg3,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${vehicle.odometerKm.toLong().toLocaleString()} · €${totalSpend.toLong()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = c.fg,
                    )
                }
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = c.fg3, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun Long.toLocaleString(): String = String.format("%,d", this).replace(',', '.')

private fun vehicleAccentColor(v: VehicleEntity): Color = when {
    v.fuelType == "electric" -> Color(0xFF6EE7A7)
    v.name.contains("Nina", ignoreCase = true) -> Color(0xFFD4FF3A)
    else -> Color(0xFFFF5630)
}
