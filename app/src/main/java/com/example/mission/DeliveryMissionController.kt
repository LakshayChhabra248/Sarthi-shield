package com.example.mission

import android.content.Context
import com.example.audio.AudioAlertEngine
import com.example.audio.VoiceSafetyDetector
import com.example.data.local.GigPlatform
import com.example.data.local.IncidentType
import com.example.data.local.TripDao
import com.example.data.local.TripEntity
import com.example.data.local.WeatherCondition
import com.example.shield.RatingShieldEngine
import com.example.wage.DdiWageEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class DeliveryStage(val title: String, val subtitle: String) {
    IDLE("Ready for Orders", "Sarthi-Shield is active in background"),
    PICKED_UP("Order Picked Up", "Heading towards delivery destination"),
    TRANSIT_POTHOLE_STRETCH("In Transit (Broken Road)", "Road craters & high roughness detected"),
    TRANSIT_WATERLOGGED("In Transit (Waterlogged Alley)", "Severe waterlogging & storm conditions"),
    ARRIVED_CUSTOMER_GATE("Arrived at Customer Location", "Geofence active - Wait timer running"),
    ORDER_COMPLETED("Delivery Completed!", "Fair wage itemized payout settled")
}

data class ActiveDeliveryOrder(
    val orderId: String = "ZOM-8942",
    val platform: GigPlatform = GigPlatform.ZOMATO,
    val restaurant: String = "Haldiram's Sweets & Restaurant",
    val customerName: String = "Rahul Sharma",
    val deliveryAddress: String = "Tower B-4, Flat 602, Gulshan Vivante, Sector 137",
    val totalDistanceKm: Float = 5.6f,
    val coveredDistanceKm: Float = 2.4f,
    val estimatedTimeMins: Int = 18,
    val stage: DeliveryStage = DeliveryStage.PICKED_UP,
    val waitTimeSeconds: Int = 0,
    val potholesCount: Int = 4,
    val isRatingImmunityActive: Boolean = true,
    val immunityCertId: String = "SARTHI-IMMUNITY-77F92A",
    val lastSettledTrip: TripEntity? = null
)

