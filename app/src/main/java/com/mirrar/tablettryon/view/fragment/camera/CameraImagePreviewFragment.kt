package com.mirrar.tablettryon.view.fragment.camera

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentCameraImagePreviewBinding
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.utility.GlobalProducts
import com.mirrar.tablettryon.view.fragment.email.EmailHelper
import com.mirrar.tablettryon.view.fragment.email.EmailSavePopupFragment

class CameraImagePreviewFragment : Fragment() {

    private var _binding: FragmentCameraImagePreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailFragment = EmailSavePopupFragment.newInstance {
            EmailHelper.sendDynamicEmail(requireContext(), "face scan") {
                if (it == null) {
                    Toast.makeText(
                        requireContext(), "Failed to send email.", Toast.LENGTH_SHORT
                    ).show()
                }
                findNavController().navigate(R.id.action_cameraImagePreviewFragment4_to_tryOnFragment)

            }
        }

        binding.cameraPreview.setImageBitmap(AR_BITMAP)
//        binding.progress.isVisible = false

        binding.back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.next.setOnClickListener {
            binding.lottieAnimation.isVisible = true
            updateScanView(false)
//            emailFragment.show(childFragmentManager, emailFragment.tag)
            val b = ImageUploadForRecommendation().resizeAndCompressBitmap(AR_BITMAP!!)
            ImageUploadForRecommendation().uploadBitmap(
                b, requireContext()
            ) {
                updateScanView(true)
                recommendationModel = it
                GlobalProducts.updateProduct(it?.recommendations ?: emptyList())
                binding.lottieAnimation.isVisible = false
                emailFragment.show(childFragmentManager, emailFragment.tag)
//                GlobalScope.launch {
//                    withContext(Dispatchers.Main) {
//                        emailFragment.update()
//                    }
//                }
            }
        }

    }

    private fun updateScanView(b: Boolean) {
        binding.back.isVisible = b
        binding.next.isVisible = b
        binding.textView4.isVisible = b
        binding.tvtv.isVisible = b
        binding.scanning.isVisible = !b
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}