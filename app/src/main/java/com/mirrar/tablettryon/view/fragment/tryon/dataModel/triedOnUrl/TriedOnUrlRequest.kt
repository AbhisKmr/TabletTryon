package com.mirrar.tablettryon.view.fragment.tryon.dataModel.triedOnUrl

import com.google.gson.annotations.SerializedName

data class TriedOnUrlRequest(
    val uuid: String,
    @SerializedName("glasses_urls")
    val glassesUrls: MutableMap<String, String>,
)

data class GlassesUrls(val objectId: Map<String, String>)