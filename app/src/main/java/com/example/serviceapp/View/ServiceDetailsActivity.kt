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
                // ১. ইমেজ লোডিং
                ivServiceImage.setImageResource(service.imageRes)

                // ২. প্রোভাইডার নাম ও ইমেল লজিক
                val name = service.providerName
                tvProviderName.text = if (name == "Unknown" || name.isEmpty()) {
                    service.providerEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                } else {
                    name
                }
                tvProviderEmail.text = service.providerEmail

                // ৩. ফোন নাম্বার লজিক (নিশ্চিত করুন এই অংশটি ঠিক আছে)
                if (!service.phone.isNullOrEmpty()) {
                    tvPhone.text = service.phone
                    phoneContainer.visibility = android.view.View.VISIBLE
                } else {
                    tvPhone.text = "Not Available"
                    // phoneContainer.visibility = android.view.View.GONE
                }

                // ৪. ব্যাজ সেটআপ
                if (service.isOffering) {
                    tvBadge.text = "Offering Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_offering)
                    tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_offering_text))
                } else {
                    tvBadge.text = "Looking for Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_looking)
                    tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_looking_text))
                }

                // ৫. অন্যান্য তথ্য
                tvServiceTitle.text = service.title
                tvRating.text = service.rating.toString()
                tvReviewCount.text = "(${service.reviewCount} reviews)"
                tvPrice.text = "৳ ${service.price}"
                tvDescription.text = service.description
                tvLocation.text = service.location

                // ৬. স্কিল চিপস
                chipGroupSkills.removeAllViews()
                service.skills.forEach { skill ->
                    val chip = layoutInflater.inflate(R.layout.item_skill_chip, chipGroupSkills, false)
                    // chip.findViewById<TextView>(R.id.tvSkillTag).text = skill
                    chipGroupSkills.addView(chip)
                }
            }
        }

        // ৭. বুকমার্ক এবং ৮. টোস্ট অবজার্ভার আগের মতোই থাকবে...
        viewModel.isBookmarked.observe(this) { bookmarked ->
            binding.ivBookmark.setImageResource(
                if (bookmarked) R.drawable.bookmark else R.drawable.bookmark2
            )
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
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
