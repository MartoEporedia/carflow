package com.carflow.network.llm

import kotlinx.serialization.Serializable

@Serializable
sealed interface LlmProvider {
    val id: String
    val name: String
    val endpoint: String
    val authHeaderName: String
    val requiresVersionHeader: Boolean

    @Serializable
    data object OpenAI : LlmProvider {
        override val id = "openai"
        override val name = "OpenAI"
        override val endpoint = "https://api.openai.com/v1/chat/completions"
        override val authHeaderName = "Authorization"
        override val requiresVersionHeader = false
    }

    @Serializable
    data object Anthropic : LlmProvider {
        override val id = "anthropic"
        override val name = "Anthropic"
        override val endpoint = "https://api.anthropic.com/v1/messages"
        override val authHeaderName = "x-api-key"
        override val requiresVersionHeader = true
    }

    @Serializable
    data object Groq : LlmProvider {
        override val id = "groq"
        override val name = "Groq"
        override val endpoint = "https://api.groq.com/openai/v1/chat/completions"
        override val authHeaderName = "Authorization"
        override val requiresVersionHeader = false
    }

    @Serializable
    data object OpenRouter : LlmProvider {
        override val id = "openrouter"
        override val name = "OpenRouter"
        override val endpoint = "https://openrouter.ai/api/v1/chat/completions"
        override val authHeaderName = "Authorization"
        override val requiresVersionHeader = false
    }

    @Serializable
    data object OllamaCloud : LlmProvider {
        override val id = "ollama_cloud"
        override val name = "Ollama Cloud"
        override val endpoint = "https://cloud.ollama.com/api/chat"
        override val authHeaderName = "Authorization"
        override val requiresVersionHeader = false
    }

    @Serializable
    data class Custom(
        override val id: String = "custom",
        override val name: String = "Custom",
        override val endpoint: String,
        override val authHeaderName: String = "Authorization",
        override val requiresVersionHeader: Boolean = false
    ) : LlmProvider

    companion object {
        fun fromId(id: String): LlmProvider = when (id) {
            OpenAI.id -> OpenAI
            Anthropic.id -> Anthropic
            Groq.id -> Groq
            OpenRouter.id -> OpenRouter
            OllamaCloud.id -> OllamaCloud
            else -> throw IllegalArgumentException("Unknown provider: $id")
        }

        val all: List<LlmProvider> = listOf(OpenAI, Anthropic, Groq, OpenRouter, OllamaCloud)
    }
}
