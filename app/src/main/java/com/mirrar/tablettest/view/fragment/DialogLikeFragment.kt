package com.mirrar.tablettest.view.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.mirrar.tablettest.databinding.CustomAlertLayoutBinding


class DialogLikeFragment(private val tncTxt: String, val onNext: () -> Unit) : Fragment() {

    private var _binding: CustomAlertLayoutBinding? = null
    private val binding get() = _binding!!

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

        binding.tnc.text = Html.fromHtml( tncTxt, Html.FROM_HTML_MODE_COMPACT)
        binding.loader.isVisible = binding.tnc.text.isEmpty()
        binding.button.setOnClickListener {
            if (!binding.checkbox.isChecked) {
                binding.checkbox.error = "Please accept the terms to proceed."
                return@setOnClickListener
            }

            onNext()
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
        fun newInstance(s: String, o: () -> Unit) = DialogLikeFragment(s, o)
    }
}