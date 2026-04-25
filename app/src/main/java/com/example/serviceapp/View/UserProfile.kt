package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.serviceapp.ViewModel.ProfileViewModel
import com.example.serviceapp.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupListeners()
        observeViewModel()

    }

    private fun setupListeners() {
        // Menu options
        binding.menuMyPosts.setOnClickListener {
            // TODO: navigate to MyPostsActivity
            Toast.makeText(this, "My Posts", Toast.LENGTH_SHORT).show()
        }

        binding.menuSavedPosts.setOnClickListener {
            // TODO: navigate to SavedPostsActivity
            Toast.makeText(this, "Saved Posts", Toast.LENGTH_SHORT).show()
        }

        binding.menuReviews.setOnClickListener {
            Toast.makeText(this, "Reviews", Toast.LENGTH_SHORT).show()
        }

        binding.menuSettings.setOnClickListener {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }

        // 3-dot menu → edit profile
        binding.btnMore.setOnClickListener {
            Toast.makeText(this, "Edit Profile", Toast.LENGTH_SHORT).show()
        }

        // Sign out with confirmation dialog
        // Sign out listener change korun:
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

        // Bottom nav
        binding.tabHome.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        binding.tabPost.setOnClickListener {
            startActivity(Intent(this, PostServiceScreen::class.java))
        }

        binding.tabChats.setOnClickListener {
            Toast.makeText(this, "Chats coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {

        // Profile data
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

            // Skills chips — add programmatically
            binding.chipGroupSkills.removeAllViews()
            user.skills.forEach { skill ->
                val chip = com.google.android.material.chip.Chip(this)
                chip.text = skill
                chip.isCheckable = false
                chip.setChipBackgroundColorResource(com.example.serviceapp.R.color.chip_bg)
                chip.setTextColor(getColor(com.example.serviceapp.R.color.primary))
                binding.chipGroupSkills.addView(chip)
            }

            // Profile image
            if (user.profileImageUrl.isNotBlank()) {
                Glide.with(this)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(com.example.serviceapp.R.drawable.person1)
                    .into(binding.ivProfileImage)
            } else {
                // Show initials if no image
                binding.tvInitials.visibility = View.VISIBLE
                binding.ivProfileImage.visibility = View.INVISIBLE
                binding.tvInitials.text = getInitials(user.fullName)
            }
        }

        // Loading
        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        // Error
        viewModel.errorMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Sign out → go to LoginActivity
        viewModel.signOutEvent.observe(this) { signedOut ->
            if (signedOut) {
                val intent = Intent(this, LogInScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────
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