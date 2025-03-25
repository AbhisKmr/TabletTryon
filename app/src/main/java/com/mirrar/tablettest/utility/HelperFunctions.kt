package com.mirrar.tablettest.utility

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mirrar.tablettest.products.model.product.Product
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

    fun getFileNameAndExtension(url: String): Pair<String, String> {
        val regex = ".*/([^/?#]+)(?:\\?.*)?".toRegex()
        val match = regex.find(url)?.groupValues?.get(1) ?: return "" to ""

        val fileName = match.substringBeforeLast(".")
        val extension = match.substringAfterLast(".", "")

        return fileName to extension
    }

    fun downloadAndSaveFile(context: Context, fileUrl: String, fileName: String): String? {
        return try {
            val directory = context.cacheDir
            val file = File(directory, fileName)

            if (file.exists()) {
                Log.d("Download", "File already exists: ${file.absolutePath}")
                return file.absolutePath
            }

            val client = OkHttpClient()
            val request = Request.Builder().url(fileUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("Download", "Failed to download file (${fileName}): ${response.message}")
                return null
            }

            val inputStream: InputStream? = response.body?.byteStream()
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

            Log.d("Download", "File saved: ${file.absolutePath}")
            file.absolutePath

        } catch (e: Exception) {
            Log.e("Download", "Error: ${e.message}")
            null
        }
    }

    fun isValidUrl(url: String?): Boolean {
        if (url == null) return false
        return Patterns.WEB_URL.matcher(url).matches()
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

    fun clearAppCache(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir?.let { deleteDir(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { child ->
                deleteDir(child)
            }
        }
        return dir.delete()
    }

    fun rotateImage(imageView: ImageView, angle: Float = 180f, startAngle: Float = 0f) {
        val rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotation", startAngle, angle)

        rotationAnimator.duration = 200

        rotationAnimator.interpolator = DecelerateInterpolator()

        rotationAnimator.start()
    }
}