package com.example.serviceapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.databinding.ActivityHomeScreenBinding

class HomeActivity : AppCompatActivity() {

    // View Binding variable
    private lateinit var binding: ActivityHomeScreenBinding

    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate binding
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
        setupCategories()
        setupServices()
        setupBottomNav()
    }

    private fun initListeners() {
        // Hero button using binding
        binding.btnExploreNow.setOnClickListener {
            Toast.makeText(this, "Explore clicked", Toast.LENGTH_SHORT).show()
        }

        // See All links using binding
        binding.tvSeeAllCategories.setOnClickListener {
            Toast.makeText(this, "See All Categories", Toast.LENGTH_SHORT).show()
        }

        binding.tvSeeAllServices.setOnClickListener {
            Toast.makeText(this, "See All Services", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategories() {
        val categories = listOf(
            Category(1, "Cleaning",    R.drawable.cleaning),
            Category(2, "Plumbing",    R.drawable.ic_plumbing),
            Category(3, "Electrician", R.drawable.electrician),
            Category(4, "Carpentry",   R.drawable.img_4),
            Category(5, "AC Repair",   R.drawable.ac),
            Category(6, "Gardening",   R.drawable.gurden),
            Category(7, "Painting",    R.drawable.painting),
            Category(8, "More",        R.drawable.more)
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            Toast.makeText(this, "Category: ${category.name}", Toast.LENGTH_SHORT).show()
        }

        // Using binding for rvCategories
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun setupServices() {
        val services = listOf(
            ServicePost(1, "Professional House Cleaning", "John Doe", 4.8f, 120, 500, R.drawable.img3, true),
            ServicePost(2, "Need a Plumber", "Alex Smith", 4.6f, 80, 700, R.drawable.img2, true),
            ServicePost(3, "Need Electrician", "Sarah Ahmed", 4.7f, 60, 1000, R.drawable.img_2, false),
            ServicePost(4, "Carpentry Work", "Mike Johnson", 4.9f, 150, 1500, R.drawable.img_3, true)
        )

        serviceAdapter = ServiceAdapter(
            onItemClick = { service ->
                Toast.makeText(this, "Opening: ${service.title}", Toast.LENGTH_SHORT).show()
            },
            onBookmarkClick = { service ->
                Toast.makeText(this, "Bookmarked: ${service.title}", Toast.LENGTH_SHORT).show()
            }
        )

        // Using binding for rvServices
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = serviceAdapter
            isNestedScrollingEnabled = false
        }

        serviceAdapter.submitList(services)
    }

    private fun setupBottomNav() {
        // Accessing tabs directly through binding
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