package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SensorViewModel
import com.example.ui.components.CameraVisionPreview
import com.example.ui.components.ImuWaveformCanvas
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VisionImuScreen(viewModel: SensorViewModel) {
    val detectedObjects by viewModel.detectedObjects.collectAsState()
    val latencyMs by viewModel.inferenceLatencyMs.collectAsState()
    val fps by viewModel.cameraFps.collectAsState()
    val isSimulated by viewModel.isSimulatedRoad.collectAsState()
    val imuFeatures by viewModel.imuFeatures.collectAsState()
    val liveG by viewModel.liveGForce.collectAsState()
    val liveTilt by viewModel.liveTilt.collectAsState()
    val waveformPoints by viewModel.waveformPoints.collectAsState()

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
                    "SHIELD-VISION & SARTHI-NODE (AI-IOT BRAIN)",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Vision & IMU Edge Core",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E7D32))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("100 Hz SYNC", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // CameraX YOLO Vision Module
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, tint = BentoPrimary)
                        Text("Shield-Vision (YOLOv8 INT8)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${fps} FPS", fontSize = 10.sp, color = BentoPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("•", fontSize = 10.sp, color = BentoTextTertiary)
                        Text("${latencyMs} ms", fontSize = 10.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                // Camera Preview Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    CameraVisionPreview(
                        visionDetector = viewModel.visionDetector,
                        detectedHazards = detectedObjects,
                        isSimulated = isSimulated,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isSimulated) "Simulated Road Feed Active" else "CameraX Zero-Copy Streaming",
                        fontSize = 10.sp,
                        color = BentoTextSecondary
                    )

                    Switch(
                        checked = isSimulated,
                        onCheckedChange = { viewModel.toggleSimulationMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BentoPrimary, checkedTrackColor = BentoPrimaryContainer)
                    )
                }
            }
        }

        // IMU Sensor & DSP Oscilloscope Module
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = BentoAccentPink)
                        Text("Sarthi-Node IMU (LPF α=0.18)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }

                    Text(
                        imuFeatures.classifiedSurface.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary,
                        modifier = Modifier
                            .background(BentoPrimaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Dual Oscilloscope
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF14141E))
                ) {
                    ImuWaveformCanvas(
                        waveformPoints = waveformPoints,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // DSP Metrics Bento Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Kurtosis", fontSize = 8.sp, color = BentoTextTertiary)
                            Text(String.format(Locale.US, "%.2f", imuFeatures.kurtosisZ), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("RMS Energy", fontSize = 8.sp, color = BentoTextTertiary)
                            Text(String.format(Locale.US, "%.1f", imuFeatures.rmsZ), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("G-Force Peak", fontSize = 8.sp, color = BentoTextTertiary)
                            Text(String.format(Locale.US, "%.1fg", liveG), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoAccentPink)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Tilt Angle", fontSize = 8.sp, color = BentoTextTertiary)
                            Text("${liveTilt.toInt()}°", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Test Voice & Fusion Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.speakTestAlert() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test TTS Alert", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.simulatePotholeImpact() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
            ) {
                Text("Trigger Fused Impact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
