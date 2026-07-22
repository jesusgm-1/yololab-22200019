

package com.jbn.yololab_22200019.detector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val nv21 = yuv420888ToNv21(image)
        val jpeg = ByteArrayOutputStream().use { output ->
            YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
                .compressToJpeg(Rect(0, 0, image.width, image.height), 90, output)
            output.toByteArray()
        }
        val bitmap = requireNotNull(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)) {
            "No se pudo decodificar el frame de la cámara"
        }
        val rotation = image.imageInfo.rotationDegrees
        if (rotation == 0) return bitmap

        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            .also { rotated -> if (rotated !== bitmap) bitmap.recycle() }
    }

    private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
        val width = image.width
        val height = image.height
        val output = ByteArray(width * height * 3 / 2)
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]
        val yBuffer = yPlane.buffer.duplicate()
        val uBuffer = uPlane.buffer.duplicate()
        val vBuffer = vPlane.buffer.duplicate()

        var destination = 0
        for (row in 0 until height) {
            val rowStart = row * yPlane.rowStride
            for (column in 0 until width) {
                output[destination++] = yBuffer.get(rowStart + column * yPlane.pixelStride)
            }
        }
        for (row in 0 until height / 2) {
            val uRowStart = row * uPlane.rowStride
            val vRowStart = row * vPlane.rowStride
            for (column in 0 until width / 2) {
                output[destination++] = vBuffer.get(vRowStart + column * vPlane.pixelStride)
                output[destination++] = uBuffer.get(uRowStart + column * uPlane.pixelStride)
            }
        }
        return output
    }

    fun letterbox(source: Bitmap, targetSize: Int): Bitmap {
        require(targetSize > 0) { "targetSize debe ser positivo" }
        val output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(Color.BLACK)
        val scale = minOf(
            targetSize.toFloat() / source.width,
            targetSize.toFloat() / source.height
        )
        val scaledWidth = (source.width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (source.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(
            scaled,
            (targetSize - scaledWidth) / 2f,
            (targetSize - scaledHeight) / 2f,
            null
        )
        if (scaled !== source) scaled.recycle()
        return output
    }
}