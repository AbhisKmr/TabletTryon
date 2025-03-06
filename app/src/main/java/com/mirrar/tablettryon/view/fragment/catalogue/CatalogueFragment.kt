package com.mirrar.tablettryon.view.fragment.catalogue

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentCatalogueBinding
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.CatalogueProductAdapter
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterChipAdapter


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

        binding.close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.filterChipRecycler.adapter = FilterChipAdapter()
        binding.productRecycler.adapter = CatalogueProductAdapter()
    }
}