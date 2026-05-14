package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ServiceDetailViewModel : ViewModel() {

    private val repository = AppRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _serviceDetail = MutableLiveData<ServicePost?>()
    val serviceDetail: LiveData<ServicePost?> = _serviceDetail

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> = _isBookmarked

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    // ✅ চ্যাট আইডি অবজার্ভ করার জন্য নতুন লাইভ ডাটা
    private val _chatIdResult = MutableLiveData<String?>()
    val chatIdResult: LiveData<String?> = _chatIdResult

    // Load service data by ID
    fun loadService(serviceId: String) {
        viewModelScope.launch {
            try {
                val service = repository.getServiceById(serviceId)
                _serviceDetail.value = service

                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val bookmarks = repository.getBookmarkedIds(uid)
                    _isBookmarked.value = bookmarks.contains(serviceId)
                }
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    // Toggle bookmark state in Firestore
    fun toggleBookmark(serviceId: String) {
        val currentStatus = _isBookmarked.value ?: false

        viewModelScope.launch {
            try {
                val newState = repository.toggleBookmark(serviceId, currentStatus)
                _isBookmarked.value = newState
                _toastMessage.value = if (newState) "Added to bookmarks" else "Removed from bookmarks"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to update bookmark"
            }
        }
    }

    // ✅ চ্যাট বাটন ক্লিক করলে ইউনিক আইডি জেনারেট করার লজিক
    fun onMessageClick(providerId: String, providerName: String) {
        repository.getOrCreateChatId(providerId, providerName) { chatId ->
            _chatIdResult.postValue(chatId)
        }
    }

    // চ্যাট আইডি রিসেট করার ফাংশন (যাতে বারবার নেভিগেট না হয়)
    fun resetChatId() {
        _chatIdResult.value = null
    }

    fun clearToast() { _toastMessage.value = null }
}