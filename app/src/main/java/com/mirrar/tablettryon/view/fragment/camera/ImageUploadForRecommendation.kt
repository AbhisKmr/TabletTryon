package com.mirrar.tablettryon.view.fragment.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mirrar.tablettryon.network.RetrofitClient
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageUploadForRecommendation {

    private fun bitmapToFile(bitmap: Bitmap, context: Context): File? {
        // Create a temporary file in the cache directory
        val file = File(context.cacheDir, "temp_image.png")
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    private fun createMultipartBodyPartFromBitmap(
        bitmap: Bitmap,
        mediaType: String,
        context: Context
    ): MultipartBody.Part? {
        val file = bitmapToFile(bitmap, context)
        if (file != null) {
            val requestBody = RequestBody.create(mediaType.toMediaTypeOrNull(), file)
            return MultipartBody.Part.createFormData("image", file.name, requestBody)
        }
        return null
    }

    fun uploadBitmap(bitmap: Bitmap, context: Context, callBack: (FaceRecommendationModel?) -> Unit) {

        val apiService = RetrofitClient.getInstance("https://glass-tryon.mirrar.com/")

        val imagePart = createMultipartBodyPartFromBitmap(bitmap, "image/png", context)

        if (imagePart != null) {
            apiService.getRecommendationBasedOnFace(imagePart)
                .enqueue(object : Callback<FaceRecommendationModel> {
                    override fun onResponse(
                        call: Call<FaceRecommendationModel>,
                        response: Response<FaceRecommendationModel>
                    ) {
                        callBack(response.body())
                    }

                    override fun onFailure(call: Call<FaceRecommendationModel>, t: Throwable) {
                        callBack(null)
                    }
                })
        }
    }

    fun reduceBitmapSize(bitmap: Bitmap, maxSize: Long = 4 * 512 * 512): Bitmap {
        var quality = 100
        val outputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.size() > maxSize && quality > 10) {
            outputStream.reset()  // Reset the ByteArrayOutputStream
            quality -= 5
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        val byteArray = outputStream.toByteArray()
        val b = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return b
    }

    fun resizeAndCompressBitmap(bitmap: Bitmap, maxSizeKB: Int = 2000): Bitmap {
        // Define the maximum size in bytes (2MB = 2000KB)
        val maxSizeBytes = maxSizeKB * 1024

        var width = bitmap.width
        var height = bitmap.height
        var resizedBitmap = bitmap

        // Resize the bitmap while maintaining the aspect ratio
        while ((width * height * 4) > maxSizeBytes) {
            width = (width * 0.9).toInt()
            height = (height * 0.9).toInt()
            resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        }

        var quality = 100
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Compress the image
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

        // Reduce quality until under 2MB
        while (byteArrayOutputStream.toByteArray().size > maxSizeBytes && quality > 10) {
            byteArrayOutputStream.reset()
            quality -= 10
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        }

        return BitmapFactory.decodeByteArray(byteArrayOutputStream.toByteArray(), 0, byteArrayOutputStream.size())
    }
}
