package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadResponse
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

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
}