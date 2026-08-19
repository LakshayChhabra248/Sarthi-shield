package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.SensorViewModel
import com.example.ui.components.CameraVisionPreview
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
fun VisionScreen(viewModel: SensorViewModel) {
    val detectedHazards by viewModel.detectedObjects.collectAsState()
    val latencyMs by viewModel.inferenceLatencyMs.collectAsState()
    val fps by viewModel.cameraFps.collectAsState()
    val isSimulated by viewModel.isSimulatedRoad.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "MODULE 1: VISION PIPELINE",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "CameraX & YOLO Inference",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isSimulated) "Demo Feed" else "Live Camera",
                    fontSize = 11.sp,
                    color = BentoTextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isSimulated,
                    onCheckedChange = { viewModel.toggleSimulationMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BentoPrimary,
                        checkedTrackColor = BentoPrimaryContainer
                    )
                )
            }
        }

        // Camera Preview with Bounding Box overlays
        CameraVisionPreview(
            visionDetector = viewModel.visionDetector,
            detectedHazards = detectedHazards,
            isSimulated = isSimulated,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        // Telemetry Bento Bar (Latency, FPS, Model)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Latency Block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("INFERENCE", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${latencyMs}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        Text(" ms", fontSize = 11.sp, color = BentoTextTertiary, modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Text("INT8 Quantized", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }

            // FPS Block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("FRAME RATE", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$fps", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                        Text(" FPS", fontSize = 11.sp, color = BentoTextTertiary, modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Text("ImageAnalysis UseCase", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }

            // Active Targets Block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("TRACKED", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text("${detectedHazards.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    Text("Road Surface Ahead", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }
        }

        // Live Bounding Box Stream List
        Text(
            "Active Detection Feed (Zero-Copy Buffer):",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            fontWeight = FontWeight.Medium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (detectedHazards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BentoCard, RoundedCornerShape(16.dp))
                            .border(1.dp, BentoBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Road surface clear. Scanning for upcoming hazards...", fontSize = 12.sp, color = BentoTextTertiary)
                    }
                }
            } else {
                items(detectedHazards) { hazard ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BentoCard, RoundedCornerShape(16.dp))
                            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val tagColor = if (hazard.label == "POTHOLE") Color(0xFFFF5252) else Color(0xFFFFD740)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tagColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, tint = tagColor, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(hazard.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                                    Text("Dist: ~${String.format("%.1f", hazard.estimatedDistanceMeters)}m ahead", fontSize = 11.sp, color = BentoTextSecondary)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("${(hazard.confidence * 100).toInt()}% Conf", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                                LinearProgressIndicator(
                                    progress = { hazard.confidence },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BentoPrimary,
                                    trackColor = BentoPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Test Trigger Button
        Button(
            onClick = { viewModel.simulatePotholeImpact() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = BentoPrimary)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simulate Vision + IMU Fusion Test", color = BentoPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}
