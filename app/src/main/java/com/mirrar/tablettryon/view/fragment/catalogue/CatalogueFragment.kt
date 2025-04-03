package com.mirrar.tablettryon.view.fragment.catalogue

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.databinding.FragmentCatalogueBinding
import com.mirrar.tablettryon.network.ApiService
import com.mirrar.tablettryon.network.Repository
import com.mirrar.tablettryon.network.Resource
import com.mirrar.tablettryon.network.Retrofit
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.tools.FilterManager
import com.mirrar.tablettryon.utility.AppConstraint.IS_FILTER_APPLIED
import com.mirrar.tablettryon.utility.AppConstraint.totalProducts
import com.mirrar.tablettryon.utility.GlobalProducts
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.CatalogueProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CatalogueFragment(
    var sortingOrder: String = "null",
    var currentPage: Int = 0,
    var minPrice: Int = 0,
    var maxPrice: Int = 1000,
    var brandList: MutableList<String> = mutableListOf(),
    var productTryonClick: (Int, Product) -> Unit
) : Fragment() {

    private var _binding: FragmentCatalogueBinding? = null
    private val binding get() = _binding!!

    private val response = Retrofit.getInstance()?.create(ApiService::class.java)
    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModel.Factory(Repository((response!!)))
    }

    private var isLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.filterNavLayout.recyclerDropdownBrand.title.text = "Brand"
        binding.filterBtn.setOnClickListener {
            binding.drawerLayout.elevation = 100f
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding.drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                binding.drawerLayout.elevation = 100f
            }

            override fun onDrawerOpened(drawerView: View) {
                binding.drawerLayout.elevation = 100f
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.drawerLayout.elevation = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })

        val adapter = CatalogueProductAdapter { i, p ->
            openDialogFragment(ProductDetailsFragment.newInstance(p, {
                productTryonClick(i, p)
                Handler().postDelayed({
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }, 500)
            }))
        }

        val filterManager = FilterManager(
            binding.filterNavLayout,
            productViewModel
        ) { sorting, min, max, brandList ->
            this.sortingOrder = sorting
            this.currentPage = 0
            totalProducts = 0
            this.minPrice = min.toInt()
            this.maxPrice = maxPrice
            this.brandList.clear()
            this.brandList.addAll(brandList)
            binding.productRecycler.scrollToPosition(0)
            binding.productRecyclerLoader.isVisible = true
        }

        binding.productRecycler.adapter = adapter

        binding.productRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (totalItemCount < 10) return

                if (!isLoading && totalItemCount > visibleItemCount &&
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1 &&
                    firstVisibleItemPosition >= 0
                ) {

                    if (maxPrice == 0) {
                        maxPrice = 1000
                    }
                    isLoading = true
                    binding.productLoading.isVisible = true
                    if (IS_FILTER_APPLIED) {
                        productViewModel.filterProduct(
                            sortingOrder = sortingOrder,
                            page = currentPage,
                            min = minPrice,
                            max = maxPrice,
                            brands = brandList,
                        )
                    } else {
                        productViewModel.fetchProduct(
                            sortingOrder = sortingOrder,
                            page = currentPage,
                            min = minPrice,
                            max = maxPrice,
                            brands = brandList
                        )
                    }
                }
            }
        })

        viewModel.filter.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                filterManager.updateBrandList(it)
            }

            filterManager.updatePreselection(
                sortingOrder,
                currentPage,
                minPrice,
                maxPrice,
                brandList
            )
        }

        viewModel.price.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            minPrice = it.min().toInt()
            maxPrice = it.max().toInt()
            filterManager.updateRange(it.min().toFloat(), it.max().toFloat())
        }

        productViewModel.products.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    isLoading = false
                    binding.productLoading.isVisible = false
                    binding.productRecyclerLoader.isVisible = false
                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    isLoading = false
                    binding.productRecyclerLoader.isVisible = false
                    binding.productLoading.isVisible = false
                    if (totalProducts > currentPage * 10) {
                        GlobalProducts.addProduct(it.data.products)
                    } else {
                        if (GlobalProducts.getproducts().isEmpty()) {
                            GlobalProducts.updateProduct(it.data.products)
                        } else {
                            GlobalProducts.addProduct(it.data.products)
                        }
                    }

                    currentPage++
                    totalProducts = it.data.totalHits
                }
            }
            binding.drawerLayout.closeDrawers()
            binding.filterNavLayout.applyProgress.isVisible = false
            binding.filterNavLayout.apply.text = "Apply"
        }

        GlobalProducts.products.observe(viewLifecycleOwner) {
            binding.productRecyclerLoader.isVisible = false
            adapter.updateData(it)
        }

        binding.productRecyclerLoader.isVisible = false
        viewModel.fetchAllBrands()
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.fetchAllRecords()
        }
//        productViewModel.fetchProduct()
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(childFragmentManager, fragment.tag)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            sortingOrder: String,
            currentPage: Int,
            minPrice: Int,
            maxPrice: Int,
            brandList: MutableList<String>,
            productTryonClick: (Int, Product) -> Unit
        ) = CatalogueFragment(
            sortingOrder,
            currentPage,
            minPrice,
            maxPrice,
            brandList,
            productTryonClick
        )
    }
}