package com.mirrar.tablettryon.view.fragment.email

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentEmailPopupBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.utility.AppConstraint.userEmail
import com.mirrar.tablettryon.utility.AppConstraint.userName
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.email.dataModel.emailApi.Object
import com.mirrar.tablettryon.view.fragment.email.dataModel.emailApi.SendEmailApiRequest

class EmailPopupFragment(val emailTag: String, val p: Product? = null) : DialogFragment() {

    private var _binding: FragmentEmailPopupBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.close.setOnClickListener { dismissDialog() }

        if (userEmail != null) {
            binding.email.setText(userEmail)
        }

        if (userName != null) {
            binding.name.setText(userName)
        }

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

            val objs = if (p == null) {
                Bookmarks.getBookmarks().map {
                    Object(
                        it.brand,
                        it.imageUrlBase!!,
                        it.priceDutyFree.toInt(),
                        it.productUrl ?: "",
                        ""
                    )
                }
            } else {
                listOf(
                    Object(
                        p.brand, p.imageUrlBase!!, p.priceDutyFree.toInt(), p.productUrl ?: "", ""
                    )
                )
            }

            userEmail = binding.email.text.toString()
            userName = binding.name.text.toString()

            EmailHelper.sendDynamicEmail(SendEmailApiRequest(
                binding.email.text.toString(), binding.name.text.toString(), objs, emailTag
            ), {
                if (it != null) {
                    dismissDialog()
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to send email.", Toast.LENGTH_SHORT
                    ).show()
                }
            })

//            EmailHelper.sendDynamicEmail(
//                context = requireContext(),
//                recipientEmail = binding.email.text.toString(),
//                username = binding.name.text.toString(), {
//                    if (it) {
//                        dismissDialog()
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Failed to send email.",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            )
        }
    }

    private fun dismissDialog() {
        try {
            parentFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).remove(this).commit()
        } catch (e: Exception) {

        }
    }


    companion object {
        @JvmStatic
        fun newInstance(emailTag: String, p: Product? = null) = EmailPopupFragment(emailTag, p)
    }
}