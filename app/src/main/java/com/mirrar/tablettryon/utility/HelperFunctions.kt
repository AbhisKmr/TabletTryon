package com.mirrar.tablettryon.utility

import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.TypedValue
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

object HelperFunctions {

    fun getImageUrlFromProduct(product: Product): String {
        return if (!product.imageUrlBase.isNullOrBlank()) {
            product.imageUrlBase
        } else if (!product.imageThumbnail.isNullOrBlank()) {
            product.imageThumbnail
        } else {
            product.imageSmall ?: ""
        }
    }

    fun generateQRCode(text: String, width: Int = 500, height: Int = 500): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.MARGIN to 0)

            val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints)

            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix) // Convert BitMatrix to Bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getDisplaySize(context: Context): Pair<Int, Int> {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    fun getActionBarSize(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
    }

    fun getNavigationBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}