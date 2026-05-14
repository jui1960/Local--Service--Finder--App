package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serviceapp.Model.ServicePost
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
        viewModel.serviceDetail.observe(this) { service ->
            service?.let { updateUI(it) }
        }


        viewModel.chatIdResult.observe(this) { chatId ->
            chatId?.let { id ->
                val service = viewModel.serviceDetail.value ?: return@observe

                val intent = Intent(this, Chat::class.java).apply {
                    putExtra("CHAT_ID", id)
                    putExtra("OTHER_USER_ID", service.providerId)
                    putExtra("OTHER_USER_NAME", service.providerName)
                }
                startActivity(intent)
                viewModel.resetChatId()
            }
        }
        viewModel.isBookmarked.observe(this) { bookmarked ->
            binding.ivBookmark.setImageResource(
                if (bookmarked) R.drawable.bookmark else R.drawable.bookmark2
            )
        }

        viewModel.toastMessage.observe(this) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearToast()
            }
        }
    }

    private fun updateUI(service: ServicePost) {
        binding.apply {
            // সার্ভিস টাইটেল ও বর্ণনা
            tvServiceTitle.text = service.title
            tvDescription.text = service.description
            tvPrice.text = "৳ ${service.price}"
            tvLocation.text = service.location

            // প্রোভাইডার ইনফো
            val name = service.providerName
            tvProviderName.text = if (name == "Unknown" || name.isEmpty()) {
                service.providerEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            } else {
                name
            }
            tvProviderEmail.text = service.providerEmail

            // ফোন নাম্বার লজিক
            if (!service.phone.isNullOrEmpty()) {
                tvPhone.text = service.phone
                phoneContainer.visibility = View.VISIBLE
            } else {
                phoneContainer.visibility = View.GONE
            }

            // ব্যাজ (Offering vs Looking)
            if (service.isOffering) {
                tvBadge.text = "Offering Service"
                tvBadge.setBackgroundResource(R.drawable.bg_badge_offering)
                tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_offering_text))
            } else {
                tvBadge.text = "Looking for Service"
                tvBadge.setBackgroundResource(R.drawable.bg_badge_looking)
                tvBadge.setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.badge_looking_text))
            }

            // স্কিল চিপস
            chipGroupSkills.removeAllViews()
            service.skills.forEach { skill ->
                val chip = layoutInflater.inflate(R.layout.item_skill_chip, chipGroupSkills, false)
                // chip.findViewById<TextView>(R.id.tvSkillTag).text = skill
                chipGroupSkills.addView(chip)
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // বুকমার্ক বাটন
        binding.ivBookmark.setOnClickListener {
            val serviceId = intent.getStringExtra(EXTRA_SERVICE_ID) ?: ""
            viewModel.toggleBookmark(serviceId)
        }

        // মেসেজ বাটন - চ্যাট শুরু করা
        binding.btnMessage.setOnClickListener {
            val service = viewModel.serviceDetail.value ?: return@setOnClickListener
            viewModel.onMessageClick(service.providerId, service.providerName)
        }

        // শেয়ার বাটন
        binding.btnShare.setOnClickListener {
            val service = viewModel.serviceDetail.value ?: return@setOnClickListener
            val shareText = "${service.title}\nProvider: ${service.providerName}\nPrice: ৳${service.price}"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Service"))
        }
    }
}