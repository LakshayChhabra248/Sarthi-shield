package com.example.fusion

import android.content.Context
import com.example.audio.AudioAlertEngine
import com.example.data.local.FusionSource
import com.example.data.local.HazardDao
import com.example.data.local.HazardEntity
import com.example.data.local.HazardType
import com.example.location.LocationTracker
import com.example.sensor.ImuFeatureSet
import com.example.sensor.ImuSignalProcessor
import com.example.sensor.RoadSurfaceClassification
import com.example.vision.DetectedHazard
import com.example.vision.YoloVisionDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedDeque

data class FusionEvent(
    val title: String,
    val description: String,
    val fusionSource: FusionSource,
    val confidencePercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class DataFusionEngine(
    private val context: Context,
    private val visionDetector: YoloVisionDetector,
    private val imuProcessor: ImuSignalProcessor,
    private val locationTracker: LocationTracker,
    private val hazardDao: HazardDao,
    private val audioAlertEngine: AudioAlertEngine,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    // Rolling queue of recent visual detections (within past 3 seconds)
    private val recentVisualDetections = ConcurrentLinkedDeque<DetectedHazard>()

    // Event broadcast for UI notifications
    private val _fusionEvents = MutableSharedFlow<FusionEvent>(extraBufferCapacity = 20)
    val fusionEvents: SharedFlow<FusionEvent> = _fusionEvents.asSharedFlow()

    private val _activeAlertStatus = MutableStateFlow<String?>("Sarthi-Shield Active: Monitoring road & telemetry")
    val activeAlertStatus: StateFlow<String?> = _activeAlertStatus.asStateFlow()

    private val _fusedHazardsCount = MutableStateFlow(0)
    val fusedHazardsCount: StateFlow<Int> = _fusedHazardsCount.asStateFlow()

    private var proximityCheckJob: Job? = null
    private var isInitialized = false

    fun start() {
        if (isInitialized) return
        isInitialized = true

        // 1. Connect Vision stream
        visionDetector.onHazardVisualDetected = { visualHazard ->
            handleVisualDetection(visualHazard)
        }

        // 2. Connect IMU stream
        imuProcessor.onImuAnomalyDetected = { surfaceType, features, timestamp ->
            handleImuAnomaly(surfaceType, features, timestamp)
        }

        // 3. Start Geofence Proximity Monitor
        startProximityMonitor()

        // 4. Seed initial benchmark hazard landmarks if DB is empty
        seedBenchmarkHazardsIfEmpty()
    }

    private fun handleVisualDetection(visualHazard: DetectedHazard) {
        val now = System.currentTimeMillis()
        recentVisualDetections.addLast(visualHazard)

        // Remove detections older than 3 seconds
        val cutoff = now - 3000L
        while (recentVisualDetections.isNotEmpty() && recentVisualDetections.first.timestamp < cutoff) {
            recentVisualDetections.removeFirst()
        }
    }

    private fun handleImuAnomaly(
        surfaceType: RoadSurfaceClassification,
        features: ImuFeatureSet,
        timestamp: Long
    ) {
        coroutineScope.launch {
            val now = System.currentTimeMillis()
            
            // Search for correlated visual detection in the preceding time window (0.3s to 2.8s prior)
            val minCorrelationTime = now - 2800L
            val maxCorrelationTime = now - 300L

            val matchingVisual = recentVisualDetections.find { detection ->
                detection.timestamp in minCorrelationTime..maxCorrelationTime
            }

            val currentLocation = locationTracker.currentLocation.value
            val hazardType = when (surfaceType) {
                RoadSurfaceClassification.POTHOLE_IMPACT -> HazardType.POTHOLE
                RoadSurfaceClassification.SPEED_BUMP -> HazardType.SPEED_BUMP
                RoadSurfaceClassification.CRASH_EVENT -> HazardType.CRASH_IMPACT
                else -> HazardType.ROUGH_ROAD
            }

            val (fusionSource, finalConfidence) = if (matchingVisual != null) {
                // High-confidence multi-modal fusion!
                Pair(FusionSource.FUSED_VISION_AND_IMU, 0.98f)
            } else {
                // IMU physical contact only
                Pair(FusionSource.IMU_ONLY, features.anomalyConfidence.coerceAtMost(0.85f))
            }

            val hazardEntity = HazardEntity(
                type = hazardType,
                fusionSource = fusionSource,
                confidence = finalConfidence,
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                address = "Near Lat: %.4f, Lng: %.4f".format(currentLocation.latitude, currentLocation.longitude),
                speedKmh = currentLocation.speedKmh,
                peakGForce = features.peakGForce,
                zAxisDisplacement = features.peakToPeakZ,
                timestamp = timestamp,
                notes = if (matchingVisual != null) "Visual Match: ${matchingVisual.label} + IMU Shock" else "IMU Anomaly"
            )

            // Save to Room database
            hazardDao.insertHazard(hazardEntity)
            _fusedHazardsCount.value += 1

            val eventTitle = if (fusionSource == FusionSource.FUSED_VISION_AND_IMU) {
                "CONFIRMED HAZARD (Vision + IMU)"
            } else {
                "PHYSICAL IMPACT DETECTED"
            }

            val desc = "${hazardType.name}: ${String.format("%.1fg Peak, %.1f Z-p2p", features.peakGForce, features.peakToPeakZ)}"
            
            _fusionEvents.emit(
                FusionEvent(
                    title = eventTitle,
                    description = desc,
                    fusionSource = fusionSource,
                    confidencePercent = (finalConfidence * 100).toInt()
                )
            )

            _activeAlertStatus.value = "$eventTitle - Tagged at [${String.format("%.4f, %.4f", currentLocation.latitude, currentLocation.longitude)}]"

            if (surfaceType == RoadSurfaceClassification.CRASH_EVENT) {
                audioAlertEngine.announceHazardProximity("CRASH_EVENT", 0)
            }
        }
    }

    /**
     * Proximity Geofencing Monitor:
     * Compares real-time GPS coordinates against known database hazards.
     * When rider is approaching within 60-100 meters, announces voice warning via TTS!
     */
    private fun startProximityMonitor() {
        proximityCheckJob?.cancel()
        proximityCheckJob = coroutineScope.launch {
            while (isActive) {
                try {
                    val riderLoc = locationTracker.currentLocation.value
                    val allHazards = hazardDao.getAllHazardsList()

                    for (hazard in allHazards) {
                        val distanceMeters = locationTracker.calculateDistanceMeters(
                            riderLoc.latitude,
                            riderLoc.longitude,
                            hazard.latitude,
                            hazard.longitude
                        )

                        // Trigger alert if rider is within 15m to 85m of a confirmed hazard
                        if (distanceMeters in 15.0..85.0 && riderLoc.speedKmh >= 10f) {
                            audioAlertEngine.announceHazardProximity(
                                hazardType = hazard.type.name,
                                distanceMeters = distanceMeters.toInt()
                            )
                            _activeAlertStatus.value = "WARNING: Approaching ${hazard.type.name} in ${distanceMeters.toInt()}m!"
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(2000L) // Check every 2 seconds
            }
        }
    }

    private fun seedBenchmarkHazardsIfEmpty() {
        coroutineScope.launch {
            val count = hazardDao.getAllHazardsList().size
            if (count == 0) {
                val current = locationTracker.currentLocation.value
                val sampleHazards = listOf(
                    HazardEntity(
                        type = HazardType.POTHOLE,
                        fusionSource = FusionSource.FUSED_VISION_AND_IMU,
                        confidence = 0.97f,
                        latitude = current.latitude + 0.00045, // ~50 meters ahead
                        longitude = current.longitude + 0.00030,
                        address = "Ring Road North Sector, Connaught Lane",
                        speedKmh = 42f,
                        peakGForce = 2.4f,
                        zAxisDisplacement = 18.2f,
                        notes = "Deep pothole, severe rebound shock"
                    ),
                    HazardEntity(
                        type = HazardType.SPEED_BUMP,
                        fusionSource = FusionSource.FUSED_VISION_AND_IMU,
                        confidence = 0.94f,
                        latitude = current.latitude - 0.00080,
                        longitude = current.longitude + 0.00060,
                        address = "Delivery Hub Terminal 3 Approach",
                        speedKmh = 28f,
                        peakGForce = 1.6f,
                        zAxisDisplacement = 9.4f,
                        notes = "Unmarked speed breaker"
                    ),
                    HazardEntity(
                        type = HazardType.POTHOLE,
                        fusionSource = FusionSource.FUSED_VISION_AND_IMU,
                        confidence = 0.99f,
                        latitude = current.latitude + 0.00120,
                        longitude = current.longitude - 0.00050,
                        address = "Outer Bypass Sector 12",
                        speedKmh = 51f,
                        peakGForce = 2.8f,
                        zAxisDisplacement = 22.0f,
                        notes = "High-speed road crater"
                    )
                )
                hazardDao.insertHazards(sampleHazards)
                _fusedHazardsCount.value = sampleHazards.size
            }
        }
    }

    fun triggerManualFusedHazardDemo() {
        coroutineScope.launch {
            // 1. Vision sees pothole
            visionDetector.triggerSimulatedPotholeDirectly()
            delay(1200L) // 1.2 seconds later, vehicle reaches it
            // 2. IMU hits pothole
            imuProcessor.injectSimulatedImpact(RoadSurfaceClassification.POTHOLE_IMPACT)
        }
    }

    fun shutdown() {
        proximityCheckJob?.cancel()
        visionDetector.close()
        imuProcessor.stopListening()
        locationTracker.stopTracking()
        audioAlertEngine.shutdown()
    }
}
