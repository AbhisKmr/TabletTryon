package com.mirrar.tablettryon.database

import com.mirrar.tablettryon.products.model.product.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
) {

    private val algoliaObject = AlgoliaObject()

    suspend fun downloadData() {
        println("download")
        productDao.deleteAll()
        algoliaObject.fetchAllRecords().forEach {
            productDao.upsertProduct(it)
        }
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    fun getCharactersSorted(): Flow<List<Product>> =
        productDao.getProductsSortedAsc()
}