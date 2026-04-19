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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.ui.components.CarSilhouette
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    vm: CarflowViewModel,
    onBack: () -> Unit,
) {
    val c = carflowColors
    val vehicles by vm.vehicles.collectAsState()
    val expenses by vm.expenses.collectAsState()
    val vehicle = vehicles.find { it.id == vehicleId } ?: return
    val vehicleExpenses = expenses.filter { it.vehicleId == vehicleId }

    val accentColor = when {
        vehicle.fuelType == "electric" -> Color(0xFF6EE7A7)
        vehicle.name.contains("Nina", ignoreCase = true) -> Color(0xFFD4FF3A)
        else -> Color(0xFFFF5630)
    }

    val totalFuel = vehicleExpenses.filter { it.category == "FUEL" }.sumOf { it.amount }
    val totalL = vehicleExpenses.filter { it.category == "FUEL" && it.quantity != null }.sumOf { it.quantity ?: 0.0 }
    val totalMaint = vehicleExpenses.filter { it.category == "MAINTENANCE" }.sumOf { it.amount }
    val totalExtra = vehicleExpenses.filter { it.category == "EXTRA" }.sumOf { it.amount }

    var tab by remember { mutableStateOf("overview") }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.28f),
                            c.bg,
                        ),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(c.card)
                            .border(0.5.dp, c.line, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "Indietro", tint = c.fg, modifier = Modifier.size(20.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(c.card)
                            .border(0.5.dp, c.line, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = c.fg, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "· ${vehicle.year ?: ""} · ${vehicle.fuelType}".uppercase(),
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = c.fg3,
                )
                Text(
                    vehicle.name,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.W500,
                    letterSpacing = (-1.2).sp,
                    lineHeight = 40.sp,
                    color = c.fg,
                )
                Text(
                    "${vehicle.make} ${vehicle.model} · ${vehicle.licensePlate}",
                    fontSize = 14.sp,
                    color = c.fg2,
                )

                Spacer(Modifier.height(18.dp))

                CarSilhouette(
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                )
            }
        }

        // Stats card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(c.cardHi)
                .border(0.5.dp, c.line, RoundedCornerShape(22.dp))
                .padding(20.dp),
        ) {
            Column {
                Text(
                    "CONTACHILOMETRI",
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = c.fg3,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        vehicle.odometerKm.toLong().toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.W500,
                        letterSpacing = (-1).sp,
                        color = c.fg,
                    )
                    Text("km", fontFamily = MonoFamily, fontSize = 16.sp, color = c.fg3, modifier = Modifier.padding(bottom = 4.dp))
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Stat2(
                        label = "Fuel",
                        value = "€${totalFuel.toLong()}",
                        sub = "${totalL.toLong()}L",
                        color = c.accent,
                    )
                    Stat2(
                        label = "Maint",
                        value = "€${totalMaint.toLong()}",
                        sub = "${vehicleExpenses.count { it.category == "MAINTENANCE" }} voci",
                        color = Color(0xFF8AB4FF),
                    )
                    Stat2(
                        label = "Extra",
                        value = "€${totalExtra.toLong()}",
                        sub = "${vehicleExpenses.count { it.category == "EXTRA" }} voci",
                        color = Color(0xFFFF9F43),
                    )
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // Expenses timeline
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            Text(
                "· CRONOLOGIA",
                fontFamily = MonoFamily,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = c.fg3,
            )
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vehicleExpenses.forEach { e ->
                    ExpenseRowItem(e)
                }
                if (vehicleExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Nessuna spesa registrata", fontFamily = MonoFamily, fontSize = 12.sp, color = c.fg3)
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Stat2(label: String, value: String, sub: String, color: Color) {
    val c = carflowColors
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
            Text(label.uppercase(), fontFamily = MonoFamily, fontSize = 9.sp, letterSpacing = 0.6.sp, color = c.fg3)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.W600, letterSpacing = (-0.2).sp, color = c.fg)
        Text(sub, fontFamily = MonoFamily, fontSize = 10.sp, color = c.fg2)
    }
}
