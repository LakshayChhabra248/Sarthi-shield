package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FusionSource
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: SensorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val allHazards by viewModel.allHazards.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val detectedHazards by viewModel.detectedObjects.collectAsState()
    val imuFeatures by viewModel.imuFeatures.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SARTHI-SHIELD FUSION CORE",
                    color = BentoPrimary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Intelligent Edge Node",
                    color = BentoTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF49454F))
                    .border(1.dp, BentoBorder.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Security, contentDescription = "Active", tint = BentoPrimary)
            }
        }

        // Active Warning / Alert Top Banner (if any)
        uiState.activeWarningText?.let { warning ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (uiState.status == AppStatus.CRASH_SOS) Color(0xFFC62828) else BentoPrimaryContainer)
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (uiState.status == AppStatus.CRASH_SOS) Icons.Filled.Warning else Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = if (uiState.status == AppStatus.CRASH_SOS) Color.White else BentoPrimary
                    )
                    Text(
                        text = warning,
                        color = if (uiState.status == AppStatus.CRASH_SOS) Color.White else BentoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top Row (G-Force & Multi-modal status)
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // G-Force Block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BentoCard, RoundedCornerShape(24.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BentoPrimaryContainer, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Filled.Speed, contentDescription = "Motion", tint = BentoPrimary, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.0f", currentLocation.speedKmh)} KM/H",
                            color = BentoPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column {
                        Text("Live G-Force", fontSize = 13.sp, color = BentoTextSecondary, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", uiState.gForce),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.gForce > 2.0f) BentoAccentPink else Color.White
                            )
                            Text(
                                text = " G",
                                fontSize = 12.sp,
                                color = BentoTextTertiary,
                                modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }

            // Status & Tilt Column
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tilt
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BentoCard, RoundedCornerShape(24.dp))
                        .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.ScreenRotation, contentDescription = "Tilt", tint = BentoAccentPink, modifier = Modifier.size(20.dp))
                    Column {
                        Text("TILT ANGLE", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.tilt),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextPrimary
                            )
                            Text("°", fontSize = 10.sp, color = BentoTextTertiary)
                        }
                    }
                }

                // Road Status
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (uiState.status != AppStatus.SAFE) Color(0xFFC62828) else BentoCard,
                            RoundedCornerShape(24.dp)
                        )
                        .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        if (uiState.status != AppStatus.SAFE) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = "Status",
                        tint = if (uiState.status != AppStatus.SAFE) Color.White else BentoAccentGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text("SURFACE", fontSize = 9.sp, color = if (uiState.status != AppStatus.SAFE) Color.White.copy(alpha = 0.7f) else BentoTextTertiary, letterSpacing = 1.sp)
                        Text(
                            text = imuFeatures.classifiedSurface.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.status != AppStatus.SAFE) Color.White else BentoTextPrimary
                        )
                    }
                }
            }
        }

        // Multi-Modal Fusion Summary Bento Card
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
                        Icon(Icons.Filled.Hub, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                        Text("Multi-Modal Data Fusion", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }
                    Text(
                        "${allHazards.size} LOGGED IN ROOM",
                        fontSize = 10.sp,
                        color = BentoPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("VISION PIPELINE", fontSize = 9.sp, color = BentoTextTertiary)
                        Text("${detectedHazards.size} in View", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                    }
                    Column {
                        Text("IMU SIGNAL", fontSize = 9.sp, color = BentoTextTertiary)
                        Text("Kurt: %.1f".format(imuFeatures.kurtosisZ), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoTextPrimary)
                    }
                    Column {
                        Text("FUSED CONFIDENCE", fontSize = 9.sp, color = BentoTextTertiary)
                        Text("98.4% (Max)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                    }
                }
            }
        }

        // Spatial Orientation (Tri-axial Raw Accelerometer)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = BentoPrimary, modifier = Modifier.size(16.dp))
                    Text("Telemetry & GPS Anchor", fontSize = 13.sp, color = BentoTextSecondary, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = "FusedLocationClient",
                    color = BentoPrimary,
                    fontSize = 9.sp,
                    modifier = Modifier
                        .background(BentoPrimaryContainer, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LATITUDE", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text(String.format(Locale.US, "%.4f", currentLocation.latitude), fontSize = 16.sp, fontFamily = FontFamily.Monospace, color = BentoTextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BentoBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LONGITUDE", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text(String.format(Locale.US, "%.4f", currentLocation.longitude), fontSize = 16.sp, fontFamily = FontFamily.Monospace, color = BentoTextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BentoBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Z-FILTERED", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp)
                    Text(String.format(Locale.US, "%.1f", imuFeatures.rmsZ), fontSize = 16.sp, fontFamily = FontFamily.Monospace, color = BentoPrimary)
                }
            }
        }

        // Actions Row (Manual SOS & Quick Actions)
        Row(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // SOS Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (uiState.status == AppStatus.CRASH_SOS) Color(0xFFC62828) else BentoPrimary, RoundedCornerShape(24.dp))
                    .clickable { viewModel.triggerSOS() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "SOS",
                    tint = if (uiState.status == AppStatus.CRASH_SOS) Color.White else BentoPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
                Column {
                    Text(
                        text = "MANUAL SOS",
                        color = if (uiState.status == AppStatus.CRASH_SOS) Color.White else BentoPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Dispatch alert",
                        color = if (uiState.status == AppStatus.CRASH_SOS) Color.White.copy(alpha = 0.7f) else BentoPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Pothole Simulation Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(BentoCard, RoundedCornerShape(24.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .clickable { viewModel.simulatePotholeImpact() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Filled.AutoFixHigh, contentDescription = "Test Fusion", tint = BentoPrimary, modifier = Modifier.size(30.dp))
                Column {
                    Text("TEST FUSION", color = BentoTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Vision + IMU shock", color = BentoTextTertiary, fontSize = 11.sp)
                }
            }
        }
    }
}
