package com.mirrar.tablettryon.utility

import android.content.Context
import android.util.Log
import com.mirrar.tablettryon.network.RetrofitClient
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettryon.view.fragment.email.dataModel.EmailResponse
import com.mirrar.tablettryon.view.fragment.email.dataModel.Recipient
import com.mirrar.tablettryon.view.fragment.email.dataModel.Sender
import retrofit2.Call
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

object EmailHelper {

    var BREVO_API_KEY = ""
    var SENDER_NAME = ""
    var SENDER_EMAIL = ""
    var WELCOME_MESSAGE = ""

    fun sendDynamicEmail(
        context: Context,
        recipientEmail: String,
        username: String,
        response: (Boolean) -> Unit
    ) {
        val emailContent = getEmailTemplate(context, username)

        val emailRequest = EmailRequest(
            sender = Sender(name = SENDER_NAME, email = SENDER_EMAIL),
            to = listOf(Recipient(email = recipientEmail)),
            subject = "$WELCOME_MESSAGE, $username!",
            htmlContent = emailContent
        )

        RetrofitClient.api.sendEmail(
            BREVO_API_KEY,
            emailRequest
        ).enqueue(object :
            retrofit2.Callback<EmailResponse> {
            override fun onResponse(call: Call<EmailResponse>, response: Response<EmailResponse>) {
                response(response.isSuccessful)
                if (response.isSuccessful) {
                    Log.d("Brevo", "Email Sent! ID: ${response.body()?.messageId}")
                } else {
                    Log.e("Brevo", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<EmailResponse>, t: Throwable) {
                Log.e("Brevo", "Failed: ${t.message}")
                response(false)
            }
        })
    }

    fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailPattern.toRegex())
    }

    private fun getEmailTemplate(context: Context, username: String): String {
        val inputStream = context.assets.open("email_template.html")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append("\n")
        }
        reader.close()

        return stringBuilder.toString()
            .replace("{{username}}", username)
    }

}