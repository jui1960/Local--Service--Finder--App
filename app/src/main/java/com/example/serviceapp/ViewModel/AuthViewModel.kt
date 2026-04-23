package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    // Function name-ti authenticate theke performAuthAction kora holo
    fun performAuthAction() {
        _isProcessing.value = true

        // Simulated delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _isProcessing.value = false
            _authSuccess.value = true
        }, 1000)
    }
}