package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.example.serviceapp.ViewModel.ProfileViewModel
import com.example.serviceapp.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Edge-to-edge enable kora
        enableEdgeToEdge()

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Padding logic set kora
        setupEdgeToEdge()
        setupListeners()
        observeViewModel()
    }

    private fun setupEdgeToEdge() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
        windowInsetsController.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // 🟢 Fix Top Bar: Increase total height (56dp + status bar)
            binding.topBarBg.updateLayoutParams {
                val baseHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics).toInt()
                height = baseHeight + systemBars.top
            }
            // Move content down within the bar
            binding.topBarBg.setPadding(0, systemBars.top, 0, 0)

            // 🔴 Fix Bottom Nav: Increase total height (58dp + nav bar)
            binding.bottomNav.updateLayoutParams {
                val baseHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58f, resources.displayMetrics).toInt()
                height = baseHeight + systemBars.bottom
            }
            // Move icons up within the bar
            binding.bottomNav.setPadding(0, 0, 0, systemBars.bottom)

            insets
        }
    }

    private fun setupListeners() {
        binding.menuMyPosts.setOnClickListener {
            Toast.makeText(this, "My Posts", Toast.LENGTH_SHORT).show()
        }

        binding.menuSavedPosts.setOnClickListener {
            Toast.makeText(this, "Saved Posts", Toast.LENGTH_SHORT).show()
        }

        binding.menuReviews.setOnClickListener {
            Toast.makeText(this, "Reviews", Toast.LENGTH_SHORT).show()
        }

        binding.menuSettings.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }

        binding.btnMore.setOnClickListener {
            Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
        }

        binding.menuLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out") { _, _ ->
                    viewModel.signOut()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.tabHome.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.tabPost.setOnClickListener {
            startActivity(Intent(this, PostServiceScreen::class.java))
        }
        binding.tabExplore.setOnClickListener {
            val intent = Intent(this, Search::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

        }


        binding.tabChats.setOnClickListener {
            Toast.makeText(this, "Chats coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.profile.observe(this) { user ->
            if (user == null) return@observe

            binding.tvFullName.text = user.fullName
            binding.tvUsername.text = user.username
            binding.tvRating.text = "★ ${user.rating} (${user.reviewCount} reviews)"
            binding.tvBio.text = user.bio
            binding.tvPostCount.text = user.postCount.toString()
            binding.tvReviewCount.text = user.reviewCount.toString()
            binding.tvFollowerCount.text = formatCount(user.followerCount)
            binding.tvFollowingCount.text = user.followingCount.toString()

            binding.chipGroupSkills.removeAllViews()
            user.skills.forEach { skill ->
                val chip = com.google.android.material.chip.Chip(this)
                chip.text = skill
                chip.isCheckable = false
                chip.setChipBackgroundColorResource(com.example.serviceapp.R.color.chip_bg)
                chip.setTextColor(getColor(com.example.serviceapp.R.color.primary))
                binding.chipGroupSkills.addView(chip)
            }
            viewModel.userPosts.observe(this) { posts ->
                if (posts != null) {
                    // ডাটাবেসে যতগুলো পোস্ট আছে তার সংখ্যা এখানে বসবে
                    binding.tvPostCount.text = posts.size.toString()
                }
            }

            if (user.profileImageUrl.isNotBlank()) {
                binding.tvInitials.visibility = View.GONE
                binding.ivProfileImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(com.example.serviceapp.R.drawable.person1)
                    .into(binding.ivProfileImage)
            } else {
                binding.tvInitials.visibility = View.VISIBLE
                binding.ivProfileImage.visibility = View.INVISIBLE
                binding.tvInitials.text = getInitials(user.fullName)
            }
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

        viewModel.signOutEvent.observe(this) { signedOut ->
            if (signedOut) {
                val intent = Intent(this, LogInScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getInitials(name: String): String {
        return name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    private fun formatCount(n: Int): String {
        return if (n >= 1000) "%.1fK".format(n / 1000.0) else n.toString()
    }
}