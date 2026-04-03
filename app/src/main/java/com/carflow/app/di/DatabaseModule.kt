package com.carflow.app.di

import android.content.Context
import androidx.room.Room
import com.carflow.app.data.dao.CategoryDao
import com.carflow.app.data.dao.ExpenseDao
import com.carflow.app.data.dao.VehicleDao
import com.carflow.app.data.database.CarFlowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CarFlowDatabase {
        return Room.databaseBuilder(
            context,
            CarFlowDatabase::class.java,
            "carflow.db"
        ).build()
    }

    @Provides
    fun provideVehicleDao(database: CarFlowDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideExpenseDao(database: CarFlowDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideCategoryDao(database: CarFlowDatabase): CategoryDao = database.categoryDao()
}
