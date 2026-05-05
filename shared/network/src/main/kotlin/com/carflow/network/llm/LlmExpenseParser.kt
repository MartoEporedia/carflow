package com.carflow.network.llm

import com.carflow.network.client.DirectLlmClient
import com.carflow.network.client.ProxyLlmClient
import com.carflow.parser.ExpenseParser
import com.carflow.parser.model.ParseConfidence
import com.carflow.parser.model.ParsedExpense
import kotlinx.serialization.json.Json

class LlmExpenseParser(
    private val configResolver: LlmConfigResolver,
    private val fallbackParser: ExpenseParser
) : ExpenseParserStrategy {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun parse(input: String): ParsedExpense {
        return try {
            val config = configResolver.resolve()
            val client = LlmClientFactory.create(config)
            val todayTimestamp = System.currentTimeMillis()
            val response = client.chat(
                LlmPrompt.SYSTEM,
                LlmPrompt.userPrompt(input, todayTimestamp)
            )
            parseLlmResponse(response, input)
        } catch (e: LlmConfigResolver.UnconfiguredException) {
            fallbackParser.parse(input).copy(
                warnings = listOf("LLM not configured, using rule-based parser")
            )
        } catch (e: LlmConfigResolver.UnauthenticatedException) {
            fallbackParser.parse(input).copy(
                warnings = listOf("Authentication required for LLM proxy, using rule-based parser")
            )
        } catch (e: Exception) {
            fallbackParser.parse(input).copy(
                warnings = listOf("LLM parsing failed: ${e.message ?: "unknown error"}, using rule-based parser")
            )
        }
    }

    private fun parseLlmResponse(response: String, rawInput: String): ParsedExpense {
        return try {
            val cleaned = cleanJsonResponse(response)
            json.decodeFromString<ParsedExpense>(cleaned)
        } catch (e: Exception) {
            ParsedExpense(
                category = com.carflow.parser.model.ExpenseCategory.UNKNOWN,
                description = rawInput,
                rawInput = rawInput,
                confidence = ParseConfidence.LOW,
                warnings = listOf("LLM response parsing failed: ${e.message ?: "invalid JSON"}")
            )
        }
    }

    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substringAfter("```")
            cleaned = cleaned.substringAfter("json\n").substringAfter("json\r\n")
            cleaned = cleaned.removeSuffix("```").trim()
        }
        return cleaned
    }
}
