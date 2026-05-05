package com.carflow.network.client

import com.carflow.network.llm.LlmClient
import com.carflow.network.llm.LlmConfig
import com.carflow.network.llm.LlmProvider
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.add

@Serializable
data class OpenAIMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Float = 0.0f,
    val max_tokens: Int = 500
)

@Serializable
data class OpenAIChoice(
    val message: OpenAIMessage
)

@Serializable
data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

@Serializable
data class AnthropicMessage(
    val role: String = "assistant",
    val type: String = "text",
    val text: String = ""
)

@Serializable
data class AnthropicRequest(
    val model: String,
    val system: String,
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 500
)

@Serializable
data class AnthropicResponse(
    val content: List<AnthropicMessage>
)

class DirectLlmClient(
    private val config: LlmConfig.Direct
) : LlmClient {

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeoutMs
            connectTimeoutMillis = config.timeoutMs
            socketTimeoutMillis = config.timeoutMs
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }

    override suspend fun chatWithImage(
        systemPrompt: String,
        userPrompt: String,
        imageBase64: String,
        mimeType: String
    ): String {
        val provider = config.provider
        val url = provider.endpoint
        val response = when (provider) {
            is LlmProvider.Anthropic -> chatWithImageAnthropic(url, systemPrompt, userPrompt, imageBase64, mimeType)
            is LlmProvider.OllamaCloud -> throw UnsupportedOperationException("Image analysis not supported for OllamaCloud provider")
            else -> chatWithImageOpenAI(url, systemPrompt, userPrompt, imageBase64, mimeType)
        }
        return extractContent(response, provider)
    }

    private suspend fun chatWithImageOpenAI(
        url: String,
        system: String,
        user: String,
        imageBase64: String,
        mimeType: String
    ): String {
        val body = buildJsonObject {
            put("model", config.model)
            put("temperature", config.temperature)
            put("max_tokens", config.maxTokens)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", system)
                })
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        add(buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") {
                                put("url", "data:$mimeType;base64,$imageBase64")
                            }
                        })
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", user)
                        })
                    }
                })
            }
        }
        val authHeader = when (config.provider) {
            is LlmProvider.OpenRouter -> {
                return httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer ${config.apiKey}")
                    header("HTTP-Referer", "https://carflow.app")
                    header("X-Title", "CarFlow")
                    setBody(body.toString())
                }.bodyAsText()
            }
            else -> "Bearer ${config.apiKey}"
        }
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", authHeader)
            setBody(body.toString())
        }.bodyAsText()
    }

    private suspend fun chatWithImageAnthropic(
        url: String,
        system: String,
        user: String,
        imageBase64: String,
        mimeType: String
    ): String {
        val body = buildJsonObject {
            put("model", config.model)
            put("system", system)
            put("max_tokens", config.maxTokens)
            putJsonArray("messages") {
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("content") {
                        add(buildJsonObject {
                            put("type", "image")
                            putJsonObject("source") {
                                put("type", "base64")
                                put("media_type", mimeType)
                                put("data", imageBase64)
                            }
                        })
                        add(buildJsonObject {
                            put("type", "text")
                            put("text", user)
                        })
                    }
                })
            }
        }
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("x-api-key", config.apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(body.toString())
        }.bodyAsText()
    }

    override suspend fun chat(systemPrompt: String, userPrompt: String): String {
        val provider = config.provider
        val url = provider.endpoint

        val response = when (provider) {
            is LlmProvider.OpenAI -> chatOpenAI(url, systemPrompt, userPrompt)
            is LlmProvider.Anthropic -> chatAnthropic(url, systemPrompt, userPrompt)
            is LlmProvider.Groq -> chatOpenAICompatible(url, systemPrompt, userPrompt, "Bearer ${config.apiKey}")
            is LlmProvider.OpenRouter -> chatOpenAICompatible(url, systemPrompt, userPrompt, "Bearer ${config.apiKey}")
            is LlmProvider.OllamaCloud -> chatOllamaCloud(url, systemPrompt, userPrompt)
            is LlmProvider.Custom -> chatOpenAICompatible(url, systemPrompt, userPrompt, "Bearer ${config.apiKey}")
        }

        return extractContent(response, provider)
    }

    private suspend fun chatOpenAI(url: String, system: String, user: String): String {
        val request = OpenAIRequest(
            model = config.model,
            messages = listOf(
                OpenAIMessage("system", system),
                OpenAIMessage("user", user)
            ),
            temperature = config.temperature,
            max_tokens = config.maxTokens
        )

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.apiKey}")
            setBody(request)
        }.bodyAsText()
    }

    private suspend fun chatAnthropic(url: String, system: String, user: String): String {
        val request = AnthropicRequest(
            model = config.model,
            system = system,
            messages = listOf(OpenAIMessage("user", user)),
            max_tokens = config.maxTokens
        )

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("x-api-key", config.apiKey)
            header("anthropic-version", "2023-06-01")
            setBody(request)
        }.bodyAsText()
    }

    private suspend fun chatOpenAICompatible(url: String, system: String, user: String, authHeader: String): String {
        val request = OpenAIRequest(
            model = config.model,
            messages = listOf(
                OpenAIMessage("system", system),
                OpenAIMessage("user", user)
            ),
            temperature = config.temperature,
            max_tokens = config.maxTokens
        )

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", authHeader)
            // OpenRouter headers
            if (config.provider is LlmProvider.OpenRouter) {
                header("HTTP-Referer", "https://carflow.app")
                header("X-Title", "CarFlow")
            }
            setBody(request)
        }.bodyAsText()
    }

    private suspend fun chatOllamaCloud(url: String, system: String, user: String): String {
        val request = mapOf(
            "model" to config.model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to system),
                mapOf("role" to "user", "content" to user)
            ),
            "stream" to false
        )

        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.apiKey}")
            setBody(request)
        }.bodyAsText()
    }

    private fun extractContent(responseBody: String, provider: LlmProvider): String {
        return try {
            when (provider) {
                is LlmProvider.OpenAI,
                is LlmProvider.Groq,
                is LlmProvider.OpenRouter,
                is LlmProvider.OllamaCloud,
                is LlmProvider.Custom -> {
                    val resp = json.decodeFromString<OpenAIResponse>(responseBody)
                    resp.choices.firstOrNull()?.message?.content
                        ?: throw IllegalStateException("Empty response from LLM")
                }
                is LlmProvider.Anthropic -> {
                    val resp = json.decodeFromString<AnthropicResponse>(responseBody)
                    resp.content.firstOrNull()?.text
                        ?: throw IllegalStateException("Empty response from LLM")
                }
            }
        } catch (e: Exception) {
            // If structured parsing fails, return raw response for downstream handling
            throw IllegalStateException("Failed to parse LLM response: ${e.message}", e)
        }
    }
}
