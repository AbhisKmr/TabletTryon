package com.mirrar.tablettryon.view.fragment.tryon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.DialogLikeFragment
import com.mirrar.tablettryon.view.fragment.EmailFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel

class TryOnFragment : Fragment() {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

    private var selectedProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTryOnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.catalogue.setOnClickListener {
            findNavController().navigate(R.id.action_tryOnFragment_to_catalogueFragment)
        }

        binding.details.setOnClickListener {
            if (selectedProduct != null) {
                openFragment(ProductDetailsFragment.newInstance(selectedProduct!!))
            }
        }

        binding.email.setOnClickListener {
            openFragment(EmailFragment.newInstance())
        }

        binding.cardView2.setOnClickListener {
            openFragment(YouBookmarkFragment.newInstance())
        }

        binding.wishlist.setOnClickListener {
            if (selectedProduct != null) {
                Bookmarks.addToBookmark(selectedProduct!!)
            }
        }

        val adapter = ProductAdapter {
            selectedProduct = it
            updateHeartIcon(Bookmarks.getBookmarks())
        }

        binding.productRecycler.adapter = adapter

        viewModel.product.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        Bookmarks.bookmarks.observe(viewLifecycleOwner) { bookmarkedProducts ->
            if (bookmarkedProducts == null) {
                return@observe
            }

            updateHeartIcon(bookmarkedProducts)
            binding.bookmarkCount.text = "${bookmarkedProducts.size}"
        }

        viewModel.getData()
    }

    private fun updateHeartIcon(list: List<Product>) {
        val drawable = if (list.contains(selectedProduct)) {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_red)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_gray)
        }

        binding.wishlist.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    private fun openFragment(fr: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
        transaction.add(R.id.container, fr)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}