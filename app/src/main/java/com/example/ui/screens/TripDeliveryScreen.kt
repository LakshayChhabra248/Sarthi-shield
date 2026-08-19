package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.AppStatus
import com.example.SensorViewModel
import com.example.data.local.GigPlatform
import com.example.data.local.WeatherCondition
import com.example.mission.DeliveryStage
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun TripDeliveryScreen(viewModel: SensorViewModel) {
    val order by viewModel.currentOrder.collectAsState()
    val liveDdi by viewModel.liveDdi.collectAsState()
    val liveWage by viewModel.liveWage.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val voiceState by viewModel.voiceSafetyState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Title & Platform Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SARTHI-SHIELD COCKPIT",
                    fontSize = 11.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Live Delivery Mission",
                    fontSize = 24.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Platform Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(order.platform.brandColorHex))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    order.platform.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Active Warning / SOS Banner
        if (uiState.status == AppStatus.CRASH_SOS || voiceState.isAggressionDetected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFC62828))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            if (voiceState.isAggressionDetected) "VOICE SAFETY ALERT: AGGRESSION DETECTED" else "EMERGENCY SOS DISPATCHED",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            if (voiceState.isAggressionDetected) "Ambient audio evidence encrypted. Rating Immunity guaranteed." else "Crash impact telemetry transmitted to gig fleet safety center.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Hero Bento Card: Live Fair Wage Counter & Dynamic Difficulty Level
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "LIVE FAIR WAGE EARNINGS",
                            fontSize = 10.sp,
                            color = BentoTextTertiary,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "₹${String.format(Locale.US, "%.1f", liveWage.totalFairPayout)}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoPrimary
                            )
                            Text(
                                " (Base ₹${String.format(Locale.US, "%.0f", liveWage.basePay)})",
                                fontSize = 13.sp,
                                color = BentoTextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }

                    // DDI Level Badge
                    val ddiBg = when (liveDdi.level) {
                        5 -> Color(0xFFC62828)
                        4 -> Color(0xFFE65100)
                        3 -> Color(0xFFEF6C00)
                        2 -> Color(0xFFF57F17)
                        else -> BentoPrimaryContainer
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(ddiBg)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DDI LEVEL", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${liveDdi.level} / 5", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Dynamic Wage Breakdown Pill Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Distance Fee", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("₹${String.format(Locale.US, "%.1f", liveWage.distanceFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("DDI Bonus", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("+₹${String.format(Locale.US, "%.1f", liveWage.ddiBonus)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Wait Penalty", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("₹${String.format(Locale.US, "%.1f", liveWage.waitCharge)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoAccentPink)
                        }
                    }
                }

                // Economic Justice Comparison
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoPrimaryContainer.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mehnat Ka Samman: You earned +₹${String.format(Locale.US, "%.1f", liveWage.extraMoneyEarned)} more than flat rate!",
                            fontSize = 11.sp,
                            color = BentoPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Active Order & Route Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.DeliveryDining, contentDescription = null, tint = BentoPrimary)
                        Text("Order #${order.orderId}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }

                    Text(
                        order.stage.title,
                        fontSize = 11.sp,
                        color = BentoPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(BentoPrimaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF69F0AE)))
                        Text(order.restaurant, fontSize = 12.sp, color = BentoTextSecondary)
                    }
                    Box(modifier = Modifier.padding(start = 3.dp).width(2.dp).height(12.dp).background(BentoBorder))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BentoAccentPink))
                        Text(order.deliveryAddress, fontSize = 12.sp, color = BentoTextPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                LinearProgressIndicator(
                    progress = { (order.coveredDistanceKm / order.totalDistanceKm).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = BentoPrimary,
                    trackColor = BentoPrimaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${String.format(Locale.US, "%.1f", order.coveredDistanceKm)} / ${order.totalDistanceKm} KM Covered", fontSize = 10.sp, color = BentoTextTertiary)
                    Text("${order.potholesCount} Potholes Tagged", fontSize = 10.sp, color = BentoTextTertiary)
                }
            }
        }

        // Rating & ID Shield Status Bento Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = if (order.isRatingImmunityActive) Color(0xFF69F0AE) else BentoTextSecondary)
                        Text("Rating Immunity & ID Shield", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }
                    Text(
                        if (order.isRatingImmunityActive) "ACTIVE (IMMUNE)" else "MONITORING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isRatingImmunityActive) Color(0xFF69F0AE) else BentoTextTertiary
                    )
                }

                if (order.isRatingImmunityActive) {
                    Text(
                        "Cert ID: ${order.immunityCertId}\nAI verified delay due to road craters / weather. Negative customer ratings automatically neutralized.",
                        fontSize = 11.sp,
                        color = BentoTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        "Road conditions normal. If severe potholes, waterlogging, or customer wait occurs, Rating Immunity will latch automatically.",
                        fontSize = 11.sp,
                        color = BentoTextSecondary
                    )
                }
            }
        }

        // Customer Wait-Time Billing Meter (Interactive & Automatic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = BentoAccentPink)
                        Text("Customer Doorstep Wait Meter", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                    }
                    val waitMins = order.waitTimeSeconds / 60
                    val waitSecs = order.waitTimeSeconds % 60
                    Text(
                        "%02d:%02d".format(waitMins, waitSecs),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoAccentPink,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    "Geofenced Rule: First 5 minutes free grace. Thereafter ₹2.00 / minute fine automatically added to customer bill.",
                    fontSize = 11.sp,
                    color = BentoTextSecondary
                )

                if (order.stage != DeliveryStage.ARRIVED_CUSTOMER_GATE && order.stage != DeliveryStage.ORDER_COMPLETED) {
                    Button(
                        onClick = { viewModel.missionController.arriveAtCustomerGate() },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryContainer)
                    ) {
                        Icon(Icons.Filled.LocationCity, contentDescription = null, tint = BentoPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arrived at Customer Building (Start Wait Timer)", color = BentoPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Interactive Scenario Trigger Bar (For Demo & Verification)
        Text(
            "Simulate Live Delivery Events & Road Hazards:",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.missionController.simulatePotholeEncounter() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
            ) {
                Text("Broken Road (DDI 3)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.missionController.simulateWaterloggingStorm() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
            ) {
                Text("Waterlogging (DDI 5)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.voiceSafetyDetector.triggerSimulatedCustomerAggression() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F))
            ) {
                Text("Customer Aggression", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.missionController.completeDelivery() },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Complete & Settle Pay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // New Trip Button
        OutlinedButton(
            onClick = { viewModel.missionController.startNewTrip() },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start New Delivery Order", fontSize = 12.sp)
        }
    }
}
