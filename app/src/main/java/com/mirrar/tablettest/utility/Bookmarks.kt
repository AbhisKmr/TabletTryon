package com.mirrar.tablettest.utility

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mirrar.tablettest.products.model.product.Product

object Bookmarks {
    private val _bookmarks = MutableLiveData<MutableList<Product>>()
    val bookmarks: LiveData<MutableList<Product>> = _bookmarks

    fun addToBookmark(product: Product) {
        val currentList = _bookmarks.value ?: mutableListOf()
        if (!currentList.contains(product)) {
            product.isBookmarked = true
            currentList.add(product)
            _bookmarks.value = currentList
        } else {
            removeBookmark(product)
        }
    }

    fun removeBookmark(product: Product) {
        val currentList = _bookmarks.value ?: mutableListOf()
        if (currentList.contains(product)) {
            product.isBookmarked = false
            currentList.remove(product)
            _bookmarks.value = currentList
        }
    }

    fun clearAll() {
        _bookmarks.value = mutableListOf()
    }

    fun getBookmarks(): List<Product> {
        return _bookmarks.value ?: emptyList()
    }
}