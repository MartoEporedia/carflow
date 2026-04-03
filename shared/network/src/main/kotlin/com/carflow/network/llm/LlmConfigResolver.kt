package com.carflow.network.llm

import com.carflow.network.llm.LlmConfigResolver.UnauthenticatedException
import com.carflow.network.llm.LlmConfigResolver.UnconfiguredException

interface LlmConfigResolver {
    fun resolve(): LlmConfig

    class UnconfiguredException : Exception("LLM not configured")
    class UnauthenticatedException : Exception("User not authenticated for proxy mode")
}
