package com.mirrar.tablettryon.tools.model

import com.google.gson.annotations.SerializedName

data class FaceRecommendationModel(
    @SerializedName("face_analysis")
    val faceAnalysis: FaceAnalysis,
    val recommendations: List<Recommendation>,
    val uuid: String?
)

data class FaceAnalysis(
    @SerializedName("face_shape")
    val faceShape: String,
    val gender: String,
)

data class Recommendation(
    val asset2DUrl: String,
    val objectID: String,
    val triedOnImageUrl: String,
)
