package com.example.serviceapp.View

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.CategoryAdapter
import com.example.serviceapp.Adapter.ServiceAdapter
import com.example.serviceapp.ViewModel.HomeViewModel
import com.example.serviceapp.databinding.ActivityHomeScreenBinding

class HomeScreen : AppCompatActivity() {

    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: HomeViewModel by viewModels()

    private val postServiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Your service has been posted!", Toast.LENGTH_LONG).show()
            // No manual refresh needed — Firestore SnapshotListener auto-updates the list
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        setupAdapters()
        observeViewModel()   // ← only called once
        setupBottomNav()

        binding.tabProfile.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
    }


    // ─── SINGLE observeViewModel (Firebase version) ───────────
    private fun observeViewModel() {

        // Categories
        viewModel.categories.observe(this) { categories ->
            categoryAdapter = CategoryAdapter(categories) { category ->
                Toast.makeText(this, "Category: ${category.name}", Toast.LENGTH_SHORT).show()
            }
            binding.rvCategories.adapter = categoryAdapter
        }

        // Services — auto-updated by Firestore SnapshotListener
        viewModel.services.observe(this) { services ->
            serviceAdapter.submitList(services)
            binding.tvEmptyState.visibility =
                if (services.isEmpty()) View.VISIBLE else View.GONE
        }

        // Loading spinner
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        // Error message
        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupAdapters() {
        serviceAdapter = ServiceAdapter(
            onItemClick = { service ->
                val intent = Intent(this, ServiceDetailsActivity::class.java)
                intent.putExtra(ServiceDetailsActivity.EXTRA_SERVICE_ID, service.id) // String ID
                startActivity(intent)
            },
            onBookmarkClick = { service ->
                Toast.makeText(this, "Bookmarked: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen)
            adapter = serviceAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun initListeners() {
        binding.btnExploreNow.setOnClickListener { /* Explore logic */ }
        binding.tvSeeAllCategories.setOnClickListener { /* See all categories */ }
        binding.tvSeeAllServices.setOnClickListener { /* See all services */ }
    }

    private fun setupBottomNav() {
        binding.tabHome.setOnClickListener { /* Already on Home */ }

        binding.tabExplore.setOnClickListener {
            Toast.makeText(this, "Explore Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.tabPost.setOnClickListener {
            val intent = Intent(this, PostServiceScreen::class.java)
            postServiceLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.tabChats.setOnClickListener {
            Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show()
        }

        binding.tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }
    }


}
