package com.jbn.yololab_22200019.detector

import kotlin.math.max
import kotlin.math.min

internal object YoloPostProcessor {
    private const val PERSON_CLASS_ID = 0
    private const val BOX_VALUES = 4

    fun decode(
        raw: FloatArray,
        outputShape: IntArray,
        inputSize: Int,
        originalWidth: Int,
        originalHeight: Int,
        confidenceThreshold: Float,
        iouThreshold: Float
    ): List<BoundingBox> {
        require(outputShape.size == 3 && outputShape[0] == 1)
        val attributesFirst = outputShape[1] < outputShape[2]
        val attributeCount = if (attributesFirst) outputShape[1] else outputShape[2]
        val boxCount = if (attributesFirst) outputShape[2] else outputShape[1]
        require(attributeCount > BOX_VALUES + PERSON_CLASS_ID)
        require(raw.size == attributeCount * boxCount)

        fun value(attribute: Int, box: Int): Float = if (attributesFirst) {
            raw[attribute * boxCount + box]
        } else {
            raw[box * attributeCount + attribute]
        }

        val scale = min(
            inputSize.toFloat() / originalWidth,
            inputSize.toFloat() / originalHeight
        )
        val padX = (inputSize - originalWidth * scale) / 2f
        val padY = (inputSize - originalHeight * scale) / 2f
        val candidates = buildList {
            for (box in 0 until boxCount) {
                val score = value(BOX_VALUES + PERSON_CLASS_ID, box)
                if (score < confidenceThreshold) continue

                var centerX = value(0, box)
                var centerY = value(1, box)
                var width = value(2, box)
                var height = value(3, box)
                if (centerX <= 1f && centerY <= 1f && width <= 1f && height <= 1f) {
                    centerX *= inputSize
                    centerY *= inputSize
                    width *= inputSize
                    height *= inputSize
                }

                val x1 = ((centerX - width / 2f - padX) / scale)
                    .coerceIn(0f, originalWidth.toFloat())
                val y1 = ((centerY - height / 2f - padY) / scale)
                    .coerceIn(0f, originalHeight.toFloat())
                val x2 = ((centerX + width / 2f - padX) / scale)
                    .coerceIn(0f, originalWidth.toFloat())
                val y2 = ((centerY + height / 2f - padY) / scale)
                    .coerceIn(0f, originalHeight.toFloat())
                if (x2 > x1 && y2 > y1) {
                    add(BoundingBox(x1, y1, x2, y2, score, PERSON_CLASS_ID, "person"))
                }
            }
        }
        return nonMaxSuppression(candidates, iouThreshold)
    }

    internal fun nonMaxSuppression(
        boxes: List<BoundingBox>,
        iouThreshold: Float
    ): List<BoundingBox> {
        val pending = boxes.sortedByDescending(BoundingBox::score).toMutableList()
        val selected = mutableListOf<BoundingBox>()
        while (pending.isNotEmpty()) {
            val best = pending.removeAt(0)
            selected += best
            pending.removeAll { candidate ->
                candidate.classId == best.classId && intersectionOverUnion(best, candidate) > iouThreshold
            }
        }
        return selected
    }

    internal fun intersectionOverUnion(first: BoundingBox, second: BoundingBox): Float {
        val intersectionWidth = max(0f, min(first.x2, second.x2) - max(first.x1, second.x1))
        val intersectionHeight = max(0f, min(first.y2, second.y2) - max(first.y1, second.y1))
        val intersectionArea = intersectionWidth * intersectionHeight
        val firstArea = max(0f, first.x2 - first.x1) * max(0f, first.y2 - first.y1)
        val secondArea = max(0f, second.x2 - second.x1) * max(0f, second.y2 - second.y1)
        val union = firstArea + secondArea - intersectionArea
        return if (union <= 0f) 0f else intersectionArea / union
    }
}