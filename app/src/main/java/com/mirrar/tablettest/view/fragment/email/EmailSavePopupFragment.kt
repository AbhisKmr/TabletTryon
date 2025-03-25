package com.mirrar.tablettest.view.fragment.email

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.mirrar.tablettest.R
import com.mirrar.tablettest.databinding.FragmentEmailPopupBinding
import com.mirrar.tablettest.utility.AppConstraint.userEmail
import com.mirrar.tablettest.utility.AppConstraint.userName

class EmailSavePopupFragment(val close: () -> Unit) : DialogFragment() {

    private var _binding: FragmentEmailPopupBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentEmailPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.saveProgress.isVisible = true
//        binding.sendTv.text = ""

        binding.close.setOnClickListener { dismissDialog() }

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

            userEmail = binding.email.text.toString()
            userName = binding.name.text.toString()
            dismissDialog()
        }
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .remove(this)
            .commit()
        close()
    }

    fun update() {
        try {
            binding.saveProgress.isVisible = false
            binding.sendTv.text = "Save"
        } catch (e: Exception){
            e.message?.toString()?.let { Log.e("update", it) }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(close: () -> Unit) = EmailSavePopupFragment(close)
    }
}