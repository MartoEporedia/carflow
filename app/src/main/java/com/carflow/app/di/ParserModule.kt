package com.carflow.app.di

import android.content.Context
import com.carflow.app.data.settings.AndroidLlmConfigResolver
import com.carflow.app.data.settings.AuthRepository
import com.carflow.app.data.settings.LlmSettings
import com.carflow.network.llm.ExpenseParserStrategy
import com.carflow.network.llm.LlmConfigResolver
import com.carflow.network.llm.LlmExpenseParser
import com.carflow.parser.ExpenseParser
import com.carflow.parser.keywords.KeywordDictionary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideKeywordDictionary(): KeywordDictionary = KeywordDictionary()

    @Provides
    @Singleton
    @Named("default")
    fun provideDefaultExpenseParser(dictionary: KeywordDictionary): ExpenseParser =
        ExpenseParser(dictionary)

    @Provides
    @Singleton
    fun provideLlmSettings(@ApplicationContext context: Context): LlmSettings =
        LlmSettings(context)

    @Provides
    @Singleton
    fun provideLlmConfigResolver(
        settings: LlmSettings,
        authRepository: AuthRepository
    ): LlmConfigResolver =
        AndroidLlmConfigResolver(settings, authRepository)

    @Provides
    @Singleton
    @Named("llm")
    fun provideLlmExpenseParser(
        configResolver: LlmConfigResolver,
        @Named("default") fallbackParser: ExpenseParser
    ): ExpenseParserStrategy =
        LlmExpenseParser(configResolver, fallbackParser)
}
