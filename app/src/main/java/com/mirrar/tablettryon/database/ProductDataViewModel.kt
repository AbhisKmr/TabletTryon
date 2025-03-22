package com.mirrar.tablettryon.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductDataViewModel(private val repository: ProductRepository) : ViewModel() {

//    val list: LiveData<List<ProductData>> = repository.getCharactersSorted().asLiveData()


    fun downloadList() {
        viewModelScope.launch {
            println("init")
            repository.downloadData()
        }
    }

    class ProductViewModelFactory(private val repository: ProductRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductDataViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductDataViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}