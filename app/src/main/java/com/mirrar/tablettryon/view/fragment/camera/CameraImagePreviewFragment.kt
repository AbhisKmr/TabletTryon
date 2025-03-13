package com.mirrar.tablettryon.view.fragment.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentCameraImagePreviewBinding
import com.mirrar.tablettryon.tools.model.FaceRecommendationModel
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel

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

        binding.cameraPreview.setImageBitmap(AR_BITMAP)
        binding.progress.isVisible = false

        binding.back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.next.setOnClickListener {
            binding.progress.isVisible = true
            val b = ImageUploadForRecommendation().resizeAndCompressBitmap(AR_BITMAP!!)
            ImageUploadForRecommendation().uploadBitmap(b, requireContext()
            ) {
                recommendationModel = it

                findNavController().navigate(R.id.action_cameraImagePreviewFragment4_to_tryOnFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}