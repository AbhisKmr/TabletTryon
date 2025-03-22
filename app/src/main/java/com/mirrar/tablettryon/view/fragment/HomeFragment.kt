package com.mirrar.tablettryon.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.database.DownloadState
import com.mirrar.tablettryon.database.ProductDataViewModel
import com.mirrar.tablettryon.database.ProductDatabase
import com.mirrar.tablettryon.database.ProductRepository
import com.mirrar.tablettryon.databinding.FragmentHomeBinding


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

//        characterViewModel.list.observe(viewLifecycleOwner) {
//            it.forEach { p ->
//                println(p.name)
//            }
//        }

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

//        binding.club.setOnClickListener {
//            val dis = getDisplaySize(requireContext())
//            val bar = getActionBarSize(requireContext())
//            val nav = getNavigationBarHeight(requireContext())
//            binding.reso.text = "w: ${dis.first} || h: ${dis.second + nav} || nav: $nav"
//            //displayMetrics.widthPixels, displayMetrics.heightPixels
//        }

        productViewModel.downloadState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DownloadState.Progress -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.txtPercent.text = "${state.percentage}%"
                }

                is DownloadState.Success -> {
                    binding.progress.visibility = View.GONE
                    binding.txtPercent.text = "${state.products.size}%"
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}