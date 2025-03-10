package com.mirrar.tablettryon.utility

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
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
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.createBitmap(bitMatrix) // Convert BitMatrix to Bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}