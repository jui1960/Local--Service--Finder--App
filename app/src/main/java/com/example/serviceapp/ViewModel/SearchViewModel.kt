package com.example.serviceapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.serviceapp.Model.SearchSortOrder
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Repository.AppRepository
import com.google.firebase.firestore.ListenerRegistration

class SearchViewModel() : ViewModel() {


    private val repository = AppRepository()
    private var searchListener: ListenerRegistration? = null

    // ── Search state ──────────────────────────────────────────
    val searchQuery = MutableLiveData("")
    val filterCategory = MutableLiveData<String?>(null)
    val filterMaxPrice = MutableLiveData<Int?>(null)
    val filterOffering = MutableLiveData<Boolean?>(null)
    val sortOrder = MutableLiveData(SearchSortOrder.NEWEST)

    // ── Results ───────────────────────────────────────────────
    private val _results = MutableLiveData<List<ServicePost>>(emptyList())
    val results: LiveData<List<ServicePost>> = _results

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ── Show recent/categories or result list ─────────────────
    private val _showResults = MutableLiveData(false)
    val showResults: LiveData<Boolean> = _showResults

    val categories: List<String> = repository.getCategoryNames()

    // ── Search trigger ────────────────────────────────────────
    fun search() {
        val query = searchQuery.value.orEmpty()
        _showResults.value = query.isNotBlank() ||
                filterCategory.value != null ||
                filterMaxPrice.value != null

        searchListener?.remove()
        _isLoading.value = true

        searchListener = repository.searchServices(
            query = query,
            category = filterCategory.value,
            maxPrice = filterMaxPrice.value,
            isOffering = filterOffering.value,
            sortBy = sortOrder.value ?: SearchSortOrder.NEWEST,
            onUpdate = { posts ->
                _isLoading.value = false
                _results.value = posts
            },
            onError = { e ->
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        )
    }

    fun applyFilters(
        category: String?,
        maxPrice: Int?,
        isOffering: Boolean?,
        sort: SearchSortOrder
    ) {
        filterCategory.value = category
        filterMaxPrice.value = maxPrice
        filterOffering.value = isOffering
        sortOrder.value = sort
        search()
    }

    fun clearFilters() {
        filterCategory.value = null
        filterMaxPrice.value = null
        filterOffering.value = null
        sortOrder.value = SearchSortOrder.NEWEST
        search()
    }

    fun onQueryChanged(q: String) {
        searchQuery.value = q
        if (q.isBlank() && filterCategory.value == null) {
            _showResults.value = false
            searchListener?.remove()
        } else {
            search()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        searchListener?.remove()
    }

}