package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository

class ServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()

    private val _serviceDetail = MutableLiveData<ServicePost?>()
    val serviceDetail: LiveData<ServicePost?> = _serviceDetail

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    // Load service data by ID
    fun loadService(serviceId: Int) {
        val service = repository.getServiceById(serviceId)
        _serviceDetail.value = service
        _isBookmarked.value = service?.isBookmarked ?: false
    }

    // Toggle bookmark state
    fun toggleBookmark() {
        val current = _isBookmarked.value ?: false
        _isBookmarked.value = !current
        val msg = if (!current) "Added to bookmarks" else "Removed from bookmarks"
        _toastMessage.value = msg
    }

    // Message button pressed
    fun onMessageClick() {
        _toastMessage.value = "Opening chat..."
        // In real app: navigate to ChatActivity with providerId
    }
}