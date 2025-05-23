package com.mirrar.tablettryon.utility

import android.graphics.Bitmap
import android.util.Size
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel

object AppConstraint {
    var IMAGE_RENDER_SIZE = Size(0,0)

    var BREVO_API_KEY = ""
    var SENDER_NAME = ""
    var SENDER_EMAIL = ""
    var WELCOME_MESSAGE = ""
    var CLUB_AVOLTA = ""
    var ALGOLIA_INDEX = "avolta-glasses"
    var IS_3D_ENABLED = false
    var IS_FILTER_APPLIED = false

    var AR_BITMAP: Bitmap? = null
    var recommendationModel: FaceRecommendationModel? = null

    var userName: String? = null
    var userEmail: String? = null

    var priceMin: Float? = 0f
    var priceMax: Float? = 10000f

    var cameraRatio = 1f

    var totalProducts = 0
}