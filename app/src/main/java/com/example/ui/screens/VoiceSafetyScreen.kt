package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SensorViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VoiceSafetyScreen(viewModel: SensorViewModel) {
    val voiceState by viewModel.voiceSafetyState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "DOORSTEP CONFLICT GUARD & AI AUDIO",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Voice Safety & Aggression AI",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (voiceState.isAggressionDetected) Color(0xFFC62828) else BentoPrimaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    if (voiceState.isAggressionDetected) "ALERT ACTIVE" else "GUARD ON",
                    fontSize = 10.sp,
                    color = if (voiceState.isAggressionDetected) Color.White else BentoPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Decibel Meter Bento Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = null,
                            tint = if (voiceState.isAggressionDetected) Color(0xFFFF5252) else BentoPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("AMBIENT ACOUSTIC MONITOR", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Text("On-Device Microphone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        }
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format(Locale.US, "%.1f", voiceState.currentDecibel),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (voiceState.currentDecibel > 75f) Color(0xFFFF5252) else BentoPrimary
                        )
                        Text(" dB", fontSize = 12.sp, color = BentoTextTertiary, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }

                // Live Audio Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14141A))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp)) {
                        val w = size.width
                        val h = size.height

                        // Draw threshold guideline (75 dB line)
                        val thresholdY = h * (1f - (75f - 30f) / 60f)
                        drawLine(
                            color = Color(0xFFFF5252).copy(alpha = 0.5f),
                            start = Offset(0f, thresholdY),
                            end = Offset(w, thresholdY),
                            strokeWidth = 2f
                        )

                        // Draw audio wave bars
                        val points = voiceState.audioWaveform
                        if (points.isNotEmpty()) {
                            val barWidth = (w / points.size.coerceAtLeast(1)) - 3f
                            points.forEachIndexed { i, db ->
                                val x = i * (barWidth + 3f)
                                val norm = ((db - 30f) / 60f).coerceIn(0.1f, 1.0f)
                                val barH = h * norm
                                val y = h - barH
                                val barColor = if (db >= 75f) Color(0xFFFF5252) else BentoPrimary

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth.coerceAtLeast(2f), barH),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Baseline: 40-50 dB (Normal)", fontSize = 9.sp, color = BentoTextTertiary)
                    Text("Aggression Threshold: ≥ 76 dB", fontSize = 9.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Active Evidence Status / Alert Card
        if (voiceState.isAggressionDetected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF880E4F))
                    .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.SecurityUpdateWarning, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    Column {
                        Text("HOSTILE VOCAL PATTERN DETECTED", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(
                            "Evidence clip timestamped. Legal rating immunity generated to block retaliation reviews.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Feature Breakdown Cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(20.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = BentoPrimary)
                    Text("Doorstep Protection Protocols:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Text(
                    "1. Automatic Decibel Trigger: When you arrive at a delivery address geofence, ambient mic monitoring is armed.\n" +
                    "2. Badtameezi & Abuse Shield: If customer shouts, audio snippet is encrypted on-device to serve as objective evidence.\n" +
                    "3. De-Escalation Prompt: Plays soothing audio or alerts gig fleet support in 1-tap.",
                    fontSize = 11.sp,
                    color = BentoTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // Action Buttons
        Button(
            onClick = { viewModel.voiceSafetyDetector.triggerSimulatedCustomerAggression() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simulate Customer Shouting / Aggression (86 dB)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { viewModel.triggerSOS() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Sos, contentDescription = null, tint = BentoAccentPink)
            Spacer(modifier = Modifier.width(8.dp))
            Text("1-Tap Emergency Fleet Dispatch Call", fontSize = 12.sp, color = BentoAccentPink, fontWeight = FontWeight.Bold)
        }
    }
}
