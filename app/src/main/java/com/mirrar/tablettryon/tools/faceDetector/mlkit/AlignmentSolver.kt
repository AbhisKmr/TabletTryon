package com.mirrar.tablettryon.tools.faceDetector.mlkit

import android.graphics.PointF
import android.util.Size
import com.google.mlkit.vision.common.InputImage

class AlignmentSolver {

    var image: InputImage? = null
    var canvasSize = Size(0, 0)

    fun rearrangePoints(points: List<PointF>): List<PointF> {
        val img = image ?: return emptyList() // Handle null image safely
        if (img.width <= 0 || img.height <= 0 || canvasSize.width <= 0 || canvasSize.height <= 0) {
            return emptyList()
        }

        return points.map { point ->
            PointF(point.x, point.y)
            /*PointF(
                (point.x / img.width) * canvasSize.width,
                (point.y / img.height) * canvasSize.height
            )*/
        }
    }
}
