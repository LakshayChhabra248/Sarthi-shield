package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SensorViewModel
import com.example.data.local.TripEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EarningsFairWageScreen(viewModel: SensorViewModel) {
    val allTrips by viewModel.allTrips.collectAsState()
    val totalEarnings by viewModel.totalEarnings.collectAsState()
    val totalProtectedBonus by viewModel.totalProtectedBonus.collectAsState()
    val liveWage by viewModel.liveWage.collectAsState()
    val liveDdi by viewModel.liveDdi.collectAsState()

    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

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
                    "ECONOMIC JUSTICE & DDI WAGE MODEL",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Fair-Wage Ledger",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoPrimaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("DDI 1.0 Active", fontSize = 11.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Hero Economic Summary Bento
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BentoCard, RoundedCornerShape(24.dp))
                .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TODAY'S ACCUMULATED EARNINGS", fontSize = 10.sp, color = BentoTextTertiary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "₹${String.format(Locale.US, "%.2f", totalEarnings ?: 580.0)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "${allTrips.size} Deliveries with Sarthi-Shield",
                            fontSize = 11.sp,
                            color = BentoTextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("DDI & WAIT BONUS", fontSize = 9.sp, color = BentoTextTertiary)
                        Text(
                            "+₹${String.format(Locale.US, "%.2f", totalProtectedBonus ?: 182.5)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF69F0AE)
                        )
                        Text("31.4% Effort Surcharge", fontSize = 9.sp, color = Color(0xFF69F0AE))
                    }
                }
            }
        }

        // The Fair-Wage Formula Mathematical Proposal Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1E1E28))
                .border(1.dp, BentoBorder.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Calculate, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                    Text("The Sarthi-Shield Wage Formula:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Text(
                    "Final Payout = [Base Pay ₹25] + [Distance Fee ₹6.5/km] + [DDI Risk Premium (0-50%)] + [Waiting Charge ₹2/min]",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    "• DDI 1: Smooth (+0%) | DDI 2: (+12%) | DDI 3: Potholes (+25%) | DDI 4: Rain (+40%) | DDI 5: Storm (+50%)",
                    fontSize = 9.sp,
                    color = BentoTextSecondary
                )
            }
        }

        // Deliveries History List
        Text(
            "Itemized Delivery Settlements:",
            fontSize = 12.sp,
            color = BentoTextSecondary,
            fontWeight = FontWeight.Medium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allTrips) { trip ->
                TripSettlementCard(trip = trip)
            }
        }
    }
}

@Composable
fun TripSettlementCard(trip: TripEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BentoCard, RoundedCornerShape(18.dp))
            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Order ID, Platform, and Final Payout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(trip.platform.brandColorHex))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(trip.platform.displayName, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text("#${trip.orderId}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "₹${String.format(Locale.US, "%.1f", trip.finalPayout)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoPrimary
                    )
                    Text(
                        " (vs ₹${String.format(Locale.US, "%.0f", trip.oldFlatPayout)})",
                        fontSize = 10.sp,
                        color = BentoTextTertiary,
                        modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                    )
                }
            }

            // Locations
            Text(
                "${trip.pickupLocation} → ${trip.dropoffLocation}",
                fontSize = 11.sp,
                color = BentoTextSecondary,
                fontWeight = FontWeight.Medium
            )

            // Formula Breakdown Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Dist: ${trip.distanceKm} km (₹${String.format(Locale.US, "%.1f", trip.distanceFee)})",
                    fontSize = 9.sp,
                    color = BentoTextTertiary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "| DDI-${trip.ddiLevel}: +₹${String.format(Locale.US, "%.1f", trip.ddiBonus)}",
                    fontSize = 9.sp,
                    color = Color(0xFF69F0AE),
                    fontFamily = FontFamily.Monospace
                )
                if (trip.waitCharge > 0) {
                    Text(
                        "| Wait (${trip.waitMinutes}m): +₹${String.format(Locale.US, "%.0f", trip.waitCharge)}",
                        fontSize = 9.sp,
                        color = BentoAccentPink,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Bottom Badges: Rating Immunity & Weather
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${trip.weather.emoji} ${trip.weather.label}", fontSize = 10.sp, color = BentoTextSecondary)
                    if (trip.potholesEncountered > 0) {
                        Text("• ${trip.potholesEncountered} Potholes", fontSize = 10.sp, color = BentoTextTertiary)
                    }
                }

                if (trip.ratingImmunityApplied) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(12.dp))
                        Text("Rating Immune", fontSize = 10.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
