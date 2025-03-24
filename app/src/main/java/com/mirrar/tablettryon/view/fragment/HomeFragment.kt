package com.mirrar.tablettryon.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.database.DownloadState
import com.mirrar.tablettryon.database.ProductDataViewModel
import com.mirrar.tablettryon.database.ProductDatabase
import com.mirrar.tablettryon.database.ProductRepository
import com.mirrar.tablettryon.databinding.FragmentHomeBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.tools.FirebaseHelper
import com.mirrar.tablettryon.utility.AppSharedPref
import com.mirrar.tablettryon.utility.HelperFunctions.clearAppCache
import com.mirrar.tablettryon.utility.HelperFunctions.downloadAndSaveFile
import com.mirrar.tablettryon.utility.HelperFunctions.getFileNameAndExtension
import com.mirrar.tablettryon.utility.HelperFunctions.isValidUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val firebaseHelper = FirebaseHelper()
    private lateinit var appSharedPref: AppSharedPref

    private var holdHandler: Handler? = null
    private var executionHandler: Handler? = null
    private var isHolding = false
    private var tnc: String = ""

    private val database by lazy { ProductDatabase.getDatabase(requireContext()) }
    private val repository by lazy { ProductRepository(database.getProductDao()) }

    private val productViewModel: ProductDataViewModel by viewModels {
        ProductDataViewModel.ProductViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        appSharedPref = AppSharedPref(requireActivity())

        val appAlreadyStart = appSharedPref.getBoolean("appStart")

        changeGotoView(appAlreadyStart)

        if (!appAlreadyStart) {
            binding.start.setOnClickListener {
                onStartDownload()
            }
        }

        productViewModel.downloadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DownloadState.Progress -> {
                    binding.downloadText.visibility = View.VISIBLE
                    binding.percentText.visibility = View.VISIBLE
                    binding.progress.visibility = View.VISIBLE
                    binding.percentText.text = "${state.percentage}%"
                    binding.progress.progress = state.percentage
                }

                is DownloadState.Success -> {
//                    binding.progress.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        binding.downloadText.text = "Preparing assets..."
                        for (i in state.products.indices) {
                            binding.progress.progress = ((i * 100) / state.products.size)
                            binding.percentText.text = "${((i * 100) / state.products.size)}%"

                            withContext(Dispatchers.IO) {
                                saveFileToLocal(state.products[i])
                            }
                        }

//                        binding.progress.visibility = View.GONE
//                        binding.txtPercent.text = "${state.products.size}%"
                        afterStartDownload()
                        binding.downloadText.text = "Downloading..."
                        binding.downloadText.visibility = View.GONE
                        binding.percentText.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        appSharedPref.putBoolean("appStart", true)
                        changeGotoView(true)
                    }
                }

                is DownloadState.Error -> {
//                    binding.progress.visibility = View.GONE
//                    binding.txtPercent.text = state.message
                }
            }
        }

        binding.imageView6.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHolding = true
                    startHoldTimer()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                }
            }
            true
        }

        firebaseHelper.getTermAndCondition {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    tnc = it ?: ""
                    binding.button.setOnClickListener {
                        val transaction = childFragmentManager.beginTransaction()
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
                        transaction.add(R.id.container, DialogLikeFragment.newInstance(tnc, {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            findNavController().navigate(R.id.cameraFragment)
                        }))
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun onStartDownload() {
        binding.downloadText.text = "Downloading..."
        whenStartDownload()
        productViewModel.startDownload()
    }

    private fun changeGotoView(dataDownloaded: Boolean) {
        binding.goToApp.isVisible = dataDownloaded
        binding.firstAppSetup.isVisible = !dataDownloaded
    }

    private fun whenStartDownload() {
        binding.start.isVisible = false
        binding.message.text =
            "Setting things up... Please wait while we download the necessary assets. This may take a moment."
    }

    private fun afterStartDownload() {
        binding.start.isVisible = true
        binding.message.text =
            "Welcome! This is the first step to setting up the app. Tap 'Setup' to begin, and please be patient while we download the necessary assets."
    }

    private fun stopTimers() {
        holdHandler?.removeCallbacksAndMessages(null)
        executionHandler?.removeCallbacksAndMessages(null)
    }

    private fun startHoldTimer() {
        holdHandler = Handler(Looper.getMainLooper())
        holdHandler?.postDelayed({
            if (isHolding) {
                isHolding = false
                stopTimers()
                showYesNoDialog()
            }
        }, 3000)
    }


    private fun showYesNoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to proceed? This will reset all local data.")
            .setPositiveButton("Yes") { dialog, _ ->
                appSharedPref.putBoolean("appStart", false)
                clearAppCache(requireContext())
                changeGotoView(false)
                onStartDownload()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
//            .setNeutralButton("Delete all") { dialog, _ ->
//                productViewModel.deleteAll()
//                dialog.dismiss()
//            }
            .setCancelable(false) // Prevent dismissing by tapping outside
            .show()
    }

    private suspend fun saveFileToLocal(p: Product) = withContext(Dispatchers.IO) {
        val asset2DUrlPath = async { urlToFilePath(p.asset2DUrl) }
        val imageExtra1Path = async { urlToFilePath(p.imageExtra1) }
        val imageExtra2Path = async { urlToFilePath(p.imageExtra2) }
        val imageSmallPath = async { urlToFilePath(p.imageSmall) }
        val imageThumbnailPath = async { urlToFilePath(p.imageThumbnail) }
        val imageUrlBasePath = async { urlToFilePath(p.imageUrlBase) }
        val asset3DUrlPath = async { urlToFilePath(p.asset3DUrl) }

        // Await all at once to execute in parallel
        p.asset2DUrlPath = asset2DUrlPath.await()
        p.imageExtra1Path = imageExtra1Path.await()
        p.imageExtra2Path = imageExtra2Path.await()
        p.imageSmallPath = imageSmallPath.await()
        p.imageThumbnailPath = imageThumbnailPath.await()
        p.imageUrlBasePath = imageUrlBasePath.await()
        p.asset3DUrlPath = asset3DUrlPath.await()

        productViewModel.updateProduct(p)
    }

    private fun urlToFilePath(url: String?): String? {
        if (url == null || !isValidUrl(url)) {
            return null
        }

        val (name, extension) = getFileNameAndExtension(url)
        return downloadAndSaveFile(requireContext(), url, "${name}.${extension}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}