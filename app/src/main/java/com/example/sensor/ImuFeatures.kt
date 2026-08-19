package com.example.sensor

data class ImuSample(
    val timestamp: Long,
    val rawX: Float,
    val rawY: Float,
    val rawZ: Float,
    val filteredZ: Float,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f
)

data class ImuFeatureSet(
    val peakGForce: Float,
    val peakToPeakZ: Float,
    val rmsZ: Float,
    val kurtosisZ: Float,
    val crestFactor: Float,
    val gyroVariance: Float,
    val classifiedSurface: RoadSurfaceClassification,
    val anomalyConfidence: Float
)

enum class RoadSurfaceClassification(val label: String, val isAnomaly: Boolean) {
    NORMAL_ROAD("Normal Road", false),
    POTHOLE_IMPACT("Pothole Impact", true),
    SPEED_BUMP("Speed Bump", true),
    ROUGH_SURFACE("Rough / Unpaved", false),
    CRASH_EVENT("Severe Crash", true)
}
