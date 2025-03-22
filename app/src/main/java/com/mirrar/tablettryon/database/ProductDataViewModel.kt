package com.mirrar.tablettryon.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductDataViewModel(private val repository: ProductRepository) : ViewModel() {

//    val list: LiveData<List<ProductData>> = repository.getCharactersSorted().asLiveData()

    private val _downloadState = MutableLiveData<DownloadState>()
    val downloadState: LiveData<DownloadState> get() = _downloadState

    fun startDownload() {
        viewModelScope.launch {
            repository.downloadDataWithProgress().collect { state ->
                _downloadState.value = state
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
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