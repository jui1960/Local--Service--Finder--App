package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Model.UserProfile
import com.example.serviceapp.Repository.AppRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repository = AppRepository()
    private val auth = FirebaseAuth.getInstance()
    private var postsListener: ListenerRegistration? = null

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _userPosts = MutableLiveData<List<ServicePost>>(emptyList())
    val userPosts: LiveData<List<ServicePost>> = _userPosts

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _signOutEvent = MutableLiveData(false)
    val signOutEvent: LiveData<Boolean> = _signOutEvent

    // Currently logged-in user UID
    val currentUid: String? get() = auth.currentUser?.uid

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = currentUid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            val profile = repository.getUserProfile(uid)
            _profile.value = profile
            _isLoading.value = false
        }

        // Listen to user's own posts in realtime
        postsListener?.remove()
        postsListener = repository.listenToUserPosts(
            uid = uid,
            onUpdate = { posts ->
                _userPosts.value = posts
            },
            onError = { e ->
                _errorMessage.value = e.message
            }
        )
    }

    fun signOut() {
        repository.signOut()
        _signOutEvent.value = true
    }

    fun clearError() { _errorMessage.value = null }

    override fun onCleared() {
        super.onCleared()
        postsListener?.remove()
    }
}