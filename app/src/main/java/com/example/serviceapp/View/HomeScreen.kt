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
        setupEdgeToEdge()

        initListeners()
        setupAdapters()
        observeViewModel()   // ← only called once
        setupBottomNav()

        binding.tabProfile.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }
    }

    private fun setupEdgeToEdge() {
        // 🎨 Status/Nav bar icon color dark kora
        val windowInsetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())

            // 🟢 Top Bar Padding (Status Bar height adjust)
            binding.topBar.setPadding(
                binding.topBar.paddingLeft,
                systemBars.top,
                binding.topBar.paddingRight,
                binding.topBar.paddingBottom
            )

            // 🔴 Bottom Nav Padding (System Nav height adjust)
            binding.bottomNav.setPadding(
                binding.bottomNav.paddingLeft,
                binding.bottomNav.paddingTop,
                binding.bottomNav.paddingRight,
                systemBars.bottom
            )

          

            insets
        }
    }    private fun observeViewModel() {

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
       /* binding.etSearch.setOnClickListener {
            val intent = Intent(this, Search::class.java)
            startActivity(intent)
        }*/
        binding.etSearch.isFocusable = false
        binding.etSearch.isClickable = true

        binding.btnExploreNow.setOnClickListener {
            val intent = Intent(this, Search::class.java)
            startActivity(intent)
        }

        binding.tvSeeAllCategories.setOnClickListener {
            val intent = Intent(this, Search::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNav() {

        binding.tabHome.setOnClickListener {
            // Already on Home
        }

        binding.tabExplore.setOnClickListener {
            startActivity(Intent(this, Search::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.tabPost.setOnClickListener {
            val intent = Intent(this, PostServiceScreen::class.java)
            postServiceLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.tabChats.setOnClickListener {
            Toast.makeText(this, "Chats coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tabProfile.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }


}
