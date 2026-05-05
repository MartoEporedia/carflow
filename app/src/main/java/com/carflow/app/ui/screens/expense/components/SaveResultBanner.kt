package com.carflow.app.ui.screens.expense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.ui.screens.expense.viewmodel.SaveResult
import kotlinx.coroutines.delay

@Composable
fun SaveResultBanner(
    result: SaveResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (result) {
        is SaveResult.Success -> {
            LaunchedEffect(Unit) {
                delay(3000)
                onDismiss()
            }
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "Spesa salvata!",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        is SaveResult.Error -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Riprova", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Success banner")
@Composable
private fun SaveResultBannerSuccessPreview() {
    SaveResultBanner(result = SaveResult.Success, onDismiss = {})
}

@Preview(showBackground = true, name = "Error banner")
@Composable
private fun SaveResultBannerErrorPreview() {
    SaveResultBanner(
        result = SaveResult.Error("Errore nel salvataggio. Riprova."),
        onDismiss = {}
    )
}
