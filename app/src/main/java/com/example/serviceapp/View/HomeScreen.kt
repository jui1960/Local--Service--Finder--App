package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.CategoryAdapter
import com.example.serviceapp.Adapter.ServiceAdapter
import com.example.serviceapp.ViewModel.HomeViewModel
import com.example.serviceapp.databinding.ActivityHomeScreenBinding

class HomeScreen : AppCompatActivity() {

    // View Binding variable
    private lateinit var binding: ActivityHomeScreenBinding

    // Adapters
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    // ViewModel (MVVM Part)
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate binding
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        setupAdapters()
        observeViewModel()
        setupBottomNav()
    }

    private fun observeViewModel() {
        // ViewModel theke Category data observe kora
        viewModel.categories.observe(this) { categories ->
            categoryAdapter = CategoryAdapter(categories) { category ->
                Toast.makeText(this, "Category: ${category.name}", Toast.LENGTH_SHORT).show()
            }
            binding.rvCategories.adapter = categoryAdapter
        }

        // ViewModel theke Service data observe kora
        viewModel.services.observe(this) { services ->
            serviceAdapter.submitList(services)
        }
    }

    private fun setupAdapters() {
        // Service RecyclerView Setup
        serviceAdapter = ServiceAdapter(
            onItemClick = { service ->
                // Ekhane Details Screen-e jabar intent likhte hobe
                val intent = Intent(this, ServiceDetailsActivity::class.java)
                intent.putExtra(ServiceDetailsActivity.EXTRA_SERVICE_ID, service.id)
                startActivity(intent)
            },
            onBookmarkClick = { service ->
                Toast.makeText(this, "Bookmarked: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen)
            adapter = serviceAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun initListeners() {
        // Hero button
        binding.btnExploreNow.setOnClickListener {
            Toast.makeText(this, "Explore clicked", Toast.LENGTH_SHORT).show()
        }

        // See All links
        binding.tvSeeAllCategories.setOnClickListener {
            Toast.makeText(this, "See All Categories", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllServices.setOnClickListener {
            Toast.makeText(this, "See All Services", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        binding.tabHome.setOnClickListener {
            // Already on Home
        }

        binding.tabExplore.setOnClickListener {
            Toast.makeText(this, "Explore", Toast.LENGTH_SHORT).show()
        }

        binding.tabPost.setOnClickListener {
            Toast.makeText(this, "Post a Service", Toast.LENGTH_SHORT).show()
        }

        binding.tabChats.setOnClickListener {
            Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show()
        }

        binding.tabProfile.setOnClickListener {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
        }
    }
}