

package com.jbn.yololab_22200019.ui.theme

import android.graphics.Bitmap
import android.graphics.Paint
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jbn.yololab_22200019.camera.CameraController
import com.jbn.yololab_22200019.detector.BoundingBox
import com.jbn.yololab_22200019.detector.ImageUtils
import com.jbn.yololab_22200019.detector.ObjectDetectorHelper
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectorRef = remember { AtomicReference<ObjectDetectorHelper?>(null) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val labelPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            isAntiAlias = true
        }
    }
    var detections by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var frameWidth by remember { mutableIntStateOf(0) }
    var frameHeight by remember { mutableIntStateOf(0) }

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
                                                frameWidth = width
                                                frameHeight = height
                                            }
                                        } catch (error: Exception) {
                                            Log.e(TAG, "Error analizando frame", error)
                                        } finally {
                                            bitmap?.recycle()
                                            frame.close()
                                        }
                                    },
                                    onAnalysisClosed = {
                                        detectorRef.getAndSet(null)?.close()
                                    }
                                )
                                previewView.tag = controller
                                controller.start()
                                previewView
                            },
                            onRelease = { view ->
                                (view.tag as? CameraController)?.close()
                                view.tag = null
                            }
                        )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (frameWidth == 0 || frameHeight == 0) return@Canvas
                            val scale = max(size.width / frameWidth, size.height / frameHeight)
                            val offsetX = (frameWidth * scale - size.width) / 2f
                            val offsetY = (frameHeight * scale - size.height) / 2f
                            detections.forEach { box ->
                                val left = box.x1 * scale - offsetX
                                val top = box.y1 * scale - offsetY
                                val right = box.x2 * scale - offsetX
                                val bottom = box.y2 * scale - offsetY
                                drawRect(
                                    color = Color.Green,
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
                } private const val TAG = "CameraScreen"