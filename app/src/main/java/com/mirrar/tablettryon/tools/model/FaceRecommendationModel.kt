package com.mirrar.tablettryon.tools.model

import com.google.gson.annotations.SerializedName

data class FaceRecommendationModel(
    @SerializedName("face_shape")
    val faceShape: String,
    val gender: String,
    val recommendations: List<String>
)