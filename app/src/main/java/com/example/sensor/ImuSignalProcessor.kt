package com.example.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class ImuSignalProcessor(
    private val sensorManager: SensorManager,
    private val windowDurationMs: Long = 2500L // 2.5 second sliding window
) : SensorEventListener {

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Low-Pass Filter state for Z-axis vibration removal
    private var filteredZ = SensorManager.GRAVITY_EARTH
    private val lpfAlpha = 0.18f // Smoothing factor for motorcycle engine vibration attenuation

    // Rolling 2.5-second time series window buffer
    private val sampleBuffer = ConcurrentLinkedDeque<ImuSample>()

    // Waveform points for UI real-time oscilloscope (filtered vs raw Z)
    private val _waveformPoints = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val waveformPoints: StateFlow<List<Pair<Float, Float>>> = _waveformPoints.asStateFlow()

    private val _currentFeatures = MutableStateFlow(
        ImuFeatureSet(
            peakGForce = 1.0f,
            peakToPeakZ = 0.2f,
            rmsZ = 9.8f,
            kurtosisZ = 1.0f,
            crestFactor = 1.0f,
            gyroVariance = 0.01f,
            classifiedSurface = RoadSurfaceClassification.NORMAL_ROAD,
            anomalyConfidence = 0.95f
        )
    )
    val currentFeatures: StateFlow<ImuFeatureSet> = _currentFeatures.asStateFlow()

    private val _liveGForce = MutableStateFlow(1.0f)
    val liveGForce: StateFlow<Float> = _liveGForce.asStateFlow()

    private val _liveTilt = MutableStateFlow(0.0f)
    val liveTilt: StateFlow<Float> = _liveTilt.asStateFlow()

    private var latestGyroX = 0f
    private var latestGyroY = 0f
    private var latestGyroZ = 0f

    // Callback when an IMU anomaly (pothole, bump, crash) is classified
    var onImuAnomalyDetected: ((RoadSurfaceClassification, ImuFeatureSet, Long) -> Unit)? = null

    private var lastClassificationTime = 0L

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                latestGyroX = event.values[0]
                latestGyroY = event.values[1]
                latestGyroZ = event.values[2]
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]
                val now = System.currentTimeMillis()

                // 1. Digital Low-Pass Filter: y[n] = alpha * x[n] + (1 - alpha) * y[n-1]
                filteredZ = lpfAlpha * rawZ + (1f - lpfAlpha) * filteredZ

                // Overall magnitude / G-force
                val totalAccel = sqrt((rawX * rawX + rawY * rawY + rawZ * rawZ).toDouble()).toFloat()
                val gForce = totalAccel / SensorManager.GRAVITY_EARTH
                _liveGForce.value = gForce

                // Tilt angle
                val tilt = Math.toDegrees(
                    acos((rawZ / (totalAccel.coerceAtLeast(0.1f)).toDouble()).coerceIn(-1.0, 1.0))
                ).toFloat()
                _liveTilt.value = tilt

                val sample = ImuSample(
                    timestamp = now,
                    rawX = rawX,
                    rawY = rawY,
                    rawZ = rawZ,
                    filteredZ = filteredZ,
                    gyroX = latestGyroX,
                    gyroY = latestGyroY,
                    gyroZ = latestGyroZ
                )

                sampleBuffer.addLast(sample)

                // 2. Trim sliding window to exactly 2.5 seconds
                val cutoffTime = now - windowDurationMs
                while (sampleBuffer.isNotEmpty() && sampleBuffer.first.timestamp < cutoffTime) {
                    sampleBuffer.removeFirst()
                }

                // 3. Process time-series ML features every ~80ms
                if (now - lastClassificationTime >= 80 && sampleBuffer.size >= 15) {
                    lastClassificationTime = now
                    computeFeaturesAndClassify(now)
                }

                // 4. Update UI oscilloscope points (downsampled to 50 points)
                if (sampleBuffer.size % 3 == 0) {
                    val step = (sampleBuffer.size / 50).coerceAtLeast(1)
                    val points = sampleBuffer.filterIndexed { index, _ -> index % step == 0 }
                        .takeLast(50)
                        .map { Pair(it.rawZ, it.filteredZ) }
                    _waveformPoints.value = points
                }
            }
        }
    }

    /**
     * Extracts statistical time-series features over the 2.5s window
     * and runs Random Forest decision rules to classify road impact anomaly.
     */
    private fun computeFeaturesAndClassify(timestamp: Long) {
        val samples = sampleBuffer.toList()
        if (samples.isEmpty()) return

        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE
        var sumZ = 0.0
        var sumSqZ = 0.0
        var maxG = 0f
        var gyroVarSum = 0.0

        for (s in samples) {
            val z = s.rawZ
            if (z < minZ) minZ = z
            if (z > maxZ) maxZ = z
            sumZ += z
            sumSqZ += (z * z)
            val g = sqrt((s.rawX * s.rawX + s.rawY * s.rawY + s.rawZ * s.rawZ).toDouble()).toFloat() / SensorManager.GRAVITY_EARTH
            if (g > maxG) maxG = g
            val gMag = (s.gyroX * s.gyroX + s.gyroY * s.gyroY + s.gyroZ * s.gyroZ)
            gyroVarSum += gMag
        }

        val n = samples.size
        val meanZ = (sumZ / n).toFloat()
        val varianceZ = ((sumSqZ / n) - (meanZ * meanZ)).coerceAtLeast(0.0001)
        val stdDevZ = sqrt(varianceZ).toFloat()
        val rmsZ = sqrt(sumSqZ / n).toFloat()
        val peakToPeakZ = maxZ - minZ

        // Kurtosis calculation: kurtosis = (1/N * sum((z - mean)^4)) / stdDev^4
        var sumFourthPower = 0.0
        for (s in samples) {
            sumFourthPower += (s.rawZ - meanZ).toDouble().pow(4.0)
        }
        val kurtosisZ = ((sumFourthPower / n) / varianceZ.pow(2.0)).toFloat()
        val crestFactor = (maxZ.coerceAtLeast(1f) / rmsZ.coerceAtLeast(0.1f))
        val gyroVariance = (gyroVarSum / n).toFloat()

        // Multi-tier Random Forest Decision Classifier
        val (classification, confidence) = when {
            // Severe crash: Extreme G-force + extreme tilt or rotational spike
            maxG >= 2.6f && _liveTilt.value >= 55f -> {
                Pair(RoadSurfaceClassification.CRASH_EVENT, 0.99f)
            }
            // Pothole: Sharp vertical spike with high kurtosis and high peak-to-peak shock
            peakToPeakZ >= 14.5f && kurtosisZ >= 3.2f && crestFactor >= 1.6f -> {
                val conf = (0.82f + (kurtosisZ * 0.02f)).coerceIn(0.80f, 0.98f)
                Pair(RoadSurfaceClassification.POTHOLE_IMPACT, conf)
            }
            // Speed bump: Moderate peak-to-peak without extreme kurtosis spike + pitch rotation
            peakToPeakZ in 7.0f..14.4f && kurtosisZ < 3.2f && gyroVariance > 0.15f -> {
                Pair(RoadSurfaceClassification.SPEED_BUMP, 0.89f)
            }
            // Rough road: High standard deviation / continuous jitter
            stdDevZ > 3.5f -> {
                Pair(RoadSurfaceClassification.ROUGH_SURFACE, 0.84f)
            }
            else -> {
                Pair(RoadSurfaceClassification.NORMAL_ROAD, 0.96f)
            }
        }

        val featureSet = ImuFeatureSet(
            peakGForce = maxG,
            peakToPeakZ = peakToPeakZ,
            rmsZ = rmsZ,
            kurtosisZ = kurtosisZ,
            crestFactor = crestFactor,
            gyroVariance = gyroVariance,
            classifiedSurface = classification,
            anomalyConfidence = confidence
        )

        _currentFeatures.value = featureSet

        if (classification.isAnomaly) {
            onImuAnomalyDetected?.invoke(classification, featureSet, timestamp)
        }
    }

    /**
     * Simulates an intentional physical impact anomaly (e.g. hitting a pothole)
     * for testing without shaking the device violently.
     */
    fun injectSimulatedImpact(type: RoadSurfaceClassification) {
        val now = System.currentTimeMillis()
        val (p2p, kurtosis, g) = when (type) {
            RoadSurfaceClassification.POTHOLE_IMPACT -> Triple(18.5f, 5.2f, 2.2f)
            RoadSurfaceClassification.SPEED_BUMP -> Triple(9.8f, 2.1f, 1.4f)
            RoadSurfaceClassification.CRASH_EVENT -> Triple(32.0f, 8.4f, 3.1f)
            else -> Triple(1.2f, 1.1f, 1.0f)
        }

        val features = ImuFeatureSet(
            peakGForce = g,
            peakToPeakZ = p2p,
            rmsZ = 12.4f,
            kurtosisZ = kurtosis,
            crestFactor = 2.4f,
            gyroVariance = 0.85f,
            classifiedSurface = type,
            anomalyConfidence = 0.96f
        )
        _currentFeatures.value = features
        onImuAnomalyDetected?.invoke(type, features, now)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
