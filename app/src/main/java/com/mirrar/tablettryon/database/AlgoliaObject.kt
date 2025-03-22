package com.mirrar.tablettryon.database

import com.algolia.search.client.ClientSearch
import com.algolia.search.configuration.ConfigurationSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.search.Query
import com.google.gson.GsonBuilder
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.utility.AppConstraint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AlgoliaObject {
    private val client = ClientSearch(
        ConfigurationSearch(
            ApplicationID("V0MFZORLHS"),
            APIKey("f9b905571a819c23a15b192b778e7b3a")
        )
    )
    private val index = client.initIndex(IndexName(AppConstraint.ALGOLIA_INDEX))
    private val gson = GsonBuilder().create()

    suspend fun fetchAllRecords(): Flow<FetchProgress> = flow {
        val products = mutableListOf<Product>()
        var currentPage = 0
        var totalPages = 1

        println("Starting to fetch records from Algolia")

        try {
            while (currentPage < totalPages) {
                println("Fetching page $currentPage")

                val query = Query("").apply {
                    page = currentPage
                    hitsPerPage = 1000
                }

                val response = index.search(query)
                totalPages = response.nbPages
                println("Total pages: $totalPages, Current page: $currentPage")

                val hits = response.hits
                println("Received ${hits.size} hits on this page")

                if (hits.isEmpty()) {
                    println("No more hits found, breaking the loop")
                    break
                }

                val parsedProducts = hits.mapNotNull { hit ->
                    try {
                        gson.fromJson(hit.json.toString(), Product::class.java)
                    } catch (e: Exception) {
                        println("Failed to parse product: ${e.message}")
                        null
                    }
                }

                products.addAll(parsedProducts)
                println("Successfully parsed ${parsedProducts.size} products from this page")

                // Emit progress as a percentage
                val progress = ((currentPage + 1) * 100) / totalPages
                emit(FetchProgress.Progress(progress.coerceAtMost(100)))

                currentPage++
            }

            println("Completed fetching all records. Total products: ${products.size}")
            emit(FetchProgress.Success(products))

        } catch (e: Exception) {
            println("Error fetching records from Algolia: ${e.message}")
            e.printStackTrace()
            emit(FetchProgress.Error("Failed to fetch data: ${e.message}"))
        }
    }
}

sealed class FetchProgress {
    data class Progress(val percentage: Int) : FetchProgress()
    data class Success(val products: List<Product>) : FetchProgress()
    data class Error(val message: String) : FetchProgress()
}
