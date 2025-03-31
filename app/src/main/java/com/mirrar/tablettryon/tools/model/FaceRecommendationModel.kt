package com.mirrar.tablettryon.tools.model

import com.google.gson.annotations.SerializedName
import com.mirrar.tablettryon.products.model.product.Product

data class FaceRecommendationModel(
    @SerializedName("face_analysis")
    val faceAnalysis: FaceAnalysis,
    val recommendations: List<Product>,
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
