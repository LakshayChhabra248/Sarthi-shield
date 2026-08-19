package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SensorViewModel
import com.example.data.local.FusionSource
import com.example.data.local.HazardEntity
import com.example.data.local.HazardType
import com.example.ui.theme.BentoAccentPink
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HazardsMapScreen(viewModel: SensorViewModel) {
    val allHazards by viewModel.allHazards.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val lastAlert by viewModel.audioAlertEngine.lastAlert.collectAsState()

    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp),
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
                    "MODULE 3: FUSION & ALERTS",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "GPS Map & Hazard Database",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = { viewModel.speakTestAlert() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BentoPrimaryContainer)
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = "Test Audio Alert", tint = BentoPrimary)
            }
        }

        // Radar / Proximity Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BentoCard)
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)

                // Concentric radar rings (25m, 50m, 100m)
                drawCircle(color = BentoBorder.copy(alpha = 0.3f), radius = 35.dp.toPx(), center = center, style = Stroke(width = 1.5f))
                drawCircle(color = BentoBorder.copy(alpha = 0.3f), radius = 65.dp.toPx(), center = center, style = Stroke(width = 1.5f))
                drawCircle(color = BentoBorder.copy(alpha = 0.3f), radius = 95.dp.toPx(), center = center, style = Stroke(width = 1.5f))

                // Crosshairs
                drawLine(color = BentoBorder.copy(alpha = 0.2f), start = Offset(center.x, 0f), end = Offset(center.x, h), strokeWidth = 1f)
                drawLine(color = BentoBorder.copy(alpha = 0.2f), start = Offset(0f, center.y), end = Offset(w, center.y), strokeWidth = 1f)

                // Current Rider Position (Center Blue dot)
                drawCircle(color = BentoPrimary.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = center)
                drawCircle(color = BentoPrimary, radius = 6.dp.toPx(), center = center)

                // Plot nearby hazards on radar based on delta latitude / longitude
                allHazards.take(8).forEachIndexed { i, hazard ->
                    val dLat = (hazard.latitude - currentLocation.latitude) * 50000f
                    val dLng = (hazard.longitude - currentLocation.longitude) * 50000f
                    val hazardX = (center.x + dLng.toFloat()).coerceIn(20f, w - 20f)
                    val hazardY = (center.y - dLat.toFloat()).coerceIn(20f, h - 20f)

                    val dotColor = when (hazard.fusionSource) {
                        FusionSource.FUSED_VISION_AND_IMU -> Color(0xFFFF5252)
                        FusionSource.IMU_ONLY -> Color(0xFFFFD740)
                        else -> Color(0xFF69F0AE)
                    }

                    drawCircle(color = dotColor.copy(alpha = 0.3f), radius = 10.dp.toPx(), center = Offset(hazardX, hazardY))
                    drawCircle(color = dotColor, radius = 4.dp.toPx(), center = Offset(hazardX, hazardY))
                }
            }

            // Radar overlay HUD labels
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "GPS: %.4f, %.4f".format(currentLocation.latitude, currentLocation.longitude),
                            fontSize = 10.sp,
                            color = BentoTextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        "${String.format("%.0f", currentLocation.speedKmh)} km/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Proximity Geofence Active (100m)", fontSize = 9.sp, color = BentoTextTertiary)
                    Text("Red = Fused (98%) | Yellow = IMU", fontSize = 9.sp, color = BentoTextTertiary)
                }
            }
        }

        // Active Foreground Audio Alert Card
        lastAlert?.let { alert ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF3E2723))
                    .border(1.dp, Color(0xFFFF7043), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color(0xFFFF7043))
                    Column {
                        Text("TTS AUDIO BROADCAST ACTIVE", fontSize = 9.sp, color = Color(0xFFFFAB91), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(alert.message, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Tagged Hazards Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Room Database (${allHazards.size} Logged):",
                fontSize = 12.sp,
                color = BentoTextSecondary,
                fontWeight = FontWeight.Medium
            )

            if (allHazards.isNotEmpty()) {
                Text(
                    "Clear DB",
                    fontSize = 11.sp,
                    color = BentoAccentPink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        // Hazard List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allHazards) { hazard ->
                HazardBentoCard(
                    hazard = hazard,
                    onDelete = { viewModel.deleteHazard(hazard) }
                )
            }
        }
    }
}

@Composable
fun HazardBentoCard(
    hazard: HazardEntity,
    onDelete: () -> Unit
) {
    val sourceColor = when (hazard.fusionSource) {
        FusionSource.FUSED_VISION_AND_IMU -> Color(0xFFFF5252)
        FusionSource.IMU_ONLY -> Color(0xFFFFD740)
        else -> Color(0xFF69F0AE)
    }

    val sourceLabel = when (hazard.fusionSource) {
        FusionSource.FUSED_VISION_AND_IMU -> "FUSED (VISION + IMU)"
        FusionSource.IMU_ONLY -> "IMU ONLY (PHYSICAL)"
        FusionSource.VISION_ONLY -> "VISION ANTICIPATED"
        FusionSource.MANUAL_REPORT -> "MANUAL"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BentoCard, RoundedCornerShape(16.dp))
            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sourceColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(sourceLabel, fontSize = 9.sp, color = sourceColor, fontWeight = FontWeight.Bold)
                    }

                    Text(hazard.type.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Text("${(hazard.confidence * 100).toInt()}% Conf", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
            }

            Text(
                hazard.address ?: "Lat: %.4f, Lng: %.4f".format(hazard.latitude, hazard.longitude),
                fontSize = 11.sp,
                color = BentoTextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Peak Shock: %.1fg | Z-Disp: %.1f m/s²".format(hazard.peakGForce, hazard.zAxisDisplacement),
                    fontSize = 10.sp,
                    color = BentoTextTertiary,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = BentoTextTertiary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
