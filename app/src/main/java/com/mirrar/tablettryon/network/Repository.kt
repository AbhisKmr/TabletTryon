package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.products.model.product.ApiProduct
import retrofit2.Response

class Repository(private val apiService: ApiService) {

    suspend fun fetchProducts(
        sortingOrder: String,
        uuid: String,
        page: Int,
        minPrice: Int,
        maxPrice: Int,
        brands: List<String>
    ): Resource<ApiProduct> {
        return try {
            val response =
                apiService.getProducts(sortingOrder, page, uuid, minPrice, maxPrice, brands)
            handleApiResponse(response)
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun filterProducts(
        sortingOrder: String,
        uuid: String,
        page: Int,
        minPrice: Int,
        maxPrice: Int,
        brands: List<String>
    ): Resource<ApiProduct> {
        return try {
            val response =
                apiService.getFilteredProducts(sortingOrder, page, uuid, minPrice, maxPrice, brands)
            handleApiResponse(response)
        } catch (e: Exception) {
            Resource.Error("Network error: ${e.localizedMessage}")
        }
    }

    private fun <T> handleApiResponse(response: Response<T>): Resource<T> {
        return try {
            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    Resource.Success(data)
                } else {
                    Resource.Error("Empty response")
                }
            } else {
                Resource.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error("An error occurred: ${e.localizedMessage}")
        }
    }

}