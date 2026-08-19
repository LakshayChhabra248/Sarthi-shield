package com.example.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.BentoBorder
import com.example.vision.DetectedHazard
import com.example.vision.YoloVisionDetector
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun CameraVisionPreview(
    visionDetector: YoloVisionDetector,
    detectedHazards: List<DetectedHazard>,
    isSimulated: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, isSimulated) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                }
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
            .border(1.dp, BentoBorder.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        if (!isSimulated) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        // Use COMPATIBLE (TextureView) to prevent BLASTBufferQueue abandon errors during tab transitions
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, visionDetector)
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Simulated road environment canvas for demo & emulator execution
            SimulatedRoadCanvas(modifier = Modifier.fillMaxSize())
        }

        // Pure Compose Canvas for Bounding Box lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (canvasWidth > 10f && canvasHeight > 10f) {
                // Horizon guide
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, canvasHeight * 0.40f),
                    end = Offset(canvasWidth, canvasHeight * 0.40f),
                    strokeWidth = 2f
                )

                detectedHazards.forEach { hazard ->
                    val box = hazard.boundingBox
                    val left = box.left * canvasWidth
                    val top = box.top * canvasHeight
                    val right = box.right * canvasWidth
                    val bottom = box.bottom * canvasHeight
                    val width = right - left
                    val height = bottom - top

                    val boxColor = when (hazard.label) {
                        "POTHOLE" -> Color(0xFFFF5252)
                        "SPEED BUMP" -> Color(0xFFFFD740)
                        else -> Color(0xFF40C4FF)
                    }

                    // Draw bounding box outline
                    drawRoundRect(
                        color = boxColor,
                        topLeft = Offset(left, top),
                        size = Size(width.coerceAtLeast(4f), height.coerceAtLeast(4f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 4f)
                    )

                    // Fill subtle tint
                    drawRoundRect(
                        color = boxColor.copy(alpha = 0.20f),
                        topLeft = Offset(left, top),
                        size = Size(width.coerceAtLeast(4f), height.coerceAtLeast(4f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                }
            }
        }

        // Pure Compose UI text overlays (zero native canvas memory pinning)
        detectedHazards.forEach { hazard ->
            val box = hazard.boundingBox
            val offsetX = (totalWidth * box.left).coerceAtLeast(4.dp)
            val offsetY = ((totalHeight * box.top) - 24.dp).coerceAtLeast(4.dp)
            val boxColor = when (hazard.label) {
                "POTHOLE" -> Color(0xFFFF5252)
                "SPEED BUMP" -> Color(0xFFFFD740)
                else -> Color(0xFF40C4FF)
            }

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, boxColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${hazard.label} ${(hazard.confidence * 100).toInt()}% [${String.format(Locale.US, "%.1fm", hazard.estimatedDistanceMeters)}]",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SimulatedRoadCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(Color(0xFF18181E))) {
        val w = size.width
        val h = size.height

        if (w > 10f && h > 10f) {
            // Sky / horizon
            drawRect(
                color = Color(0xFF101015),
                topLeft = Offset(0f, 0f),
                size = Size(w, h * 0.40f)
            )

            // Road asphalt perspective trapezoid
            val roadTopLeft = Offset(w * 0.42f, h * 0.40f)
            val roadTopRight = Offset(w * 0.58f, h * 0.40f)
            val roadBottomLeft = Offset(w * -0.15f, h)
            val roadBottomRight = Offset(w * 1.15f, h)

            val roadPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(roadTopLeft.x, roadTopLeft.y)
                lineTo(roadTopRight.x, roadTopRight.y)
                lineTo(roadBottomRight.x, roadBottomRight.y)
                lineTo(roadBottomLeft.x, roadBottomLeft.y)
                close()
            }
            drawPath(roadPath, color = Color(0xFF26252C))

            // Center lane divider dashes
            val numDashes = 6
            for (i in 0 until numDashes) {
                val progress = (i.toFloat() / numDashes)
                val dashY = h * 0.40f + (h * 0.60f) * (progress * progress)
                val dashH = (12f + progress * 40f)
                val dashW = (4f + progress * 8f)
                val dashX = (w * 0.50f) - (dashW / 2f)

                drawRect(
                    color = Color(0xFFFFD54F).copy(alpha = 0.8f),
                    topLeft = Offset(dashX, dashY),
                    size = Size(dashW, dashH)
                )
            }
        }
    }
}
