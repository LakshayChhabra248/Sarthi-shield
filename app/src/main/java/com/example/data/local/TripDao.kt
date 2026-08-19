package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTrips(limit: Int): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTripById(id: Long): TripEntity?

    @Query("SELECT SUM(finalPayout) FROM trips")
    fun getTotalEarnings(): Flow<Double?>

    @Query("SELECT SUM(ddiBonus + waitCharge) FROM trips")
    fun getTotalProtectedBonus(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips")
    suspend fun clearAll()
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentReportEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: Long): IncidentReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentReportEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<IncidentReportEntity>)

    @Update
    suspend fun updateIncident(incident: IncidentReportEntity)

    @Delete
    suspend fun deleteIncident(incident: IncidentReportEntity)

    @Query("DELETE FROM incidents")
    suspend fun clearAll()
}
