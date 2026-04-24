package com.example.serviceapp.Repository

import android.net.Uri
import com.example.serviceapp.Model.Category
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AppRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage   = FirebaseStorage.getInstance()
    private val auth      = FirebaseAuth.getInstance()

    private val servicesCollection = firestore.collection("services")

    // ─── CATEGORIES (static) ──────────────────────────────────
    fun getCategories(): List<Category> {
        return listOf(
            Category(1, "Cleaning",    R.drawable.cleaning),
            Category(2, "Plumbing",    R.drawable.ic_plumbing),
            Category(3, "Electrician", R.drawable.electrician),
            Category(4, "Carpentry",   R.drawable.img_4),
            Category(5, "AC Repair",   R.drawable.ac),
            Category(6, "Gardening",   R.drawable.gurden),
            Category(7, "Painting",    R.drawable.painting),
            Category(8, "More",        R.drawable.more)
        )
    }

    fun getCategoryNames(): List<String> = getCategories().map { it.name }

    // ─── REALTIME LISTENER (HomeViewModel uses this) ──────────
    fun listenToServices(
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return servicesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServicePost::class.java)
                } ?: emptyList()
                onUpdate(posts)
            }
    }

    // ─── FETCH SINGLE SERVICE (ServiceDetailsActivity uses this)
    suspend fun getServiceById(serviceId: String): ServicePost? {
        return try {
            val doc = servicesCollection.document(serviceId).get().await()
            doc.toObject(ServicePost::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ─── BOOKMARK TOGGLE ──────────────────────────────────────
    suspend fun toggleBookmark(serviceId: String, currentState: Boolean): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val bookmarkRef = firestore
            .collection("users")
            .document(uid)
            .collection("bookmarks")
            .document(serviceId)
        return try {
            if (currentState) {
                bookmarkRef.delete().await()
                false
            } else {
                bookmarkRef.set(mapOf("serviceId" to serviceId)).await()
                true
            }
        } catch (e: Exception) {
            currentState
        }
    }

    // ─── VALIDATION ───────────────────────────────────────────
    fun validatePost(
        title: String,
        category: String,
        description: String,
        price: String,
        location: String
    ): String? {
        if (title.isBlank())       return "Title is required"
        if (category.isBlank())    return "Please select a category"
        if (description.isBlank()) return "Description is required"
        if (price.isBlank() || price.toIntOrNull() == null || price.toInt() <= 0)
            return "Enter a valid price"
        if (location.isBlank())    return "Location is required"
        return null
    }

    // ─── UPLOAD IMAGES to Firebase Storage ───────────────────
    suspend fun uploadImages(localUris: List<String>): List<String> {
        val uid = auth.currentUser?.uid ?: "anonymous"
        val downloadUrls = mutableListOf<String>()
        for (uriString in localUris) {
            val uri = Uri.parse(uriString)
            val fileName = "services/$uid/${System.currentTimeMillis()}_${uri.lastPathSegment}"
            val ref = storage.reference.child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            downloadUrls.add(downloadUrl)
        }
        return downloadUrls
    }

    // ─── SUBMIT POST to Firestore ─────────────────────────────
    suspend fun submitPost(
        title: String,
        category: String,
        description: String,
        price: Int,
        location: String,
        isOffering: Boolean,
        uploadedImageUrls: List<String>
    ): ServicePost {
        val user = auth.currentUser
        val uid  = user?.uid ?: ""

        val newPost = ServicePost(
            title            = title,
            providerName     = user?.displayName ?: "Unknown",
            providerUsername = "@${user?.displayName?.lowercase()?.replace(" ", "") ?: "user"}",
            rating           = 0f,
            reviewCount      = 0,
            price            = price,
            imageRes         = 0,
            isOffering       = isOffering,
            description      = description,
            location         = location,
            phone            = user?.phoneNumber ?: "",
            skills           = listOf(category),
            isBookmarked     = false,
            imageUrls        = uploadedImageUrls,
            providerId       = uid,
            createdAt        = System.currentTimeMillis()
        )

        val docRef = servicesCollection.add(newPost).await()
        return newPost.copy(id = docRef.id)
    }
}
