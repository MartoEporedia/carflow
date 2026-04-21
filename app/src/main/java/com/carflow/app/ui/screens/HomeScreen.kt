package com.carflow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.data.entity.ExpenseEntity
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.ui.components.CarflowCard
import com.carflow.app.ui.components.CarflowTag
import com.carflow.app.ui.components.MonoEyebrow
import com.carflow.app.ui.components.MonoTag
import com.carflow.app.ui.components.SectionHeader
import com.carflow.app.ui.components.TagTone
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors
import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.ParseConfidence
import com.carflow.parser.model.ParsedExpense

private val DEMO_INPUTS = listOf(
    "benzina 50€ 30L",
    "tagliando 200 euro",
    "parcheggio 5€",
    "bollo 250€",
    "gasolio 65€",
    "gomme invernali 320€",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: CarflowViewModel,
    onNavStats: () -> Unit,
    onNavProfile: () -> Unit,
    onNavExpenses: () -> Unit,
) {
    val c = carflowColors
    val vehicles by vm.vehicles.collectAsState()
    val expenses by vm.recentExpenses.collectAsState()
    val selectedId by vm.selectedVehicleId.collectAsState()
    val inputText by vm.inputText.collectAsState()
    val parsed by vm.parsedExpense.collectAsState()

    val activeVehicle = vehicles.find { it.id == selectedId } ?: vehicles.firstOrNull()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Brand strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(c.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("c", fontSize = 18.sp, fontWeight = FontWeight.W600, color = c.accentInk)
                }
                Text(
                    "CARFLOW",
                    fontFamily = MonoFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.4.sp,
                    color = c.fg2,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.card)
                        .border(0.5.dp, c.line, RoundedCornerShape(10.dp))
                        .clickable(onClick = onNavStats),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null, tint = c.fg, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c.accent)
                        .clickable(onClick = onNavProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("MP", fontSize = 13.sp, fontWeight = FontWeight.W700, color = c.accentInk)
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // Vehicle switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            vehicles.forEach { v ->
                val isActive = v.id == (activeVehicle?.id)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isActive) c.fg else c.card)
                        .border(0.5.dp, c.line, RoundedCornerShape(99.dp))
                        .clickable { vm.selectVehicle(v.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isActive) c.bg else c.accent),
                    )
                    Text(v.name, fontSize = 13.sp, fontWeight = FontWeight.W500, color = if (isActive) c.bg else c.fg2)
                    Text(
                        v.licensePlate.replace(" ", ""),
                        fontFamily = MonoFamily,
                        fontSize = 10.sp,
                        color = if (isActive) c.bg.copy(alpha = 0.6f) else c.fg3,
                    )
                }
            }
            // Add vehicle placeholder
            Text(
                "+ auto",
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .border(0.5.dp, c.line2, RoundedCornerShape(99.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                fontSize = 13.sp,
                color = c.fg3,
            )
        }

        Spacer(Modifier.height(22.dp))

        // Hero headline
        Column {
            MonoEyebrow("scrivi. basta.")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Cos'hai speso oggi?",
                fontSize = 36.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = (-1.2).sp,
                lineHeight = 36.sp,
                color = c.fg,
            )
        }

        Spacer(Modifier.height(14.dp))

        // Big input card
        NLInputCard(
            inputText = inputText,
            parsed = parsed,
            onInputChanged = { vm.onInputChanged(it) },
            onSave = { activeVehicle?.let { v -> vm.saveExpense(v.id) } },
        )

        Spacer(Modifier.height(18.dp))

        // Demo chips
        if (inputText.isBlank()) {
            Column {
                Text(
                    "· PROVA COSÌ",
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = c.fg3,
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DEMO_INPUTS.forEach { demo ->
                        Text(
                            text = "· $demo",
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(c.card)
                                .border(0.5.dp, c.line, RoundedCornerShape(99.dp))
                                .clickable { vm.onInputChanged(demo) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            fontFamily = MonoFamily,
                            fontSize = 11.sp,
                            color = c.fg2,
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        // Month summary
        activeVehicle?.let { v ->
            MonthSummaryCard(expenses = expenses, vehicleName = v.name)
            Spacer(Modifier.height(28.dp))
        }

        // Recent expenses
        SectionHeader(
            title = "Ultimi ingressi",
            action = {
                Text(
                    "tutti →",
                    modifier = Modifier.clickable(onClick = onNavExpenses),
                    fontFamily = MonoFamily,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = c.fg2,
                )
            },
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            expenses.take(5).forEach { e ->
                ExpenseRowItem(e)
            }
        }

        Spacer(Modifier.height(32.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "OFFLINE · NESSUN API",
                fontFamily = MonoFamily,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = c.fg3,
            )
            Text(
                "carflow",
                fontSize = 18.sp,
                color = c.fg3,
            )
        }
    }
}

@Composable
fun NLInputCard(
    inputText: String,
    parsed: ParsedExpense?,
    onInputChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val c = carflowColors
    val canSave = parsed != null && parsed.confidence != ParseConfidence.LOW

    CarflowCard(
        elevated = true,
        padding = 18.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (inputText.isNotBlank()) 1.5.dp else 0.5.dp,
                color = if (inputText.isNotBlank()) c.accent else c.line,
                shape = RoundedCornerShape(22.dp),
            ),
    ) {
        Column {
            androidx.compose.material3.TextField(
                value = inputText,
                onValueChange = onInputChanged,
                placeholder = {
                    Text(
                        "es. benzina 50€ 30L eni",
                        fontSize = 22.sp,
                        color = c.fg3,
                        fontWeight = FontWeight.W500,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W500,
                    color = c.fg,
                    letterSpacing = (-0.3).sp,
                ),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                ),
            )

            // Parse preview
            AnimatedVisibility(visible = parsed != null, enter = fadeIn(), exit = fadeOut()) {
                parsed?.let { p ->
                    Column {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(c.line),
                        )
                        Spacer(Modifier.height(14.dp))
                        ParsePreviewRow(p)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MicroChip("foto scontrino")
                    MicroChip("data")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (canSave) c.accent else c.card)
                        .border(0.5.dp, if (canSave) Color.Transparent else c.line, RoundedCornerShape(99.dp))
                        .clickable(enabled = canSave, onClick = onSave)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                ) {
                    Text(
                        "salva →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = if (canSave) c.accentInk else c.fg3,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParsePreviewRow(parsed: ParsedExpense) {
    val c = carflowColors
    val confColor = when (parsed.confidence) {
        ParseConfidence.HIGH   -> c.ok
        ParseConfidence.MEDIUM -> c.warn
        ParseConfidence.LOW    -> c.danger
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("→", fontFamily = MonoFamily, fontSize = 10.sp, color = c.fg3, letterSpacing = 0.8.sp)

            // Category pill
            val catColor = catAccent(parsed.category, c)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(catColor.copy(alpha = 0.16f))
                    .border(0.5.dp, catColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(catColor))
                Text(
                    catLabel(parsed.category, parsed.subcategory).uppercase(),
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    letterSpacing = 0.6.sp,
                    color = catColor,
                    fontWeight = FontWeight.W500,
                )
            }

            // Amount token
            parsed.amount?.let { amt ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.accent)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("€", fontFamily = MonoFamily, fontSize = 11.sp, color = c.accentInk.copy(alpha = 0.7f), fontWeight = FontWeight.W600)
                    Text(String.format("%.2f", amt).replace('.', ','), fontFamily = MonoFamily, fontSize = 11.sp, color = c.accentInk, fontWeight = FontWeight.W600)
                }
            }

            // Quantity token
            parsed.quantity?.let { qty ->
                val unit = if (parsed.quantityUnit?.name == "KWH") "kWh" else "L"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(c.card)
                        .border(0.5.dp, c.line2, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(unit, fontFamily = MonoFamily, fontSize = 11.sp, color = c.fg3, fontWeight = FontWeight.W600)
                    Text(qty.toString(), fontFamily = MonoFamily, fontSize = 11.sp, color = c.fg, fontWeight = FontWeight.W600)
                }
            }

            // Confidence badge
            Row(
                modifier = Modifier.padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(confColor))
                Text(
                    parsed.confidence.name,
                    fontFamily = MonoFamily,
                    fontSize = 9.sp,
                    letterSpacing = 0.8.sp,
                    color = confColor,
                )
            }
        }

        if (parsed.warnings.isNotEmpty()) {
            Text(
                "! ${parsed.warnings.joinToString(" · ")}",
                fontFamily = MonoFamily,
                fontSize = 10.sp,
                color = c.warn,
            )
        }
    }
}

@Composable
private fun MicroChip(label: String) {
    val c = carflowColors
    Text(
        text = label,
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(c.card)
            .border(0.5.dp, c.line, RoundedCornerShape(99.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        fontFamily = MonoFamily,
        fontSize = 10.sp,
        letterSpacing = 0.6.sp,
        color = c.fg2,
    )
}

@Composable
fun MonthSummaryCard(expenses: List<ExpenseEntity>, vehicleName: String) {
    val c = carflowColors
    val total = expenses.sumOf { it.amount }

    CarflowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "APRILE 2026",
                    fontFamily = MonoFamily,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = c.fg3,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text(
                        "€${total.toLong()}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-1).sp,
                        lineHeight = 40.sp,
                        color = c.fg,
                    )
                    val cents = ((total % 1) * 100).toLong().toString().padStart(2, '0')
                    Text(
                        ",$cents",
                        fontSize = 18.sp,
                        color = c.fg3,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${expenses.size} voci · $vehicleName",
                    fontFamily = MonoFamily,
                    fontSize = 10.sp,
                    color = c.fg2,
                )
            }
        }
    }
}

@Composable
fun ExpenseRowItem(e: ExpenseEntity, onClick: (() -> Unit)? = null) {
    val c = carflowColors
    val catColor = catAccentFromString(e.category, c)
    val catLabel = catLabelFromString(e.category, e.subcategory)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.card)
            .border(0.5.dp, c.line, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(catColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(catGlyph(e.category), fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    catLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = c.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                e.quantity?.let { qty ->
                    val unit = if (e.quantityUnit == "KWH") "kWh" else "L"
                    Text(
                        "${qty}${unit}",
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(c.bg2)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        fontFamily = MonoFamily,
                        fontSize = 10.sp,
                        color = c.fg2,
                    )
                }
            }
            Text(
                "${e.description.ifBlank { e.category }}".uppercase(),
                fontFamily = MonoFamily,
                fontSize = 10.sp,
                letterSpacing = 0.4.sp,
                color = c.fg3,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "€${String.format("%.2f", e.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                color = c.fg,
            )
            e.odometerKm?.let { km ->
                Text(
                    "${km.toLong().toLocaleString()} KM",
                    fontFamily = MonoFamily,
                    fontSize = 9.sp,
                    color = c.fg3,
                    letterSpacing = 0.sp,
                )
            }
        }
    }
}

