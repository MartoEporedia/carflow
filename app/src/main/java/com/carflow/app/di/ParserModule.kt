package com.carflow.app.di

import com.carflow.parser.ExpenseParser
import com.carflow.parser.keywords.KeywordDictionary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideKeywordDictionary(): KeywordDictionary = KeywordDictionary()

    @Provides
    @Singleton
    fun provideExpenseParser(dictionary: KeywordDictionary): ExpenseParser =
        ExpenseParser(dictionary)
}
