package com.mirrar.tablettryon.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.database.ProductDataViewModel
import com.mirrar.tablettryon.database.ProductDatabase
import com.mirrar.tablettryon.database.ProductRepository
import com.mirrar.tablettryon.databinding.FragmentHomeBinding
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.utility.HelperFunctions.getActionBarSize
import com.mirrar.tablettryon.utility.HelperFunctions.getDisplaySize
import com.mirrar.tablettryon.utility.HelperFunctions.getNavigationBarHeight

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val database by lazy { ProductDatabase.getDatabase(requireContext()) }
    private val repository by lazy { ProductRepository(database.getProductDao()) }

    private val productViewModel: ProductDataViewModel by viewModels {
        ProductDataViewModel.ProductViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        productViewModel.downloadList()
//        characterViewModel.list.observe(viewLifecycleOwner) {
//            it.forEach { p ->
//                println(p.name)
//            }
//        }

        binding.button.setOnClickListener {
            val transaction = childFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_MATCH_ACTIVITY_OPEN)
            transaction.add(R.id.container, DialogLikeFragment.newInstance({
                requireActivity().onBackPressedDispatcher.onBackPressed()
                findNavController().navigate(R.id.cameraFragment)
            }))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.club.setOnClickListener {
            val dis = getDisplaySize(requireContext())
            val bar = getActionBarSize(requireContext())
            val nav = getNavigationBarHeight(requireContext())
            binding.reso.text = "w: ${dis.first} || h: ${dis.second + nav} || nav: $nav"
            //displayMetrics.widthPixels, displayMetrics.heightPixels
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}