package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HazardDao {
    @Query("SELECT * FROM hazards ORDER BY timestamp DESC")
    fun getAllHazards(): Flow<List<HazardEntity>>

    @Query("SELECT * FROM hazards ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHazards(limit: Int): Flow<List<HazardEntity>>

    @Query("SELECT * FROM hazards WHERE type = :type ORDER BY timestamp DESC")
    fun getHazardsByType(type: HazardType): Flow<List<HazardEntity>>

    @Query("SELECT * FROM hazards WHERE fusionSource = 'FUSED_VISION_AND_IMU' ORDER BY timestamp DESC")
    fun getHighConfidenceHazards(): Flow<List<HazardEntity>>

    @Query("SELECT * FROM hazards WHERE id = :id")
    suspend fun getHazardById(id: Long): HazardEntity?

    @Query("SELECT * FROM hazards")
    suspend fun getAllHazardsList(): List<HazardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: HazardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazards(hazards: List<HazardEntity>)

    @Update
    suspend fun updateHazard(hazard: HazardEntity)

    @Delete
    suspend fun deleteHazard(hazard: HazardEntity)

    @Query("DELETE FROM hazards")
    suspend fun clearAll()
}
