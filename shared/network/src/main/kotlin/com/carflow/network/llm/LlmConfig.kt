package com.carflow.network.llm

import kotlinx.serialization.Serializable

@Serializable
sealed interface LlmConfig {
    @Serializable
    data class Direct(
        val apiKey: String,
        val provider: LlmProvider,
        val model: String,
        val temperature: Float = 0.0f,
        val timeoutMs: Long = 15_000,
        val maxTokens: Int = 500
    ) : LlmConfig

    @Serializable
    data class Proxy(
        val baseUrl: String,
        val authToken: String,
        val timeoutMs: Long = 15_000
    ) : LlmConfig
}

enum class LlmMode {
    PROXY,
    DIRECT
}
