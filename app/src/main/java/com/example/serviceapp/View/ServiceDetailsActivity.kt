package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serviceapp.R
import com.example.serviceapp.ViewModel.ServiceDetailViewModel
import com.example.serviceapp.databinding.ActivityServiceDetailsBinding

class ServiceDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SERVICE_ID = "extra_service_id"
    }

    private lateinit var binding: ActivityServiceDetailsBinding
    private val viewModel: ServiceDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServiceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get service ID passed from HomeScreen or SearchActivity
        val serviceId = intent.getStringExtra(EXTRA_SERVICE_ID)
        if (serviceId.isNullOrEmpty()) {
            Toast.makeText(this, "Service ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadService(serviceId)

        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {

        // Observe service detail data
        viewModel.serviceDetail.observe(this) { service ->
            if (service == null) return@observe

            binding.apply {
                // Image
                ivServiceImage.setImageResource(service.imageRes)

                // Badge (Offering / Looking)
                if (service.isOffering) {
                    tvBadge.text = "Offering Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_offering)
                    tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_offering_text))
                } else {
                    tvBadge.text = "Looking for Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_looking)
                    tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_looking_text))
                }

                // Title
                tvServiceTitle.text = service.title

                // Provider info
                tvProviderName.text = service.providerName
                tvProviderUsername.text = service.providerUsername
                tvRating.text = service.rating.toString()
                tvReviewCount.text = "(${service.reviewCount} reviews)"

                // Price
                tvPrice.text = "৳ ${service.price}"

                // Description
                tvDescription.text = service.description

                // Location
                tvLocation.text = service.location

                // Phone
                tvPhone.text = service.phone

                // Skills chips — dynamically add chips
                chipGroupSkills.removeAllViews()
                service.skills.forEach { skill ->
                    val chip = layoutInflater.inflate(
                        R.layout.item_skill_chip, chipGroupSkills, false
                    )
                    // If using Material Chip:
                    // val chip = Chip(this@ServiceDetailsActivity)
                    // chip.text = skill
                    // chip.isCheckable = false
                    chipGroupSkills.addView(chip)
                }
            }
        }

        // Observe bookmark toggle
        viewModel.isBookmarked.observe(this) { bookmarked ->
            binding.ivBookmark.setImageResource(
                if (bookmarked) R.drawable.bookmark else R.drawable.bookmark2
            )
        }

        // Observe toast messages
        viewModel.toastMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Share button
        binding.btnShare.setOnClickListener {
            val service = viewModel.serviceDetail.value ?: return@setOnClickListener
            val shareText = "${service.title}\nProvider: ${service.providerName}\nPrice: ৳${service.price}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Service"))
        }

        // Bookmark button

        binding.ivBookmark.setOnClickListener {
            val serviceId = intent.getStringExtra(EXTRA_SERVICE_ID) ?: ""
            if (serviceId.isNotEmpty()) {
                viewModel.toggleBookmark(serviceId)
            }
        }

        // Message button
        binding.btnMessage.setOnClickListener {
            viewModel.onMessageClick()

        }
    }
}
