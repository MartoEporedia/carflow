package com.carflow.app.data.settings

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.carflow.network.llm.LlmMode
import com.carflow.network.llm.LlmProvider

class LlmSettings(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "llm_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getMode(): LlmMode =
        prefs.getString(KEY_MODE, LlmMode.PROXY.name)
            ?.let { LlmMode.valueOf(it) }
            ?: LlmMode.PROXY

    fun setMode(mode: LlmMode) =
        prefs.edit().putString(KEY_MODE, mode.name).apply()

    fun getDirectApiKey(): String? =
        prefs.getString(KEY_API_KEY, null)

    fun setDirectApiKey(key: String) =
        prefs.edit().putString(KEY_API_KEY, key).apply()

    fun getDirectProvider(): LlmProvider =
        prefs.getString(KEY_PROVIDER, LlmProvider.OpenRouter.id)
            ?.let { LlmProvider.fromId(it) }
            ?: LlmProvider.OpenRouter

    fun setDirectProvider(provider: LlmProvider) =
        prefs.edit().putString(KEY_PROVIDER, provider.id).apply()

    fun getDirectModel(): String =
        prefs.getString(KEY_MODEL, "openai/gpt-4o-mini")
            ?: "openai/gpt-4o-mini"

    fun setDirectModel(model: String) =
        prefs.edit().putString(KEY_MODEL, model).apply()

    fun hasDirectConfig(): Boolean =
        !getDirectApiKey().isNullOrBlank()

    fun clearDirectConfig() =
        prefs.edit()
            .remove(KEY_API_KEY)
            .remove(KEY_PROVIDER)
            .remove(KEY_MODEL)
            .apply()

    companion object {
        private const val KEY_MODE = "llm_mode"
        private const val KEY_API_KEY = "llm_api_key"
        private const val KEY_PROVIDER = "llm_provider"
        private const val KEY_MODEL = "llm_model"
    }
}
