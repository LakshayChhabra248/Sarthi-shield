package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HazardType {
    POTHOLE,
    SPEED_BUMP,
    WATERLOGGING,
    ROUGH_ROAD,
    OBSTACLE,
    CRASH_IMPACT
}

enum class FusionSource {
    FUSED_VISION_AND_IMU,
    VISION_ONLY,
    IMU_ONLY,
    MANUAL_REPORT
}

@Entity(tableName = "hazards")
data class HazardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: HazardType,
    val fusionSource: FusionSource,
    val confidence: Float, // 0.0 to 1.0 (e.g. 0.96 for fused)
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val speedKmh: Float = 0f,
    val peakGForce: Float = 0f,
    val zAxisDisplacement: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isSyncedToCloud: Boolean = false,
    val notes: String = ""
)
