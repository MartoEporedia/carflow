package com.carflow.app.ui.screens.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onAttachClick,
                enabled = isEnabled
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Allega immagine",
                    tint = if (isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                enabled = isEnabled,
                placeholder = { Text("Descrivi la spesa…") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onSendClick,
                enabled = isEnabled && text.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Invia",
                    tint = if (isEnabled && text.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Input row — enabled with text")
@Composable
private fun ChatInputRowEnabledPreview() {
    ChatInputRow(
        text = "50 euro benzina",
        onTextChange = {},
        onSendClick = {},
        onAttachClick = {},
        isEnabled = true
    )
}

@Preview(showBackground = true, name = "Input row — disabled")
@Composable
private fun ChatInputRowDisabledPreview() {
    ChatInputRow(
        text = "",
        onTextChange = {},
        onSendClick = {},
        onAttachClick = {},
        isEnabled = false
    )
}
