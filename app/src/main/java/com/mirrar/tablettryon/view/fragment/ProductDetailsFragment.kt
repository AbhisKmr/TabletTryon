package com.mirrar.tablettryon.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentProductDetailsBinding
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.HelperFunctions.getImageUrlFromProduct
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

class ProductDetailsFragment(private val product: Product) : DialogFragment() {

    private var _binding: FragmentProductDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.tryOnBtn.setOnClickListener {
            dismissDialog()
        }

        val url = getImageUrlFromProduct(product)

        Glide.with(requireContext()).load(url).into(binding.thumb)

        binding.productDetailsLayout.brand.text = product.brand
        binding.productDetailsLayout.productCode.text = product.localItemCode
        binding.productDetailsLayout.productPrice.text =
            "${product.currency} ${product.priceDutyFree}"
        binding.productDetailsLayout.textView9.text = product.description

        binding.productDetailsLayout.wishlist.setOnClickListener {
            Bookmarks.addToBookmark(product)
        }

        Bookmarks.bookmarks.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            updateHeartIcon(it)
        }
    }

    private fun updateHeartIcon(list: List<Product>) {
        val drawable = if (list.contains(product)) {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_red)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_gray)
        }

        binding.productDetailsLayout.wishlist.setImageDrawable(drawable)
    }

    private fun dismissDialog() {
        parentFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
            .remove(this)
            .commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(p: Product) = ProductDetailsFragment(p)
    }
}