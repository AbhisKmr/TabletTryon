package com.mirrar.tablettryon.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentProductDetailsBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.tools.faceDetector.mlkit.CardSLideAdapter
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.HelperFunctions.getImageUrlFromProduct
import com.mirrar.tablettryon.view.fragment.email.EmailPopupFragment

class ProductDetailsFragment(
    private val product: Product,
    val onTryOn: () -> Unit
) :
    DialogFragment() {

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
            onTryOn()
            dismissDialog()
        }

//        val url = getImageUrlFromProduct(product)

//        Glide.with(requireContext()).load(url).into(binding.thumb)

        binding.productDetailsLayout.brand.text = product.brand
        binding.productDetailsLayout.productCode.text = product.localItemCode
        binding.productDetailsLayout.productPrice.text =
            "${product.currency} ${product.priceDutyFree}"

        setSeeMoreFunctionality(
            binding.productDetailsLayout.textView9,
            binding.productDetailsLayout.textView10,
            product.description
        )

        binding.productDetailsLayout.wishlist.setOnClickListener {
            Bookmarks.addToBookmark(product)
        }

        Bookmarks.bookmarks.observe(viewLifecycleOwner) {
            if (it == null) {
                return@observe
            }
            updateHeartIcon(it)
        }

        binding.email.setOnClickListener {
            openDialogFragment(EmailPopupFragment.newInstance("product-details", product))
        }
//        6534509

        val url = mutableListOf<String>()
        if (product.imageUrlBase != null) {
            url.add(product.imageUrlBase)
        }

        if (product.triedOnUrl != null) {
            url.add(product.triedOnUrl)
        }

        if (product.imageSmall != null) {
            url.add(product.imageSmall)
        }

        if (product.imageThumbnail != null) {
            url.add(product.imageThumbnail)
        }

        if (product.imageExtra1 != null) {
            url.add(product.imageExtra1)
        }

        if (product.imageExtra2 != null) {
            url.add(product.imageExtra2)
        }

//        if (product.asset2DUrl != null) {
//            url.add(product.asset2DUrl)
//        }

        binding.viewPager.adapter =
            CardSLideAdapter(url.toTypedArray())

        TabLayoutMediator(binding.tablayout, binding.viewPager) { tab, position -> }.attach()
    }

    private fun setSeeMoreFunctionality(
        textView: TextView,
        seeMoreButton: TextView,
        fullText: String
    ) {

        textView.text = fullText
//        textView.maxLines = 3
//        textView.ellipsize = android.text.TextUtils.TruncateAt.END

        seeMoreButton.setOnClickListener {
            if (textView.maxLines == 3) {
                textView.maxLines = Int.MAX_VALUE
                seeMoreButton.text = "See Less"
            } else {
                textView.maxLines = 3
                seeMoreButton.text = "See More"
            }
        }
        isTextEllipsized(textView) {
            seeMoreButton.isVisible = it
        }
    }

    private fun isTextEllipsized(textView: TextView, callback: (Boolean) -> Unit) {
        textView.post {
            val layout = textView.layout
            if (layout != null) {
                val lineCount = layout.lineCount
                if (lineCount > 0) {
                    val ellipsisCount = layout.getEllipsisCount(lineCount - 1)
                    callback(ellipsisCount > 0)
                } else {
                    callback(false)
                }
            } else {
                callback(false)
            }
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
        fun newInstance(p: Product, listener: () -> Unit) =
            ProductDetailsFragment(p, listener)
    }

    private fun openDialogFragment(fragment: DialogFragment) {
        fragment.show(childFragmentManager, fragment.tag)
    }
}