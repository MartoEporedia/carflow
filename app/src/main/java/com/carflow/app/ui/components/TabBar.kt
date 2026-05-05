package com.carflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

enum class CarflowTab { HOME, EXPENSES, ADD, VEHICLES, STATS }

@Composable
fun CarflowTabBar(
    current: CarflowTab,
    onNav: (CarflowTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = carflowColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(c.bg.copy(alpha = 0.9f))
            .border(0.5.dp, c.line, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(top = 10.dp, bottom = 22.dp, start = 18.dp, end = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItem(
                icon = Icons.Outlined.Home,
                label = "Cattura",
                active = current == CarflowTab.HOME,
                onClick = { onNav(CarflowTab.HOME) },
            )
            TabItem(
                icon = Icons.Outlined.List,
                label = "Spese",
                active = current == CarflowTab.EXPENSES,
                onClick = { onNav(CarflowTab.EXPENSES) },
            )
            // FAB center button
            Box(
                modifier = Modifier.offset(y = (-14).dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(c.accent)
                        .clickable { onNav(CarflowTab.ADD) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Aggiungi",
                        tint = c.accentInk,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            TabItem(
                icon = Icons.Outlined.DirectionsCar,
                label = "Veicoli",
                active = current == CarflowTab.VEHICLES,
                onClick = { onNav(CarflowTab.VEHICLES) },
            )
            TabItem(
                icon = Icons.Outlined.BarChart,
                label = "Stats",
                active = current == CarflowTab.STATS,
                onClick = { onNav(CarflowTab.STATS) },
            )
        }
    }
}

@Composable
private fun TabItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val c = carflowColors
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) c.fg else c.fg3,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label.uppercase(),
            fontFamily = MonoFamily,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            color = if (active) c.fg else c.fg3,
            fontWeight = if (active) FontWeight.W600 else FontWeight.Normal,
        )
    }
}
