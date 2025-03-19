package com.mirrar.tablettryon.network

import com.mirrar.tablettryon.products.model.product.ApiProduct
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.UUID

class Repository(private val apiService: ApiService) {

    suspend fun fetchCategories(
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