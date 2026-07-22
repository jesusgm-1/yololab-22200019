

package com.jbn.yololab_22200019.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jbn.yololab_22200019.camera.CameraController
import com.jbn.yololab_22200019.detector.ImageUtils
import com.jbn.yololab_22200019.detector.ObjectDetectorHelper
import java.util.concurrent.atomic.AtomicReference

@Composable
fun CameraScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Preparando cámara...")
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val detectorRef = remember { AtomicReference<ObjectDetectorHelper?>(null) }

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
                    var bitmap: android.graphics.Bitmap? = null
                    try {
                        bitmap = ImageUtils.imageProxyToBitmap(frame)
                        val detector = detectorRef.get() ?: ObjectDetectorHelper(
                            context.applicationContext
                        ).also(detectorRef::set)
                        detector.detect(bitmap)
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
}