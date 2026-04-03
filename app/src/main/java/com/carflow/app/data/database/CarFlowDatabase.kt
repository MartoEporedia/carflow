package com.carflow.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true
)
abstract class CarFlowDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to expenses table
                database.execSQL("ALTER TABLE expenses ADD COLUMN isFullTank INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE expenses ADD COLUMN gasStationName TEXT")
                database.execSQL("ALTER TABLE expenses ADD COLUMN gasStationLocation TEXT")
                database.execSQL("ALTER TABLE expenses ADD COLUMN pricePerLiter REAL")
            }
        }
    }
}
