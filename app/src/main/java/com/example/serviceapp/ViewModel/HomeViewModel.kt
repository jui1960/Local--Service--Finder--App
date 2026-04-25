package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.serviceapp.Model.Category
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository
import com.google.firebase.firestore.ListenerRegistration

class HomeViewModel : ViewModel() {

    private val repository = AppRepository()

    // ListenerRegistration — must be removed in onCleared() to avoid memory leaks
    private var servicesListener: ListenerRegistration? = null

    // ── LiveData ──────────────────────────────────────────────
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _services = MutableLiveData<List<ServicePost>>()
    val services: LiveData<List<ServicePost>> = _services

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadCategories()
        startListeningToServices()
    }

    // ── Static categories (no Firebase needed) ────────────────
    private fun loadCategories() {
        _categories.value = repository.getCategories()
    }

    // ── Realtime Firestore listener ───────────────────────────
    // Every time a new post is added/updated/deleted in Firestore,
    // this automatically updates the Home Screen — no manual refresh!
    private fun startListeningToServices() {
        _isLoading.value = true

        servicesListener = repository.listenToServices(
            onUpdate = { posts ->
                _isLoading.value = false
                _services.value = posts
            },
            onError = { exception ->
                _isLoading.value = false
                _errorMessage.value = "Failed to load services: ${exception.message}"
            }
        )


    }



    // Call this to manually retry after an error
    fun retryLoading() {
        servicesListener?.remove()
        startListeningToServices()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // ── CRITICAL: Remove listener when ViewModel is destroyed ─
    // Without this, Firestore keeps sending updates even after
    // the screen is closed → memory leak!
    override fun onCleared() {
        super.onCleared()
        servicesListener?.remove()
    }
}
