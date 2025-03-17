package com.mirrar.tablettryon.view.fragment.camera

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.DeepARActivity
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentCameraImagePreviewBinding
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.view.fragment.email.EmailPopupFragment
import com.mirrar.tablettryon.view.fragment.email.EmailSavePopupFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            Handler().postDelayed({
//                startActivity(Intent(requireActivity(), DeepARActivity::class.java))
                findNavController().navigate(R.id.action_cameraImagePreviewFragment4_to_tryOnFragment)
            }, 100)
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