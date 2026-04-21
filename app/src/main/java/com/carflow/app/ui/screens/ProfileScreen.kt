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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
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
import com.carflow.app.ui.viewmodel.CarflowViewModel
import com.carflow.app.ui.theme.MonoFamily
import com.carflow.app.ui.theme.carflowColors

@Composable
fun ProfileScreen(
    vm: CarflowViewModel,
    onBack: () -> Unit,
) {
    val c = carflowColors
    val vehicles by vm.vehicles.collectAsState()
    val expenses by vm.expenses.collectAsState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp, bottom = 30.dp),
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
            Icon(Icons.Outlined.ChevronLeft, contentDescription = null, tint = c.fg, modifier = Modifier.size(20.dp))
        }

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(c.accent, Color(0xFFFF9F43))),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("MP", fontSize = 30.sp, fontWeight = FontWeight.W700, color = c.accentInk)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Marto P.",
                fontSize = 26.sp,
                fontWeight = FontWeight.W500,
                letterSpacing = (-0.4).sp,
                color = c.fg,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "${vehicles.size} VEICOLI · ${expenses.size} VOCI".uppercase(),
                fontFamily = MonoFamily,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = c.fg2,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "I miei veicoli",
                "Parole chiave personalizzate",
                "Esporta dati (JSON)",
                "Lingua · Italiano",
                "Tema",
            ).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(c.card)
                        .border(0.5.dp, c.line, RoundedCornerShape(14.dp))
                        .clickable { }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item, fontSize = 14.sp, color = c.fg)
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = c.fg3, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
