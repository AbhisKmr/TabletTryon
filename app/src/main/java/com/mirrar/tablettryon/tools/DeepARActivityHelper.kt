package com.mirrar.tablettryon.tools

import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mirrar.tablettryon.DeepARActivity
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.ActivityDeepAractivityBinding
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.HelperFunctions.rotateImage
import com.mirrar.tablettryon.view.fragment.ClubAvoltaFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.catalogue.CatalogueFragment
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterListAdapter
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DeepARActivityHelper(
    private val deepARActivity: DeepARActivity,
    private val applyEffect: (String) -> Unit,
    private val onScreenshot: (SCREENSHOT, Product) -> Unit
) {

    private var binding: ActivityDeepAractivityBinding = deepARActivity.binding
    private var selectedProduct: Product? = null

    init {
        binding.catalogue.setOnClickListener {
            val transaction = deepARActivity.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
            transaction.add(R.id.container, CatalogueFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }


        val viewModel = ViewModelProvider.create(deepARActivity)[AlgoliaViewModel::class.java]

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


        binding.details.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(ProductDetailsFragment.newInstance(selectedProduct!!, {}))
            }
        }

        binding.email.setOnClickListener {
            if (selectedProduct != null) {
//                deepAR.takeScreenshot()
                // **Bitmap
//                openDialogFragment(
//                    EmailFragment.newInstance(
//                        selectedProduct!!,
//                        viewToBitmap(binding.cardView3)!!
//                    )
//                )
            }
        }

        binding.next.setOnClickListener {
            if (selectedProduct != null) {
                onScreenshot(SCREENSHOT.SELFIE, selectedProduct!!)
            }
        }

        binding.cardView2.setOnClickListener {
            if (Bookmarks.getBookmarks().isEmpty()) {
                Toast.makeText(deepARActivity, "Wishlist is empty", Toast.LENGTH_SHORT).apply {
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

            deepARActivity.lifecycleScope.launch {
                val path = withContext(Dispatchers.IO) {
                    val name = p.localItemCode.trim().replace(" ", "_")
                    downloadAndSaveFile(
                        "https://github.com/AbhisKmr/alpha/raw/refs/heads/master/glass.deepar",
                        "$name.deepar"
                    )
                }
                applyEffect(path!!)
            }
            updateHeartIcon(Bookmarks.getBookmarks())
        }

        binding.productRecycler.adapter = adapter

        binding.productRecyclerLoader.isVisible = true
        viewModel.product.observe(deepARActivity) {
            binding.productRecyclerLoader.isVisible = false
            adapter.updateData(it)
            // remove this
            binding.filterNavLayout.applyProgress.isVisible = false
            binding.filterNavLayout.apply.text = "Apply"
        }

        binding.filterNavLayout.sortbyDropdown.dropArrow.setOnClickListener {
            val vis = binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible
            binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible = !vis

            rotateImage(
                binding.filterNavLayout.sortbyDropdown.dropArrow,
                if (!vis) 180f else 0f,
                0f,
            )
        }

        viewModel.filter.observe(deepARActivity) {
            if (!it.isNullOrEmpty()) {

                binding.filterNavLayout.apply.setOnClickListener { v ->
                    val selectedIndex =
                        binding.filterNavLayout.sortbyDropdown.radioGroup.indexOfChild(
                            binding.root.findViewById(binding.filterNavLayout.sortbyDropdown.radioGroup.checkedRadioButtonId)
                        )

                    if (it.none { iii -> iii.isSelected } && selectedIndex < 0) {
                        Toast.makeText(
                            deepARActivity, "Please select filter first", Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.filterNavLayout.applyProgress.isVisible = true
                    binding.filterNavLayout.apply.text = ""

                    viewModel.fetchFilteredProducts(it, selectedIndex)
                    binding.drawerLayout.closeDrawers()
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
                    viewModel.fetchFilteredProducts(it)
                    ad.notifyDataSetChanged()
                }

            }
        }

        Bookmarks.bookmarks.observe(deepARActivity) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }

        viewModel.getData()
        viewModel.fetchAllBrands()
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

    private fun downloadAndSaveFile(fileUrl: String, fileName: String): String? {
        return try {
            val directory = deepARActivity.cacheDir
            val file = File(directory, fileName)

            if (file.exists()) {
                Log.d("Download", "File already exists: ${file.absolutePath}")
                return file.absolutePath
            }

            val client = OkHttpClient()
            val request = Request.Builder().url(fileUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("Download", "Failed to download file: ${response.message}")
                return null
            }

            val inputStream: InputStream? = response.body?.byteStream()
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

            Log.d("Download", "File saved: ${file.absolutePath}")
            file.absolutePath

        } catch (e: Exception) {
            Log.e("Download", "Error: ${e.message}")
            null
        }
    }

    enum class SCREENSHOT {
        EMAIL, SELFIE
    }
}