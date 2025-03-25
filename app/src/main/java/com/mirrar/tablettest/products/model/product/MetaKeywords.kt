package com.mirrar.tablettest.products.model.product

data class MetaKeywords(
    val matchLevel: String,
    val matchedWords: List<Any>,
    val value: String
)