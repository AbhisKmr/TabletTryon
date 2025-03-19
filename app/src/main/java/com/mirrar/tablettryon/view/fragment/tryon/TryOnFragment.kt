package com.mirrar.tablettryon.view.fragment.tryon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.network.ApiService
import com.mirrar.tablettryon.network.Repository
import com.mirrar.tablettryon.network.Resource
import com.mirrar.tablettryon.network.Retrofit
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.tools.FilterManager
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.AppConstraint.filterTryOn
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.GlobalProducts
import com.mirrar.tablettryon.utility.HelperFunctions.isValidUrl
import com.mirrar.tablettryon.view.fragment.ClubAvoltaFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.catalogue.CatalogueFragment
import com.mirrar.tablettryon.view.fragment.email.EmailFragment
import com.mirrar.tablettryon.view.fragment.selfie.SelfieFragment
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class TryOnFragment : Fragment() {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

    private var selectedProduct: Product? = null

    private val response = Retrofit.getInstance()?.create(ApiService::class.java)
    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModel.Factory(Repository((response!!)))
    }

    private var sortingOrder = "low_to_high"
    private var currentPage = 0
    private var totalProducts = 0
    private var minPrice = 0
    private var maxPrice = 0
    private var brandList = mutableListOf<String>()
    private var isLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTryOnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterTryOn = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imagePreview.setImageBitmap(AR_BITMAP)

        binding.filterNavLayout.recyclerDropdownBrand.title.text = "Brand"
        binding.imageView3.setOnClickListener {
            filterTryOn = null
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

        binding.switchMode.setOnClickListener {
        }

        binding.details.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(ProductDetailsFragment.newInstance(selectedProduct!!, {}))
            }
        }

        binding.email.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(
                    EmailFragment.newInstance(
                        selectedProduct!!,
                        viewToBitmap(binding.cardView3)!!
                    )
                )
            }
        }

        binding.next.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(
                    SelfieFragment.newInstance(
                        selectedProduct!!,
                        viewToBitmap(binding.cardView3)!!
                    )
                )
            }
        }

        binding.cardView2.setOnClickListener {
            if (Bookmarks.getBookmarks().isEmpty()) {
                Toast.makeText(requireContext(), "Wishlist is empty", Toast.LENGTH_SHORT).apply {
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

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        val adapter = ProductAdapter { i, p ->
            selectedProduct = p
            binding.brand.text = p.brand
            binding.productCode.text = p.localItemCode
            binding.productPrice.text =
                "${p.currency} ${p.priceDutyFree}"

            updateHeartIcon(Bookmarks.getBookmarks())
            applyAR()

        }

        binding.catalogue.setOnClickListener {
            filterTryOn = null
            val transaction = childFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
            transaction.add(
                R.id.container, CatalogueFragment.newInstance(
                    sortingOrder,
                    currentPage,
                    totalProducts,
                    minPrice,
                    maxPrice,
                    brandList
                ) { i, p ->
                    adapter.scrollToPosition(i)
                    binding.productRecycler.scrollToPosition(i)
                }
            )
            transaction.addToBackStack(null)
            transaction.commit()
//            findNavController().navigate(R.id.action_tryOnFragment_to_catalogueFragment)
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
            adapter.clear()
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

        viewModel.filter.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                filterManager.updateBrandList(it)
            }
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

        Bookmarks.bookmarks.observe(viewLifecycleOwner) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }
        GlobalProducts.products.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
//        binding.productRecyclerLoader.isVisible = true
        viewModel.fetchAllBrands()
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.fetchAllRecords()
        }
        productViewModel.fetchProduct()
    }

    private fun updateHeartIcon(list: List<Product>) {
        val drawable = if (list.contains(selectedProduct)) {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_red)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_gray)
        }

        binding.wishlist.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(childFragmentManager, fragment.tag)
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermissionAndOpenGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionRationale(permission)
            }

            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionRationale(permission: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("This app requires access to your gallery to select images.")
            .setPositiveButton("OK") { _, _ ->
                permissionLauncher.launch(permission)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun handleImageSelection(uri: Uri) {
        //binding.imagePreview.setImageURI(uri)
        applyAR()
        Toast.makeText(requireContext(), "Image Selected: $uri", Toast.LENGTH_SHORT).show()
    }

    private fun applyAR() {
        try {
            if (isValidUrl(selectedProduct?.triedOnUrl)) {
                binding.lottieAnimation.isVisible = true
                binding.imagePreview.setImageBitmap(AR_BITMAP)
                CoroutineScope(Dispatchers.IO).launch {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(selectedProduct?.triedOnUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.lottieAnimation.isVisible = false
                                    binding.imagePreview.setImageBitmap(resource)
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.lottieAnimation.isVisible = false
                                }
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    binding.lottieAnimation.isVisible = false
                                    binding.imagePreview.setImageBitmap(AR_BITMAP)
                                }
                            }
                        })
                }
            } else
                binding.imagePreview.setImageBitmap(AR_BITMAP)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun viewToBitmap(view: View): Bitmap? {
        if (view.width == 0 && view.height == 0) {
            return null
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}