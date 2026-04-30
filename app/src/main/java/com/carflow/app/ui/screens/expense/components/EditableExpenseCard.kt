package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.ui.screens.expense.viewmodel.EditableExpense
import com.carflow.parser.model.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableExpenseCard(
    expense: EditableExpense,
    onExpenseChange: (EditableExpense) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountValid = expense.amount.toDoubleOrNull()?.let { it > 0 } == true

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Risultato Analisi", style = MaterialTheme.typography.titleMedium)

            // Category
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = expense.category.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoria") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    listOf(
                        ExpenseCategory.FUEL,
                        ExpenseCategory.MAINTENANCE,
                        ExpenseCategory.EXTRA
                    ).forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName()) },
                            onClick = {
                                onExpenseChange(expense.copy(category = cat))
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = expense.amount,
                onValueChange = { v ->
                    onExpenseChange(expense.copy(amount = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
                },
                label = { Text("Importo (€)") },
                singleLine = true,
                isError = expense.amount.isNotBlank() && !amountValid,
                modifier = Modifier.fillMaxWidth()
            )

            // Quantity (shown only when parser detected one)
            if (expense.quantity.isNotBlank() || expense.quantityUnit != null) {
                OutlinedTextField(
                    value = expense.quantity,
                    onValueChange = { v ->
                        onExpenseChange(expense.copy(quantity = v.replace(',', '.').filter { it.isDigit() || it == '.' }))
                    },
                    label = { Text("Quantità${expense.quantityUnit?.let { " (${it.name})" } ?: ""}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            OutlinedTextField(
                value = expense.description,
                onValueChange = { onExpenseChange(expense.copy(description = it)) },
                label = { Text("Descrizione") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // Warnings
            if (expense.warnings.isNotEmpty()) {
                Text(
                    text = "⚠ ${expense.warnings.joinToString("; ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                enabled = amountValid
            ) {
                Text("Conferma e Salva")
            }
        }
    }
}

private fun ExpenseCategory.displayName() = when (this) {
    ExpenseCategory.FUEL -> "Carburante"
    ExpenseCategory.MAINTENANCE -> "Manutenzione"
    ExpenseCategory.EXTRA -> "Extra"
    ExpenseCategory.UNKNOWN -> "Sconosciuto"
}

@Preview(showBackground = true, name = "Editable card — fuel")
@Composable
private fun EditableExpenseCardPreview() {
    EditableExpenseCard(
        expense = EditableExpense(
            category = ExpenseCategory.FUEL,
            amount = "50.0",
            description = "Benzina",
            date = System.currentTimeMillis(),
            fuelType = null,
            quantityUnit = null,
            quantity = "30.0",
            warnings = emptyList()
        ),
        onExpenseChange = {},
        onConfirm = {}
    )
}

@Preview(showBackground = true, name = "Editable card — with warnings")
@Composable
private fun EditableExpenseCardWarningsPreview() {
    EditableExpenseCard(
        expense = EditableExpense(
            category = ExpenseCategory.MAINTENANCE,
            amount = "",
            description = "",
            date = System.currentTimeMillis(),
            fuelType = null,
            quantityUnit = null,
            quantity = "",
            warnings = listOf("Importo non riconosciuto")
        ),
        onExpenseChange = {},
        onConfirm = {}
    )
}
