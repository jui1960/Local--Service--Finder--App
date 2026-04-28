package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.SearchHistoryAdapter
import com.example.serviceapp.Adapter.ServiceAdapter
import com.example.serviceapp.ViewModel.SearchViewModel
import com.example.serviceapp.databinding.ActivitySearchBinding

class Search : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter
    private lateinit var historyManager: SearchHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyManager = SearchHistoryManager(this)

        setupHistoryRecyclerView()
        setupResultsRecyclerView()
        setupSearchBar()
        setupListeners()
        observeViewModel()

        // Load history on open
        refreshHistory()
    }

    // ── History RecyclerView ──────────────────────────────────
    private fun setupHistoryRecyclerView() {
        historyAdapter = SearchHistoryAdapter(
            onItemClick = { query ->
                // Tap a history item → fill search bar and search
                binding.etSearch.setText(query)
                binding.etSearch.setSelection(query.length)
                doSearch(query)
            },
            onRemoveClick = { query ->
                // ✅ Remove single item
                historyManager.removeSearch(query)
                refreshHistory()
            }
        )
        binding.rvSearchHistory.apply {
            layoutManager = LinearLayoutManager(this@Search)
            adapter = historyAdapter
        }
    }

    // ── Results RecyclerView ──────────────────────────────────
    private fun setupResultsRecyclerView() {
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
        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@Search)
            adapter = serviceAdapter
        }
    }

    // ── Search bar setup ──────────────────────────────────────
    private fun setupSearchBar() {
        binding.etSearch.requestFocus()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val text = s.toString()

                // Show/hide clear X button
                binding.btnClearText.visibility =
                    if (text.isNotBlank()) View.VISIBLE else View.GONE

                if (text.isBlank()) {
                    // Back to recent/category view
                    showRecentView()
                    viewModel.onQueryChanged("")
                } else {
                    viewModel.onQueryChanged(text)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Press Enter/Search on keyboard → save to history
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                if (query.isNotBlank()) doSearch(query)
                true
            } else false
        }
    }

    // ── Listeners ─────────────────────────────────────────────
    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvCancel.setOnClickListener {
            finish()
        }

        // ✅ Clear X inside search bar — clears text only
        binding.btnClearText.setOnClickListener {
            binding.etSearch.setText("")
            showRecentView()
        }

        // ✅ Clear All history button
        binding.tvClearHistory.setOnClickListener {
            historyManager.clearAll()
            refreshHistory()
        }

        // Filter bottom sheet
        binding.btnFilter.setOnClickListener {
            FilterBottomSheet(
                categories = viewModel.categories,
                currentCategory = viewModel.filterCategory.value,
                currentMaxPrice = viewModel.filterMaxPrice.value,
                currentSort = viewModel.sortOrder.value!!,
                onApply = { cat, price, offering, sort ->
                    viewModel.applyFilters(cat, price, offering, sort)
                    showResultsView()
                },
                onClear = {
                    viewModel.clearFilters()
                }
            ).show(supportFragmentManager, "FilterBottomSheet")
        }

        // Category chips → pre-fill search
        binding.catCleaning.setOnClickListener { searchByCategory("Cleaning") }
        binding.catPlumbing.setOnClickListener { searchByCategory("Plumbing") }
        binding.catElectrician.setOnClickListener { searchByCategory("Electrician") }
        binding.catCarpentry.setOnClickListener { searchByCategory("Carpentry") }
        binding.catAcRepair.setOnClickListener { searchByCategory("AC Repair") }
        binding.catGardening.setOnClickListener { searchByCategory("Gardening") }
        binding.catPainting.setOnClickListener { searchByCategory("Painting") }
        binding.catMore.setOnClickListener { searchByCategory("More") }
    }

    // ── ViewModel observers ───────────────────────────────────
    private fun observeViewModel() {

        viewModel.showResults.observe(this) { show ->
            if (show) showResultsView() else showRecentView()
        }

        viewModel.results.observe(this) { results ->
            serviceAdapter.submitList(results)
            binding.tvResultCount.text =
                "${results.size} result${if (results.size != 1) "s" else ""} found"
            binding.tvResultCount.visibility = View.VISIBLE

            binding.layoutEmpty.visibility =
                if (results.isEmpty() && viewModel.showResults.value == true)
                    View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    // Do a search and save to history
    private fun doSearch(query: String) {
        historyManager.addSearch(query)   // ✅ Save to history
        refreshHistory()
        viewModel.onQueryChanged(query)
        showResultsView()
    }

    // Search by category tap
    private fun searchByCategory(category: String) {
        binding.etSearch.setText(category)
        binding.etSearch.setSelection(category.length)
        doSearch(category)
    }

    // Reload history list from SharedPreferences
    private fun refreshHistory() {
        val history = historyManager.getHistory()
        historyAdapter.submitList(history)

        // Show "No recent searches" if empty
        binding.tvNoHistory.visibility =
            if (history.isEmpty()) View.VISIBLE else View.GONE
        binding.tvClearHistory.visibility =
            if (history.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun showRecentView() {
        binding.layoutRecent.visibility = View.VISIBLE
        binding.rvResults.visibility = View.GONE
        binding.tvResultCount.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
    }

    private fun showResultsView() {
        binding.layoutRecent.visibility = View.GONE
        binding.rvResults.visibility = View.VISIBLE
    }
}