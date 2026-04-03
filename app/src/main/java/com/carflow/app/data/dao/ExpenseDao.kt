package com.carflow.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carflow.app.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE vehicleId = :vehicleId AND isDeleted = 0 ORDER BY date DESC")
    fun getByVehicle(vehicleId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllActive(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("UPDATE expenses SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("""
        SELECT * FROM expenses
        WHERE vehicleId = :vehicleId
        AND isDeleted = 0
        AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getByDateRange(vehicleId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT category, SUM(amount) as total
        FROM expenses
        WHERE vehicleId = :vehicleId AND isDeleted = 0 AND date BETWEEN :startDate AND :endDate
        GROUP BY category
    """)
    suspend fun getTotalsByCategory(vehicleId: String, startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("""
        SELECT SUM(amount)
        FROM expenses
        WHERE vehicleId = :vehicleId AND isDeleted = 0 AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmount(vehicleId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT * FROM expenses WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Long): List<ExpenseEntity>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
