package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carflow.parser.model.ParsedExpense
import java.text.NumberFormat
import java.util.*

@Composable
fun ParsedResultCard(
    parsedExpense: ParsedExpense,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Risultato Analisi",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Categoria:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    parsedExpense.category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            parsedExpense.amount?.let { amount ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Importo:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        NumberFormat.getCurrencyInstance(Locale.ITALY).format(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            parsedExpense.quantity?.let { qty ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantità:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "$qty ${parsedExpense.quantityUnit?.name ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (!parsedExpense.description.isBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Descrizione:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        parsedExpense.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (parsedExpense.warnings.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Attenzione:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        parsedExpense.warnings.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conferma e Salva")
            }
        }
    }
}
