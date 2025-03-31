package com.mirrar.tablettryon.database

import com.mirrar.tablettryon.products.model.product.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val productDao: ProductDao,
) {

    private val algoliaObject = AlgoliaObject()

    suspend fun downloadDataWithProgress(): Flow<DownloadState> {
        return algoliaObject.fetchAllRecords().map { fetchProgress ->
            when (fetchProgress) {
                is FetchProgress.Progress -> DownloadState.Progress(fetchProgress.percentage)
                is FetchProgress.Success -> {
                    productDao.deleteAll()
                    fetchProgress.products.forEach { productDao.upsertProduct(it) }
                    DownloadState.Success(fetchProgress.products)
                }
                is FetchProgress.Error -> DownloadState.Error(fetchProgress.message)
            }
        }
    }

    suspend fun updateProduct(p: Product) {
        productDao.updateSingleProduct(p)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    fun getCharactersSorted(): Flow<List<Product>> =
        productDao.getProductsSortedAsc()
}

sealed class DownloadState {
    data class Progress(val percentage: Int) : DownloadState()
    data class Success(val products: List<Product>) : DownloadState()
    data class Error(val message: String) : DownloadState()
}