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
import com.mirrar.tablettryon.utility.HelperFunctions.downloadAndSaveFile
import com.mirrar.tablettryon.utility.HelperFunctions.getFileNameAndExtension
import com.mirrar.tablettryon.utility.HelperFunctions.isValidUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var holdHandler: Handler? = null
    private var executionHandler: Handler? = null
    private var isHolding = false

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

        binding.button.setOnClickListener {
            val transaction = childFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
            transaction.add(R.id.container, DialogLikeFragment.newInstance({
                requireActivity().onBackPressedDispatcher.onBackPressed()
                findNavController().navigate(R.id.cameraFragment)
            }))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        productViewModel.downloadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DownloadState.Progress -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.txtPercent.text = "${state.percentage}%"
                }

                is DownloadState.Success -> {
                    binding.progress.visibility = View.VISIBLE

                    lifecycleScope.launch {
                        for (i in state.products.indices) {
                            binding.txtPercent.text = "${((i * 100) / state.products.size)}%"

                            withContext(Dispatchers.IO) {
                                saveFileToLocal(state.products[i])
                            }
                        }

                        binding.progress.visibility = View.GONE
                        binding.txtPercent.text = "${state.products.size}%"
                    }
                }

                is DownloadState.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.txtPercent.text = state.message
                }
            }
        }
        binding.club.setOnTouchListener { _, event ->
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
                productViewModel.startDownload()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Delete all") { dialog, _ ->
                productViewModel.deleteAll()
                dialog.dismiss()
            }
            .setCancelable(false) // Prevent dismissing by tapping outside
            .show()
    }

    private suspend fun saveFileToLocal(p: Product) = withContext(Dispatchers.IO) {
        val asset2DUrlPath = async { urlToFilePath(p.asset2DUrl) }.await()
        val imageExtra1Path = async { urlToFilePath(p.imageExtra1) }.await()
        val imageExtra2Path = async { urlToFilePath(p.imageExtra2) }.await()
        val imageSmallPath = async { urlToFilePath(p.imageSmall) }.await()
        val imageThumbnailPath = async { urlToFilePath(p.imageThumbnail) }.await()
        val imageUrlBasePath = async { urlToFilePath(p.imageUrlBase) }.await()
        val asset3DUrlPath = async { urlToFilePath(p.asset3DUrl) }.await()

        p.asset2DUrlPath = asset2DUrlPath
        p.imageExtra1Path = imageExtra1Path
        p.imageExtra2Path = imageExtra2Path
        p.imageSmallPath = imageSmallPath
        p.imageThumbnailPath = imageThumbnailPath
        p.imageUrlBasePath = imageUrlBasePath
        p.asset3DUrlPath = asset3DUrlPath

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