package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.products.model.product.ApiProduct
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailResponse
import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.ImageUploadResponse
import com.mirrar.tablettryon.view.fragment.email.dataModel.emailApi.SendEmailApiRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.emailApi.SendEmailApiResponse
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.triedOnUrl.TriedOnUrlRequest
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.triedOnUrl.TriedOnUrlResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface ApiService {

    @Headers("accept: application/json", "content-type: application/json")
    @POST("smtp/email")
    fun sendEmail(
        @Header("api-key") apiKey: String,
        @Body emailData: EmailRequest
    ): Call<EmailResponse>

    @Multipart
    @POST("/apply-glasses")
    suspend fun applyGlasses(
        @Part face: MultipartBody.Part,
        @Part("glasses_url") glassesUrl: RequestBody
    ): ResponseBody

    @POST("send-email")
    fun sendApiEmail(
        @Body emailData: SendEmailApiRequest
    ): Call<SendEmailApiResponse>

    @POST("apply-glasses")
    suspend fun getMoreGlasses(
        @Body body: TriedOnUrlRequest
    ): Response<TriedOnUrlResponse>

    @POST("api/v1/image/base64/upload")
    fun uploadImage(
        @Body image: ImageUploadRequest,
    ): Call<ImageUploadResponse>

    @Multipart
    @POST("process-glasses")
    fun getRecommendationBasedOnFace(@Part image: MultipartBody.Part): Call<FaceRecommendationModel>

    @GET("filter-products")
    suspend fun getFilteredProducts(
        @Query("sort_order") sortOrder: String,
        @Query("page") page: Int,
        @Query("uuid") uuid: String,
        @Query("brand") brands: List<String>
    ): Response<ApiProduct>
}