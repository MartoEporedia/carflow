package com.carflow.network.llm

import com.carflow.network.client.DirectLlmClient
import com.carflow.network.client.ProxyLlmClient

object LlmClientFactory {
    fun create(config: LlmConfig): LlmClient = when (config) {
        is LlmConfig.Direct -> DirectLlmClient(config)
        is LlmConfig.Proxy -> ProxyLlmClient(config)
    }
}
