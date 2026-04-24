package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Eti add korun
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository
import kotlinx.coroutines.launch // Eti add korun

class ServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _serviceDetail = MutableLiveData<ServicePost?>()
    val serviceDetail: LiveData<ServicePost?> = _serviceDetail

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    // Load service data by ID
    // Note: serviceId-ke String-e convert korun jodi Firestore bebohar koren
    fun loadService(serviceId: String) {
        // 🔥 ERROR FIX: Coroutine scope shuru korun
        viewModelScope.launch {
            try {
                val service = repository.getServiceById(serviceId)
                _serviceDetail.value = service
                _isBookmarked.value = service?.isBookmarked ?: false
            } catch (e: Exception) {
                _toastMessage.value = "Error loading service: ${e.message}"
            }
        }
    }

    // Toggle bookmark state
    fun toggleBookmark() {
        val current = _isBookmarked.value ?: false
        _isBookmarked.value = !current
        val msg = if (!current) "Added to bookmarks" else "Removed from bookmarks"
        _toastMessage.value = msg
    }

    fun onMessageClick() {
        _toastMessage.value = "Opening chat..."
    }
}