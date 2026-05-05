package com.carflow.app.ui.screens.chat.viewmodel

import com.carflow.parser.model.ExpenseCategory

enum class MessageRole { USER, SYSTEM }

enum class ContentType { TEXT, IMAGE, CONFIRMATION_SUMMARY }

enum class RequiredField { AMOUNT, CATEGORY, VEHICLE, DATE, LITERS, PRICE_PER_LITER }

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val contentType: ContentType,
    val text: String? = null,
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DraftExpense(
    val amount: Double? = null,
    val category: ExpenseCategory? = null,
    val vehicleId: String? = null,
    val date: Long? = null,
    val description: String? = null,
    val fuelType: String? = null,
    val liters: Double? = null,
    val pricePerLiter: Double? = null,
    val warnings: List<String> = emptyList()
)

sealed class ConversationState {
    object Idle : ConversationState()
    object Processing : ConversationState()
    data class AwaitingAnswer(
        val field: RequiredField,
        val options: List<String>? = null
    ) : ConversationState()
    data class Confirming(val draft: DraftExpense) : ConversationState()
    object Saved : ConversationState()
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val conversationState: ConversationState = ConversationState.Idle,
    val inputText: String = "",
    val isAttachEnabled: Boolean = true,
    val errorMessage: String? = null
)
