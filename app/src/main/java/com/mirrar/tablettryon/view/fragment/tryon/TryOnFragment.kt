package com.mirrar.tablettryon.view.fragment.tryon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mediapipe.examples.facelandmarker.FaceLandmarkerHelper
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.network.CallApi
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.AppConstraint.filterTryOn
import com.mirrar.tablettryon.utility.AppConstraint.priceMax
import com.mirrar.tablettryon.utility.AppConstraint.priceMin
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.HelperFunctions.isValidUrl
import com.mirrar.tablettryon.utility.HelperFunctions.rotateImage
import com.mirrar.tablettryon.view.fragment.ClubAvoltaFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterListAdapter
import com.mirrar.tablettryon.view.fragment.email.EmailFragment
import com.mirrar.tablettryon.view.fragment.selfie.SelfieFragment
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService

class TryOnFragment : Fragment(), FaceLandmarkerHelper.LandmarkerListener {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

    private var selectedProduct: Product? = null

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var backgroundExecutor: ScheduledExecutorService

    private var currentPage = 0
    private val totalProducts = MutableLiveData<Int>()
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


        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.filterNavLayout.recyclerDropdownBrand.title.text = "Brand"
        binding.imageView3.setOnClickListener {
            binding.drawerLayout.elevation = 100f
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

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

        binding.catalogue.setOnClickListener {
            findNavController().navigate(R.id.action_tryOnFragment_to_catalogueFragment)
        }

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

        val adapter = ProductAdapter { i, p ->
            selectedProduct = p
            binding.brand.text = p.brand
            binding.productCode.text = p.localItemCode
            binding.productPrice.text =
                "${p.currency} ${p.priceDutyFree}"

            updateHeartIcon(Bookmarks.getBookmarks())
            applyAR()

        }

        binding.productRecycler.adapter = adapter

        binding.productRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {

                    isLoading = true

                    viewModel.fetchProducts(
                        false,
                        progressBar = binding.progressBar,
                        viewModel.filter.value ?: emptyList(),
                        binding.filterNavLayout.sortbyDropdown.radioGroup.indexOfChild(
                            view.findViewById(binding.filterNavLayout.sortbyDropdown.radioGroup.checkedRadioButtonId)
                        )
                    )
                }

                /* val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                 layoutManager?.let {
                     val firstVisibleItemIndex = it.findFirstVisibleItemPosition()
                     if (recommendationModel?.recommendations != null) {
                         if (firstVisibleItemIndex > recommendationModel!!.recommendations.size) {
                             CallApi.getMoreAssets(
                                 recommendationModel!!.uuid!!,
                                 recommendationModel!!.recommendations.map { obj -> obj.objectID }
                             ) { res ->
                                 adapter.updateAssetUrl(res?.tryonOutputs ?: emptyMap())
                             }
                         }
                     }
                 }*/
            }
        })

        binding.productRecyclerLoader.isVisible = true

        viewModel.product.observe(viewLifecycleOwner) {

            if (it.isNullOrEmpty()) {
//                adapter.clear()
//                binding.emptyList.isVisible = true
//                viewModel.pageCount = 1
                return@observe
            }
            if (viewModel.itIsForRecommendation) {
                updateProductList(viewModel, adapter, it)
            } else if (recommendationModel != null) {
                val map = mutableMapOf<String, String>()
                it.forEach { obj -> map[obj.objectID] = obj.asset2DUrl.toString() }

                CallApi.getMoreAssets(recommendationModel!!.uuid!!, map) { res ->
                    it.forEach {
                        if (res?.tryonOutputs!!.contains(it.objectID)) {
                            it.triedOnImageUrl = res?.tryonOutputs?.get(it.objectID) ?: ""
                        }
                    }
                    requireActivity().runOnUiThread(Runnable {
                        updateProductList(viewModel, adapter, it)
                    })
                }
            } else {
                updateProductList(viewModel, adapter, it)

            }

            binding.filterNavLayout.applyProgress.isVisible = false
            binding.filterNavLayout.apply.text = "Apply"
            binding.drawerLayout.closeDrawers()
            binding.emptyList.isVisible = false
            isLoading = false

            totalProducts.value = viewModel.nbHits
            viewModel.pageCount++
            applyAR()
        }

        binding.filterNavLayout.sortbyDropdown.dropArrow.setOnClickListener {
            val vis = binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible
            binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible = !vis

            requireActivity().runOnUiThread {
                rotateImage(
                    binding.filterNavLayout.sortbyDropdown.dropArrow,
                    if (!vis) 180f else 0f,
                    0f,
                )
            }
        }

        updateRange(priceMin!!, priceMax!!, priceMin!!, priceMax!!)

        binding.filterNavLayout.priceRange.priceRange.addOnChangeListener { slider, _, _ ->
            binding.filterNavLayout.priceRange.min.text = "Min: CHF${slider.values[0].toInt()}"
            binding.filterNavLayout.priceRange.max.text = "Max: CHF${slider.values[1].toInt()}"
            priceMin = slider.values[0]
            priceMax = slider.values[1]
        }

        viewModel.filter.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {

                binding.filterNavLayout.apply.setOnClickListener { v ->
                    val selectedIndex =
                        binding.filterNavLayout.sortbyDropdown.radioGroup.indexOfChild(
                            view.findViewById(binding.filterNavLayout.sortbyDropdown.radioGroup.checkedRadioButtonId)
                        )

                    if (it.none { iii -> iii.isSelected } && selectedIndex < 0) {
                        Toast.makeText(
                            requireContext(), "Please select filter first", Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.filterNavLayout.applyProgress.isVisible = true
                    binding.filterNavLayout.apply.text = ""

                    Handler().postDelayed({
                        viewModel.pageCount = 1
                        viewModel.fetchProducts(false, binding.progressBar, it, selectedIndex)
                    }, 500)
                }

                binding.filterNavLayout.priceRange.dropArrow.setOnClickListener {
                    val vis = binding.filterNavLayout.priceRange.optionParent.isVisible
                    binding.filterNavLayout.priceRange.optionParent.isVisible = !vis

                    rotateImage(
                        binding.filterNavLayout.priceRange.dropArrow,
                        if (!vis) 180f else 0f,
                        0f,
                    )
                }

                binding.filterNavLayout.recyclerDropdownBrand.dropArrow.setOnClickListener {
                    val vis = binding.filterNavLayout.recyclerDropdownBrand.optionParent.isVisible
                    binding.filterNavLayout.recyclerDropdownBrand.optionParent.isVisible = !vis

                    rotateImage(
                        binding.filterNavLayout.recyclerDropdownBrand.dropArrow,
                        if (!vis) 180f else 0f,
                        0f,
                    )
                }
                val ad = FilterListAdapter(it) {

                }
                binding.filterNavLayout.recyclerDropdownBrand.options.adapter = ad


                binding.filterNavLayout.reset.setOnClickListener { v ->
                    it.forEach { pp -> pp.isSelected = false }
                    viewModel.onlyRecommendation()
                    priceMin = 0f
                    priceMax = 1000f
                    binding.filterNavLayout.sortbyDropdown.radioGroup.clearCheck()
                    updateRange(priceMin!!, priceMax!!, priceMin!!, priceMax!!)
                    ad.notifyDataSetChanged()
                }
            }
        }

        Bookmarks.bookmarks.observe(viewLifecycleOwner) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }

        viewModel.onlyRecommendation()
        viewModel.fetchAllBrands()
    }

    private fun updateProductList(
        viewModel: AlgoliaViewModel,
        adapter: ProductAdapter,
        list: List<Product>
    ) {
        binding.productRecyclerLoader.isVisible = false
        binding.progressBar.isVisible = false

        if (viewModel.loadMore) {
            adapter.addData(list)
        } else {
            adapter.updateData(list)
        }
    }

    private fun updateRange(min: Float, max: Float, minValue: Float, maxValue: Float) {
        binding.filterNavLayout.priceRange.priceRange.valueFrom = min
        binding.filterNavLayout.priceRange.priceRange.valueTo = max
        binding.filterNavLayout.priceRange.priceRange.values =
            listOf(minValue, maxValue) // Set the initial range
        binding.filterNavLayout.priceRange.min.text = "Min: CHF${minValue.toInt()}"
        binding.filterNavLayout.priceRange.max.text = "Max: CHF${maxValue.toInt()}"
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
            if (isValidUrl(selectedProduct?.triedOnImageUrl)) {
                GlobalScope.launch {
                    val bb = withContext(Dispatchers.IO) {
                        try {
                            Glide
                                .with(requireContext())
                                .asBitmap()
                                .load(selectedProduct?.triedOnImageUrl)
                                .submit()
                                .get()
                        } catch (e: Exception) {
                            AR_BITMAP
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.imagePreview.setImageBitmap(bb)
                    }

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

    override fun onError(error: String, errorCode: Int) {

    }

    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {

    }
}