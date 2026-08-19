package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sin
import kotlin.random.Random

data class VoiceSafetyState(
    val isMonitoring: Boolean = true,
    val currentDecibel: Float = 42.0f,
    val isAggressionDetected: Boolean = false,
    val isRecordingEvidence: Boolean = false,
    val alertMessage: String? = null,
    val audioWaveform: List<Float> = emptyList()
)

class VoiceSafetyDetector(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    private val _safetyState = MutableStateFlow(VoiceSafetyState())
    val safetyState: StateFlow<VoiceSafetyState> = _safetyState.asStateFlow()

    private var monitoringJob: Job? = null
    private var isSimulatingNoise = true

    // Callback when aggression / shouting spike is verified
    var onAggressionIncidentDetected: ((Float, String) -> Unit)? = null

    init {
        startMonitoring()
    }

    fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = coroutineScope.launch {
            var step = 0f
            val waveformHistory = ArrayDeque<Float>()

            while (isActive) {
                step += 0.1f
                // Base ambient decibel between 38 and 50 dB (typical conversation/street)
                var dB = 42f + (sin(step * 0.7f) * 6f).toFloat() + Random.nextFloat() * 4f

                // If simulated aggression spike is active
                if (_safetyState.value.isAggressionDetected) {
                    dB = 82f + Random.nextFloat() * 10f
                }

                waveformHistory.addLast(dB)
                if (waveformHistory.size > 30) {
                    waveformHistory.removeFirst()
                }

                val aggression = dB >= 76f
                val alert = if (aggression) {
                    "⚠️ Hostile Voice / Shouting Detected (%.1f dB)! Evidence Recording Active.".format(dB)
                } else null

                _safetyState.value = _safetyState.value.copy(
                    isMonitoring = true,
                    currentDecibel = dB,
                    isAggressionDetected = aggression,
                    isRecordingEvidence = aggression,
                    alertMessage = alert,
                    audioWaveform = waveformHistory.toList()
                )

                if (aggression) {
                    onAggressionIncidentDetected?.invoke(dB, "Hostile vocal shouting at delivery dropoff")
                }

                delay(120L)
            }
        }
    }

    /**
     * Trigger simulated customer shouting / badtameezi scenario
     */
    fun triggerSimulatedCustomerAggression() {
        coroutineScope.launch {
            _safetyState.value = _safetyState.value.copy(
                isAggressionDetected = true,
                isRecordingEvidence = true,
                currentDecibel = 86.5f,
                alertMessage = "⚠️ Customer Aggression Detected (86.5 dB)! Evidence Encrypted & ID Protected."
            )
            onAggressionIncidentDetected?.invoke(86.5f, "Customer verbal harassment at doorstep")
            delay(6000L)
            _safetyState.value = _safetyState.value.copy(
                isAggressionDetected = false,
                isRecordingEvidence = false,
                currentDecibel = 44.0f,
                alertMessage = null
            )
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        _safetyState.value = _safetyState.value.copy(isMonitoring = false)
    }
}
