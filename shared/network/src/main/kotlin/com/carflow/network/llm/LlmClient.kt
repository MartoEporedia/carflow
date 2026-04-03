package com.carflow.network.llm

import com.carflow.parser.model.ParsedExpense

interface LlmClient {
    suspend fun chat(systemPrompt: String, userPrompt: String): String
}

interface ExpenseParserStrategy {
    suspend fun parse(input: String): ParsedExpense
}
