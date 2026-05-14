package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository
import kotlinx.coroutines.launch

sealed class PostResult {
    object Idle : PostResult()
    object Loading : PostResult()
    data class Success(val post: ServicePost) : PostResult()
    data class Error(val message: String) : PostResult()
}

// ViewModel class extend kora dorkar
class PostServiceViewModel : ViewModel() {

    private val repository = AppRepository()

    // ── Form state ────────────────────────────────────────────
    val isOffering = MutableLiveData(true)
    val title = MutableLiveData("")
    val category = MutableLiveData("")
    val description = MutableLiveData("")
    val price = MutableLiveData("")
    val location = MutableLiveData("")

    val categories: List<String> = repository.getCategoryNames()

    private val _photoUris = MutableLiveData<List<String>>(emptyList())
    val photoUris: LiveData<List<String>> = _photoUris

    private val _postResult = MutableLiveData<PostResult>(PostResult.Idle)
    val postResult: LiveData<PostResult> = _postResult

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    // ── Actions ───────────────────────────────────────────────
    fun setIsOffering(offering: Boolean) {
        isOffering.value = offering
    }

    fun addPhoto(uri: String) {
        val list = _photoUris.value?.toMutableList() ?: mutableListOf()
        list.add(uri)
        _photoUris.value = list
    }

    fun removePhoto(uri: String) {
        val list = _photoUris.value?.toMutableList() ?: mutableListOf()
        list.remove(uri)
        _photoUris.value = list
    }

    // PostServiceViewModel.kt-er submitPost() function-ti update korun
    fun submitPost() {
        val t: String = title.value.orEmpty()
        val c: String = category.value.orEmpty()
        val d: String = description.value.orEmpty()
        val p: String = price.value.orEmpty()
        val l: String = location.value.orEmpty()

        // 1. Validation (Ekhon error reference thik hobe)
        val error: String? = repository.validatePost(t, c, d, p, l)
        if (error != null) {
            _validationError.value = error
            return
        }

        _postResult.value = PostResult.Loading

        viewModelScope.launch {
            try {
                val currentUris = _photoUris.value ?: emptyList()

                // 2. Upload Images
                val uploadedUrls: List<String> = if (currentUris.isNotEmpty()) {
                    repository.uploadImages(currentUris)
                } else {
                    emptyList()
                }

                // 3. Submit Post to Firestore
                val newPost = repository.submitPost(
                    title = t,
                    category = c,
                    description = d,
                    price = p.toInt(),
                    location = l,
                    isOffering = isOffering.value ?: true,
                    uploadedImageUrls = uploadedUrls
                )

                _postResult.value = PostResult.Success(newPost)
            } catch (e: Exception) {
                _postResult.value = PostResult.Error(e.message ?: "Submission failed")
            }
        }
    }
    fun clearError() {
        _validationError.value = null
    }

    fun resetResult() {
        _postResult.value = PostResult.Idle
    }
}