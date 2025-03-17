package com.mirrar.tablettryon.network

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class CurlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val curlCommand = buildCurlCommand(request)
        println("cURL Command:\n$curlCommand")
        return chain.proceed(request)
    }

    private fun buildCurlCommand(request: Request): String {
        val curlCommand = StringBuilder("curl --location ")
            .append("'${request.url}' \\\n")

        // Add method
        if (request.method != "GET") {
            curlCommand.append("--request ${request.method} \\\n")
        }

        // Add headers
        for ((name, value) in request.headers) {
            curlCommand.append("--header '$name: $value' \\\n")
        }

        // Add body if applicable
        request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            val bodyString = buffer.readUtf8()
            if (bodyString.isNotEmpty()) {
                curlCommand.append("--data '$bodyString' \\\n")
            }
        }

        return curlCommand.toString().trimEnd('\\', '\n')
    }
}
