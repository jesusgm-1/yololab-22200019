
package com.jbn.yololab_22200019.detector

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ObjectDetectorHelper(
    context: Context,
    private val modelName: String = "yolov8n_person_fp16.tflite",
    private val confidenceThreshold: Float = 0.5f,
    private val iouThreshold: Float = 0.45f
) : AutoCloseable {
    private val appContext = context.applicationContext
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null

    val isReady: Boolean
        get() = interpreter != null

    init {
        require(confidenceThreshold in 0f..1f)
        require(iouThreshold in 0f..1f)
        setupInterpreter()
    }

    private fun setupInterpreter() {
        val model = loadModelFile()
        val compatibilityList = CompatibilityList()
        if (compatibilityList.isDelegateSupportedOnThisDevice) {
            try {
                gpuDelegate = GpuDelegate(compatibilityList.bestOptionsForThisDevice)
                interpreter = Interpreter(
                    model,
                    Interpreter.Options().addDelegate(requireNotNull(gpuDelegate))
                )
            } catch (error: Exception) {
                Log.w(TAG, "GPU no disponible; se usará CPU", error)
                gpuDelegate?.close()
                gpuDelegate = null
            }
        }
        if (interpreter == null) {
            model.rewind()
            interpreter = Interpreter(model, Interpreter.Options().setNumThreads(4))
        }

        Log.i(
            TAG,
            "Modelo $modelName listo. Entrada=${inputShape().contentToString()}, " +
                    "salida=${outputShape().contentToString()}"
        )
    }

    private fun loadModelFile(): MappedByteBuffer =
        appContext.assets.openFd(modelName).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { input ->
                input.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    descriptor.startOffset,
                    descriptor.declaredLength
                )
            }
        }

    internal fun inputShape(): IntArray =
        requireNotNull(interpreter) { "El modelo no está inicializado" }
            .getInputTensor(0)
            .shape()

    internal fun outputShape(): IntArray =
        requireNotNull(interpreter) { "El modelo no está inicializado" }
            .getOutputTensor(0)
            .shape()

    override fun close() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }

    private companion object {
        const val TAG = "ObjectDetectorHelper"
    }
}