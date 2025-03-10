package com.mirrar.tablettryon.view.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentEmailBinding
import com.mirrar.tablettryon.utility.EmailHelper
import com.mirrar.tablettryon.utility.HelperFunctions.GET_IMAGE_URL_FROM_PRODUCT
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

class EmailFragment(private val p: Product, private val bitmap: Bitmap) : DialogFragment() {

    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeView.setOnClickListener {
            dismissDialog()
        }

        binding.imageView2.setOnClickListener {
            dismissDialog()
        }

        Glide.with(requireContext()).load(GET_IMAGE_URL_FROM_PRODUCT(p)).into(binding.glassImage)
        binding.modelImage.setImageBitmap(bitmap)

        binding.send.setOnClickListener {

            if (binding.name.text.trim().isEmpty()) {
                binding.name.error = "Required"
                return@setOnClickListener
            }

            if (binding.email.text.trim().isEmpty()) {
                binding.email.error = "Required"
                return@setOnClickListener
            }

            if (!EmailHelper.isValidEmail(binding.email.text.trim().toString())) {
                binding.email.error = "Invalid email"
                return@setOnClickListener
            }

            EmailHelper.sendDynamicEmail(
                context = requireContext(),
                recipientEmail = binding.email.text.toString(),
                username = binding.name.text.toString(), {
                    if (it) {
                        dismissDialog()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to send email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).remove(this).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(p: Product, bitmap: Bitmap) = EmailFragment(p, bitmap)
    }
}