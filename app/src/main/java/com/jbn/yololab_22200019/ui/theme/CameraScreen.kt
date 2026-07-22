

package com.jbn.yololab_22200019.ui.theme

import android.graphics.Bitmap
import android.graphics.Paint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jbn.yololab_22200019.detector.ObjectDetectorHelper
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
@@ -53,27 +62,46 @@
var detections by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
var frameWidth by remember { mutableIntStateOf(0) }
var frameHeight by remember { mutableIntStateOf(0) }
var tiltAngle by remember { mutableStateOf(0f) }

DisposableEffect(context) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            tiltAngle = Math.toDegrees(atan2(-x.toDouble(), y.toDouble())).toFloat()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }
    accelerometer?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
    }
    onDispose { sensorManager.unregisterListener(listener) }
}

Box(modifier = Modifier.fillMaxSize()) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            val previewView = PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val controller = CameraController(
                context = viewContext,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                onFrame = { frame ->
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = ImageUtils.imageProxyToBitmap(frame)
                        val detector = detectorRef.get() ?: ObjectDetectorHelper(
                            context.applicationContext
                        ).also(detectorRef::set)
                        val results = detector.detect(bitmap)
                        val width = bitmap.width
                        val height = bitmap.height
                        mainExecutor.execute {
                            detections = results
                            @@ -116,27 +144,64 @@
                            topLeft = Offset(left, top),
                            size = Size(right - left, bottom - top),
                            style = Stroke(width = 4f)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "${box.label} ${(box.score * 100).roundToInt()}%",
                                left + 8f,
                                (top - 12f).coerceAtLeast(labelPaint.textSize),
                                labelPaint
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = "Personas detectadas: ${detections.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    CompassOverlay(
                        angleDegrees = tiltAngle,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                    )
                }
        }

        @Composable
        private fun CompassOverlay(angleDegrees: Float, modifier: Modifier = Modifier) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val radius = size.minDimension / 2f
                    drawCircle(color = Color.Black.copy(alpha = 0.45f), radius = radius)
                    drawCircle(color = Color.White, radius = radius, style = Stroke(width = 3f))
                    rotate(angleDegrees) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val needleLength = radius - 8f
                        drawLine(
                            color = Color.Red,
                            start = center,
                            end = Offset(center.x, center.y - needleLength),
                            strokeWidth = 5f
                        )
                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(center.x, center.y + needleLength),
                            strokeWidth = 5f
                        )
                    }
                }
                Text(
                    text = "${angleDegrees.roundToInt()}°",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }