package com.mirrar.tablettryon.tools

import android.annotation.SuppressLint
import android.widget.RadioButton
import androidx.core.view.isVisible
import com.mirrar.tablettryon.databinding.FilterNavLayoutBinding
import com.mirrar.tablettryon.products.viewModel.ProductViewModel
import com.mirrar.tablettryon.tools.filter.FilterDataModel
import com.mirrar.tablettryon.utility.AppConstraint.IS_FILTER_APPLIED
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.utility.GlobalProducts
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

        updateRange(minPrince, maxPrince)

        binding.sortbyDropdown.clickView.setOnClickListener {
            val vis = binding.sortbyDropdown.radioGroup.isVisible
            binding.sortbyDropdown.radioGroup.isVisible = !vis

            CoroutineScope(Dispatchers.Main).launch {
                rotateImage(
                    binding.sortbyDropdown.dropArrow,
                    if (!vis) 180f else 0f,
                    if (vis) 180f else 0f,
                )
            }
        }

        binding.priceRange.clickView.setOnClickListener {
            val vis = binding.priceRange.optionParent.isVisible
            binding.priceRange.optionParent.isVisible = !vis

            rotateImage(
                binding.priceRange.dropArrow,
                if (!vis) 180f else 0f,
                if (vis) 180f else 0f,
            )
        }

        binding.recyclerDropdownBrand.clickView.setOnClickListener {
            val vis = binding.recyclerDropdownBrand.optionParent.isVisible
            binding.recyclerDropdownBrand.optionParent.isVisible = !vis

            rotateImage(
                binding.recyclerDropdownBrand.dropArrow,
                if (!vis) 180f else 0f,
                if (vis) 180f else 0f,
            )
        }

        binding.reset.setOnClickListener {
            binding.sortbyDropdown.radioGroup.clearCheck()
            filterDataModels.forEach { it.isSelected = false }
            filterListAdapter.notifyDataSetChanged()
            minPrince = 0f
            maxPrince = 10000f
            IS_FILTER_APPLIED = false
            updateRange(minPrince, maxPrince)
            applyFilter("low_to_high", minPrince, maxPrince, emptyList())
            GlobalProducts.updateProduct(recommendationModel?.recommendations ?: emptyList())
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

            val sorting = when (selectedIndex) {
                0 -> {
                    "low_to_high"
                }

                1 -> {
                    "high_to_low"
                }

                else -> {
                    "null"
                }
            }

            binding.applyProgress.isVisible = true
            binding.apply.text = ""

            val lst = filterDataModels.mapNotNull { if (it.isSelected) it.value else null }

            if (maxPrince == 0f) {
                maxPrince = 1000f
            }
            IS_FILTER_APPLIED = true
            productViewModel.filterProduct(
                sortingOrder = sorting,
                min = minPrince.toInt(),
                max = maxPrince.toInt(),
                brands = lst
            )
            GlobalProducts.clearAll()
            applyFilter(sorting, minPrince, maxPrince, lst)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateBrandList(filterDataModels: List<FilterDataModel>) {
        this.filterDataModels.clear()
        this.filterDataModels.addAll(filterDataModels)

        filterListAdapter.notifyDataSetChanged()
    }

    fun updatePreselection(
        sortingOrder: String,
        currentPage: Int,
        minPrice: Int,
        maxPrice: Int,
        brandList: MutableList<String>
    ) {
        this.minPrince = minPrice.toFloat()
        this.maxPrince = maxPrice.toFloat()
        updateRange(minPrince, maxPrince)

        this.filterListAdapter.updateSelection(brandList)

        val sorting = when (sortingOrder) {
            "low_to_high" -> {
                0
            }

            "high_to_low" -> {
                1
            }

            else -> {
                -1
            }
        }

        selectRadioButtonByIndex(sorting)
    }

    private fun selectRadioButtonByIndex(index: Int) {
        if (index in 0 until binding.sortbyDropdown.radioGroup.childCount) {
            val radioButton = binding.sortbyDropdown.radioGroup.getChildAt(index) as? RadioButton
            radioButton?.let {
                binding.sortbyDropdown.radioGroup.check(it.id)
            }
        }
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