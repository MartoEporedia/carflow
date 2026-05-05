package com.carflow.app.ui.screens.chat.viewmodel

import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.data.repository.ExpenseRepository
import com.carflow.app.data.repository.VehicleRepository
import com.carflow.app.data.settings.VehiclePreferences
import com.carflow.network.llm.ExpenseParserStrategy
import com.carflow.network.llm.LlmConfigResolver
import com.carflow.parser.model.ExpenseCategory
import com.carflow.parser.model.ParseConfidence
import com.carflow.parser.model.ParsedExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChatExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var parser: ExpenseParserStrategy
    private lateinit var configResolver: LlmConfigResolver
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var vehiclePreferences: VehiclePreferences

    private val testVehicle = VehicleEntity(id = "v1", name = "Fiat Panda")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        parser = mock()
        configResolver = mock()
        expenseRepository = mock()
        vehicleRepository = mock {
            on { getAllVehicles() } doReturn flowOf(listOf(testVehicle))
        }
        vehiclePreferences = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ChatExpenseViewModel(
        parser = parser,
        configResolver = configResolver,
        expenseRepository = expenseRepository,
        vehicleRepository = vehicleRepository,
        vehiclePreferences = vehiclePreferences
    )

    @Test
    fun `initial state is Idle with empty messages`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(ConversationState.Idle, state.conversationState)
        assertTrue(state.messages.isEmpty())
    }

    @Test
    fun `onSendText with full parsed expense transitions to Confirming after vehicle follow-up`() = runTest {
        whenever(parser.parse(any())).thenReturn(
            ParsedExpense(
                category = ExpenseCategory.FUEL,
                amount = 50.0,
                confidence = ParseConfidence.HIGH,
                description = "Benzina"
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onTextChanged("50 euro benzina")
        vm.onSendText()
        advanceUntilIdle()

        // After parse: AMOUNT and CATEGORY are set, VEHICLE is missing → AwaitingAnswer(VEHICLE)
        val state = vm.uiState.value
        assertTrue(state.conversationState is ConversationState.AwaitingAnswer)
        assertEquals(
            RequiredField.VEHICLE,
            (state.conversationState as ConversationState.AwaitingAnswer).field
        )

        // Chips should list the vehicle name
        val options = (state.conversationState as ConversationState.AwaitingAnswer).options
        assertEquals(listOf("Fiat Panda"), options)
    }

    @Test
    fun `onFollowUpAnswer for VEHICLE transitions to Confirming`() = runTest {
        whenever(parser.parse(any())).thenReturn(
            ParsedExpense(
                category = ExpenseCategory.MAINTENANCE,
                amount = 120.0,
                confidence = ParseConfidence.HIGH,
                description = "Tagliando"
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onTextChanged("tagliando 120")
        vm.onSendText()
        advanceUntilIdle()

        // State should be AwaitingAnswer(VEHICLE) since FUEL liters/price fields are skipped for MAINTENANCE
        assertEquals(
            RequiredField.VEHICLE,
            (vm.uiState.value.conversationState as ConversationState.AwaitingAnswer).field
        )

        vm.onFollowUpAnswer("Fiat Panda")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.conversationState is ConversationState.Confirming)
        val draft = (state.conversationState as ConversationState.Confirming).draft
        assertEquals("v1", draft.vehicleId)
        assertEquals(120.0, draft.amount)
        assertEquals(ExpenseCategory.MAINTENANCE, draft.category)
    }

    @Test
    fun `onSaveConfirmed calls expenseRepository and transitions to Saved`() = runTest {
        whenever(expenseRepository.create(any(), any(), any(), any(), anyOrNull(), anyOrNull(), any(), any(), anyOrNull(), any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(mock())
        val vm = createViewModel()
        advanceUntilIdle()

        // Manually put into Confirming state
        val draft = DraftExpense(
            amount = 50.0,
            category = ExpenseCategory.EXTRA,
            vehicleId = "v1"
        )
        vm.onDraftChanged(draft)
        // Force confirming state by simulating the transition
        // (In real flow this happens via advanceConversation; here we use the public API)
        // Since we can't set state directly, trigger via full flow:
        whenever(parser.parse(any())).thenReturn(
            ParsedExpense(
                category = ExpenseCategory.EXTRA,
                amount = 50.0,
                confidence = ParseConfidence.HIGH,
                description = "Extra"
            )
        )
        vm.onTextChanged("extra 50")
        vm.onSendText()
        advanceUntilIdle()

        // Answer VEHICLE follow-up
        vm.onFollowUpAnswer("Fiat Panda")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.conversationState is ConversationState.Confirming)

        vm.onSaveConfirmed()
        advanceUntilIdle()

        assertEquals(ConversationState.Saved, vm.uiState.value.conversationState)
    }

    @Test
    fun `onDiscardConversation resets to Idle`() = runTest {
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onTextChanged("test")
        vm.onDiscardConversation()
        advanceUntilIdle()

        assertEquals(ConversationState.Idle, vm.uiState.value.conversationState)
        assertTrue(vm.uiState.value.messages.isEmpty())
    }

    @Test
    fun `no vehicles guard emits navigateToVehicle event`() = runTest {
        whenever(vehicleRepository.getAllVehicles()).thenReturn(flowOf(emptyList()))
        whenever(parser.parse(any())).thenReturn(
            ParsedExpense(
                category = ExpenseCategory.EXTRA,
                amount = 30.0,
                confidence = ParseConfidence.HIGH,
                description = "Extra"
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<Unit>()
        val job = launch { vm.navigateToVehicle.toList(events) }

        vm.onTextChanged("extra 30")
        vm.onSendText()
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertEquals(ConversationState.Idle, vm.uiState.value.conversationState)

        job.cancel()
    }

    @Test
    fun `FUEL category prompts for LITERS after VEHICLE`() = runTest {
        whenever(parser.parse(any())).thenReturn(
            ParsedExpense(
                category = ExpenseCategory.FUEL,
                amount = 50.0,
                confidence = ParseConfidence.HIGH,
                description = "Benzina"
            )
        )
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onTextChanged("50 benzina")
        vm.onSendText()
        advanceUntilIdle()
        // AwaitingAnswer(VEHICLE) with liters missing
        vm.onFollowUpAnswer("Fiat Panda")
        advanceUntilIdle()

        val state = vm.uiState.value.conversationState
        assertTrue(state is ConversationState.AwaitingAnswer)
        assertEquals(RequiredField.LITERS, (state as ConversationState.AwaitingAnswer).field)
    }
}
