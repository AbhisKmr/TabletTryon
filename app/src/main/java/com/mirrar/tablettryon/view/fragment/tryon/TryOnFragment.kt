package com.mirrar.tablettryon.view.fragment.tryon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.tools.faceDetector.mlkit.FaceDetectionActivity
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.ClubAvoltaFragment
import com.mirrar.tablettryon.view.fragment.DialogLikeFragment
import com.mirrar.tablettryon.view.fragment.email.EmailFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel
import java.io.IOException

class TryOnFragment : Fragment() {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

    private var selectedProduct: Product? = null

    private lateinit var faceDetectionActivity: FaceDetectionActivity

    private val imageList = listOf(R.drawable.eye1, R.drawable.eye2)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTryOnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imagePreview.setImageBitmap(AR_BITMAP)

        faceDetectionActivity = FaceDetectionActivity(binding.glassPreview)

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.catalogue.setOnClickListener {
            findNavController().navigate(R.id.action_tryOnFragment_to_catalogueFragment)
        }

        binding.details.setOnClickListener {
            if (selectedProduct != null) {
                openDialogFragment(ProductDetailsFragment.newInstance(selectedProduct!!))
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

        binding.cardView2.setOnClickListener {
            if (Bookmarks.getBookmarks().isEmpty()) {
                Toast.makeText(requireContext(), "Wishlist is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            openDialogFragment(YouBookmarkFragment.newInstance())
        }

        binding.wishlist.setOnClickListener {
            if (selectedProduct != null) {
                Bookmarks.addToBookmark(selectedProduct!!)
            }
        }

        binding.cardView3.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

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
            binding.glassPreview.setImageDrawable(requireContext().resources.getDrawable(imageList[i % 2]))
        }

        binding.productRecycler.adapter = adapter

        viewModel.product.observe(viewLifecycleOwner) {
            adapter.updateData(it)

            // remove this
            applyAR()
        }

        viewModel.filter.observe(viewLifecycleOwner) {
            binding.imageView3.isVisible = !it.isNullOrEmpty()
            if (!it.isNullOrEmpty()) {
                println(it.toString())
            }
        }

        Bookmarks.bookmarks.observe(viewLifecycleOwner) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }

        viewModel.getData()
        viewModel.fetchAllBrands()
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
        binding.imagePreview.setImageURI(uri)
        applyAR()
        Toast.makeText(requireContext(), "Image Selected: $uri", Toast.LENGTH_SHORT).show()
    }

    private fun applyAR() {
        try {
            val bitmap = viewToBitmap(binding.imagePreview) ?: return
            faceDetectionActivity.detectFaces(
                InputImage.fromBitmap(bitmap, 0),
                binding.canvasView
            )
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