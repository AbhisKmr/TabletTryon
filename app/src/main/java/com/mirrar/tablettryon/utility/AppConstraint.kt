package com.mirrar.tablettryon.utility

import android.graphics.Bitmap
import android.util.Size
import com.algolia.search.model.recommend.RecommendationModel
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

object AppConstraint {
    var IMAGE_RENDER_SIZE = Size(0,0)
    var SELECTED_INDEX = -1

    var BREVO_API_KEY = ""
    var SENDER_NAME = ""
    var SENDER_EMAIL = ""
    var WELCOME_MESSAGE = ""
    var CLUB_AVOLTA = ""

    var AR_BITMAP: Bitmap? = null
    var recommendationModel: FaceRecommendationModel? = null
    var filterTryOn: Product? = null
}