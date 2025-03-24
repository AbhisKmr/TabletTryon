package com.mirrar.tablettryon.view.fragment.tryon

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.mirrar.tablettryon.DeepARActivity
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.network.ApiService
import com.mirrar.tablettryon.network.Repository
import com.mirrar.tablettryon.network.Resource
import com.mirrar.tablettryon.network.Retrofit
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.tools.FilterManager
import com.mirrar.tablettryon.tools.faceDetector.mediapipe.FaceLandmarkerHelper
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.GlobalProducts
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
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class TryOnFragment : Fragment(), FaceLandmarkerHelper.LandmarkerListener {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

    private var selectedProduct: Product? = null

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var backgroundExecutor: ScheduledExecutorService

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

    init {
        System.loadLibrary("opencv_java4")
    }

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

        binding.switchMode.setOnClickListener {
            startActivity(Intent(requireActivity(), DeepARActivity::class.java))
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
                    SelfieFragment.newInstance()
                )
            }
        }

        binding.cardView2.setOnClickListener {
            if (Bookmarks.getBookmarks().isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Click on heart to wishlist products first.",
                    Toast.LENGTH_SHORT
                ).apply {
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
            binding.catalogue.isEnabled = false
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
            binding.catalogue.postDelayed({ binding.catalogue.isEnabled = true }, 500)
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
            binding.productRecyclerLoader.isVisible = false
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

    private fun applyAR() {
        runMediapipeOnBitmap(AR_BITMAP!!)
        /*
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

         */
    }

    private fun runMediapipeOnBitmap(bitmap: Bitmap) {

        binding.overlay.clear()
        backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
        backgroundExecutor.execute {

            faceLandmarkerHelper =
                FaceLandmarkerHelper(
                    context = requireContext(),
                    runningMode = RunningMode.IMAGE,
                    minFaceDetectionConfidence = FaceLandmarkerHelper.DEFAULT_FACE_DETECTION_CONFIDENCE,
                    minFaceTrackingConfidence = FaceLandmarkerHelper.DEFAULT_FACE_TRACKING_CONFIDENCE,
                    minFacePresenceConfidence = FaceLandmarkerHelper.DEFAULT_FACE_PRESENCE_CONFIDENCE,
                    maxNumFaces = 1,
                    currentDelegate = FaceLandmarkerHelper.DEFAULT_FACE_DETECTION_CONFIDENCE.toInt()
                )

            faceLandmarkerHelper.detectImage(bitmap)?.let { result ->
                activity?.runOnUiThread {
                    Log.i("faceLandmarkerHelper", result.result.faceLandmarks().size.toString())

                    renderSunglassesOnFace(
                        AR_BITMAP!!,
                        result.result,
                        "/data/user/0/com.mirrar.tablettryon/cache/6534509.png"
                    )
//                    binding.overlay.setResults(
//                        result.result,
//                        bitmap.height,
//                        bitmap.width,
//                        RunningMode.IMAGE
//                    )

                }
            } ?: run { Log.e("faceLandmarkerHelper", "Error running face landmarker.") }

            faceLandmarkerHelper.clearFaceLandmarker()
        }
    }

    private fun renderSunglassesOnFace(
        imageBitmap: Bitmap,
        faceLandmarkerResult: FaceLandmarkerResult,
        sunglassesPath: String
    ) {
        // Convert imageBitmap to OpenCV Mat
        val originalMat = Mat()
        Utils.bitmapToMat(imageBitmap, originalMat)

        // Load sunglasses WebP with alpha
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val sunglassesBitmap = BitmapFactory.decodeFile(sunglassesPath, options) ?: return

        // Convert to OpenCV Mat
        val sunglassesMat = Mat()
        Utils.bitmapToMat(sunglassesBitmap, sunglassesMat)

        // Ensure 4-channel BGRA for transparency
        if (sunglassesMat.channels() == 3) {
            Imgproc.cvtColor(sunglassesMat, sunglassesMat, Imgproc.COLOR_BGR2BGRA)
        }

        // Get face landmarks
        val landmarks = faceLandmarkerResult.faceLandmarks()[0]
        val leftEyeOuterCorner = landmarks[33]
        val rightEyeOuterCorner = landmarks[263]
        val leftEyebrowUpperMid = landmarks[282]
        val rightEyebrowUpperMid = landmarks[52]
        val noseTop = landmarks[6]

        // Calculate sunglasses position and size
        val faceWidth = sqrt(
            (rightEyeOuterCorner.x() - leftEyeOuterCorner.x()).pow(2) +
                    (rightEyeOuterCorner.y() - leftEyeOuterCorner.y()).pow(2)
        ) * 1.5
        val sunglassesScale = faceWidth / sunglassesMat.cols()

        // Calculate rotation angle
        val dx = rightEyeOuterCorner.x() - leftEyeOuterCorner.x()
        val dy = rightEyeOuterCorner.y() - leftEyeOuterCorner.y()
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))

        // Calculate center position for sunglasses
        val centerX = (leftEyeOuterCorner.x() + rightEyeOuterCorner.x()) / 2
        val centerY = (leftEyebrowUpperMid.y() + rightEyebrowUpperMid.y()) / 2 +
                (noseTop.y() - (leftEyebrowUpperMid.y() + rightEyebrowUpperMid.y()) / 2) * 0.2

        // Transform sunglasses: scale and rotate
        val transformationMatrix = Imgproc.getRotationMatrix2D(
            Point(sunglassesMat.cols() / 2.0, sunglassesMat.rows() / 2.0),
            angle,
            sunglassesScale
        )

        val transformedSunglasses = Mat()
        Imgproc.warpAffine(
            sunglassesMat,
            transformedSunglasses,
            transformationMatrix,
            Size(sunglassesMat.cols() * sunglassesScale, sunglassesMat.rows() * sunglassesScale),
            Imgproc.INTER_LINEAR,
            Core.BORDER_CONSTANT,
            Scalar(0.0, 0.0, 0.0, 0.0)  // Ensure transparency is preserved
        )

        // Convert center position to integer coordinates
        val posX = (centerX - transformedSunglasses.cols() / 2).toInt()
        val posY = (centerY - transformedSunglasses.rows() / 2).toInt()

        // **Add Shadow for Realism**
        val shadowMat = transformedSunglasses.clone()
        Core.multiply(shadowMat, Scalar(0.2, 0.2, 0.2, 1.0), shadowMat) // Darken for shadow effect
        Imgproc.GaussianBlur(shadowMat, shadowMat, Size(15.0, 15.0), 10.0)

        // Apply shadow slightly lower for realism
        overlayImage(originalMat, shadowMat, posX + 5, posY + 5)

        // Overlay sunglasses onto face with alpha blending
        overlayImage(originalMat, transformedSunglasses, posX, posY)

        // Convert result back to Bitmap
        val resultBitmap =
            Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(originalMat, resultBitmap)

//        binding.imagePreview.setImageBitmap(resultBitmap)
    }

    private fun overlayImage(background: Mat, foreground: Mat, x: Int, y: Int) {
        for (row in 0 until foreground.rows()) {
            for (col in 0 until foreground.cols()) {
                if (y + row >= background.rows() || x + col >= background.cols()) continue // Bounds check

                val fgPixel = foreground.get(row, col) ?: continue
                val bgPixel = background.get(y + row, x + col) ?: continue

                if (fgPixel.size < 4) continue // Ensure it's a 4-channel pixel

                val alpha = fgPixel[3] / 255.0 // Extract alpha channel (0-1 range)

                if (alpha > 0) { // Apply only if there's some transparency
                    val blendedPixel = floatArrayOf(
                        (fgPixel[0] * alpha + bgPixel[0] * (1 - alpha)).toFloat(), // Blue
                        (fgPixel[1] * alpha + bgPixel[1] * (1 - alpha)).toFloat(), // Green
                        (fgPixel[2] * alpha + bgPixel[2] * (1 - alpha)).toFloat(), // Red
                        (fgPixel[3] + bgPixel[3] * (1 - alpha)).toFloat()  // Preserve alpha blending
                    )

                    background.put(y + row, x + col, blendedPixel)
                }
            }
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