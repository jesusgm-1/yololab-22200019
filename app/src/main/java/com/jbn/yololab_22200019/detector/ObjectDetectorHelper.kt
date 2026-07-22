package com.jbn.yololab_22200019.detector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ObjectDetectorHelper(
    .getOutputTensor(0)
.shape()

internal fun runInference(originalBitmap: Bitmap): FloatArray {
    val activeInterpreter = requireNotNull(interpreter) { "El modelo está cerrado" }
    val inputShape = inputShape()
    require(inputShape.size == 4 && inputShape[0] == 1) {
        "Tensor de entrada no soportado: ${inputShape.contentToString()}"
    }
    val channelsFirst = inputShape[1] == RGB_CHANNELS
    val inputSize = if (channelsFirst) inputShape[2] else inputShape[1]
    val channelCount = if (channelsFirst) inputShape[1] else inputShape[3]
    require(channelCount == RGB_CHANNELS) { "El modelo debe recibir RGB" }

    val resized = ImageUtils.letterbox(originalBitmap, inputSize)
    val input = ByteBuffer.allocateDirect(inputShape.reduce(Int::times) * FLOAT_BYTES)
        .order(ByteOrder.nativeOrder())
    val pixels = IntArray(inputSize * inputSize)
    resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
    resized.recycle()

    if (channelsFirst) {
        for (channel in 0 until RGB_CHANNELS) {
            for (pixel in pixels) input.putFloat(channelValue(pixel, channel))
        }
    } else {
        for (pixel in pixels) {
            for (channel in 0 until RGB_CHANNELS) input.putFloat(channelValue(pixel, channel))
        }
    }
    input.rewind()

    val outputShape = outputShape()
    val outputValues = outputShape.reduce(Int::times)
    val outputBytes = ByteBuffer.allocateDirect(outputValues * FLOAT_BYTES)
        .order(ByteOrder.nativeOrder())
    activeInterpreter.run(input, outputBytes)
    outputBytes.rewind()
    return FloatArray(outputValues).also { outputBytes.asFloatBuffer().get(it) }
}

private fun channelValue(pixel: Int, channel: Int): Float = when (channel) {
    0 -> ((pixel shr 16) and 0xFF) / 255f
    1 -> ((pixel shr 8) and 0xFF) / 255f
    else -> (pixel and 0xFF) / 255f
}

override fun close() {
    interpreter?.close()
    interpreter = null

    private companion object {
        const val TAG = "ObjectDetectorHelper"
        const val RGB_CHANNELS = 3
        const val FLOAT_BYTES = 4
    }
}