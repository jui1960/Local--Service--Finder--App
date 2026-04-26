package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.ServiceAdapter
import com.example.serviceapp.ViewModel.SearchViewModel
import com.example.serviceapp.databinding.ActivitySearchBinding

class Search : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var serviceAdapter: ServiceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupAdapters()
        setupSearchBar()
        setupListeners()
        observeViewModel()
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@Search)
            adapter = serviceAdapter
        }

    }

    private fun setupAdapters() {
        serviceAdapter = ServiceAdapter(
            onItemClick = { service ->
                val intent = Intent(this, ServiceDetailsActivity::class.java)
                intent.putExtra(ServiceDetailsActivity.EXTRA_SERVICE_ID, service.id)
                startActivity(intent)
            },
            onBookmarkClick = { service ->
                Toast.makeText(this, "Bookmarked: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

    }


    private fun setupSearchBar() {
        // Auto-focus on open
        binding.etSearch.requestFocus()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                viewModel.onQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Cancel button
        binding.tvCancel.setOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Filter icon → open bottom sheet
        binding.btnFilter.setOnClickListener {
            FilterBottomSheet(
                categories = viewModel.categories,
                currentCategory = viewModel.filterCategory.value,
                currentMaxPrice = viewModel.filterMaxPrice.value,
                currentSort = viewModel.sortOrder.value!!,
                onApply = { cat, price, offering, sort ->
                    viewModel.applyFilters(cat, price, offering, sort)
                },
                onClear = {
                    viewModel.clearFilters()
                }
            ).show(supportFragmentManager, "FilterBottomSheet")
        }
    }

    private fun observeViewModel() {
        // Show/hide empty state vs results vs recent
        viewModel.showResults.observe(this) { show ->
            binding.layoutRecent.visibility = if (!show) View.VISIBLE else View.GONE
            binding.rvResults.visibility = if (show) View.VISIBLE else View.GONE
            binding.tvEmptyState.visibility = View.GONE
        }

        // Results list
        viewModel.results.observe(this) { results ->
            serviceAdapter.submitList(results)
            binding.tvResultCount.text =
                "${results.size} result${if (results.size != 1) "s" else ""} found"
            binding.tvEmptyState.visibility =
                if (results.isEmpty() && viewModel.showResults.value == true)
                    View.VISIBLE else View.GONE
        }

        // Loading
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Errors
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }
}