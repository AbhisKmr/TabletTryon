package com.mirrar.tablettryon.tools


import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import com.mirrar.tablettryon.utility.AppConstraint.IMAGE_RENDER_SIZE

class CameraSourcePreview(
    context: Context,
    att: AttributeSet
) : ViewGroup(context, att) {

    @get:JvmName("getWidthProperty")
    var width = 1525//IMAGE_RENDER_SIZE.width

    @get:JvmName("getHeightProperty")
    var height = 1203//IMAGE_RENDER_SIZE.height

//    1280x720

    override fun onLayout(p0: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (isPortraitMode()) {
            val tmp = width
            width = height
            height = tmp
        }

        Log.i("previewSize", "w:${width}|| h${height}")
        val layoutWidth = right - left
        val layoutHeight = bottom - top

        var childWidth = layoutWidth
        var childHeight = (layoutWidth.toFloat() / width * height).toInt()

        if (childHeight < layoutHeight) {
            childHeight = layoutHeight
            childWidth = (layoutHeight.toFloat() / height * width).toInt()
        }
        val startX = childWidth / 2 - layoutWidth / 2

        for (i in 0 until childCount) {
//            getChildAt(i).layout(-startX/2, 0, childWidth, childHeight)
            getChildAt(i).layout(-startX, 0, childWidth - startX, childHeight)
            //left top right bottom
        }
    }

    private fun isPortraitMode(): Boolean {
        return context.resources.configuration.orientation == 1
    }
}