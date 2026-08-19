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
import com.example.data.local.IncidentReportEntity
import com.example.data.local.IncidentType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ShieldProtectionScreen(viewModel: SensorViewModel) {
    val idHealth by viewModel.idHealth.collectAsState()
    val allIncidents by viewModel.allIncidents.collectAsState()

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
                    "ID ARMOR & RATING IMMUNITY",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "The Shield Protection",
                    fontSize = 22.sp,
                    color = BentoTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("100% HEALTH", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Hero ID Health Bento Card
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color(0xFF69F0AE), modifier = Modifier.size(32.dp))
                        Column {
                            Text("RIDER ID IMMUNITY STATUS", fontSize = 9.sp, color = BentoTextTertiary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                            Text("Account 100% Protected", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BentoPrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("0.0% Block Risk", fontSize = 10.sp, color = BentoPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // ID Shield Statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Immunity Certs", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("${idHealth.immunityCertificatesIssued}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoPrimary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Negative Ratings Neutralized", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("${idHealth.ratingsProtected}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF69F0AE))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF1E1E24), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Disputes Won", fontSize = 9.sp, color = BentoTextTertiary)
                            Text("${idHealth.disputesWonAutomatically}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoAccentPink)
                        }
                    }
                }
            }
        }

        // Information Callout on Arbitrary ID Blocking
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF181822))
                .border(1.dp, BentoBorder.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                "Sarthi-Shield Anti-Blocking Protocol: Every delivery delay is backed by cryptographic sensor proof (YOLO visual tag + IMU vibration + GPS timestamp). Gig platforms cannot penalize you for road or customer fault.",
                fontSize = 11.sp,
                color = BentoTextSecondary
            )
        }

        // Verified Incident Protection Ledger Header
        Text(
            "Verifiable Rating Immunity Certificates (${allIncidents.size} Issued):",
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
            items(allIncidents) { incident ->
                IncidentCertificateCard(incident = incident)
            }
        }
    }
}

@Composable
fun IncidentCertificateCard(incident: IncidentReportEntity) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    val (badgeColor, icon) = when (incident.incidentType) {
        IncidentType.UNFAIR_LATE_REVIEW_SHIELD -> Pair(Color(0xFF69F0AE), Icons.Filled.Shield)
        IncidentType.CUSTOMER_AGGRESSION_DETECTED -> Pair(Color(0xFFFF5252), Icons.Filled.Mic)
        IncidentType.WATERLOGGING_ROADBLOCK_DELAY -> Pair(Color(0xFF40C4FF), Icons.Filled.WaterDrop)
        IncidentType.EXTREME_CUSTOMER_WAIT_TIME -> Pair(Color(0xFFFFD740), Icons.Filled.Timer)
        IncidentType.CRASH_FALL_EVENT -> Pair(Color(0xFFFF1744), Icons.Filled.Warning)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BentoCard, RoundedCornerShape(18.dp))
            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
                    Text(incident.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoTextPrimary)
                }

                Text(
                    "Order #${incident.orderId}",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                incident.description,
                fontSize = 11.sp,
                color = BentoTextSecondary
            )

            // Certificate ID & Telemetry Hash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cert: ${incident.ratingImmunityCertificateId}",
                    fontSize = 10.sp,
                    color = BentoPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    dateFormat.format(Date(incident.timestamp)),
                    fontSize = 10.sp,
                    color = BentoTextTertiary
                )
            }

            // Evidence Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (incident.visualEvidenceCaptured) {
                    Text("• YOLO Camera Tagged", fontSize = 9.sp, color = Color(0xFF69F0AE))
                }
                if (incident.decibelPeak > 70f) {
                    Text("• Audio: %.1f dB".format(incident.decibelPeak), fontSize = 9.sp, color = BentoAccentPink)
                }
                if (incident.peakGForce > 1.5f) {
                    Text("• Shock: %.1fg".format(incident.peakGForce), fontSize = 9.sp, color = BentoPrimary)
                }
                Text("• Status: Auto-Resolved", fontSize = 9.sp, color = Color(0xFF69F0AE), fontWeight = FontWeight.Bold)
            }
        }
    }
}
