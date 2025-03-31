package com.mirrar.tablettryon.products.model.product

data class ApiProduct(
    val currentPage: Int,
    val products: List<Product>,
    val totalHits: Int,
    val totalPages: Int
)