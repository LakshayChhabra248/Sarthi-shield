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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.sensor.RoadSurfaceClassification
import com.example.ui.components.ImuWaveformCanvas
import com.example.ui.theme.BentoAccentGrey
import com.example.ui.theme.BentoAccentPink
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTextTertiary
import java.util.Locale

@Composable
fun ImuScreen(viewModel: SensorViewModel) {
    val imuFeatures by viewModel.imuFeatures.collectAsState()
    val waveformPoints by viewModel.waveformPoints.collectAsState()
    val liveGForce by viewModel.liveGForce.collectAsState()
    val liveTilt by viewModel.liveTilt.collectAsState()

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
                    "MODULE 2: SENSOR PIPELINE",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "IMU Signal Processing & ML",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoPrimaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("100 Hz FASTEST", fontSize = 10.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Live Oscilloscope LPF Filter Waveform Canvas
        ImuWaveformCanvas(
            waveformPoints = waveformPoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // ML Anomaly Classification Banner
        val statusBg = when (imuFeatures.classifiedSurface) {
            RoadSurfaceClassification.POTHOLE_IMPACT -> Color(0xFFC62828)
            RoadSurfaceClassification.SPEED_BUMP -> Color(0xFFE65100)
            RoadSurfaceClassification.CRASH_EVENT -> Color(0xFF880E4F)
            RoadSurfaceClassification.ROUGH_SURFACE -> Color(0xFF4A148C)
            else -> BentoPrimaryContainer
        }

        val statusTextColor = when (imuFeatures.classifiedSurface) {
            RoadSurfaceClassification.NORMAL_ROAD -> BentoPrimary
            else -> Color.White
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(statusBg)
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        if (imuFeatures.classifiedSurface.isAnomaly) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = statusTextColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            "SURFACE CLASSIFICATION (RANDOM FOREST / LSTM)",
                            fontSize = 9.sp,
                            color = statusTextColor.copy(alpha = 0.8f),
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            imuFeatures.classifiedSurface.label,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusTextColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("CONFIDENCE", fontSize = 9.sp, color = statusTextColor.copy(alpha = 0.8f))
                    Text(
                        "${(imuFeatures.anomalyConfidence * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }
        }

        // Statistical Time-Series Features Bento Grid (2.5s Sliding Window)
        Text(
            "Extracted Time-Series Features (2.5s Sliding Window):",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Kurtosis
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("KURTOSIS (Z)", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text(
                        String.format(Locale.US, "%.2f", imuFeatures.kurtosisZ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (imuFeatures.kurtosisZ > 3.2f) BentoAccentPink else BentoTextPrimary
                    )
                    Text("Impact Sharpness", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }

            // Peak-to-Peak
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("PEAK-TO-PEAK", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format(Locale.US, "%.1f", imuFeatures.peakToPeakZ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(" m/s²", fontSize = 10.sp, color = BentoTextTertiary, modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Text("Z-Amplitude", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RMS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("RMS ACCEL", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            String.format(Locale.US, "%.2f", imuFeatures.rmsZ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(" m/s²", fontSize = 10.sp, color = BentoTextTertiary, modifier = Modifier.padding(bottom = 2.dp))
                    }
                    Text("Signal Energy", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }

            // Crest Factor
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("CREST FACTOR", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text(
                        String.format(Locale.US, "%.2f", imuFeatures.crestFactor),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                    Text("Peak/RMS Ratio", fontSize = 9.sp, color = BentoTextSecondary)
                }
            }
        }

        // Real-Time Sensor Injection Controls
        Text(
            "Test Physical Sensor Injections:",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.imuProcessor.injectSimulatedImpact(RoadSurfaceClassification.POTHOLE_IMPACT) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Text("Pothole Shock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.imuProcessor.injectSimulatedImpact(RoadSurfaceClassification.SPEED_BUMP) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
            ) {
                Text("Speed Bump", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.imuProcessor.injectSimulatedImpact(RoadSurfaceClassification.NORMAL_ROAD) },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer)
            ) {
                Text("Normal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
            }
        }
    }
}
