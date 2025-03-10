package com.mirrar.tablettryon.utility

import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

object HelperFunctions {

    fun GET_IMAGE_URL_FROM_PRODUCT(product: Product): String {
        return if (!product.imageUrlBase.isNullOrBlank()) {
            product.imageUrlBase
        } else if (!product.imageThumbnail.isNullOrBlank()) {
            product.imageThumbnail
        } else {
            product.imageSmall ?: ""
        }
    }
}