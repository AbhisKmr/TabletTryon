package com.mirrar.tablettest.view.fragment.tryon.dataModel.triedOnUrl

import com.google.gson.annotations.SerializedName

data class TriedOnUrlResponse(
    val uuid: String,
    @SerializedName("tryon_outputs")
    val tryonOutputs: Map<String, String>
)