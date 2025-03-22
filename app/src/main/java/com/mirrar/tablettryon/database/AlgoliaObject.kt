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

class AlgoliaObject {
    private val client = ClientSearch(
        ConfigurationSearch(
            ApplicationID("V0MFZORLHS"),
            APIKey("f9b905571a819c23a15b192b778e7b3a")
        )
    )
    private val index = client.initIndex(IndexName(AppConstraint.ALGOLIA_INDEX))
    private val gson = GsonBuilder().create()

    suspend fun fetchAllRecords(): List<Product> {
        val products = mutableListOf<Product>()
        var currentPage = 0
        var totalPages = 1 // Set initial value to enter the loop

        println("Starting to fetch records from Algolia")

        try {
            // Continue until we've processed all pages or hit a safety limit
            while (currentPage < totalPages) { // Safety limit of 100 pages
                println("Fetching page $currentPage")

                val query = Query("").apply {
                    page = currentPage
                    hitsPerPage = 50 // Increase for efficiency
                }

                val response = index.search(query)

                // Update totalPages based on response
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
                        println(hit.json.toString())
                        gson.fromJson(hit.json.toString(), Product::class.java)
                    } catch (e: Exception) {
                        println("Failed to parse product: ${e.message}")
                        null
                    }
                }

                println("Successfully parsed ${parsedProducts.size} products from this page")
                products.addAll(parsedProducts)

                currentPage++
            }

            println("Completed fetching all records. Total products: ${products.size}")
            return products

        } catch (e: Exception) {
            println("Error fetching records from Algolia: ${e.message}")
            e.printStackTrace()
            return emptyList() // Return empty list on error
        }
    }
}