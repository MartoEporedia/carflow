package com.carflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.data.entity.ExpenseEntity
import com.carflow.app.ui.components.Bars
import com.carflow.app.ui.components.CarflowCard
import com.carflow.app.ui.components.MonoEyebrow
import com.carflow.app.ui.components.SectionHeader
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

private val MONTHS_IT = listOf("gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic")
private val MONTHLY_MOCK = listOf(202, 816, 916, 169, 163, 252, 248, 293, 379, 166, 131, 216)

@Composable
fun StatsScreen(vm: CarflowViewModel) {
    val c = carflowColors
    val expenses by vm.expenses.collectAsState()
    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)

    val byCat = mapOf(
        "FUEL" to expenses.filter { it.category == "FUEL" }.sumOf { it.amount },
        "MAINTENANCE" to expenses.filter { it.category == "MAINTENANCE" }.sumOf { it.amount },
        "EXTRA" to expenses.filter { it.category == "EXTRA" }.sumOf { it.amount },
    )
    val total = byCat.values.sum()
    val fuelExpenses = expenses.filter { it.category == "FUEL" && it.quantity != null }
    val totalLiters = fuelExpenses.sumOf { it.quantity ?: 0.0 }
    val totalFuelCost = fuelExpenses.sumOf { it.amount }
    val avgPrice = if (totalLiters > 0) totalFuelCost / totalLiters else 0.0

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        MonoEyebrow("carflow / statistiche")
        Spacer(Modifier.height(6.dp))
        Text(
            "I tuoi numeri",
            fontSize = 34.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = (-1).sp,
            color = c.fg,
        )

        Spacer(Modifier.height(18.dp))

        // Big total
        CarflowCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("TOTALE 2026", fontFamily = MonoFamily, fontSize = 10.sp, letterSpacing = 1.sp, color = c.fg3)
                Spacer(Modifier.height(6.dp))
                Text(
                    "€${total.toLong()}",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.W500,
                    letterSpacing = (-1.6).sp,
                    lineHeight = 52.sp,
                    color = c.fg,
                )

                Spacer(Modifier.height(20.dp))

                listOf("FUEL" to "Carburante", "MAINTENANCE" to "Manutenzione", "EXTRA" to "Extra").forEach { (k, label) ->
                    val amt = byCat[k] ?: 0.0
                    val pct = if (total > 0) (amt / total * 100).toInt() else 0
                    val barColor = catAccentFromString(k, c)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor),
                            )
                            Text(label.uppercase(), fontFamily = MonoFamily, fontSize = 11.sp, color = c.fg2)
                        }
                        Text(
                            "€${amt.toLong()} · $pct%",
                            fontFamily = MonoFamily,
                            fontSize = 11.sp,
                            color = c.fg2,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(c.line),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (pct / 100f).coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(barColor),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Fuel stat tiles
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "Litri 2026",
                value = "${totalLiters.toLong()}",
                unit = "L",
                accent = c.accent,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "Prezzo medio",
                value = "€${String.format("%.2f", avgPrice)}",
                unit = "/L",
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = "Voci totali",
                value = "${expenses.size}",
                unit = "",
                modifier = Modifier.weight(1f),
            )
            StatTile(
                label = "Confidenza media",
                value = "96",
                unit = "%",
                accent = c.ok,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(14.dp))

        // 12 month chart
        CarflowCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text("· 12 MESI", fontFamily = MonoFamily, fontSize = 10.sp, letterSpacing = 1.sp, color = c.fg3)
                        Spacer(Modifier.height(2.dp))
                        Text("Storico mensile", fontSize = 18.sp, fontWeight = FontWeight.W500, color = c.fg)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendDot(color = c.accent, label = "Fuel")
                        LegendDot(color = Color(0xFF8AB4FF), label = "Main")
                        LegendDot(color = Color(0xFFFF9F43), label = "Extra")
                    }
                }
                Spacer(Modifier.height(14.dp))

                Bars(
                    data = MONTHLY_MOCK,
                    highlightIdx = currentMonth,
                    height = 140.dp,
                    tone = c.accent,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MONTHS_IT.forEachIndexed { i, m ->
                        Text(
                            m.uppercase(),
                            modifier = Modifier.weight(1f),
                            fontFamily = MonoFamily,
                            fontSize = 8.sp,
                            letterSpacing = 0.sp,
                            color = if (i == currentMonth) c.fg else c.fg3,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Percentile bar
        CarflowCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("· CONFRONTO MODELLO", fontFamily = MonoFamily, fontSize = 10.sp, letterSpacing = 1.sp, color = c.fg3)
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("Sei nel ", fontSize = 18.sp, fontWeight = FontWeight.W500, color = c.fg)
                    Text("15° percentile", fontSize = 18.sp, fontWeight = FontWeight.W500, color = c.accent)
                    Text(" per consumi", fontSize = 18.sp, fontWeight = FontWeight.W500, color = c.fg)
                }
                Spacer(Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    listOf(c.ok, c.warn, c.danger),
                                ),
                            ),
                    )
                    // Marker at 15%
                    Box(
                        modifier = Modifier
                            .padding(start = (0.15f * 1f * 300).dp)
                            .align(Alignment.CenterStart)
                            .width(3.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c.fg),
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("MIGLIORE 4.2", fontFamily = MonoFamily, fontSize = 10.sp, color = c.fg3)
                    Text("MEDIA 6.8", fontFamily = MonoFamily, fontSize = 10.sp, color = c.fg3)
                    Text("PEGGIORE 9.5", fontFamily = MonoFamily, fontSize = 10.sp, color = c.fg3)
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    unit: String,
    accent: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    val c = carflowColors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(c.card)
            .border(0.5.dp, c.line, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column {
            Text(label.uppercase(), fontFamily = MonoFamily, fontSize = 9.sp, letterSpacing = 0.6.sp, color = c.fg3)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = (-0.4).sp,
                    color = if (accent != Color.Unspecified) accent else c.fg,
                )
                if (unit.isNotBlank()) {
                    Text(unit, fontFamily = MonoFamily, fontSize = 11.sp, color = c.fg3, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val c = carflowColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(label.uppercase(), fontFamily = MonoFamily, fontSize = 9.sp, color = c.fg2)
    }
}
