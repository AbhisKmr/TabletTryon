package com.mirrar.tablettest.tools

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = android.graphics.Color.RED
        style = Paint.Style.FILL
        strokeWidth = 10f
    }

    private var points: List<Pair<Float, Float>> = emptyList()

    fun setPoints(points: List<Pair<Float, Float>>) {
        this.points = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        points.forEach { (x, y) ->
            canvas.drawCircle(x, y, 5f, paint)
        }
    }
}
