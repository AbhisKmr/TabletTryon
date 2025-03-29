package com.mirrar.tablettest.view.fragment.selfie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.mirrar.tablettest.R
import com.mirrar.tablettest.databinding.FragmentSelfieBinding
import com.mirrar.tablettest.utility.AppConstraint.recommendationModel
import com.mirrar.tablettest.utility.AppConstraint.userEmail
import com.mirrar.tablettest.utility.AppConstraint.userName
import com.mirrar.tablettest.utility.Bookmarks
import com.mirrar.tablettest.utility.HelperFunctions
import com.mirrar.tablettest.view.activity.MainActivity
import com.mirrar.tablettest.view.fragment.email.EmailHelper
import com.mirrar.tablettest.view.fragment.selfie.adapter.SelfieAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelfieFragment : DialogFragment() {

    private var _binding: FragmentSelfieBinding? = null
    private val binding get() = _binding!!
    private val scrollOffset = 100

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelfieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardView11.visibility =
            if (recommendationModel?.recommendations?.isEmpty() == true) View.VISIBLE else View.INVISIBLE
        binding.linearLayout.visibility =
            if (recommendationModel?.recommendations?.isEmpty() == true) View.VISIBLE else View.INVISIBLE
        binding.imageRecycler.isVisible = recommendationModel?.recommendations?.isNotEmpty() == true

        binding.imageRecycler.adapter =
            SelfieAdapter(recommendationModel?.recommendations ?: emptyList())
        binding.closeView.setOnClickListener {
            dismissDialog()
        }

        binding.imageView2.setOnClickListener {
            dismissDialog()
        }

        try {
            binding.imageRecycler.postDelayed({
                binding.imageRecycler.smoothScrollBy(scrollOffset, 0)
            }, 500)


            binding.imageRecycler.postDelayed({
                binding.imageRecycler.smoothScrollBy(-scrollOffset, 0)
            }, 1000)
        } catch (e: Exception) {
            Log.e("Exception", "onViewCreated: ${e.localizedMessage}", )
        }


        binding.finish.setOnClickListener {
            Bookmarks.clearAll()
            userEmail = null
            userName = null
            startActivity(Intent(requireActivity(), MainActivity::class.java))
            requireActivity().finish()
//            findNavController().popBackStack(R.id.homeFragment, false)
        }

        if (userEmail != null) {
            binding.email.setText(userEmail)
        }

        if (userName != null) {
            binding.name.setText(userName)
        }

        binding.send.setOnClickListener {

            if (binding.name.text?.trim()?.isEmpty() == true) {
                binding.name.error = "Required"
                return@setOnClickListener
            }

            if (binding.email.text?.trim()?.isEmpty() == true) {
                binding.email.error = "Required"
                return@setOnClickListener
            }

            if (!EmailHelper.isValidEmail(binding.email.text?.trim().toString())) {
                binding.email.error = "Invalid email"
                return@setOnClickListener
            }

            binding.sendProgress.isVisible = true
            binding.sendTv.text = ""
            EmailHelper.sendDynamicEmail(
                requireContext(),
                "selfie"
            ) {
                binding.sendProgress.isVisible = false
                binding.sendTv.text = "Send"
                if (it != null) {
                    requireActivity().finish()
//                    dismissDialog()
                } else {
                    Toast.makeText(
                        requireContext(), "Failed to send email.", Toast.LENGTH_SHORT
                    ).apply {
                        setGravity(Gravity.CENTER, 0, 0)
                    }.show()
                }
            }
        }

        binding.qrLoader.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            val b =
                HelperFunctions.generateQRCode("https://glass-recommendations.mirrar.com/${recommendationModel?.uuid ?: ""}")
//                        val b = HelperFunctions.generateQRCode(it.url)
            if (b != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        binding.qrLoader.isVisible = false
                        binding.imageView4.setImageBitmap(b)
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).remove(this).commit()
    }

    companion object {
        @JvmStatic
        fun newInstance() = SelfieFragment()
    }
}