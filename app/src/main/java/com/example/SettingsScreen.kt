package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoAccentPink
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTextTertiary

@Composable
fun SettingsScreen(viewModel: SensorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isTtsEnabled by viewModel.audioAlertEngine.isAudioEnabled.collectAsState()
    val isSimulated by viewModel.isSimulatedRoad.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Column {
            Text(
                "SYSTEM CONFIGURATION",
                fontSize = 10.sp,
                color = BentoPrimary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Engine Settings",
                fontSize = 24.sp,
                color = BentoTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Section 1: Vision Pipeline Config
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(20.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                    Text("Vision & YOLO Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Simulation Test Mode", fontSize = 13.sp, color = BentoTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Generate realistic road hazard stream", fontSize = 10.sp, color = BentoTextTertiary)
                    }
                    Switch(
                        checked = isSimulated,
                        onCheckedChange = { viewModel.toggleSimulationMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BentoPrimary, checkedTrackColor = BentoPrimaryContainer)
                    )
                }

                Text(
                    "Model Format: TensorFlow Lite (TFLite INT8 Quantized)\nResolution: 320x320 ImageAnalysis Stream",
                    fontSize = 10.sp,
                    color = BentoTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Section 2: Sensor & Signal Processing Config
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(20.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Sensors, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                    Text("IMU Digital Signal Processing", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sampling Rate:", fontSize = 11.sp, color = BentoTextSecondary)
                    Text("100 Hz (SENSOR_DELAY_FASTEST)", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Low-Pass Filter Alpha (α):", fontSize = 11.sp, color = BentoTextSecondary)
                    Text("0.18 (Engine Vibration Cutoff)", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sliding Time Window:", fontSize = 11.sp, color = BentoTextSecondary)
                    Text("2.50 Seconds", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Crash G-Force Threshold:", fontSize = 11.sp, color = BentoTextSecondary)
                    Text("≥ 2.5 G + 60° Tilt", fontSize = 11.sp, color = BentoAccentPink, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 3: Audio Alert Engine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(20.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                    Text("Foreground Audio Alerts (TTS)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Voice Proximity Alerts", fontSize = 13.sp, color = BentoTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Speaks warning before hazard arrival", fontSize = 10.sp, color = BentoTextTertiary)
                    }
                    Switch(
                        checked = isTtsEnabled,
                        onCheckedChange = { viewModel.toggleAudioAlerts(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BentoPrimary, checkedTrackColor = BentoPrimaryContainer)
                    )
                }

                Button(
                    onClick = { viewModel.speakTestAlert() },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer)
                ) {
                    Text("Test Voice Output", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 4: Simulation & Emergency Triggers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(20.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = BentoAccentPink, modifier = Modifier.size(18.dp))
                    Text("Simulation & Test Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.simulatePotholeImpact() },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Pothole Shock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.triggerFatigue() },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
                    ) {
                        Text("Fatigue Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { viewModel.triggerSOS() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F))
                ) {
                    Text("Simulate Crash SOS Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.clearAllHazards() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Room Hazard Database", fontSize = 11.sp)
                }
            }
        }
    }
}
