package com.mirrar.tablettryon.products.model.product

data class Brand(
    val matchLevel: String,
    val matchedWords: List<Any>,
    val value: String
)