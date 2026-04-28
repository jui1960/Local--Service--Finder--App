package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceapp.Repository.AppRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val repository = AppRepository()




    // Function name-ti authenticate theke performAuthAction kora holo
    fun performAuthAction() {
        _isProcessing.value = true

        // Simulated delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isProcessing.value = false
            _authSuccess.value = true
        }, 1000)
    }

    fun registerUser(email: String, pass: String, fullName: String,phone: String) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val success = repository.signUpWithEmail(email, pass, fullName, phone)
                _authSuccess.value = success
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isProcessing.value = false
            }
        }
    }

    // Login Function
    fun loginUser(email: String, pass: String) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                val success = repository.signInWithEmail(email, pass)
                _authSuccess.value = success
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

}