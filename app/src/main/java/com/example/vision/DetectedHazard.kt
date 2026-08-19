package com.example.vision

import android.graphics.RectF

data class DetectedHazard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String,
    val confidence: Float,
    val boundingBox: RectF, // Normalized 0..1 coordinates (left, top, right, bottom)
    val timestamp: Long = System.currentTimeMillis(),
    val estimatedDistanceMeters: Float = 15f
)

enum class VisionHazardCategory(val displayName: String) {
    POTHOLE("Pothole"),
    SPEED_BUMP("Speed Bump"),
    MANHOLE("Manhole Hazard"),
    ROAD_DEBRIS("Road Debris"),
    CRACK_NETWORK("Severe Cracking")
}
