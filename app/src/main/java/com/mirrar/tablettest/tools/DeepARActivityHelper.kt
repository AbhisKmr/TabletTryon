package com.mirrar.tablettest.tools

import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettest.DeepARActivity
import com.mirrar.tablettest.R
import com.mirrar.tablettest.databinding.ActivityDeepAractivityBinding
import com.mirrar.tablettest.network.Resource
import com.mirrar.tablettest.products.model.product.Product
import com.mirrar.tablettest.products.viewModel.ProductViewModel
import com.mirrar.tablettest.utility.Bookmarks
import com.mirrar.tablettest.utility.GlobalProducts
import com.mirrar.tablettest.utility.HelperFunctions.downloadAndSaveFile
import com.mirrar.tablettest.view.fragment.ClubAvoltaFragment
import com.mirrar.tablettest.view.fragment.ProductDetailsFragment
import com.mirrar.tablettest.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettest.view.fragment.catalogue.CatalogueFragment
import com.mirrar.tablettest.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettest.view.fragment.tryon.viewModel.AlgoliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeepARActivityHelper(
    private val deepARActivity: DeepARActivity,
    private val productViewModel: ProductViewModel,
    private val applyEffect: (String) -> Unit,
    private val onScreenshot: (SCREENSHOT, Product) -> Unit
) {

    private var binding: ActivityDeepAractivityBinding = deepARActivity.binding
    private var selectedProduct: Product? = null
    private var adapter: ProductAdapter? = null

    private val assetsUrl = arrayOf(
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass2.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass3.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass4.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass5.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass6.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass7.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass8.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass9.deepar",
        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass10.deepar",
    )

    private var sortingOrder = "low_to_high"
    private var currentPage = 0
    private var totalProducts = 0
    private var minPrice = 0
    private var maxPrice = 0
    private var brandList = mutableListOf<String>()
    private var isLoading: Boolean = false

    init {

        binding.catalogue.setOnClickListener {
            binding.catalogue.isEnabled = false
            val transaction = deepARActivity.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
            transaction.add(R.id.container, CatalogueFragment.newInstance(
                sortingOrder,
                currentPage,
                totalProducts,
                minPrice,
                maxPrice,
                brandList
            ) { i, p ->
                adapter?.scrollToPosition(i)
                binding.productRecycler.scrollToPosition(i)
            }
            )
            transaction.addToBackStack(null)
            transaction.commit()
            binding.catalogue.postDelayed({ binding.catalogue.isEnabled = true }, 500)

        }


        val viewModel = ViewModelProvider.create(deepARActivity)[AlgoliaViewModel::class.java]

        binding.filterNavLayout.recyclerDropdownBrand.title.text = "Brand"
        binding.imageView3.setOnClickListener {
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


        binding.details.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(ProductDetailsFragment.newInstance(selectedProduct!!, {}))
            }
        }

        binding.email.setOnClickListener {

        }

        binding.next.setOnClickListener {
            if (selectedProduct != null) {
                onScreenshot(SCREENSHOT.SELFIE, selectedProduct!!)
            }
        }

        binding.cardView2.setOnClickListener {
            if (Bookmarks.getBookmarks().isEmpty()) {
                Toast.makeText(deepARActivity, "Click on heart to wishlist products first.", Toast.LENGTH_SHORT).apply {
                    setGravity(Gravity.TOP or Gravity.RIGHT, 200, 10)
                }.show()
                return@setOnClickListener
            }
            openDialogFragment(YouBookmarkFragment.newInstance())
        }

        binding.wishlist.setOnClickListener {
            if (selectedProduct != null) {
                Bookmarks.addToBookmark(selectedProduct!!)
            }
        }

//        binding.cardView3.setOnClickListener {
//            checkPermissionAndOpenGallery()
//        }

        binding.cardView4.setOnClickListener {
            openDialogFragment(ClubAvoltaFragment.newInstance())
        }

        adapter = ProductAdapter { i, p ->
            binding.lottieAnimation.isVisible = true
            applyEffect("none")
            selectedProduct = p
            binding.brand.text = p.brand
            binding.productCode.text = p.localItemCode
            binding.productPrice.text =
                "${p.currency} ${p.priceDutyFree}"

            CoroutineScope(Dispatchers.IO).launch {
                val name = p.localItemCode.trim().replace(" ", "_")
                val path =
                    downloadAndSaveFile(deepARActivity, p.asset3DUrlPath ?: "none", "$name.deepar")
                println(p.asset3DUrl)
//                val path =
//                    downloadAndSaveFile(
//                        deepARActivity,
//                        assetsUrl[i % assetsUrl.size],
//                        "$name.deepar"
//                    )

                applyEffect(path ?: "none")

                deepARActivity.runOnUiThread {
                    binding.lottieAnimation.isVisible = false
                }
            }
            updateHeartIcon(Bookmarks.getBookmarks())
        }

        val filterManager = FilterManager(
            binding.filterNavLayout,
            productViewModel
        ) { sorting, min, max, brandList ->
            this.sortingOrder = sorting
            this.currentPage = 0
            this.totalProducts = 0
            this.minPrice = min.toInt()
            this.maxPrice = max.toInt()
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


                if (totalProducts <= totalItemCount) return

                if (!isLoading && totalItemCount > visibleItemCount &&
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 1 &&
                    firstVisibleItemPosition >= 0
                ) {

                    if (maxPrice == 0) {
                        maxPrice = 1000
                    }
                    isLoading = true
                    productViewModel.fetchProduct(
                        sortingOrder = sortingOrder,
                        page = currentPage,
                        min = minPrice,
                        max = maxPrice,
                        brands = brandList
                    )
                }
            }
        })

        viewModel.filter.observe(deepARActivity) {
            if (!it.isNullOrEmpty()) {
                filterManager.updateBrandList(it)
            }
        }

        viewModel.price.observe(deepARActivity) {
            if (it == null) return@observe
            minPrice = it.min().toInt()
            maxPrice = it.max().toInt()
            filterManager.updateRange(it.min().toFloat(), it.max().toFloat())
        }

        productViewModel.products.observe(deepARActivity) {
            when (it) {
                is Resource.Error -> {
                    isLoading = false
                    binding.productRecyclerLoader.isVisible = false
                }

                is Resource.Loading -> {

                }

                is Resource.Success -> {
                    isLoading = false
                    binding.productRecyclerLoader.isVisible = false

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

        Bookmarks.bookmarks.observe(deepARActivity) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }
        GlobalProducts.products.observe(deepARActivity) {
            binding.productRecyclerLoader.isVisible = false
            adapter!!.updateData(it)
        }
//        binding.productRecyclerLoader.isVisible = true
        viewModel.fetchAllBrands()
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.fetchAllRecords()
        }
        productViewModel.fetchProduct()
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(deepARActivity.supportFragmentManager, fragment.tag)
    }

    private fun updateHeartIcon(list: List<Product>) {
        val drawable = if (list.contains(selectedProduct)) {
            ContextCompat.getDrawable(deepARActivity, R.drawable.ic_heart_red)
        } else {
            ContextCompat.getDrawable(deepARActivity, R.drawable.ic_heart_gray)
        }

        binding.wishlist.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

//    fun onResume() {
//        if (adapter != null) {
//            adapter!!.applyFilteredTryon()
//        }
//    }

    enum class SCREENSHOT {
        EMAIL, SELFIE
    }
}