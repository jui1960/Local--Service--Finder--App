package com.example.serviceapp.ViewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.serviceapp.Model.Category
import com.example.serviceapp.Repository.AppRepository
import com.example.serviceapp.Model.ServicePost

class HomeViewModel : ViewModel() {
    private val repository = AppRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _services = MutableLiveData<List<ServicePost>>()
    val services: LiveData<List<ServicePost>> = _services

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _categories.value = repository.getCategories()
        _services.value = repository.getServices()
    }
}