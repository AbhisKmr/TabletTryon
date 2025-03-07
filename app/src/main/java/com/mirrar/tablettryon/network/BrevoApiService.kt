package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailResponse
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface BrevoApiService {
    @Headers("accept: application/json", "content-type: application/json")
    @POST("smtp/email")
    fun sendEmail(
        @Header("api-key") apiKey: String,
        @Body emailData: EmailRequest
    ): Call<EmailResponse>
}