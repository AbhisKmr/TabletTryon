package com.mirrar.tablettest.view.fragment.email

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.mirrar.tablettest.network.RetrofitClient
import com.mirrar.tablettest.utility.AppConstraint.BREVO_API_KEY
import com.mirrar.tablettest.utility.AppConstraint.SENDER_EMAIL
import com.mirrar.tablettest.utility.AppConstraint.SENDER_NAME
import com.mirrar.tablettest.utility.AppConstraint.WELCOME_MESSAGE
import com.mirrar.tablettest.utility.AppConstraint.recommendationModel
import com.mirrar.tablettest.utility.AppConstraint.userEmail
import com.mirrar.tablettest.utility.AppConstraint.userName
import com.mirrar.tablettest.view.fragment.email.dataModel.EmailRequest
import com.mirrar.tablettest.view.fragment.email.dataModel.EmailResponse
import com.mirrar.tablettest.view.fragment.email.dataModel.ImageUploadRequest
import com.mirrar.tablettest.view.fragment.email.dataModel.ImageUploadResponse
import com.mirrar.tablettest.view.fragment.email.dataModel.Recipient
import com.mirrar.tablettest.view.fragment.email.dataModel.Sender
import com.mirrar.tablettest.view.fragment.email.dataModel.emailApi.SendEmailApiRequest
import com.mirrar.tablettest.view.fragment.email.dataModel.emailApi.SendEmailApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import com.mirrar.tablettest.view.fragment.email.dataModel.emailApi.Object
import kotlin.math.roundToInt

object EmailHelper {

    fun sendDynamicEmail(
        context: Context,
        purpose: String,
        res: (SendEmailApiResponse?) -> Unit
    ) {
        val objs = recommendationModel?.recommendations?.map {
            Object(
                it.brand, it.imageUrlBase ?: "",
                it.priceDutyFree.roundToInt(), it.productUrl ?: "", it.triedOnUrl ?: ""
            )
        } ?: emptyList()

        RetrofitClient.getInstance("https://glass-tryon.mirrar.com").sendApiEmail(
            SendEmailApiRequest(
                userEmail ?: "", userName ?: "", objs, purpose
            )
        )
            .enqueue(object : Callback<SendEmailApiResponse> {
                override fun onResponse(
                    call: Call<SendEmailApiResponse>,
                    response: Response<SendEmailApiResponse>
                ) {
                    res(response.body())
                    Toast.makeText(
                        context, "Email has been sent.", Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<SendEmailApiResponse>, t: Throwable) {
                    res(null)
                }
            })
    }

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

        RetrofitClient.getInstance().sendEmail(
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

    fun uploadBase64Image(bitmap: Bitmap, res: (ImageUploadResponse?) -> Unit) {
        val base64Image = bitmapToBase64(bitmap)
        val requestBody = ImageUploadRequest(image = base64Image)

        val call = RetrofitClient.getInstance("https://m.mirrar.com/").uploadImage(requestBody)

        call.enqueue(object : Callback<ImageUploadResponse> {
            override fun onResponse(
                call: Call<ImageUploadResponse>,
                response: Response<ImageUploadResponse>
            ) {
                res(response.body())
            }

            override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                res(null)
            }
        })
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}