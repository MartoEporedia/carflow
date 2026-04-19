package com.carflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.ui.components.CarflowCard
import com.carflow.app.ui.components.MonoEyebrow
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

@Composable
fun ExpensesScreen(vm: CarflowViewModel) {
    val c = carflowColors
    val expenses by vm.expenses.collectAsState()
    var filter by remember { mutableStateOf("all") }

    val filtered = when (filter) {
        "all" -> expenses
        else -> expenses.filter { it.category == filter }
    }
    val total = filtered.sumOf { it.amount }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        MonoEyebrow("carflow / spese")
        Spacer(Modifier.height(6.dp))
        Text(
            "Tutte le voci",
            fontSize = 34.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = (-1).sp,
            color = c.fg,
        )

        Spacer(Modifier.height(18.dp))

        // Category filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip(
                label = "Tutte",
                count = expenses.size,
                active = filter == "all",
                onClick = { filter = "all" },
                color = c.fg,
            )
            listOf("FUEL" to "Carburante", "MAINTENANCE" to "Manutenzione", "EXTRA" to "Extra").forEach { (k, label) ->
                FilterChip(
                    label = label,
                    count = expenses.count { it.category == k },
                    active = filter == k,
                    onClick = { filter = k },
                    color = catAccentFromString(k, c),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Total bar
        CarflowCard(elevated = true, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        "TOTALE VISIBILE",
                        fontFamily = MonoFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = c.fg3,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "€${String.format("%.2f", total)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-0.8).sp,
                        color = c.fg,
                    )
                }
                Text(
                    "${filtered.size} voci",
                    fontFamily = MonoFamily,
                    fontSize = 11.sp,
                    color = c.fg2,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Expense list
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filtered.forEach { e ->
                ExpenseRowItem(e)
            }
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Nessuna voce",
                        fontFamily = MonoFamily,
                        fontSize = 12.sp,
                        color = c.fg3,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    active: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color,
) {
    val c = carflowColors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) color else c.card)
            .border(0.5.dp, c.line, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.W500,
            color = if (active) {
                if (color == c.fg) c.bg else c.accentInk
            } else c.fg2,
        )
        Text(
            count.toString(),
            fontFamily = MonoFamily,
            fontSize = 10.sp,
            color = if (active) {
                if (color == c.fg) c.bg.copy(alpha = 0.6f) else c.accentInk.copy(alpha = 0.7f)
            } else c.fg3,
        )
    }
}
