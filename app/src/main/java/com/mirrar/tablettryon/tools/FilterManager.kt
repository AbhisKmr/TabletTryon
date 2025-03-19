package com.mirrar.tablettryon.tools

import android.annotation.SuppressLint
import android.widget.RadioButton
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

@SuppressLint("NotifyDataSetChanged")
class FilterManager(
    val binding: FilterNavLayoutBinding,
    productViewModel: ProductViewModel,
    val applyFilter: (sortingOrder: String, min: Float, max: Float, brandList: List<String>) -> Unit
) {

    private var minPrince = 0f
    private var maxPrince = 10000f
    private val filterDataModels = mutableListOf<FilterDataModel>()
    private var filterListAdapter: FilterListAdapter = FilterListAdapter(filterDataModels) {}

    init {
        binding.recyclerDropdownBrand.options.adapter = filterListAdapter
        binding.sortbyDropdown.radioGroup.check(R.id.radioOption1)

        updateRange(minPrince, maxPrince)

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
            val firstRadioButton = binding.sortbyDropdown.radioGroup.getChildAt(0) as? RadioButton
            firstRadioButton?.let { binding.sortbyDropdown.radioGroup.check(it.id) }
            filterDataModels.forEach { it.isSelected = false }
            filterListAdapter.notifyDataSetChanged()
            minPrince = 0f
            maxPrince = 10000f
            updateRange(minPrince, maxPrince)
            applyFilter("low_to_high", minPrince, maxPrince, emptyList())
            productViewModel.fetchProduct()
        }

        binding.priceRange.priceRange.addOnChangeListener { slider, value, fromUser ->
            minPrince = slider.values[0]
            maxPrince = slider.values[1]
            binding.priceRange.min.text = "Min: CHF${slider.values[0].toInt()}"
            binding.priceRange.max.text = "Max: CHF${slider.values[1].toInt()}"
        }

        binding.apply.setOnClickListener {
            val selectedIndex = binding.sortbyDropdown.radioGroup.indexOfChild(
                binding.root.findViewById(binding.sortbyDropdown.radioGroup.checkedRadioButtonId)
            )

            val sorting = if (selectedIndex == 1) "high_to_low" else "low_to_high"

            binding.applyProgress.isVisible = true
            binding.apply.text = ""

            val lst = filterDataModels.mapNotNull { if (it.isSelected) it.value else null }

            if (maxPrince == 0f) {
                maxPrince = 1000f
            }
            productViewModel.fetchProduct(
                sortingOrder = sorting,
                min = minPrince.toInt(),
                max = maxPrince.toInt(),
                brands = lst
            )
            applyFilter(sorting, minPrince, maxPrince, lst)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBrandList(filterDataModels: List<FilterDataModel>) {
        this.filterDataModels.clear()
        this.filterDataModels.addAll(filterDataModels)

        filterListAdapter.notifyDataSetChanged()
    }

    fun updateRange(min: Float, max: Float) {
        binding.priceRange.priceRange.valueFrom = min
        binding.priceRange.priceRange.valueTo = max
        binding.priceRange.priceRange.values =
            listOf(min, max)
        // Set the limit range
        binding.priceRange.min.text = "Min: CHF${min}"
        binding.priceRange.max.text = "Max: CHF${max}"
    }

}