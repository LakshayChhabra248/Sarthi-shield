package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ActiveWarning(
    val message: String,
    val distanceMeters: Int,
    val hazardType: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AudioAlertEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val _lastAlert = MutableStateFlow<ActiveWarning?>(null)
    val lastAlert: StateFlow<ActiveWarning?> = _lastAlert.asStateFlow()

    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private var lastSpokenTime = 0L
    private val minSpeakIntervalMs = 6000L // Prevent alert spamming

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(1.0f)
            }
        }
    }

    fun setAudioEnabled(enabled: Boolean) {
        _isAudioEnabled.value = enabled
    }

    /**
     * Broadcasts proactive safety voice alert to the rider through helmet audio / phone speaker
     */
    fun announceHazardProximity(hazardType: String, distanceMeters: Int) {
        val now = System.currentTimeMillis()
        if (now - lastSpokenTime < minSpeakIntervalMs) return
        lastSpokenTime = now

        val speechText = when (hazardType.uppercase()) {
            "POTHOLE" -> "Caution! Confirmed pothole $distanceMeters meters ahead. Reduce speed."
            "SPEED_BUMP" -> "Speed bump ahead in $distanceMeters meters."
            "CRASH_EVENT" -> "Emergency alert! High impact crash detected. Initiating SOS."
            else -> "Warning! Road hazard detected $distanceMeters meters ahead."
        }

        _lastAlert.value = ActiveWarning(
            message = speechText,
            distanceMeters = distanceMeters,
            hazardType = hazardType
        )

        // Haptic pulse
        triggerVibrationAlert()

        // Audio TTS Voice
        if (_isAudioEnabled.value && isTtsReady) {
            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "hazard_alert_${System.currentTimeMillis()}")
        }
    }

    fun announceCustom(text: String) {
        if (_isAudioEnabled.value && isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "custom_alert_${System.currentTimeMillis()}")
        }
    }

    private fun triggerVibrationAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 150, 100, 250),
                    intArrayOf(0, 255, 0, 255),
                    -1
                )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 150, 100, 250), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
