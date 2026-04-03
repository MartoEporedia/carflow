package com.carflow.app.data.settings

import com.carflow.network.llm.LlmConfig
import com.carflow.network.llm.LlmConfigResolver
import com.carflow.network.llm.LlmMode

class AndroidLlmConfigResolver(
    private val settings: LlmSettings,
    private val authRepository: AuthRepository,
    private val proxyBaseUrl: String = "https://api.carflow.app"
) : LlmConfigResolver {

    override fun resolve(): LlmConfig {
        return if (settings.getMode() == LlmMode.DIRECT && settings.hasDirectConfig()) {
            LlmConfig.Direct(
                apiKey = settings.getDirectApiKey()!!,
                provider = settings.getDirectProvider(),
                model = settings.getDirectModel()
            )
        } else {
            val token = authRepository.getAuthToken()
                ?: throw UnauthenticatedException()
            LlmConfig.Proxy(
                baseUrl = proxyBaseUrl,
                authToken = token
            )
        }
    }
}
