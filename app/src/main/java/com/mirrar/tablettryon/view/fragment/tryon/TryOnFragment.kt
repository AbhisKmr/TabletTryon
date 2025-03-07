package com.mirrar.tablettryon.view.fragment.tryon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FragmentTryOnBinding
import com.mirrar.tablettryon.view.fragment.DialogLikeFragment
import com.mirrar.tablettryon.view.fragment.EmailFragment
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.bookmark.YouBookmarkFragment
import com.mirrar.tablettryon.view.fragment.tryon.adapter.ProductAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel

class TryOnFragment : Fragment() {

    private var _binding: FragmentTryOnBinding? = null
    private val binding get() = _binding!!

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
            openFragment(ProductDetailsFragment.newInstance())
        }

        binding.email.setOnClickListener {
            openFragment(EmailFragment.newInstance())
        }

        binding.cardView2.setOnClickListener {
            openFragment(YouBookmarkFragment.newInstance())
        }

        val adapter = ProductAdapter{

        }

        binding.productRecycler.adapter = adapter

        viewModel.product.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }

        viewModel.getData()
    }

    private fun openFragment(fr: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
        transaction.add(R.id.container, fr)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}