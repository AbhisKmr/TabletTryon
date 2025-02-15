package com.mirrar.tablettryon.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R


class DialogLikeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_alert_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val check = view.findViewById<CheckBox>(R.id.checkbox)

        view.findViewById<View>(R.id.closeView).setOnClickListener {
            dismissDialog()
        }

        view.findViewById<View>(R.id.imageView2).setOnClickListener {
            dismissDialog()
        }

        enterTransition = android.transition.Fade()
        exitTransition = android.transition.Fade()

        view.findViewById<Button>(R.id.button).setOnClickListener {
            if (!check.isChecked) {
                check.error = "Please accept the terms to proceed."
                return@setOnClickListener
            }

            findNavController().navigate(R.id.cameraFragment)
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