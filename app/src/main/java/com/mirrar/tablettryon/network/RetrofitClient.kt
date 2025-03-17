package com.mirrar.tablettryon.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .client(OkHttpClient.Builder().build())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }
}