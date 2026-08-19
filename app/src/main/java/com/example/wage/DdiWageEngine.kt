package com.example.wage

import com.example.data.local.WeatherCondition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

data class LiveDdiMetrics(
    val level: Int = 1, // 1 to 5
    val label: String = "Smooth Standard (DDI-1)",
    val bonusMultiplier: Float = 0.0f, // 0.0 to 0.50 (0% to 50%)
    val roughnessScore: Float = 1.2f, // derived from IMU RMS / Kurtosis
    val visualHazardDensity: Int = 0,
    val weather: WeatherCondition = WeatherCondition.CLEAR,
    val isRatingImmunityEligible: Boolean = false,
    val immunityReason: String? = null
)

data class LiveWageBreakdown(
    val basePay: Double = 25.0,
    val distanceKm: Float = 4.2f,
    val distanceFee: Double = 27.3, // e.g. 4.2 km * ₹6.5/km
    val ddiBonus: Double = 0.0,
    val waitMinutes: Int = 0,
    val waitCharge: Double = 0.0,
    val totalFairPayout: Double = 52.3,
    val oldUnfairFlatPay: Double = 25.0,
    val extraMoneyEarned: Double = 27.3
)

class DdiWageEngine {

    private val _liveDdi = MutableStateFlow(LiveDdiMetrics())
    val liveDdi: StateFlow<LiveDdiMetrics> = _liveDdi.asStateFlow()

    private val _liveWage = MutableStateFlow(LiveWageBreakdown())
    val liveWage: StateFlow<LiveWageBreakdown> = _liveWage.asStateFlow()

    // Base constants
    val standardBasePay = 25.0 // ₹25
    val ratePerKm = 6.50 // ₹6.50/km
    val waitChargePerMinute = 2.0 // ₹2.00/min after 5 mins

    /**
     * Dynamically calculates the Dynamic Difficulty Index (DDI 1 to 5)
     * using real-time IMU road roughness, Vision hazard count, and weather severity.
     */
    fun computeDdi(
        imuRms: Float,
        imuKurtosis: Float,
        visualHazardsCount: Int,
        weather: WeatherCondition,
        speedKmh: Float
    ): LiveDdiMetrics {
        // Calculate aggregate difficulty score from 0.0 to 10.0
        var score = 1.0f

        // 1. IMU Roughness contribution
        if (imuRms > 14.0f || imuKurtosis > 3.0f) {
            score += 2.2f
        } else if (imuRms > 11.5f) {
            score += 1.2f
        }

        // 2. Vision Hazard Density contribution
        if (visualHazardsCount >= 2) {
            score += 2.0f
        } else if (visualHazardsCount == 1) {
            score += 1.0f
        }

        // 3. Weather Severity contribution
        score += (weather.severityLevel - 1) * 1.3f

        // Map score to DDI Level 1..5
        val level = when {
            score >= 6.5f -> 5
            score >= 5.0f -> 4
            score >= 3.5f -> 3
            score >= 2.0f -> 2
            else -> 1
        }

        val (label, bonusMultiplier) = when (level) {
            5 -> Pair("Hazardous / Storm & Waterlogging (+50%)", 0.50f)
            4 -> Pair("Severe Road & Rain (+40%)", 0.40f)
            3 -> Pair("Potholes & Heat Stress (+25%)", 0.25f)
            2 -> Pair("Moderate Delay / Bumps (+12%)", 0.12f)
            else -> Pair("Standard Smooth Road (+0%)", 0.0f)
        }

        val immunityEligible = level >= 3
        val reason = when {
            level == 5 -> "Severe waterlogging and road obstacle verified by AI-Vision & IMU shock."
            level == 4 -> "Downpour and road crater density verified. Delivery delay immune."
            level == 3 -> "High pothole frequency verified on delivery route."
            else -> null
        }

        val metrics = LiveDdiMetrics(
            level = level,
            label = label,
            bonusMultiplier = bonusMultiplier,
            roughnessScore = imuRms,
            visualHazardDensity = visualHazardsCount,
            weather = weather,
            isRatingImmunityEligible = immunityEligible,
            immunityReason = reason
        )

        _liveDdi.value = metrics
        recalculateWage()
        return metrics
    }

    fun updateTripProgress(distanceKm: Float, waitMinutes: Int) {
        val currentDdi = _liveDdi.value
        val distFee = (distanceKm * ratePerKm)
        val subtotal = standardBasePay + distFee
        val ddiBonus = subtotal * currentDdi.bonusMultiplier

        // Wait time billing: First 5 minutes free, then ₹2/min
        val billableWaitMins = (waitMinutes - 5).coerceAtLeast(0)
        val waitCharge = billableWaitMins * waitChargePerMinute

        val total = subtotal + ddiBonus + waitCharge
        val oldFlat = standardBasePay // Old gig model paid only flat base fee

        _liveWage.value = LiveWageBreakdown(
            basePay = standardBasePay,
            distanceKm = distanceKm,
            distanceFee = distFee,
            ddiBonus = ddiBonus,
            waitMinutes = waitMinutes,
            waitCharge = waitCharge,
            totalFairPayout = total,
            oldUnfairFlatPay = oldFlat,
            extraMoneyEarned = total - oldFlat
        )
    }

    private fun recalculateWage() {
        val w = _liveWage.value
        updateTripProgress(w.distanceKm, w.waitMinutes)
    }

    fun setWeather(weather: WeatherCondition) {
        computeDdi(
            imuRms = _liveDdi.value.roughnessScore,
            imuKurtosis = 1.5f,
            visualHazardsCount = _liveDdi.value.visualHazardDensity,
            weather = weather,
            speedKmh = 35f
        )
    }
}
