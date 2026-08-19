package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoAccentPink
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoCard
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoTextTertiary

@Composable
fun ImuWaveformCanvas(
    waveformPoints: List<Pair<Float, Float>>, // Pair(RawZ, FilteredZ)
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BentoCard)
            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Z-AXIS SIGNAL FILTERING",
                        fontSize = 10.sp,
                        color = BentoPrimary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Low-Pass Vibration Attenuation",
                        fontSize = 14.sp,
                        color = BentoTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BentoAccentPink)
                        )
                        Text("Raw (with Noise)", fontSize = 10.sp, color = BentoAccentPink)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(BentoPrimary)
                        )
                        Text("LPF Filtered", fontSize = 10.sp, color = BentoPrimary)
                    }
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 12.dp)
            ) {
                val w = size.width
                val h = size.height
                if (w <= 10f || h <= 10f) return@Canvas

                val midY = h / 2f

                // Draw zero / 1G reference grid lines
                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, h * 0.25f),
                    end = Offset(w, h * 0.25f),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, h * 0.75f),
                    end = Offset(w, h * 0.75f),
                    strokeWidth = 1f
                )

                if (waveformPoints.size >= 2) {
                    val rawPath = Path()
                    val filteredPath = Path()
                    val stepX = w / (waveformPoints.size - 1).coerceAtLeast(1)

                    // Baseline is around 9.8m/s² (1G)
                    val baseline = 9.8f
                    val scaleY = h / 30f // scale factor for +/- 15 m/s² swing
                    val minY = 4f
                    val maxY = (h - 4f).coerceAtLeast(minY)

                    waveformPoints.forEachIndexed { i, pt ->
                        val x = i * stepX
                        // Raw Z
                        val rawY = (midY - (pt.first - baseline) * scaleY).coerceIn(minY, maxY)
                        if (i == 0) rawPath.moveTo(x, rawY) else rawPath.lineTo(x, rawY)

                        // Filtered Z
                        val filtY = (midY - (pt.second - baseline) * scaleY).coerceIn(minY, maxY)
                        if (i == 0) filteredPath.moveTo(x, filtY) else filteredPath.lineTo(x, filtY)
                    }

                    // Draw Raw Z line (translucent pink/red)
                    drawPath(
                        path = rawPath,
                        color = BentoAccentPink.copy(alpha = 0.45f),
                        style = Stroke(width = 2f)
                    )

                    // Draw Filtered Z line (bright glowing violet/cyan)
                    drawPath(
                        path = filteredPath,
                        color = BentoPrimary,
                        style = Stroke(width = 3.5f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-2.5s window", fontSize = 9.sp, color = BentoTextTertiary, fontFamily = FontFamily.Monospace)
                Text("Sample Rate: 100 Hz", fontSize = 9.sp, color = BentoTextTertiary, fontFamily = FontFamily.Monospace)
                Text("Live (0s)", fontSize = 9.sp, color = BentoTextTertiary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
