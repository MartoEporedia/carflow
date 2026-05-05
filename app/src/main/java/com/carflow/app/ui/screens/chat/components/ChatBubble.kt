package com.carflow.app.ui.screens.chat.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carflow.app.ui.screens.chat.viewmodel.ChatMessage
import com.carflow.app.ui.screens.chat.viewmodel.ContentType
import com.carflow.app.ui.screens.chat.viewmodel.MessageRole

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        when (message.contentType) {
            ContentType.TEXT -> TextBubble(
                text = message.text ?: "",
                isUser = isUser
            )
            ContentType.IMAGE -> ImageBubble(
                imageBase64 = message.imageBase64 ?: "",
                isUser = isUser
            )
            ContentType.CONFIRMATION_SUMMARY -> {
                // Rendered by ChatExpenseScreen directly; no bubble here
            }
        }
    }
}

@Composable
private fun TextBubble(text: String, isUser: Boolean) {
    Card(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isUser)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImageBubble(imageBase64: String, isUser: Boolean) {
    val bitmap = remember(imageBase64) {
        try {
            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.widthIn(max = 240.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Immagine scontrino",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "User text bubble")
@Composable
private fun UserTextBubblePreview() {
    ChatBubble(
        message = ChatMessage(
            id = "1",
            role = MessageRole.USER,
            contentType = ContentType.TEXT,
            text = "50 euro benzina oggi da ENI, 35 litri"
        )
    )
}

@Preview(showBackground = true, name = "System text bubble")
@Composable
private fun SystemTextBubblePreview() {
    ChatBubble(
        message = ChatMessage(
            id = "2",
            role = MessageRole.SYSTEM,
            contentType = ContentType.TEXT,
            text = "A quale veicolo vuoi associare la spesa?"
        )
    )
}

@Preview(showBackground = true, name = "System text bubble long")
@Composable
private fun SystemTextBubbleLongPreview() {
    ChatBubble(
        message = ChatMessage(
            id = "3",
            role = MessageRole.SYSTEM,
            contentType = ContentType.TEXT,
            text = "Non hai ancora aggiunto nessun veicolo. Vai alla sezione Veicoli per aggiungerne uno prima di registrare una spesa."
        )
    )
}

@Preview(showBackground = true, name = "User image bubble loading")
@Composable
private fun UserImageBubbleLoadingPreview() {
    ChatBubble(
        message = ChatMessage(
            id = "4",
            role = MessageRole.USER,
            contentType = ContentType.IMAGE,
            imageBase64 = ""
        )
    )
}
