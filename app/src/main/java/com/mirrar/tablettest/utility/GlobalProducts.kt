package com.mirrar.tablettest.utility

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mirrar.tablettest.products.model.product.Product

object GlobalProducts {

    private val _products = MutableLiveData<MutableList<Product>>()
    val products: LiveData<MutableList<Product>> = _products

    fun addToProduct(product: Product) {
        val currentList = _products.value ?: mutableListOf()
        if (!currentList.contains(product)) {
            currentList.add(product)
            _products.value = currentList
        } else {
            removeProduct(product)
        }
    }

    fun updateProduct(products: List<Product>) {
        clearAll()
        _products.value = products.toMutableList()
    }

    fun addProduct(products: List<Product>) {
        val mergeList = mutableListOf<Product>()
        mergeList.addAll(_products.value?: emptyList())
        mergeList.addAll(products)
        _products.value = mergeList.toMutableList()
    }

    fun removeProduct(product: Product) {
        val currentList = _products.value ?: mutableListOf()
        if (currentList.contains(product)) {
            currentList.remove(product)
            _products.value = currentList
        }
    }

    fun clearAll() {
        _products.value = mutableListOf()
    }

    fun getproducts(): List<Product> {
        return _products.value ?: emptyList()
    }
}
