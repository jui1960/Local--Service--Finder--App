package com.example.serviceapp.Model

import com.google.firebase.firestore.DocumentId

data class ServicePost(
    @DocumentId
    val id: String = "",                          // Firestore document ID (String, not Int)
    val title: String = "",
    val providerName: String = "",
    val providerUsername: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val price: Int = 0,
    val imageRes: Int = 0,                        // Local drawable (used only for static data)
    val isOffering: Boolean = true,
    val description: String = "",
    val location: String = "Dhaka, Bangladesh",
    val phone: String = "",
    val skills: List<String> = emptyList(),
    var isBookmarked: Boolean = false,
    val imageUrls: List<String> = emptyList(),    // Firebase Storage URLs
    val providerId: String = "",                  // Firebase Auth UID
    val createdAt: Long = 0L
)

data class Category(
    val id: Int,
    val name: String,
    val iconRes: Int
)
