package com.jbn.yololab_22200019.detector


import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YoloPostProcessorTest {
    @Test
    fun `decode conserva solo personas sobre el umbral`() {
        val boxCount = 100
        val attributeCount = 84
        val raw = FloatArray(attributeCount * boxCount)

        fun set(attribute: Int, box: Int, value: Float) {
            raw[attribute * boxCount + box] = value
        }

        set(0, 0, 160f)
        set(1, 0, 160f)
        set(2, 0, 100f)
        set(3, 0, 200f)
        set(4, 0, 0.85f)
        set(0, 1, 40f)
        set(1, 1, 40f)
        set(2, 1, 20f)
        set(3, 1, 20f)
        set(4, 1, 0.49f)

        val result = YoloPostProcessor.decode(
            raw = raw,
            outputShape = intArrayOf(1, attributeCount, boxCount),
            inputSize = 320,
            originalWidth = 320,
            originalHeight = 320,
            confidenceThreshold = 0.5f,
            iouThreshold = 0.45f
        )

        assertEquals(1, result.size)
        assertEquals("person", result.single().label)
        assertEquals(0.85f, result.single().score, 0.0001f)
        assertEquals(110f, result.single().x1, 0.0001f)
    }

    @Test
    fun `nms elimina la caja superpuesta con menor confianza`() {
        val best = box(10f, 10f, 110f, 110f, 0.9f)
        val duplicate = box(15f, 15f, 105f, 105f, 0.7f)
        val separate = box(200f, 200f, 260f, 260f, 0.8f)

        val result = YoloPostProcessor.nonMaxSuppression(
            listOf(duplicate, separate, best),
            iouThreshold = 0.45f
        )

        assertEquals(listOf(best, separate), result)
    }

    @Test
    fun `iou es cero para cajas separadas`() {
        val first = box(0f, 0f, 10f, 10f, 0.9f)
        val second = box(20f, 20f, 30f, 30f, 0.8f)

        assertTrue(YoloPostProcessor.intersectionOverUnion(first, second) == 0f)
    }

    private fun box(x1: Float, y1: Float, x2: Float, y2: Float, score: Float) =
        BoundingBox(x1, y1, x2, y2, score, classId = 0, label = "person")
}