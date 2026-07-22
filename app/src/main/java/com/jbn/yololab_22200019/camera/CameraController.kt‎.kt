package com.jbn.yololab_22200019.camera

import android.content.Context
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onFrame: (ImageProxy) -> Unit
    private val onFrame: (ImageProxy) -> Unit,
    private val onAnalysisClosed: () -> Unit = {}
) : AutoCloseable {
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var stopped = false

    private val orientationListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            val rotation = when {
                orientation >= 315 || orientation < 45 -> Surface.ROTATION_0
                orientation < 135 -> Surface.ROTATION_270
                orientation < 225 -> Surface.ROTATION_180
                else -> Surface.ROTATION_90
            }
            preview?.targetRotation = rotation
            imageAnalysis?.targetRotation = rotation
        }
    }

    fun start() {
        stopped = false
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            useCase.setAnalyzer(analysisExecutor) { frame -> onFrame(frame) }
        }
            this.preview = preview
                    imageAnalysis = analysis
                    provider.unbindAll()
                    provider.bindToLifecycle(
                    lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
        if (orientationListener.canDetectOrientation()) orientationListener.enable()
    } catch (error: Exception) {
        Log.e(TAG, "No se pudo iniciar CameraX", error)
    }
}, ContextCompat.getMainExecutor(context))
}

override fun close() {
    stopped = true
    orientationListener.disable()
    imageAnalysis?.clearAnalyzer()
    preview = null
    imageAnalysis = null
    analysisExecutor.execute(onAnalysisClosed)
    analysisExecutor.shutdown()
}

private companion object {
    const val TAG = "CameraController"
}
}