package com.mirrar.tablettryon.view.fragment.tryon.viewModel

import android.util.Log
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
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel

class AlgoliaViewModel : ViewModel() {

    private val searcher = HitsSearcher(
        applicationID = ApplicationID("V0MFZORLHS"),
        apiKey = APIKey("f9b905571a819c23a15b192b778e7b3a"),
        indexName = IndexName("avolta-glasses")
    )

//    private val client = ClientSearch(
//        applicationID = ApplicationID("V0MFZORLHS"),
//        apiKey = APIKey("f9b905571a819c23a15b192b778e7b3a")
//    )
//    val index = client.initIndex(indexName = IndexName("avolta-glasses"))

    val client = ClientSearch(
        ConfigurationSearch(
            ApplicationID("V0MFZORLHS"),
            APIKey("f9b905571a819c23a15b192b778e7b3a")
        )
    )
    val index = client.initIndex(IndexName("avolta-glasses"))

    private val _products = MutableLiveData<List<Product>>()
    val product: LiveData<List<Product>> = _products

    private val _filter = MutableLiveData<List<FilterDataModel>>()
    val filter: LiveData<List<FilterDataModel>> = _filter

    fun getData() {
        viewModelScope.launch {
            try {
                val objects =
                    recommendationModel?.recommendations?.map { ObjectID(it) } ?: emptyList()
                val recommendedObjects = index.getObjects(objects)

                val additionalQuery: Query = when (recommendationModel?.gender?.lowercase()) {
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

                val additionalProductsResponse = index.search(additionalQuery)


                val lst = mutableListOf<Product>()

                recommendedObjects.results.forEach {
                    val p = GsonBuilder().create().fromJson(it.toString(), Product::class.java)
                        .apply { isRecommended = true }

                    lst.add(p)
                }

                additionalProductsResponse.hits.map {
                    lst.add(
                        GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
                    )
                }
                _products.value = lst

            } catch (e: Exception) {
                Log.e("Algolia", "Error fetching products", e)
            }
        }
    }

//    fun getData() {
//        viewModelScope.launch {
//            val recommendedProducts =
//                index.getObjects(recommendationModel?.recommendations?.map { ObjectID(it) }
//                    ?: emptyList())
//
//            val additionalQuery: Query = when (recommendationModel?.gender?.lowercase()) {
//                "male" -> Query(
//                    query = "",
//                    filters = "gender:\"For Him\" OR gender:\"Unisex\""
//                )
//
//                "female" -> Query(
//                    query = "",
//                    filters = "gender:\"For Her\" OR gender:\"Unisex\""
//                )
//
//                else -> Query(query = "")
//            }
//
//            val additionalProductsResponse = index.search(additionalQuery)
//
//            val combinedProducts = mutableListOf<Any>()
//
//            combinedProducts.addAll(recommendedProducts.hits)
//            combinedProducts.addAll(additionalProductsResponse.hits)
//
////            val list = searcher.search()?.hits?.map {
////                GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
////            }
////            _products.value = list ?: listOf()
//        }
//    }

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

    fun fetchFilteredProducts(selectedBrands: List<FilterDataModel>) {
        return runBlocking {
            val query = Query()
            query.facets = selectedBrands.filter { it.isSelected }
                .map {
                    val brandValue = if (it.value.contains(" ")) "'${it.value}'" else it.value
                    Attribute("brand:$brandValue")
                }
                .toSet()

            val response = index.search(query)
            val list = response.hits.map {
                GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
            }
            _products.value = list
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }
}