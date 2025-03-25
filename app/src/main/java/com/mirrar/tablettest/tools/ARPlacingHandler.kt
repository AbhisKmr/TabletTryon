package com.mirrar.tablettest.tools

import android.graphics.PointF
import android.util.Size
import android.widget.ImageView
import kotlin.math.PI
import kotlin.math.atan2

class ARPlacingHandler(private val imageView: ImageView) {

//    private val viewRatio = imageView.width / imageView.height

    fun placeObject(s: Size, x: Float, y: Float, r: Float) {

        val params = imageView.layoutParams
        params.width = s.width
        params.height = s.height
        imageView.layoutParams = params
        imageView.requestLayout()

        imageView.rotation = r - 91f
        imageView.x = x - (s.width / 2f)
        imageView.y = y - (s.height * .47f)
    }

    private fun eulerToDegrees(eulerAngle: Float): Float {
        return eulerAngle * (180f / PI.toFloat())
    }

    fun angleBetweenPoints(p1: PointF, p2: PointF): Float {
        return atan2(p2.y - p1.y, p2.x - p1.x) * (180f / PI.toFloat())
    }
}