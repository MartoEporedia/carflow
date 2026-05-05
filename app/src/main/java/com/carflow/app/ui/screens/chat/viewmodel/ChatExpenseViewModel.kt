package com.carflow.app.ui.screens.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.ExpenseRepository
import com.carflow.app.data.repository.VehicleRepository
import com.carflow.app.data.settings.VehiclePreferences
import com.carflow.network.llm.ExpenseParserStrategy
import com.carflow.network.llm.LlmClientFactory
import com.carflow.network.llm.LlmConfigResolver
import com.carflow.network.llm.LlmPrompt
import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.ParsedExpense
import com.carflow.parser.model.QuantityUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatExpenseViewModel @Inject constructor(
    @Named("llm") private val parser: ExpenseParserStrategy,
    private val configResolver: LlmConfigResolver,
    private val expenseRepository: ExpenseRepository,
    private val vehicleRepository: VehicleRepository,
    private val vehiclePreferences: VehiclePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _vehicles = MutableStateFlow<List<VehicleEntity>>(emptyList())
    val vehicles: StateFlow<List<VehicleEntity>> = _vehicles.asStateFlow()

    private val _navigateToVehicle = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToVehicle: SharedFlow<Unit> = _navigateToVehicle.asSharedFlow()

    private var currentDraft = DraftExpense()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    init {
        viewModelScope.launch {
            vehicleRepository.getAllVehicles().collect { list ->
                _vehicles.value = list
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSendText() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val state = _uiState.value.conversationState
        if (state is ConversationState.AwaitingAnswer) {
            onFollowUpAnswer(text)
            return
        }
        if (state != ConversationState.Idle) return

        currentDraft = DraftExpense()
        val userMsg = chatMessage(MessageRole.USER, ContentType.TEXT, text)
        appendMessage(userMsg)
        _uiState.value = _uiState.value.copy(inputText = "", conversationState = ConversationState.Processing)

        viewModelScope.launch {
            try {
                val parsed = parser.parse(text)
                currentDraft = parsed.toDraft()
                advanceConversation()
            } catch (e: Exception) {
                appendSystemMessage("Si è verificato un errore nell'analisi. Riprova.")
                _uiState.value = _uiState.value.copy(conversationState = ConversationState.Idle)
            }
        }
    }

    fun onFollowUpAnswer(answer: String) {
        val state = _uiState.value.conversationState as? ConversationState.AwaitingAnswer ?: return
        val trimmed = answer.trim()

        val userMsg = chatMessage(MessageRole.USER, ContentType.TEXT, trimmed)
        appendMessage(userMsg)
        _uiState.value = _uiState.value.copy(inputText = "")

        currentDraft = resolveFollowUpAnswer(state.field, trimmed, currentDraft, _vehicles.value)
        advanceConversation()
    }

    fun onImageSelected(base64: String, mimeType: String) {
        val state = _uiState.value.conversationState
        if (state != ConversationState.Idle && state !is ConversationState.AwaitingAnswer) return

        currentDraft = DraftExpense()
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            contentType = ContentType.IMAGE,
            imageBase64 = base64,
            imageMimeType = mimeType
        )
        appendMessage(userMsg)
        _uiState.value = _uiState.value.copy(
            conversationState = ConversationState.Processing,
            isAttachEnabled = false
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = configResolver.resolve()
                val client = LlmClientFactory.create(config)
                val response = client.chatWithImage(
                    LlmPrompt.CHAT_SYSTEM,
                    LlmPrompt.imageChatPrompt(mimeType),
                    base64,
                    mimeType
                )
                val parsed = parseJsonResponse(response)
                currentDraft = parsed.toDraft()
                withContext(Dispatchers.Main) { advanceConversation() }
            } catch (e: LlmConfigResolver.UnconfiguredException) {
                withContext(Dispatchers.Main) {
                    appendSystemMessage("LLM non configurato. Descrivi la spesa a parole.")
                    _uiState.value = _uiState.value.copy(
                        conversationState = ConversationState.Idle,
                        isAttachEnabled = true
                    )
                }
            } catch (e: UnsupportedOperationException) {
                withContext(Dispatchers.Main) {
                    appendSystemMessage("Analisi immagine non disponibile per questo provider. Descrivi la spesa a parole.")
                    _uiState.value = _uiState.value.copy(
                        conversationState = ConversationState.Idle,
                        isAttachEnabled = true
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    appendSystemMessage("Analisi immagine non disponibile. Puoi descrivere la spesa a parole?")
                    _uiState.value = _uiState.value.copy(
                        conversationState = ConversationState.Idle,
                        isAttachEnabled = true
                    )
                }
            }
        }
    }

    fun onDraftChanged(draft: DraftExpense) {
        val state = _uiState.value.conversationState as? ConversationState.Confirming ?: return
        currentDraft = draft
        _uiState.value = _uiState.value.copy(conversationState = state.copy(draft = draft))
    }

    fun onSaveConfirmed() {
        val state = _uiState.value.conversationState as? ConversationState.Confirming ?: return
        val draft = state.draft
        val vehicleId = draft.vehicleId ?: return
        val amount = draft.amount?.takeIf { it > 0 } ?: return
        val category = draft.category ?: return

        viewModelScope.launch {
            try {
                expenseRepository.create(
                    vehicleId = vehicleId,
                    category = category.name,
                    subcategory = draft.fuelType ?: "",
                    amount = amount,
                    quantity = draft.liters,
                    quantityUnit = if (draft.liters != null) "LITERS" else null,
                    description = draft.description ?: "",
                    date = draft.date ?: System.currentTimeMillis(),
                    pricePerLiter = draft.pricePerLiter
                )
                vehiclePreferences.setLastUsedVehicleId(vehicleId)
                _uiState.value = _uiState.value.copy(conversationState = ConversationState.Saved)
                delay(1500L)
                resetConversation()
            } catch (e: Exception) {
                appendSystemMessage("Errore nel salvataggio. Riprova.")
                _uiState.value = _uiState.value.copy(conversationState = ConversationState.Confirming(draft))
            }
        }
    }

    fun onDiscardConversation() {
        resetConversation()
    }

    // --- State machine ---

    private fun advanceConversation() {
        val missing = computeMissingFields(currentDraft)
        if (missing.isEmpty()) {
            appendSystemMessage("Ecco il riepilogo della tua spesa. Controlla i dettagli e salva.")
            _uiState.value = _uiState.value.copy(
                conversationState = ConversationState.Confirming(currentDraft),
                isAttachEnabled = false
            )
            return
        }

        val nextField = missing.first()

        if (nextField == RequiredField.VEHICLE && _vehicles.value.isEmpty()) {
            appendSystemMessage(
                "Non hai ancora aggiunto nessun veicolo. Vai alla sezione Veicoli per aggiungerne uno."
            )
            _navigateToVehicle.tryEmit(Unit)
            _uiState.value = _uiState.value.copy(conversationState = ConversationState.Idle)
            return
        }

        val (question, options) = buildFollowUpMessage(nextField, _vehicles.value)
        appendSystemMessage(question)
        _uiState.value = _uiState.value.copy(
            conversationState = ConversationState.AwaitingAnswer(nextField, options)
        )
    }

    private fun computeMissingFields(draft: DraftExpense): List<RequiredField> {
        val missing = mutableListOf<RequiredField>()
        if (draft.amount == null || draft.amount <= 0) missing.add(RequiredField.AMOUNT)
        if (draft.category == null) missing.add(RequiredField.CATEGORY)
        if (draft.vehicleId == null) missing.add(RequiredField.VEHICLE)
        // DATE is optional: null → defaults to System.currentTimeMillis() at save time
        if (draft.category == ExpenseCategory.FUEL) {
            if (draft.liters == null) missing.add(RequiredField.LITERS)
            if (draft.pricePerLiter == null) missing.add(RequiredField.PRICE_PER_LITER)
        }
        return missing
    }

    private fun buildFollowUpMessage(
        field: RequiredField,
        vehicles: List<VehicleEntity>
    ): Pair<String, List<String>?> = when (field) {
        RequiredField.AMOUNT -> "Quanto hai speso? (inserisci l'importo in €)" to null
        RequiredField.CATEGORY -> "Che tipo di spesa è?" to listOf("Carburante", "Manutenzione", "Extra")
        RequiredField.VEHICLE -> "A quale veicolo vuoi associare la spesa?" to vehicles.map { it.name }
        RequiredField.DATE -> "Che data? (lascia vuoto per oggi)" to null
        RequiredField.LITERS -> "Quanti litri hai rifornito? (opzionale — premi Invia per saltare)" to null
        RequiredField.PRICE_PER_LITER -> "Prezzo al litro in €? (opzionale — premi Invia per saltare)" to null
    }

    private fun resolveFollowUpAnswer(
        field: RequiredField,
        answer: String,
        draft: DraftExpense,
        vehicles: List<VehicleEntity>
    ): DraftExpense = when (field) {
        RequiredField.AMOUNT -> {
            val amount = answer.replace(',', '.').toDoubleOrNull()
            if (amount != null && amount > 0) draft.copy(amount = amount) else draft
        }
        RequiredField.CATEGORY -> {
            val cat = when (answer.lowercase().trim()) {
                "carburante", "fuel", "benzina", "diesel", "gasolio" -> ExpenseCategory.FUEL
                "manutenzione", "maintenance", "tagliando", "revisione" -> ExpenseCategory.MAINTENANCE
                "extra", "altro", "assicurazione", "multa", "bollo" -> ExpenseCategory.EXTRA
                else -> null
            }
            if (cat != null) draft.copy(category = cat) else draft
        }
        RequiredField.VEHICLE -> {
            val vehicle = vehicles.firstOrNull { it.name.equals(answer, ignoreCase = true) }
            if (vehicle != null) draft.copy(vehicleId = vehicle.id) else draft
        }
        RequiredField.DATE -> draft // blank or unrecognised → stays null (today at save)
        RequiredField.LITERS -> {
            val liters = answer.replace(',', '.').toDoubleOrNull()
            draft.copy(liters = if (liters != null && liters > 0) liters else null)
        }
        RequiredField.PRICE_PER_LITER -> {
            val price = answer.replace(',', '.').toDoubleOrNull()
            draft.copy(pricePerLiter = if (price != null && price > 0) price else null)
        }
    }

    // --- Helpers ---

    private fun appendMessage(msg: ChatMessage) {
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + msg)
    }

    private fun appendSystemMessage(text: String) {
        appendMessage(chatMessage(MessageRole.SYSTEM, ContentType.TEXT, text))
    }

    private fun chatMessage(role: MessageRole, contentType: ContentType, text: String) = ChatMessage(
        id = UUID.randomUUID().toString(),
        role = role,
        contentType = contentType,
        text = text
    )

    private fun resetConversation() {
        currentDraft = DraftExpense()
        _uiState.value = ChatUiState()
    }

    private fun parseJsonResponse(response: String): ParsedExpense {
        return try {
            var cleaned = response.trim()
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substringAfter("```")
                cleaned = cleaned.substringAfter("json\n").substringAfter("json\r\n")
                cleaned = cleaned.removeSuffix("```").trim()
            }
            jsonParser.decodeFromString<ParsedExpense>(cleaned)
        } catch (e: Exception) {
            ParsedExpense(
                category = ExpenseCategory.UNKNOWN,
                description = "",
                warnings = listOf("Could not parse image response")
            )
        }
    }
}

private fun ParsedExpense.toDraft() = DraftExpense(
    amount = amount,
    category = category.takeIf { it != ExpenseCategory.UNKNOWN },
    date = date,
    description = description.takeIf { it.isNotBlank() },
    fuelType = fuelType?.name,
    liters = quantity?.takeIf { quantityUnit == QuantityUnit.LITERS },
    pricePerLiter = pricePerLiter,
    warnings = warnings
)