private fun Long.toLocaleString(): String = String.format("%,d", this).replace(',', '.')

fun catAccent(cat: ExpenseCategory, c: com.carflow.app.ui.theme.CarflowColors): Color = when (cat) {
    ExpenseCategory.FUEL        -> c.accent
    ExpenseCategory.MAINTENANCE -> Color(0xFF8AB4FF)
    ExpenseCategory.EXTRA       -> Color(0xFFFF9F43)
    ExpenseCategory.UNKNOWN     -> c.fg3
}

fun catAccentFromString(cat: String, c: com.carflow.app.ui.theme.CarflowColors): Color = when (cat) {
    "FUEL"        -> c.accent
    "MAINTENANCE" -> Color(0xFF8AB4FF)
    "EXTRA"       -> Color(0xFFFF9F43)
    else          -> c.fg3
}

fun catLabel(cat: ExpenseCategory, sub: String?): String = when (cat) {
    ExpenseCategory.FUEL        -> if (sub != null) "Carburante · ${fuelSubLabel(sub)}" else "Carburante"
    ExpenseCategory.MAINTENANCE -> "Manutenzione"
    ExpenseCategory.EXTRA       -> "Extra"
    ExpenseCategory.UNKNOWN     -> "Sconosciuto"
}

fun catLabelFromString(cat: String, sub: String?): String = when (cat) {
    "FUEL"        -> if (!sub.isNullOrBlank()) fuelSubLabel(sub) else "Carburante"
    "MAINTENANCE" -> "Manutenzione"
    "EXTRA"       -> "Extra"
    else          -> "Sconosciuto"
}

fun fuelSubLabel(sub: String): String = when (sub) {
    "petrol"   -> "Benzina"
    "diesel"   -> "Diesel"
    "electric" -> "Elettrico"
    "hybrid"   -> "Ibrido"
    "lpg"      -> "GPL"
    "cng"      -> "Metano"
    else       -> sub.replaceFirstChar { it.uppercase() }
}

fun catGlyph(cat: String): String = when (cat) {
    "FUEL"        -> "⛽"
    "MAINTENANCE" -> "🔧"
    "EXTRA"       -> "📄"
    else          -> "❓"
}
