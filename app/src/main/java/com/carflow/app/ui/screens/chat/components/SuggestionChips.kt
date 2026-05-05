package com.carflow.app.ui.screens.chat.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SuggestionChips(
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            SuggestionChip(
                onClick = { onOptionSelected(option) },
                label = { Text(option) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionChipsPreview() {
    SuggestionChips(
        options = listOf("Carburante", "Manutenzione", "Extra"),
        onOptionSelected = {}
    )
}
