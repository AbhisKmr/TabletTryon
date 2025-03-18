package com.mirrar.tablettryon.view.fragment.tryon.viewModel

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.search.client.ClientSearch
import com.algolia.search.configuration.ConfigurationSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.Attribute
import com.algolia.search.model.IndexName
import com.algolia.search.model.ObjectID
import com.google.gson.GsonBuilder
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.algolia.search.model.search.Query
import com.mirrar.tablettryon.tools.filter.FilterDataModel
import com.mirrar.tablettryon.utility.AppConstraint
import com.mirrar.tablettryon.utility.AppConstraint.priceMax
import com.mirrar.tablettryon.utility.AppConstraint.priceMin
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.utility.Bookmarks

class AlgoliaViewModel : ViewModel() {

//    private val searcher = HitsSearcher(
//        applicationID = ApplicationID("V0MFZORLHS"),
//        apiKey = APIKey("0feee6ee25524813cd5ada3b0fc68384"),
//        indexName = IndexName(AppConstraint.ALGOLIA_INDEX)
//    )

//    private val client = ClientSearch(
//        applicationID = ApplicationID("V0MFZORLHS"),
//        apiKey = APIKey("f9b905571a819c23a15b192b778e7b3a")
//    )
//    val index = client.initIndex(indexName = IndexName("avolta-glasses"))

    private val client = ClientSearch(
        ConfigurationSearch(
            ApplicationID("V0MFZORLHS"),
            APIKey("f9b905571a819c23a15b192b778e7b3a")
        )
    )
    private val index = client.initIndex(IndexName(AppConstraint.ALGOLIA_INDEX))

    private val _products = MutableLiveData<List<Product>>()
    val product: LiveData<List<Product>> = _products

    private val _filter = MutableLiveData<List<FilterDataModel>>()
    val filter: LiveData<List<FilterDataModel>> = _filter

    var loadMore = false
    var itIsForRecommendation = false
    var nbHits = 10
    var pageCount = 1

    fun onlyRecommendation() {
        viewModelScope.launch {
            val objects =
                recommendationModel?.recommendations?.map { ObjectID(it.objectID) }
                    ?: emptyList()

            val lst = mutableListOf<Product>()

            if (objects.isEmpty()) {
                val additionalQuery: Query =
                    when (recommendationModel?.faceAnalysis?.gender?.lowercase()) {
                        "male" -> Query(
                            query = "",
                            filters = "gender:\"For Him\" OR gender:\"Unisex\""
                        )

                        "female" -> Query(
                            query = "",
                            filters = "gender:\"For Her\" OR gender:\"Unisex\""
                        )

                        else -> Query(query = "", filters = "gender:\"Unisex\"")
                    }

                additionalQuery.apply {
                    hitsPerPage = 10
                }
                val additionalProductsResponse = index.search(additionalQuery)

                additionalProductsResponse.hits.map {
                    lst.add(
                        GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
                    )
                }

                itIsForRecommendation = false
                _products.value = lst
                return@launch
            }
            val recommendedObjects = index.getObjects(objects)

            recommendedObjects.results.forEach {
                val p = GsonBuilder().create().fromJson(it.toString(), Product::class.java)
                    .apply { isRecommended = true }

                recommendationModel?.recommendations?.forEach { pod ->
                    if (pod.objectID == p.objectID) {
                        p.triedOnImageUrl = pod.triedOnImageUrl
                    }
                }
                lst.add(p)
            }

            itIsForRecommendation = true
            _products.value = lst
        }
    }

    fun fetchProducts(
        isLoading: Boolean = false,
        progressBar: ProgressBar,
        bs: List<FilterDataModel>? = null,
        shortingIndex: Int = -1
    ) {
        if (isLoading) return
        progressBar.visibility = View.VISIBLE
        itIsForRecommendation = false
        viewModelScope.launch {
            try {
                val brd = bs?.filter { it.isSelected } ?: emptyList()
                val filterString =
                    brd.joinToString(separator = " OR ") { brand -> "brand:\"${brand.value}\"" }
                val filterPrice = "priceDutyFree >= $priceMin AND priceDutyFree <= $priceMax"

                val finalFilter = if (brd.isEmpty()) filterPrice else "$filterString AND $filterPrice"

                val indexName = when (shortingIndex) {
                    0 -> "${AppConstraint.ALGOLIA_INDEX}-asc"
                    1 -> "${AppConstraint.ALGOLIA_INDEX}-desc"
                    else -> AppConstraint.ALGOLIA_INDEX
                }

                val cc = client.initIndex(IndexName(indexName))

                val additionalQuery = Query().apply {
                    filters = finalFilter
                    hitsPerPage = 10
                    page = pageCount
                }

                val searchResponse = cc.search(additionalQuery)

                // Debugging logs
                Log.d("Algolia", "Filters applied: $finalFilter")
                Log.d("Algolia", "Total hits: ${searchResponse.nbHits}")

                if (searchResponse.nbHits > 0) {
                    val lst = searchResponse.hits.map {
                        GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
                    }
                    _products.value = lst
                    loadMore = searchResponse.nbHits > (pageCount * 10)
                    nbHits = searchResponse.nbHits
                } else {
                    _products.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("Algolia", "Error fetching products: ${e.message}")
                _products.value = emptyList()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    fun fetchAllBrands() {
        _filter.value = runBlocking {
            val query = Query().apply {
                facets = setOf(Attribute("brand"))
            }

            val response = index.search(query)
            response.facets[Attribute("brand")]?.map {
                FilterDataModel(it.value)
            } ?: emptyList()
        }
    }
}