package com.carflow.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.carflow.app.data.dao.CategoryDao
import com.carflow.app.data.dao.ExpenseDao
import com.carflow.app.data.dao.VehicleDao
import com.carflow.app.data.entity.CategoryEntity
import com.carflow.app.data.entity.ExpenseEntity
import com.carflow.app.data.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        ExpenseEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CarFlowDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
}
