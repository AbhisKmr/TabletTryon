package com.mirrar.tablettryon.network

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.triedOnUrl.TriedOnUrlRequest
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.triedOnUrl.TriedOnUrlResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object CallApi {

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
            return MultipartBody.Part.createFormData("face", file.name, requestBody)
        }
        return null
    }

    fun uploadGlasses(
        context: Context,
        faceBitmap: Bitmap,
        glassesUrl: String,
        res: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val imagePart = createMultipartBodyPartFromBitmap(faceBitmap, "image/png", context)

                val glassesUrlRequestBody =
                    RequestBody.create("text/plain".toMediaTypeOrNull(), glassesUrl)

                val response = RetrofitClient.getInstance("https://glass-tryon.mirrar.com")
                    .applyGlasses(imagePart!!, glassesUrlRequestBody)

                val obj = JSONObject(response.string())

                res(obj.getString("image_url"))
                Log.d("UPLOAD", "Success: ${response.string()}")
            } catch (e: Exception) {
                Log.e("UPLOAD", "Error: ${e.message} ${e.localizedMessage}")
            }
        }
    }

    fun getMoreAssets(
        uuid: String,
        objectIds: Map<String, String>,
        res: (TriedOnUrlResponse?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = TriedOnUrlRequest(uuid, objectIds as MutableMap<String, String>)
                val response = RetrofitClient.getInstance("https://glass-tryon.mirrar.com")
                    .getMoreGlasses(body)
                res(response.body())
            } catch (e: Exception) {
                Log.e("getMoreAssets", "Error: ${e.message} ${e.localizedMessage}")
            }
        }
    }
}