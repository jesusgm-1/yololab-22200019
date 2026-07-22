
package com.jbn.yololab_22200019.detector

/** Una detección expresada en píxeles del frame original de la cámara. */
data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val score: Float,
    val classId: Int,
    val label: String
)