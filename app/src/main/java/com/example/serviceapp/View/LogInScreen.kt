package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.serviceapp.R
import com.example.serviceapp.ViewModel.AuthViewModel
import com.example.serviceapp.databinding.ActivityLogInScreenBinding

class LogInScreen : AppCompatActivity() {

    private lateinit var binding: ActivityLogInScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupListeners()

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                authViewModel.loginUser(email, password)
            }
        }



    }

    private fun observeViewModel() {
        authViewModel.isProcessing.observe(this) { processing ->
            binding.btnLogin.isEnabled = !processing
            binding.btnLogin.text = if (processing) "Logging in..." else "Login"
        }

        authViewModel.authSuccess.observe(this) { success ->
            if (success) navigateToHome()
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val inputType = if (isPasswordVisible) {
                binding.ivTogglePassword.setImageResource(R.drawable.eye2)
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.ivTogglePassword.setImageResource(R.drawable.eye)
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            binding.etPassword.inputType = inputType
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnLogin.setOnClickListener {
            if (validateInputs()) authViewModel.performAuthAction()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, SignInScreen::class.java))
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        // Baki social buttons (Google, FB, Apple) er click listener ager moto thakbe
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email required"
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }
        return true
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}