package com.carflow.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.carflow.app.data.settings.LlmSettings
import com.carflow.network.llm.LlmMode
import com.carflow.network.llm.LlmProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmSettingsScreen(
    llmSettings: LlmSettings,
    onNavigateBack: () -> Unit
) {
    var mode by remember { mutableStateOf(llmSettings.getMode()) }
    var apiKey by remember { mutableStateOf(llmSettings.getDirectApiKey() ?: "") }
    var selectedProvider by remember { mutableStateOf(llmSettings.getDirectProvider()) }
    var selectedModel by remember { mutableStateOf(llmSettings.getDirectModel()) }
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni LLM") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Modalità di connessione", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LlmMode.entries.forEach { m ->
                    FilterChip(
                        selected = mode == m,
                        onClick = {
                            mode = m
                            llmSettings.setMode(m)
                        },
                        label = { Text(m.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (mode == LlmMode.DIRECT) {
                Text("Configurazione diretta", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        llmSettings.setDirectApiKey(it)
                    },
                    label = { Text("API Key") },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Text(if (showApiKey) "🙈" else "👁")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProvider.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Provider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LlmProvider.all.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.name) },
                                onClick = {
                                    selectedProvider = provider
                                    llmSettings.setDirectProvider(provider)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {
                        selectedModel = it
                        llmSettings.setDirectModel(it)
                    },
                    label = { Text("Modello") },
                    placeholder = { Text("Es: openai/gpt-4o-mini") },
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Modelli consigliati (OpenRouter)", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "openai/gpt-4o-mini" to "Veloce, economico",
                            "anthropic/claude-3.5-haiku" to "Ottimo per parsing",
                            "meta-llama/llama-3.1-8b-instruct:free" to "Gratis",
                            "groq/llama-3.3-70b-versatile" to "Ultra veloce"
                        ).forEach { (model, desc) ->
                            TextButton(
                                onClick = {
                                    selectedModel = model
                                    llmSettings.setDirectModel(model)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(model, style = MaterialTheme.typography.bodyMedium)
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Modalità Proxy", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Le richieste passano attraverso il server CarFlow. " +
                            "Richiede un account premium. L'API key è gestita dal server.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private val LlmMode.label: String
    get() = when (this) {
        LlmMode.PROXY -> "Proxy (Premium)"
        LlmMode.DIRECT -> "Diretta"
    }
