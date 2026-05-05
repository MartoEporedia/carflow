package com.carflow.network.client

import com.carflow.network.llm.LlmClient
import com.carflow.network.llm.LlmConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ProxyParseRequest(
    val systemPrompt: String,
    val userPrompt: String
)

@Serializable
data class ProxyParseResponse(
    val json: String
)

@Serializable
data class ProxyImageParseRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val imageBase64: String,
    val mimeType: String
)

class ProxyLlmClient(
    private val config: LlmConfig.Proxy
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
        val response = httpClient.post("${config.baseUrl}/api/parse-image") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.authToken}")
            setBody(ProxyImageParseRequest(systemPrompt, userPrompt, imageBase64, mimeType))
        }
        val body = response.body<ProxyParseResponse>()
        return body.json
    }

    override suspend fun chat(systemPrompt: String, userPrompt: String): String {
        val response = httpClient.post("${config.baseUrl}/api/parse") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.authToken}")
            setBody(ProxyParseRequest(systemPrompt, userPrompt))
        }

        val body = response.body<ProxyParseResponse>()
        return body.json
    }
}
