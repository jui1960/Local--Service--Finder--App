package com.example.serviceapp.Model

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val postCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val createdAt: Long = 0L
)
