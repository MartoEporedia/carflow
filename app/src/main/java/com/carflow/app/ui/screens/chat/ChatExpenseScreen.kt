package com.carflow.app.ui.screens.chat

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.carflow.app.data.entity.VehicleEntity
import com.carflow.app.ui.screens.chat.components.*
import com.carflow.app.ui.screens.chat.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun ChatExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVehicle: () -> Unit,
    viewModel: ChatExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()

    ChatExpenseContent(
        uiState = uiState,
        vehicles = vehicles,
        navigateToVehicleFlow = viewModel.navigateToVehicle,
        onTextChanged = viewModel::onTextChanged,
        onSendText = viewModel::onSendText,
        onFollowUpAnswer = viewModel::onFollowUpAnswer,
        onImageSelected = viewModel::onImageSelected,
        onDraftChanged = viewModel::onDraftChanged,
        onSaveConfirmed = viewModel::onSaveConfirmed,
        onDiscardConversation = viewModel::onDiscardConversation,
        onNavigateBack = onNavigateBack,
        onNavigateToVehicle = onNavigateToVehicle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatExpenseContent(
    uiState: ChatUiState,
    vehicles: List<VehicleEntity>,
    navigateToVehicleFlow: SharedFlow<Unit>,
    onTextChanged: (String) -> Unit,
    onSendText: () -> Unit,
    onFollowUpAnswer: (String) -> Unit,
    onImageSelected: (String, String) -> Unit,
    onDraftChanged: (DraftExpense) -> Unit,
    onSaveConfirmed: () -> Unit,
    onDiscardConversation: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToVehicle: () -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        navigateToVehicleFlow.collect { onNavigateToVehicle() }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                coroutineScope.launch {
                    encodeImageUri(context, uri)?.let { (b64, mime) -> onImageSelected(b64, mime) }
                }
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            coroutineScope.launch {
                encodeImageUri(context, it)?.let { (b64, mime) -> onImageSelected(b64, mime) }
            }
        }
    }

    var showAttachMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spesa rapida") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    if (uiState.messages.isEmpty() && uiState.conversationState == ConversationState.Idle) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(top = 64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Descrivi la spesa o allega uno scontrino",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(uiState.messages, key = { it.id }) { ChatBubble(it) }

                    if (uiState.conversationState is ConversationState.Confirming) {
                        item {
                            ExpenseConfirmationCard(
                                draft = (uiState.conversationState as ConversationState.Confirming).draft,
                                vehicles = vehicles,
                                onDraftChange = onDraftChanged,
                                onSave = onSaveConfirmed,
                                onDiscard = onDiscardConversation,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (uiState.conversationState == ConversationState.Saved) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "✓ Spesa salvata!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.conversationState == ConversationState.Processing) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Analizzo…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Suggestion chips
            val awaitingState = uiState.conversationState as? ConversationState.AwaitingAnswer
            awaitingState?.options?.takeIf { it.isNotEmpty() }?.let { options ->
                SuggestionChips(options = options, onOptionSelected = onFollowUpAnswer)
            }

            // Input row (hidden when Confirming or Saved)
            val showInput = uiState.conversationState !is ConversationState.Confirming &&
                    uiState.conversationState != ConversationState.Saved
            if (showInput) {
                Box {
                    ChatInputRow(
                        text = uiState.inputText,
                        onTextChange = onTextChanged,
                        onSendClick = onSendText,
                        onAttachClick = { showAttachMenu = true },
                        isEnabled = uiState.conversationState == ConversationState.Idle ||
                                uiState.conversationState is ConversationState.AwaitingAnswer
                    )
                    DropdownMenu(
                        expanded = showAttachMenu,
                        onDismissRequest = { showAttachMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Fotocamera") },
                            onClick = {
                                showAttachMenu = false
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Galleria") },
                            onClick = {
                                showAttachMenu = false
                                galleryLauncher.launch("image/*")
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").also { it.mkdirs() }
    val imageFile = File.createTempFile("camera_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private suspend fun encodeImageUri(context: Context, uri: Uri): Pair<String, String>? =
    withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
            val scaled = scaleBitmap(bitmap, 1024)
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP) to "image/jpeg"
        } catch (e: Exception) {
            null
        }
    }

private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val w = bitmap.width
    val h = bitmap.height
    if (w <= maxDimension && h <= maxDimension) return bitmap
    val scale = maxDimension.toFloat() / maxOf(w, h)
    return Bitmap.createScaledBitmap(bitmap, (w * scale).toInt(), (h * scale).toInt(), true)
}

private val emptySharedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 0)

// --- Previews ---

@Preview(showBackground = true, name = "Chat — Idle")
@Composable
private fun ChatIdlePreview() {
    ChatExpenseContent(
        uiState = ChatUiState(),
        vehicles = emptyList(),
        navigateToVehicleFlow = emptySharedFlow,
        onTextChanged = {}, onSendText = {}, onFollowUpAnswer = {},
        onImageSelected = { _, _ -> }, onDraftChanged = {},
        onSaveConfirmed = {}, onDiscardConversation = {},
        onNavigateBack = {}, onNavigateToVehicle = {}
    )
}

@Preview(showBackground = true, name = "Chat — AwaitingAnswer with chips")
@Composable
private fun ChatAwaitingPreview() {
    ChatExpenseContent(
        uiState = ChatUiState(
            messages = listOf(
                ChatMessage("1", MessageRole.USER, ContentType.TEXT, "spesa auto"),
                ChatMessage("2", MessageRole.SYSTEM, ContentType.TEXT, "Che tipo di spesa è?")
            ),
            conversationState = ConversationState.AwaitingAnswer(
                RequiredField.CATEGORY, listOf("Carburante", "Manutenzione", "Extra")
            )
        ),
        vehicles = listOf(VehicleEntity("v1", "Fiat Panda")),
        navigateToVehicleFlow = emptySharedFlow,
        onTextChanged = {}, onSendText = {}, onFollowUpAnswer = {},
        onImageSelected = { _, _ -> }, onDraftChanged = {},
        onSaveConfirmed = {}, onDiscardConversation = {},
        onNavigateBack = {}, onNavigateToVehicle = {}
    )
}

@Preview(showBackground = true, name = "Chat — Processing")
@Composable
private fun ChatProcessingPreview() {
    ChatExpenseContent(
        uiState = ChatUiState(
            messages = listOf(
                ChatMessage("1", MessageRole.USER, ContentType.TEXT, "50 euro benzina oggi")
            ),
            conversationState = ConversationState.Processing
        ),
        vehicles = emptyList(),
        navigateToVehicleFlow = emptySharedFlow,
        onTextChanged = {}, onSendText = {}, onFollowUpAnswer = {},
        onImageSelected = { _, _ -> }, onDraftChanged = {},
        onSaveConfirmed = {}, onDiscardConversation = {},
        onNavigateBack = {}, onNavigateToVehicle = {}
    )
}

@Preview(showBackground = true, name = "Chat — Saved")
@Composable
private fun ChatSavedPreview() {
    ChatExpenseContent(
        uiState = ChatUiState(
            messages = listOf(
                ChatMessage("1", MessageRole.SYSTEM, ContentType.TEXT, "Ecco il riepilogo…")
            ),
            conversationState = ConversationState.Saved
        ),
        vehicles = emptyList(),
        navigateToVehicleFlow = emptySharedFlow,
        onTextChanged = {}, onSendText = {}, onFollowUpAnswer = {},
        onImageSelected = { _, _ -> }, onDraftChanged = {},
        onSaveConfirmed = {}, onDiscardConversation = {},
        onNavigateBack = {}, onNavigateToVehicle = {}
    )
}

@Preview(showBackground = true, name = "Chat — Confirming")
@Composable
private fun ChatConfirmingPreview() {
    ChatExpenseContent(
        uiState = ChatUiState(
            messages = listOf(
                ChatMessage("1", MessageRole.USER, ContentType.TEXT, "50 euro benzina"),
                ChatMessage("2", MessageRole.SYSTEM, ContentType.TEXT, "Ecco il riepilogo:")
            ),
            conversationState = ConversationState.Confirming(
                DraftExpense(amount = 50.0, category = com.carflow.parser.model.ExpenseCategory.FUEL, vehicleId = "v1")
            )
        ),
        vehicles = listOf(VehicleEntity("v1", "Fiat Panda")),
        navigateToVehicleFlow = emptySharedFlow,
        onTextChanged = {}, onSendText = {}, onFollowUpAnswer = {},
        onImageSelected = { _, _ -> }, onDraftChanged = {},
        onSaveConfirmed = {}, onDiscardConversation = {},
        onNavigateBack = {}, onNavigateToVehicle = {}
    )
}
