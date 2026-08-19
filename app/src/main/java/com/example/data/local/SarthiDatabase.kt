package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HazardEntity::class,
        TripEntity::class,
        IncidentReportEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SarthiDatabase : RoomDatabase() {
    abstract fun hazardDao(): HazardDao
    abstract fun tripDao(): TripDao
    abstract fun incidentDao(): IncidentDao

    companion object {
        @Volatile
        private var INSTANCE: SarthiDatabase? = null

        fun getDatabase(context: Context): SarthiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SarthiDatabase::class.java,
                    "sarthi_shield_db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
