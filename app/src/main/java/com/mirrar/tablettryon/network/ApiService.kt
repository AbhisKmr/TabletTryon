package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadResponse
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Headers("accept: application/json", "content-type: application/json")
    @POST("smtp/email")
    fun sendEmail(
        @Header("api-key") apiKey: String,
        @Body emailData: EmailRequest
    ): Call<EmailResponse>

    @POST("api/v1/image/base64/upload")
    fun uploadImage(
        @Body image: ImageUploadRequest,
    ): Call<ImageUploadResponse>

    @Multipart
    @POST("recommend-frames")
    fun getRecommendationBasedOnFace(@Part image: MultipartBody.Part): Call<FaceRecommendationModel>
}