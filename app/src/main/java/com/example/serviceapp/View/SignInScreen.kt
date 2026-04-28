package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serviceapp.R
import com.example.serviceapp.ViewModel.AuthViewModel
import com.example.serviceapp.databinding.ActivitySignInScreenBinding

class SignInScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignInScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupListeners()
        setupPasswordStrengthWatcher()


        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val name = binding.etFullName.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                authViewModel.registerUser(email, password, name,phone)
            }
        }
    }

    private fun observeViewModel() {
        authViewModel.isProcessing.observe(this) { processing ->
            binding.btnRegister.isEnabled = !processing
            binding.btnRegister.text = if (processing) "Creating account..." else "Register"
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

        binding.btnRegister.setOnClickListener {
            if (validateInputs()) authViewModel.performAuthAction()
        }

        binding.tvLogin.setOnClickListener { finish() }



    }

    private fun setupPasswordStrengthWatcher() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s.toString())
            }
        })
    }

    private fun updatePasswordStrength(password: String) {
        when {
            password.isEmpty() -> {
                binding.tvPasswordStrength.text = ""
            }
            password.length < 6 -> {
                binding.tvPasswordStrength.text = "● Weak password"
                binding.tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_weak))
            }
            password.length < 10 || !password.any { it.isDigit() } -> {
                binding.tvPasswordStrength.text = "●● Fair — add numbers or symbols"
                binding.tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_fair))
            }
            password.any { it.isDigit() } && password.any { it.isUpperCase() } &&
                    password.any { !it.isLetterOrDigit() } -> {
                binding.tvPasswordStrength.text = "●●● Strong password"
                binding.tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_strong))
            }
            else -> {
                binding.tvPasswordStrength.text = "●● Good password"
                binding.tvPasswordStrength.setTextColor(ContextCompat.getColor(this, R.color.strength_good))
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            binding.etFullName.error = "Full name is required"
            binding.etFullName.requestFocus()
            return false
        }
        if (name.length < 2) {
            binding.etFullName.error = "Enter a valid name"
            binding.etFullName.requestFocus()
            return false
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            binding.etEmail.requestFocus()
            return false
        }
        if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches() || phone.length < 10) {
            binding.etPhone.error = "Valid phone number is required"
            binding.etPhone.requestFocus()
            return false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
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