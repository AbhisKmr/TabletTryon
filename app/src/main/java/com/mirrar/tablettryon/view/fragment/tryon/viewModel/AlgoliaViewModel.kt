package com.mirrar.tablettryon.view.fragment.tryon.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algolia.instantsearch.searcher.hits.HitsSearcher
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.google.gson.GsonBuilder
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import kotlinx.coroutines.launch

class AlgoliaViewModel : ViewModel() {

    private val searcher = HitsSearcher(
        applicationID = ApplicationID("V0MFZORLHS"),
        apiKey = APIKey("f9b905571a819c23a15b192b778e7b3a"),
        indexName = IndexName("avolta-glasses")
    )

    private val _products = MutableLiveData<List<Product>>()
    val product: LiveData<List<Product>> = _products
            /*
                private val _searchLookFlow: MutableStateFlow<NetworkResult<List<AlgoliaLookModel>>> =
        MutableStateFlow(NetworkResult.Idle())
    val searchLookFlow = _searchLookFlow.asStateFlow()
             */

//    private val searcher = HitsSearcher(
//        applicationID = ApplicationID("latency"),
//        apiKey = APIKey("1f6fd3a6fb973cb08419fe7d288fa4db"),
//        indexName = IndexName("instant_search")
//    )
    
    /*
    ALGOLIA_APPLICATION_ID=V0MFZORLHS
    ALGOLIA_SECRET_KEY=5168d91cbcaa03d0400d579c40d8570d
    ALGOLIA_SEARCH_API_KEY=f9b905571a819c23a15b192b778e7b3a
    APPLICATION=avolta-data
    INDEX=ZurichFrames3.0-CopyofSheet1
     */

    fun getData() {
        viewModelScope.launch {
            val list = searcher.search()?.hits?.map {
                GsonBuilder().create().fromJson(it.json.toString(), Product::class.java)
            }
            _products.value = list?: listOf()
        }
    }

    override fun onCleared() {
        super.onCleared()
        searcher.cancel()
    }
}