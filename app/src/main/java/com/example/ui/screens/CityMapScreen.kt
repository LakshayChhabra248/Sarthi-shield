package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SensorViewModel
import com.example.data.local.HazardEntity
import com.example.data.local.HazardType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CityMapScreen(viewModel: SensorViewModel) {
    val allHazards by viewModel.allHazards.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

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
                    "MUNICIPALITY & ONDC MOBILE DATA HUB",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "City Infrastructure Map",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1976D2))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("LIVE ONDC FEED", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Radar / Heatmap Bento Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("URBAN ROAD ROUGHNESS RADAR (2 KM)", fontSize = 10.sp, color = BentoTextTertiary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Text("GPS: ${String.format(Locale.US, "%.4f, %.4f", currentLocation.latitude, currentLocation.longitude)}", fontSize = 9.sp, color = BentoPrimary, fontFamily = FontFamily.Monospace)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val maxRadius = minOf(cx, cy) - 10f

                        // Concentric rings
                        for (r in listOf(0.33f, 0.66f, 1.0f)) {
                            drawCircle(
                                color = BentoBorder.copy(alpha = 0.4f),
                                radius = maxRadius * r,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1.5f)
                            )
                        }

                        // Crosshairs
                        drawLine(
                            color = BentoBorder.copy(alpha = 0.3f),
                            start = Offset(cx, cy - maxRadius),
                            end = Offset(cx, cy + maxRadius),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = BentoBorder.copy(alpha = 0.3f),
                            start = Offset(cx - maxRadius, cy),
                            end = Offset(cx + maxRadius, cy),
                            strokeWidth = 1f
                        )

                        // Center Rider Dot
                        drawCircle(color = BentoPrimary, radius = 6f, center = Offset(cx, cy))
                        drawCircle(color = BentoPrimary.copy(alpha = 0.3f), radius = 14f, center = Offset(cx, cy))

                        // Plot Hazard clusters
                        allHazards.forEachIndexed { idx, h ->
                            val angle = (idx * 67.5f) * (Math.PI / 180f)
                            val distFraction = ((idx % 3 + 1) * 0.28f).coerceIn(0.2f, 0.9f)
                            val hx = cx + (maxRadius * distFraction * kotlin.math.cos(angle)).toFloat()
                            val hy = cy + (maxRadius * distFraction * kotlin.math.sin(angle)).toFloat()

                            val dotColor = when (h.type) {
                                HazardType.POTHOLE -> Color(0xFFFF5252)
                                HazardType.SPEED_BUMP -> Color(0xFFFFD740)
                                HazardType.WATERLOGGING -> Color(0xFF40C4FF)
                                else -> Color(0xFFFFAB40)
                            }
                            drawCircle(color = dotColor, radius = 5f, center = Offset(hx, hy))
                        }
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(color = Color(0xFFFF5252), label = "Pothole")
                    LegendItem(color = Color(0xFF40C4FF), label = "Waterlogging")
                    LegendItem(color = Color(0xFFFFD740), label = "Speed Bump")
                }
            }
        }

        // Municipality / Gov Impact Statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Mapped Hazards", fontSize = 9.sp, color = BentoTextTertiary)
                    Text("${allHazards.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("City Road Quality", fontSize = 9.sp, color = BentoTextTertiary)
                    Text("68/100 (Fair)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD740))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BentoCard, RoundedCornerShape(16.dp))
                    .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("Gov API Sync", fontSize = 9.sp, color = BentoTextTertiary)
                    Text("CONNECTED", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                }
            }
        }

        // Tagged Hazards List Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Community Road Anomalies:", fontSize = 12.sp, color = BentoTextSecondary, fontWeight = FontWeight.Medium)
            TextButton(onClick = { viewModel.simulatePotholeImpact() }) {
                Text("+ Tag Pothole", fontSize = 11.sp, color = BentoPrimary)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allHazards) { hazard ->
                HazardRowCard(hazard = hazard)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 9.sp, color = BentoTextSecondary)
    }
}

@Composable
fun HazardRowCard(hazard: HazardEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    val (badgeColor, title) = when (hazard.type) {
        HazardType.POTHOLE -> Pair(Color(0xFFFF5252), "Road Crater / Pothole")
        HazardType.SPEED_BUMP -> Pair(Color(0xFFFFD740), "Unmarked Speed Bump")
        HazardType.WATERLOGGING -> Pair(Color(0xFF40C4FF), "Road Waterlogging")
        else -> Pair(Color(0xFFFFAB40), "Rough Surface")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BentoCard, RoundedCornerShape(14.dp))
            .border(1.dp, BentoBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )

                Column {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    Text(
                        "${String.format(Locale.US, "%.4f, %.4f", hazard.latitude, hazard.longitude)} • Peak %.1fg".format(hazard.peakGForce),
                        fontSize = 10.sp,
                        color = BentoTextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BentoPrimaryContainer)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    "${(hazard.confidence * 100).toInt()}% Fused",
                    fontSize = 9.sp,
                    color = BentoPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
