package com.mirrar.tablettryon.view.fragment.catalogue

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mirrar.tablettryon.databinding.FragmentCatalogueBinding
import com.mirrar.tablettryon.utility.AppConstraint.filterTryOn
import com.mirrar.tablettryon.utility.AppConstraint.priceMax
import com.mirrar.tablettryon.utility.AppConstraint.priceMin
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.utility.HelperFunctions.rotateImage
import com.mirrar.tablettryon.view.fragment.ProductDetailsFragment
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.CatalogueProductAdapter
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterChipAdapter
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterListAdapter
import com.mirrar.tablettryon.view.fragment.tryon.viewModel.AlgoliaViewModel


class CatalogueFragment : Fragment() {

    private var _binding: FragmentCatalogueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider.create(this)[AlgoliaViewModel::class.java]

        binding.close.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.filterNavLayout.recyclerDropdownBrand.title.text = "Brand"
        binding.filter.setOnClickListener {
            binding.drawerLayout.elevation = 100f
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                binding.drawerLayout.elevation = 100f
            }

            override fun onDrawerOpened(drawerView: View) {
                binding.drawerLayout.elevation = 100f
            }

            override fun onDrawerClosed(drawerView: View) {
                binding.drawerLayout.elevation = 0f
            }

            override fun onDrawerStateChanged(newState: Int) {

            }
        })

        binding.filterChipRecycler.adapter = FilterChipAdapter()
        val adapter = CatalogueProductAdapter { _, p ->
            ProductDetailsFragment.newInstance(p, {
                filterTryOn = p
                Handler().postDelayed({
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }, 500)
            }).show(childFragmentManager, "ProductDetailsFragment")
        }

        binding.productRecycler.adapter = adapter

        viewModel.product.observe(viewLifecycleOwner) { products ->
            products.forEach { product ->
                product.isBookmarked = Bookmarks.getBookmarks().contains(product)
            }
            adapter.updateData(products)

            binding.filterNavLayout.applyProgress.isVisible = false
            binding.filterNavLayout.apply.text = "Apply"
            binding.drawerLayout.closeDrawers()
        }

        binding.filterNavLayout.sortbyDropdown.dropArrow.setOnClickListener {
            val vis = binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible
            binding.filterNavLayout.sortbyDropdown.radioGroup.isVisible = !vis

            rotateImage(
                binding.filterNavLayout.sortbyDropdown.dropArrow,
                if (!vis) 180f else 0f,
                0f,
            )
        }

        updateRange(priceMin!!, priceMax!!, priceMin!!, priceMax!!)

        binding.filterNavLayout.priceRange.priceRange.addOnChangeListener { slider, _, _ ->
            binding.filterNavLayout.priceRange.min.text = "Min: CHF${slider.values[0].toInt()}"
            binding.filterNavLayout.priceRange.max.text = "Max: CHF${slider.values[1].toInt()}"
            priceMin = slider.values[0]
            priceMax = slider.values[1]
        }

        viewModel.filter.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {

                binding.filterNavLayout.apply.setOnClickListener { v ->
                    val selectedIndex =
                        binding.filterNavLayout.sortbyDropdown.radioGroup.indexOfChild(
                            view.findViewById(binding.filterNavLayout.sortbyDropdown.radioGroup.checkedRadioButtonId)
                        )

                    if (it.none { iii -> iii.isSelected } && selectedIndex < 0) {
                        Toast.makeText(
                            requireContext(), "Please select filter first", Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    binding.filterNavLayout.applyProgress.isVisible = true
                    binding.filterNavLayout.apply.text = ""

                    Handler().postDelayed({
                        viewModel.fetchProducts(true, binding.progressBar,  it, selectedIndex)
                    }, 500)
                }

                binding.filterNavLayout.priceRange.dropArrow.setOnClickListener {
                    val vis = binding.filterNavLayout.priceRange.optionParent.isVisible
                    binding.filterNavLayout.priceRange.optionParent.isVisible = !vis

                    rotateImage(
                        binding.filterNavLayout.priceRange.dropArrow,
                        if (!vis) 180f else 0f,
                        0f,
                    )
                }

                binding.filterNavLayout.recyclerDropdownBrand.dropArrow.setOnClickListener {
                    val vis = binding.filterNavLayout.recyclerDropdownBrand.optionParent.isVisible
                    binding.filterNavLayout.recyclerDropdownBrand.optionParent.isVisible = !vis

                    rotateImage(
                        binding.filterNavLayout.recyclerDropdownBrand.dropArrow,
                        if (!vis) 180f else 0f,
                        0f,
                    )
                }
                val ad = FilterListAdapter(it) {

                }
                binding.filterNavLayout.recyclerDropdownBrand.options.adapter = ad


                binding.filterNavLayout.reset.setOnClickListener { v ->
                    it.forEach { pp -> pp.isSelected = false }
                    binding.filterNavLayout.sortbyDropdown.radioGroup.clearCheck()
                    priceMin = 0f
                    priceMax = 1000f
                    updateRange(priceMin!!, priceMax!!, priceMin!!, priceMax!!)

                    viewModel.fetchProducts(true, binding.progressBar,  it)
                    ad.notifyDataSetChanged()
                }

            }
        }

        viewModel.onlyRecommendation()
        viewModel.fetchAllBrands()
    }

    private fun updateRange(min: Float, max: Float, minValue: Float, maxValue: Float) {
        binding.filterNavLayout.priceRange.priceRange.valueFrom = min
        binding.filterNavLayout.priceRange.priceRange.valueTo = max
        binding.filterNavLayout.priceRange.priceRange.values =
            listOf(minValue, maxValue) // Set the initial range
        binding.filterNavLayout.priceRange.min.text = "Min: CHF${minValue.toInt()}"
        binding.filterNavLayout.priceRange.max.text = "Max: CHF${maxValue.toInt()}"
    }

    companion object {
        @JvmStatic
        fun newInstance() = CatalogueFragment()
    }
}