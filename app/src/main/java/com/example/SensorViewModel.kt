package com.example

import android.app.Application
import android.content.Context
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioAlertEngine
import com.example.audio.VoiceSafetyDetector
import com.example.audio.VoiceSafetyState
import com.example.data.local.*
import com.example.fusion.DataFusionEngine
import com.example.fusion.FusionEvent
import com.example.location.LocationTracker
import com.example.location.RiderLocation
import com.example.mission.ActiveDeliveryOrder
import com.example.mission.DeliveryMissionController
import com.example.sensor.ImuFeatureSet
import com.example.sensor.ImuSignalProcessor
import com.example.sensor.RoadSurfaceClassification
import com.example.shield.IdShieldHealth
import com.example.shield.RatingShieldEngine
import com.example.vision.DetectedHazard
import com.example.vision.YoloVisionDetector
import com.example.wage.DdiWageEngine
import com.example.wage.LiveDdiMetrics
import com.example.wage.LiveWageBreakdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppStatus {
    SAFE, FATIGUE_WARNING, CRASH_SOS
}

data class SarthiUiState(
    val status: AppStatus = AppStatus.SAFE,
    val gForce: Float = 1.0f,
    val tilt: Float = 0.0f,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 9.8f,
    val isVisionActive: Boolean = true,
    val isTtsEnabled: Boolean = true,
    val activeWarningText: String? = null,
    val lastFusionEvent: FusionEvent? = null
)

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val database = SarthiDatabase.getDatabase(application)
    val hazardDao: HazardDao = database.hazardDao()
    val tripDao: TripDao = database.tripDao()
    val incidentDao: IncidentDao = database.incidentDao()

    // Core Hardware & Sensor Abstractions
    val locationTracker = LocationTracker(application)
    val imuProcessor = ImuSignalProcessor(sensorManager)
    val visionDetector = YoloVisionDetector(viewModelScope)
    val audioAlertEngine = AudioAlertEngine(application)
    val voiceSafetyDetector = VoiceSafetyDetector(application, viewModelScope)

    // Economic, Shield & Mission Engines
    val ddiWageEngine = DdiWageEngine()
    val ratingShieldEngine = RatingShieldEngine(application, incidentDao, viewModelScope)
    val missionController = DeliveryMissionController(
        context = application,
        tripDao = tripDao,
        ddiEngine = ddiWageEngine,
        ratingShield = ratingShieldEngine,
        voiceSafety = voiceSafetyDetector,
        audioAlerts = audioAlertEngine,
        coroutineScope = viewModelScope
    )

    // Fusion Engine
    val dataFusionEngine = DataFusionEngine(
        context = application,
        visionDetector = visionDetector,
        imuProcessor = imuProcessor,
        locationTracker = locationTracker,
        hazardDao = hazardDao,
        audioAlertEngine = audioAlertEngine,
        coroutineScope = viewModelScope
    )

    // Room Database Observables
    val allHazards: StateFlow<List<HazardEntity>> = hazardDao.getAllHazards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTrips: StateFlow<List<TripEntity>> = missionController.allTrips
    val allIncidents: StateFlow<List<IncidentReportEntity>> = ratingShieldEngine.allIncidents
    val idHealth: StateFlow<IdShieldHealth> = ratingShieldEngine.idHealth
    val totalEarnings: StateFlow<Double?> = missionController.totalEarnings
    val totalProtectedBonus: StateFlow<Double?> = missionController.totalProtectedBonus

    // Live Mission & Economic Observables
    val currentOrder: StateFlow<ActiveDeliveryOrder> = missionController.currentOrder
    val liveDdi: StateFlow<LiveDdiMetrics> = ddiWageEngine.liveDdi
    val liveWage: StateFlow<LiveWageBreakdown> = ddiWageEngine.liveWage
    val voiceSafetyState: StateFlow<VoiceSafetyState> = voiceSafetyDetector.safetyState

    // Vision States
    val detectedObjects: StateFlow<List<DetectedHazard>> = visionDetector.detectedHazards
    val inferenceLatencyMs: StateFlow<Long> = visionDetector.inferenceLatencyMs
    val cameraFps: StateFlow<Int> = visionDetector.fps
    val isSimulatedRoad: StateFlow<Boolean> = visionDetector.isSimulatedRoadActive

    // IMU States
    val imuFeatures: StateFlow<ImuFeatureSet> = imuProcessor.currentFeatures
    val liveGForce: StateFlow<Float> = imuProcessor.liveGForce
    val liveTilt: StateFlow<Float> = imuProcessor.liveTilt
    val waveformPoints: StateFlow<List<Pair<Float, Float>>> = imuProcessor.waveformPoints

    // Location State
    val currentLocation: StateFlow<RiderLocation> = locationTracker.currentLocation

    // Overall UI State
    private val _uiState = MutableStateFlow(SarthiUiState())
    val uiState: StateFlow<SarthiUiState> = _uiState.asStateFlow()

    init {
        imuProcessor.startListening()
        locationTracker.startTracking()
        dataFusionEngine.start()

        // Observe fusion events
        viewModelScope.launch {
            dataFusionEngine.fusionEvents.collect { event ->
                _uiState.value = _uiState.value.copy(
                    lastFusionEvent = event,
                    activeWarningText = "${event.title}: ${event.description}"
                )
            }
        }

        // Observe IMU features to drive dynamic DDI in real time
        viewModelScope.launch {
            imuProcessor.currentFeatures.collect { feat ->
                val newStatus = when {
                    feat.classifiedSurface == RoadSurfaceClassification.CRASH_EVENT -> AppStatus.CRASH_SOS
                    feat.classifiedSurface == RoadSurfaceClassification.POTHOLE_IMPACT -> {
                        if (_uiState.value.status != AppStatus.CRASH_SOS) AppStatus.FATIGUE_WARNING else AppStatus.CRASH_SOS
                    }
                    else -> _uiState.value.status
                }
                _uiState.value = _uiState.value.copy(
                    gForce = feat.peakGForce,
                    status = newStatus
                )

                // Update DDI calculation with live road shock
                ddiWageEngine.computeDdi(
                    imuRms = feat.rmsZ,
                    imuKurtosis = feat.kurtosisZ,
                    visualHazardsCount = detectedObjects.value.size,
                    weather = liveDdi.value.weather,
                    speedKmh = currentLocation.value.speedKmh
                )
            }
        }
    }

    fun triggerSOS() {
        _uiState.value = _uiState.value.copy(status = AppStatus.CRASH_SOS)
        audioAlertEngine.announceCustom("Emergency SOS dispatched to gig fleet dispatch and emergency contacts.")
    }

    fun resetStatus() {
        _uiState.value = _uiState.value.copy(status = AppStatus.SAFE, activeWarningText = null)
    }

    fun triggerFatigue() {
        _uiState.value = _uiState.value.copy(status = AppStatus.FATIGUE_WARNING)
        audioAlertEngine.announceCustom("Fatigue warning. Micro-steering deviation detected. Please take a rest break.")
    }

    fun toggleSimulationMode(enabled: Boolean) {
        visionDetector.setSimulationMode(enabled)
    }

    fun toggleAudioAlerts(enabled: Boolean) {
        audioAlertEngine.setAudioEnabled(enabled)
        _uiState.value = _uiState.value.copy(isTtsEnabled = enabled)
    }

    fun simulatePotholeImpact() {
        dataFusionEngine.triggerManualFusedHazardDemo()
        missionController.simulatePotholeEncounter()
    }

    fun deleteHazard(hazard: HazardEntity) {
        viewModelScope.launch {
            hazardDao.deleteHazard(hazard)
        }
    }

    fun clearAllHazards() {
        viewModelScope.launch {
            hazardDao.clearAll()
        }
    }

    fun speakTestAlert() {
        audioAlertEngine.announceHazardProximity("POTHOLE", 45)
    }

    override fun onCleared() {
        super.onCleared()
        dataFusionEngine.shutdown()
        voiceSafetyDetector.stopMonitoring()
    }
}

class SensorViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
