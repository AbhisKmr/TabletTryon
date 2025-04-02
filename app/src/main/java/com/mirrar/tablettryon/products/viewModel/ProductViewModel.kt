package com.mirrar.tablettryon.products.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mirrar.tablettryon.network.Repository
import com.mirrar.tablettryon.network.Resource
import com.mirrar.tablettryon.products.model.product.ApiProduct
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: Repository) : ViewModel() {

    private val _products = MutableLiveData<Resource<ApiProduct>>()
    val products: LiveData<Resource<ApiProduct>> = _products

    fun fetchProduct(
        sortingOrder: String = "low_to_high",
        uuid: String = recommendationModel?.uuid ?: "",
        page: Int = 0,
        min: Int = 0,
        max: Int = 10000,
        brands: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            _products.value = repository.fetchProducts(sortingOrder, uuid, page, min, max, brands)
        }
    }

    fun filterProduct(
        sortingOrder: String = "low_to_high",
        uuid: String = recommendationModel?.uuid ?: "",
        page: Int = 0,
        min: Int = 0,
        max: Int = 10000,
        brands: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _products.value = Resource.Loading()
            _products.value = repository.filterProducts(sortingOrder, uuid, page, min, max, brands)
        }
    }

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}