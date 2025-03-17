package com.mirrar.tablettryon.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getInstance(baseUrl: String = "https://api.brevo.com/v3/"): ApiService {
        if (retrofit == null || retrofit?.baseUrl().toString() != baseUrl) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(CurlInterceptor())
                .build()

            val okHttpClient =  OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(CurlInterceptor())
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .client(okHttpClient)
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}