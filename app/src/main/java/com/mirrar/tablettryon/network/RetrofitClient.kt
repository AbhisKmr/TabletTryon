package com.mirrar.tablettryon.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.brevo.com/v3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: BrevoApiService by lazy {
        retrofit.create(BrevoApiService::class.java)
    }
}