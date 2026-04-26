package com.example.serviceapp.View

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.serviceapp.Model.SearchSortOrder
import com.example.serviceapp.R
import com.example.serviceapp.databinding.ActivityFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FilterBottomSheet(
    private val categories: List<String>,
    private val currentCategory: String?,
    private val currentMaxPrice: Int?,
    private val currentSort: SearchSortOrder,
    private val onApply: (category: String?, maxPrice: Int?, isOffering: Boolean?, sort: SearchSortOrder) -> Unit,
    private val onClear: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: ActivityFilterBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Category spinner
        val catOptions = listOf("Select Category") + categories
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, catOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        currentCategory?.let { cat ->
            val idx = catOptions.indexOf(cat)
            if (idx >= 0) binding.spinnerCategory.setSelection(idx)
        }

        // Price range slider
        binding.sliderPrice.value = (currentMaxPrice ?: 5000).toFloat()
        binding.tvPriceValue.text = "৳ ${binding.sliderPrice.value.toInt()}"
        binding.sliderPrice.addOnChangeListener { _, value, _ ->
            binding.tvPriceValue.text = "৳ ${value.toInt()}"
        }

        // Sort radio buttons — pre-select current
        when (currentSort) {
            SearchSortOrder.NEWEST     -> binding.rbNewest.isChecked = true
            SearchSortOrder.POPULAR    -> binding.rbPopular.isChecked = true
            SearchSortOrder.PRICE_LOW  -> binding.rbPriceLow.isChecked = true
            SearchSortOrder.PRICE_HIGH -> binding.rbPriceHigh.isChecked = true
        }

        // Apply button
        binding.btnApplyFilters.setOnClickListener {
            val selectedCat = binding.spinnerCategory.selectedItem.toString()
                .takeIf { it != "Select Category" }
            val maxPrice = binding.sliderPrice.value.toInt().takeIf { it < 5000 }
            val sort = when (binding.rgSort.checkedRadioButtonId) {
                binding.rbPopular.id   -> SearchSortOrder.POPULAR
                binding.rbPriceLow.id  -> SearchSortOrder.PRICE_LOW
                binding.rbPriceHigh.id -> SearchSortOrder.PRICE_HIGH
                else                   -> SearchSortOrder.NEWEST
            }
            onApply(selectedCat, maxPrice, null, sort)
            dismiss()
        }

        // Clear all
        binding.tvClearAll.setOnClickListener {
            onClear()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}