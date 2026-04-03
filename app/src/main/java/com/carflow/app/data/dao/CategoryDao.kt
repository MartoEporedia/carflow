package com.carflow.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.carflow.app.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE isDeleted = 0 ORDER BY type ASC, sortOrder ASC, name ASC")
    fun getAllActive(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = 'DEFAULT' AND isDeleted = 0 ORDER BY sortOrder ASC")
    fun getDefaults(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = 'CUSTOM' AND isDeleted = 0 ORDER BY name ASC")
    fun getCustom(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM categories WHERE isDeleted = 0")
    suspend fun count(): Int
}
