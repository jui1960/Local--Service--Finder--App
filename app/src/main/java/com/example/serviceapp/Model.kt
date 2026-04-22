package com.example.serviceapp

data class ServicePost(
    val id: Int,
    val title: String,
    val providerName: String,
    val rating: Float,
    val reviewCount: Int,
    val price: Int,
    val imageRes: Int,
    val isOffering: Boolean = true,
    val isBookmarked: Boolean = false
)

data class Category(
    val id: Int,
    val name: String,
    val iconRes: Int
)
