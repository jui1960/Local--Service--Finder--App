package com.example.serviceapp.Model

data class ServicePost(
    val id: Int,
    val title: String,
    val providerName: String,
    val providerUsername: String,
    val rating: Float,
    val reviewCount: Int,
    val price: Int,
    val imageRes: Int,
    val isOffering: Boolean,
    val description: String = "",
    val location: String = "Dhaka, Bangladesh",
    val phone: String = "",
    val skills: List<String> = emptyList(),
    var isBookmarked: Boolean = false
)

data class Category(
    val id: Int,
    val name: String,
    val iconRes: Int
)
