package com.carflow.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.carflow.app.data.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicles WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: VehicleEntity)

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vehicles WHERE isDeleted = 0")
    suspend fun count(): Int

    @Query("SELECT * FROM vehicles WHERE updatedAt > :since")
    suspend fun getModifiedSince(since: Long): List<VehicleEntity>
}
