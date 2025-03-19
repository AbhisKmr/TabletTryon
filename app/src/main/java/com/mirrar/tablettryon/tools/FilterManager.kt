package com.mirrar.tablettryon.tools

import androidx.core.view.isVisible
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FilterNavLayoutBinding
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.tools.filter.FilterDataModel
import com.mirrar.tablettryon.utility.HelperFunctions.rotateImage
import com.mirrar.tablettryon.view.fragment.catalogue.adapter.FilterListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilterManager(
    val binding: FilterNavLayoutBinding,
    productViewModel: ProductViewModel,
    val applyFilter: (sortingOrder: String, min: Float, max: Float, brandList: List<String>) -> Unit
) {

    private val filterDataModels = mutableListOf<FilterDataModel>()

    init {

        binding.sortbyDropdown.radioGroup.check(R.id.radioOption1)

        binding.sortbyDropdown.clickView.setOnClickListener {
            val vis = binding.sortbyDropdown.radioGroup.isVisible
            binding.sortbyDropdown.radioGroup.isVisible = !vis

            CoroutineScope(Dispatchers.Main).launch {
                rotateImage(
                    binding.sortbyDropdown.dropArrow,
                    if (!vis) 180f else 0f,
                    0f,
                )
            }
        }

        binding.priceRange.clickView.setOnClickListener {
            val vis = binding.priceRange.optionParent.isVisible
            binding.priceRange.optionParent.isVisible = !vis

            rotateImage(
                binding.priceRange.dropArrow,
                if (!vis) 180f else 0f,
                0f,
            )
        }

        binding.recyclerDropdownBrand.clickView.setOnClickListener {
            val vis = binding.recyclerDropdownBrand.optionParent.isVisible
            binding.recyclerDropdownBrand.optionParent.isVisible = !vis

            rotateImage(
                binding.recyclerDropdownBrand.dropArrow,
                if (!vis) 180f else 0f,
                0f,
            )
        }

        binding.reset.setOnClickListener {
            applyFilter("low_to_high", 0f, 0f, emptyList())
            productViewModel.fetchProduct()
        }

        binding.apply.setOnClickListener {
            val selectedIndex = binding.sortbyDropdown.radioGroup.indexOfChild(
                binding.root.findViewById(binding.sortbyDropdown.radioGroup.checkedRadioButtonId)
            )

            val sorting = if (selectedIndex == 1) "high_to_low" else "low_to_high"

            binding.applyProgress.isVisible = true
            binding.apply.text = ""

            val lst = filterDataModels.mapNotNull { if (it.isSelected) it.value else null }

            productViewModel.fetchProduct(sortingOrder = sorting, brands = lst)
            applyFilter(sorting, 0f, 0f, lst)
        }
    }

    fun updateBrandList(filterDataModels: List<FilterDataModel>) {
        this.filterDataModels.clear()
        this.filterDataModels.addAll(filterDataModels)

        val ad = FilterListAdapter(filterDataModels) {

        }
        binding.recyclerDropdownBrand.options.adapter = ad
    }
}