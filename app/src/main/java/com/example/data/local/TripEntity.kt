package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GigPlatform(val displayName: String, val brandColorHex: Long) {
    ZOMATO("Zomato", 0xFFE23744),
    SWIGGY("Swiggy", 0xFFFC8019),
    ZEPTO("Zepto", 0xFF880E4F),
    BLINKIT("Blinkit", 0xFFF4C430),
    UBER_EATS("Uber", 0xFF000000),
    ONDC_DIRECT("ONDC Direct", 0xFF1976D2)
}

enum class WeatherCondition(val label: String, val severityLevel: Int, val emoji: String) {
    CLEAR("Clear Weather", 1, "☀️"),
    MILD_RAIN("Mild Rain", 2, "🌦️"),
    EXTREME_HEAT("Extreme Heat (44°C+)", 3, "🔥"),
    HEAVY_DOWNPOUR("Heavy Downpour", 4, "⛈️"),
    WATERLOGGED_STORM("Severe Waterlogging & Storm", 5, "🌊")
}

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val platform: GigPlatform,
    val pickupLocation: String,
    val dropoffLocation: String,
    val distanceKm: Float,
    val basePay: Double,
    val distanceFee: Double,
    val ddiLevel: Int, // 1 to 5
    val ddiBonus: Double,
    val waitMinutes: Int,
    val waitCharge: Double,
    val finalPayout: Double,
    val oldFlatPayout: Double, // The unfair flat pay without Sarthi-Shield
    val ratingImmunityApplied: Boolean,
    val potholesEncountered: Int,
    val weather: WeatherCondition,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "incidents")
data class IncidentReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: String,
    val incidentType: IncidentType,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val decibelPeak: Float = 0f,
    val peakGForce: Float = 0f,
    val audioEvidenceRecorded: Boolean = false,
    val visualEvidenceCaptured: Boolean = false,
    val ratingImmunityCertificateId: String,
    val disputeResolved: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

enum class IncidentType {
    UNFAIR_LATE_REVIEW_SHIELD,
    CUSTOMER_AGGRESSION_DETECTED,
    WATERLOGGING_ROADBLOCK_DELAY,
    CRASH_FALL_EVENT,
    EXTREME_CUSTOMER_WAIT_TIME
}
