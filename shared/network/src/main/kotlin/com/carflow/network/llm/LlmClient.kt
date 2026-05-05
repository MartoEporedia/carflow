package com.carflow.network.llm

import com.carflow.parser.model.ParsedExpense

interface LlmClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
    suspend fun chatWithImage(
        systemPrompt: String,
        userPrompt: String,
        imageBase64: String,
        mimeType: String
    ): String
}

interface ExpenseParserStrategy {
    suspend fun parse(input: String): ParsedExpense
}