class DeliveryMissionController(
    private val context: Context,
    private val tripDao: TripDao,
    private val ddiEngine: DdiWageEngine,
    private val ratingShield: RatingShieldEngine,
    private val voiceSafety: VoiceSafetyDetector,
    private val audioAlerts: AudioAlertEngine,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    val allTrips: StateFlow<List<TripEntity>> = tripDao.getAllTrips()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalEarnings: StateFlow<Double?> = tripDao.getTotalEarnings()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), 485.50)

    val totalProtectedBonus: StateFlow<Double?> = tripDao.getTotalProtectedBonus()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), 164.20)

    private val _currentOrder = MutableStateFlow(ActiveDeliveryOrder())
    val currentOrder: StateFlow<ActiveDeliveryOrder> = _currentOrder.asStateFlow()

    private var waitTimerJob: Job? = null

    init {
        // Connect Voice Safety to Rating Shield
        voiceSafety.onAggressionIncidentDetected = { db, reason ->
            val order = _currentOrder.value
            ratingShield.issueRatingImmunity(
                orderId = order.orderId,
                incidentType = IncidentType.CUSTOMER_AGGRESSION_DETECTED,
                title = "Customer Aggression Neutralized",
                description = "Doorstep shouting ($db dB) recorded. Rating immune from retaliation.",
                lat = 28.6139,
                lng = 77.2090,
                peakG = 1.0f,
                peakDecibel = db
            )
            audioAlerts.announceCustom("Voice safety evidence captured and encrypted. ID protected.")
        }

        seedSampleTripsIfEmpty()
    }

    fun startNewTrip(platform: GigPlatform = GigPlatform.ZOMATO) {
        val randomId = platform.displayName.take(3).uppercase() + "-" + (1000..9999).random()
        _currentOrder.value = ActiveDeliveryOrder(
            orderId = randomId,
            platform = platform,
            restaurant = if (platform == GigPlatform.SWIGGY) "Bikanervala Express" else "Burger King Drive",
            customerName = "Priya Varma",
            deliveryAddress = "Tower 12, Floor 8, ATS Knightsbridge",
            totalDistanceKm = 4.8f,
            coveredDistanceKm = 0.5f,
            stage = DeliveryStage.PICKED_UP,
            waitTimeSeconds = 0,
            potholesCount = 0,
            isRatingImmunityActive = false
        )
        ddiEngine.updateTripProgress(0.5f, 0)
        ddiEngine.setWeather(WeatherCondition.CLEAR)
        audioAlerts.announceCustom("New delivery started for ${platform.displayName}. Sarthi-Shield active.")
    }

    fun simulatePotholeEncounter() {
        val order = _currentOrder.value
        val newPotholes = order.potholesCount + 2
        val certId = ratingShield.issueRatingImmunity(
            orderId = order.orderId,
            incidentType = IncidentType.UNFAIR_LATE_REVIEW_SHIELD,
            title = "Severe Pothole Cluster Verified",
            description = "Multiple road craters caused unavoidable delay. Rating Immunity issued.",
            lat = 28.6139,
            lng = 77.2090,
            peakG = 2.6f,
            peakDecibel = 48f
        )

        _currentOrder.value = order.copy(
            stage = DeliveryStage.TRANSIT_POTHOLE_STRETCH,
            coveredDistanceKm = (order.coveredDistanceKm + 1.2f).coerceAtMost(order.totalDistanceKm),
            potholesCount = newPotholes,
            isRatingImmunityActive = true,
            immunityCertId = certId
        )

        ddiEngine.computeDdi(
            imuRms = 15.2f,
            imuKurtosis = 4.1f,
            visualHazardsCount = 3,
            weather = WeatherCondition.MILD_RAIN,
            speedKmh = 28f
        )
        ddiEngine.updateTripProgress(order.coveredDistanceKm + 1.2f, order.waitTimeSeconds / 60)
        audioAlerts.announceCustom("High roughness detected. DDI upgraded to Level 3 (+25% Fair Wage bonus).")
    }

    fun simulateWaterloggingStorm() {
        val order = _currentOrder.value
        val certId = ratingShield.issueRatingImmunity(
            orderId = order.orderId,
            incidentType = IncidentType.WATERLOGGING_ROADBLOCK_DELAY,
            title = "Waterlogged Road Blockage",
            description = "Flooded underpass detected. Speed reduced to 12 km/h. Rating Immunity active.",
            lat = 28.6190,
            lng = 77.2140,
            peakG = 1.4f,
            peakDecibel = 52f
        )

        _currentOrder.value = order.copy(
            stage = DeliveryStage.TRANSIT_WATERLOGGED,
            coveredDistanceKm = (order.coveredDistanceKm + 1.5f).coerceAtMost(order.totalDistanceKm),
            isRatingImmunityActive = true,
            immunityCertId = certId
        )

        ddiEngine.computeDdi(
            imuRms = 18.0f,
            imuKurtosis = 5.2f,
            visualHazardsCount = 4,
            weather = WeatherCondition.WATERLOGGED_STORM,
            speedKmh = 14f
        )
        ddiEngine.updateTripProgress(order.coveredDistanceKm + 1.5f, order.waitTimeSeconds / 60)
        audioAlerts.announceCustom("Extreme waterlogging verified. DDI Level 5 active (+50% Risk Premium).")
    }

    fun arriveAtCustomerGate() {
        _currentOrder.value = _currentOrder.value.copy(
            stage = DeliveryStage.ARRIVED_CUSTOMER_GATE,
            coveredDistanceKm = _currentOrder.value.totalDistanceKm
        )

        startWaitTimer()
        audioAlerts.announceCustom("Arrived at customer building. Geofence wait timer started. First 5 minutes grace, ₹2/minute after.")
    }

    private fun startWaitTimer() {
        waitTimerJob?.cancel()
        waitTimerJob = coroutineScope.launch {
            while (isActive) {
                delay(1000L) // Count 1 sec (simulates time progression)
                val curSec = _currentOrder.value.waitTimeSeconds + 20 // accelerated 20x for demo convenience
                val mins = curSec / 60
                _currentOrder.value = _currentOrder.value.copy(waitTimeSeconds = curSec)
                ddiEngine.updateTripProgress(_currentOrder.value.totalDistanceKm, mins)

                if (mins == 6 && curSec % 60 < 20) {
                    audioAlerts.announceCustom("Customer wait exceeded 5 minutes. ₹2 per minute fine now adding to payout.")
                }
            }
        }
    }

    fun completeDelivery() {
        waitTimerJob?.cancel()
        val order = _currentOrder.value
        val wage = ddiEngine.liveWage.value
        val ddi = ddiEngine.liveDdi.value

        val tripEntity = TripEntity(
            orderId = order.orderId,
            platform = order.platform,
            pickupLocation = order.restaurant,
            dropoffLocation = order.deliveryAddress,
            distanceKm = order.totalDistanceKm,
            basePay = wage.basePay,
            distanceFee = wage.distanceFee,
            ddiLevel = ddi.level,
            ddiBonus = wage.ddiBonus,
            waitMinutes = wage.waitMinutes,
            waitCharge = wage.waitCharge,
            finalPayout = wage.totalFairPayout,
            oldFlatPayout = wage.oldUnfairFlatPay,
            ratingImmunityApplied = order.isRatingImmunityActive,
            potholesEncountered = order.potholesCount,
            weather = ddi.weather,
            timestamp = System.currentTimeMillis()
        )

        coroutineScope.launch {
            tripDao.insertTrip(tripEntity)
        }

        _currentOrder.value = order.copy(
            stage = DeliveryStage.ORDER_COMPLETED,
            lastSettledTrip = tripEntity
        )

        audioAlerts.announceCustom("Order delivered! Fair payout settled: ₹${String.format("%.1f", wage.totalFairPayout)}. You earned ₹${String.format("%.1f", wage.extraMoneyEarned)} extra for your effort!")
    }

    private fun seedSampleTripsIfEmpty() {
        coroutineScope.launch {
            val list = tripDao.getRecentTrips(1)
            // Seed realistic delivery history
            val seeded = listOf(
                TripEntity(
                    orderId = "ZOM-7193",
                    platform = GigPlatform.ZOMATO,
                    pickupLocation = "Haldiram's Sweets",
                    dropoffLocation = "Sector 62 Noida",
                    distanceKm = 5.2f,
                    basePay = 25.0,
                    distanceFee = 33.8,
                    ddiLevel = 4,
                    ddiBonus = 23.52,
                    waitMinutes = 9,
                    waitCharge = 8.0,
                    finalPayout = 90.32,
                    oldFlatPayout = 25.0,
                    ratingImmunityApplied = true,
                    potholesEncountered = 6,
                    weather = WeatherCondition.HEAVY_DOWNPOUR,
                    timestamp = System.currentTimeMillis() - 7200000L
                ),
                TripEntity(
                    orderId = "SWG-8821",
                    platform = GigPlatform.SWIGGY,
                    pickupLocation = "Domino's Pizza Hub",
                    dropoffLocation = "Indirapuram Habitat Center",
                    distanceKm = 3.8f,
                    basePay = 25.0,
                    distanceFee = 24.7,
                    ddiLevel = 3,
                    ddiBonus = 12.42,
                    waitMinutes = 7,
                    waitCharge = 4.0,
                    finalPayout = 66.12,
                    oldFlatPayout = 25.0,
                    ratingImmunityApplied = true,
                    potholesEncountered = 4,
                    weather = WeatherCondition.EXTREME_HEAT,
                    timestamp = System.currentTimeMillis() - 18000000L
                ),
                TripEntity(
                    orderId = "ZPT-5510",
                    platform = GigPlatform.ZEPTO,
                    pickupLocation = "Zepto Dark Store #4",
                    dropoffLocation = "Jaypee Greens Pavilion",
                    distanceKm = 2.4f,
                    basePay = 25.0,
                    distanceFee = 15.6,
                    ddiLevel = 1,
                    ddiBonus = 0.0,
                    waitMinutes = 3,
                    waitCharge = 0.0,
                    finalPayout = 40.6,
                    oldFlatPayout = 25.0,
                    ratingImmunityApplied = false,
                    potholesEncountered = 0,
                    weather = WeatherCondition.CLEAR,
                    timestamp = System.currentTimeMillis() - 43200000L
                )
            )
            tripDao.insertTrips(seeded)
        }
    }
}
