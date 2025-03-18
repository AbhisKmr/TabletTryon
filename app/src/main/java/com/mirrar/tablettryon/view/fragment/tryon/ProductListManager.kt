package com.mirrar.tablettryon.view.fragment.tryon

import android.content.Context
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel

class ProductListManager(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val progressBar: ProgressBar,
    private val adapter: ProductAdapter,
    private val algoliaViewModel: AlgoliaViewModel
) {

    private var currentPage = 0
    private val totalProducts = MutableLiveData<Int>()
    private val isLoading: Boolean = false

    init {
        algoliaViewModel.product.observe(context as LifecycleOwner) {

            if (algoliaViewModel.loadMore) {
                adapter.addData(it)
            } else {
                adapter.updateData(it)
            }
            totalProducts.value = algoliaViewModel.nbHits
            currentPage++
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    algoliaViewModel.fetchProducts(true, progressBar)
                }
            }
        })
    }
}