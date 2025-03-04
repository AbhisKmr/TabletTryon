package com.mirrar.tablettryon.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.CustomAlertLayoutBinding
import com.mirrar.tablettryon.tools.FirebaseHelper


class DialogLikeFragment : Fragment() {

    private var _binding: CustomAlertLayoutBinding? = null
    private val binding get() = _binding!!

    private val firebaseHelper = FirebaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CustomAlertLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeView.setOnClickListener {
            dismissDialog()
        }

        binding.imageView2.setOnClickListener {
            dismissDialog()
        }

        enterTransition = android.transition.Fade()
        exitTransition = android.transition.Fade()

        view.findViewById<Button>(R.id.button).setOnClickListener {
            if (!binding.checkbox.isChecked) {
                binding.checkbox.error = "Please accept the terms to proceed."
                return@setOnClickListener
            }

            findNavController().navigate(R.id.cameraFragment)
        }

        observer()
    }

    private fun observer() {
        firebaseHelper.getTermAndCondition {
            binding.tnc.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
        }
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .remove(this)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = DialogLikeFragment()
    }
}