package com.example.serviceapp.View

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serviceapp.Adapter.PhotoPreviewAdapter
import com.example.serviceapp.R
import com.example.serviceapp.ViewModel.PostResult
import com.example.serviceapp.ViewModel.PostServiceViewModel
import com.example.serviceapp.databinding.ActivityPostServiceScreenBinding

class PostServiceScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPostServiceScreenBinding
    private val viewModel: PostServiceViewModel by viewModels()
    private lateinit var photoAdapter: PhotoPreviewAdapter

    // Gallery theke image select korar launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addPhoto(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostServiceScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategorySpinner()
        setupPhotoRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupCategorySpinner() {
        // ViewModel theke category names niye adapter set kora
        val categories = listOf("Select Category") + viewModel.categories
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position > 0) {
                        viewModel.category.value = categories[position]
                    } else {
                        viewModel.category.value = ""
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun setupPhotoRecyclerView() {
        photoAdapter = PhotoPreviewAdapter { uri ->
            viewModel.removePhoto(uri)
        }
        binding.rvPhotos.adapter = photoAdapter
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener { finish() }

        // Offer / Need Toggle
        binding.btnOffer.setOnClickListener { viewModel.setIsOffering(true) }
        binding.btnNeed.setOnClickListener { viewModel.setIsOffering(false) }

        // Add Photo click
        binding.btnAddPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Post Now click
        binding.btnPostNow.setOnClickListener {
            // EditText-er data ViewModel-e sync kora
            viewModel.title.value = binding.etTitle.text.toString()
            viewModel.description.value = binding.etDescription.text.toString()
            viewModel.price.value = binding.etPrice.text.toString()
            viewModel.location.value = binding.etLocation.text.toString()

            viewModel.clearError()
            viewModel.submitPost()
        }
    }

    private fun observeViewModel() {
        // Toggle Button-er UI update
        viewModel.isOffering.observe(this) { isOffering ->
            if (isOffering) {
                binding.btnOffer.setBackgroundResource(R.drawable.bg_toggle_active)
                binding.btnOffer.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnNeed.setBackgroundResource(R.drawable.bg_toggle_inactive)
                binding.btnNeed.setTextColor(ContextCompat.getColor(this, R.color.primary))
            } else {
                binding.btnNeed.setBackgroundResource(R.drawable.bg_toggle_active)
                binding.btnNeed.setTextColor(ContextCompat.getColor(this, R.color.white))
                binding.btnOffer.setBackgroundResource(R.drawable.bg_toggle_inactive)
                binding.btnOffer.setTextColor(ContextCompat.getColor(this, R.color.primary))
            }
        }

        // Photo list update
        viewModel.photoUris.observe(this) { uris ->
            photoAdapter.submitList(uris)
            binding.rvPhotos.visibility = if (uris.isEmpty()) View.GONE else View.VISIBLE
        }

        // Validation Error handle
        viewModel.validationError.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Submission Result observe
        viewModel.postResult.observe(this) { result ->
            when (result) {
                is PostResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnPostNow.isEnabled = false
                    binding.btnPostNow.text = "Posting..."
                }

                is PostResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Post Created Successfully!", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                is PostResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnPostNow.isEnabled = true
                    binding.btnPostNow.text = "Post Now"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }

                is PostResult.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
