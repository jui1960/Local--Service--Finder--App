package com.example.serviceapp.Repository

import android.net.Uri
import com.example.serviceapp.Model.Category
import com.example.serviceapp.Model.SearchSortOrder
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Model.UserProfile
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


    suspend fun getServiceById(serviceId: String): ServicePost? {
        return try {
            // services collection theke document fetch kora
            val doc = servicesCollection.document(serviceId).get().await()
            val service = doc.toObject(ServicePost::class.java)

            // Document-er ID-ti model class-e manually set kora
            service?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

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

// VALIDATION
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



    // Registration logic
    suspend fun signUpWithEmail(email: String, pass: String, fullName: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = result.user?.uid
            if (uid != null) {
                // Register hole user profile-o create kora dorkar
                val initialProfile = UserProfile(
                    uid = uid,
                    fullName = fullName,
                    email = email,
                    createdAt = System.currentTimeMillis()
                )
                usersCollection.document(uid).set(initialProfile).await()
                true
            } else false
        } catch (e: Exception) {
            throw e // ViewModel-e error message dekhate hobe
        }
    }

    // Login logic
    suspend fun signInWithEmail(email: String, pass: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            true
        } catch (e: Exception) {
            throw e
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }



    //profile screen

    private val usersCollection = firestore.collection("users")

    // Fetch current user profile
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun listenToServices(
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return servicesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ServicePost::class.java)
                } ?: emptyList()

                // ✅ Sort client-side
                val sorted = posts.sortedByDescending { it.createdAt }
                onUpdate(sorted)
            }
    }

    // Fetch posts by a specific user
    fun listenToUserPosts(
        uid: String,
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return servicesCollection
            .whereEqualTo("providerId", uid)
                       .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull {
                    it.toObject(ServicePost::class.java)
                } ?: emptyList()

                val sorted = posts.sortedByDescending { it.createdAt }
                onUpdate(sorted)
            }
    }

    // Fetch bookmarked service IDs for current user
    suspend fun getBookmarkedIds(uid: String): List<String> {
        return try {
            val docs = usersCollection.document(uid)
                .collection("bookmarks").get().await()
            docs.documents.mapNotNull { it.getString("serviceId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Update user profile fields
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Boolean {
        return try {
            usersCollection.document(uid).update(updates).await()
            true
        } catch (e: Exception) {
            false
        }
    }



    //search
    fun searchServices(
        query: String,
        category: String?,
        maxPrice: Int?,
        isOffering: Boolean?,
        sortBy: SearchSortOrder,
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        var ref: com.google.firebase.firestore.Query = servicesCollection

        // Filter by category
        if (!category.isNullOrBlank()) {
            ref = ref.whereArrayContains("skills", category)
        }

        // Filter by offering/looking
        if (isOffering != null) {
            ref = ref.whereEqualTo("isOffering", isOffering)
        }

        // Sort
        ref = when (sortBy) {
            SearchSortOrder.NEWEST  -> ref.orderBy("createdAt", Query.Direction.DESCENDING)
            SearchSortOrder.POPULAR -> ref.orderBy("reviewCount", Query.Direction.DESCENDING)
            SearchSortOrder.PRICE_LOW  -> ref.orderBy("price", Query.Direction.ASCENDING)
            SearchSortOrder.PRICE_HIGH -> ref.orderBy("price", Query.Direction.DESCENDING)
        }

        return ref.addSnapshotListener { snapshot, error ->
            if (error != null) { onError(error); return@addSnapshotListener }

            var posts = snapshot?.documents?.mapNotNull {
                it.toObject(ServicePost::class.java)
            } ?: emptyList()

            // Client-side: text search filter (Firestore doesn't support full-text)
            if (query.isNotBlank()) {
                val q = query.lowercase()
                posts = posts.filter {
                    it.title.lowercase().contains(q) ||
                            it.description.lowercase().contains(q) ||
                            it.providerName.lowercase().contains(q) ||
                            it.skills.any { s -> s.lowercase().contains(q) }
                }
            }

            // Client-side: max price filter
            if (maxPrice != null) {
                posts = posts.filter { it.price <= maxPrice }
            }

            onUpdate(posts)
        }
    }


}
