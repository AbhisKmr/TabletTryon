package com.mirrar.tablettryon.view.fragment.catalogue

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentCatalogueBinding
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.CatalogueProductAdapter
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterChipAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel


class CatalogueFragment : Fragment() {

    private var _binding: FragmentCatalogueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.filterChipRecycler.adapter = FilterChipAdapter()
        val adapter = CatalogueProductAdapter { i, p ->

        }

        binding.productRecycler.adapter = adapter

        viewModel.product.observe(viewLifecycleOwner) { products ->
            products.forEach { product ->
                product.isBookmarked = Bookmarks.getBookmarks().contains(product)
            }
            adapter.updateData(products)
        }

        viewModel.getData()
    }
}