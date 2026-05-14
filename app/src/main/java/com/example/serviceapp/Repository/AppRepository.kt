package com.example.serviceapp.Repository

import com.example.serviceapp.Model.Category
import com.example.serviceapp.Model.ChatMessage
import com.example.serviceapp.Model.ChatUser
import com.example.serviceapp.Model.SearchSortOrder
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.Model.UserProfile
import com.example.serviceapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AppRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage   = FirebaseStorage.getInstance()
    private val auth      = FirebaseAuth.getInstance()
    private val realtimeDb = Firebase.database.reference

    private val servicesCollection = firestore.collection("services")
    private val usersCollection = firestore.collection("users")

    // ─── SERVICE LOGIC ──────────────────────────────────────
    suspend fun getServiceById(serviceId: String): ServicePost? {
        return try {
            val doc = servicesCollection.document(serviceId).get().await()
            val service = doc.toObject(ServicePost::class.java)
            service?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

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

    // ─── BOOKMARK LOGIC ──────────────────────────────────────

    // ১. বুকমার্ক অ্যাড বা রিমুভ করার ফাংশন
    suspend fun toggleBookmark(serviceId: String, currentState: Boolean): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val bookmarkRef = usersCollection
            .document(uid)
            .collection("bookmarks")
            .document(serviceId)

        return try {
            if (currentState) {
                // যদি আগে থেকেই বুকমার্ক থাকে, তবে ডিলিট করো
                bookmarkRef.delete().await()
                false // New state: Not bookmarked
            } else {
                // বুকমার্ক না থাকলে অ্যাড করো
                bookmarkRef.set(mapOf("serviceId" to serviceId)).await()
                true // New state: Bookmarked
            }
        } catch (e: Exception) {
            currentState // Error হলে আগের অবস্থায় ফেরত যাও
        }
    }

    // ২. ইউজারের সেভ করা সব বুকমার্ক আইডি নিয়ে আসার ফাংশন
    suspend fun getBookmarkedIds(uid: String): List<String> {
        return try {
            val snapshot = usersCollection
                .document(uid)
                .collection("bookmarks")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.id } // সরাসরি ডকুমেন্ট আইডি (serviceId) রিটার্ন করবে
        } catch (e: Exception) {
            emptyList()
        }
    }

    // AppRepository.kt-te nicher function-ti add korun
    fun listenToServices(
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error) // Error hole callback pathabe
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    val post = doc.toObject(ServicePost::class.java)
                    post?.copy(id = doc.id) // Document ID set kora
                } ?: emptyList()

                onUpdate(posts) // Data update hole callback pathabe
            }
    }

    // AppRepository.kt-er bhetore eygulo add korun
    fun validatePost(t: String, c: String, d: String, p: String, l: String): String? {
        return when {
            t.isEmpty() -> "Title cannot be empty"
            c.isEmpty() -> "Please select a category"
            d.length < 10 -> "Description must be at least 10 characters"
            p.isEmpty() || p.toIntOrNull() == null -> "Please enter a valid price"
            l.isEmpty() -> "Location is required"
            else -> null
        }
    }

    // Firebase Storage-e image upload korar suspend function
    suspend fun uploadImages(uris: List<String>): List<String> {
        val storageRef =
            com.google.firebase.storage.FirebaseStorage.getInstance().reference.child("service_images")
        val uploadedUrls = mutableListOf<String>()

        for (uriString in uris) {
            val fileName = "${System.currentTimeMillis()}_${java.util.UUID.randomUUID()}"
            val fileRef = storageRef.child(fileName)
            val uri = android.net.Uri.parse(uriString)

            // Coroutine bebohar kore upload task handle kora
            val uploadTask = fileRef.putFile(uri).await() // Task-কে await kora dorkar
            val downloadUrl = fileRef.downloadUrl.await().toString()
            uploadedUrls.add(downloadUrl)
        }
        return uploadedUrls
    }
    // AppRepository.kt-er bhetore add korun

    // User-er nijossho post gulo realtime listen korar jonno
    fun listenToUserPosts(
        uid: String,
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return firestore.collection("services")
            .whereEqualTo("providerId", uid) // Sudhu current user-er post filter kora
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    val post = doc.toObject(ServicePost::class.java)
                    post?.copy(id = doc.id)
                } ?: emptyList()

                onUpdate(posts)
            }
    }

    // Sign out logic
    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    suspend fun submitPost(
        title: String, category: String, description: String,
        price: Int, location: String, isOffering: Boolean,
        uploadedImageUrls: List<String>
    ): ServicePost {
        val user = auth.currentUser
        val uid = user?.uid ?: ""
        val userProfile = getUserProfile(uid)
        val finalName = userProfile?.fullName ?: (user?.displayName ?: "Unknown")
        val finalPhone = userProfile?.phone ?: (user?.phoneNumber ?: "")

        val newPost = ServicePost(
            title = title, providerName = finalName,
            providerUsername = "@${finalName.lowercase().replace(" ", "")}",
            price = price, providerEmail = user?.email ?: "",
            isOffering = isOffering, description = description,
            location = location, phone = finalPhone,
            skills = listOf(category), providerId = uid,
            imageUrls = uploadedImageUrls, createdAt = System.currentTimeMillis()
        )
        val docRef = servicesCollection.add(newPost).await()
        return newPost.copy(id = docRef.id)
    }

    // ─── AUTH & PROFILE ─────────────────────────────────────
    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        fullName: String,
        phone: String
    ): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = result.user?.uid ?: return false
        val profile = UserProfile(
            uid = uid,
            fullName = fullName,
            email = email,
            phone = phone,
            createdAt = System.currentTimeMillis()
        )
        usersCollection.document(uid).set(profile).await()
        return true
    }

    suspend fun signInWithEmail(email: String, pass: String): Boolean {
        auth.signInWithEmailAndPassword(email, pass).await()
        return true
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            usersCollection.document(uid).get().await().toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ─── CHAT CONVERSION LOGIC ──────────────────────────────

    // Service Details theke message dile unique chatId generate hobe
    fun getOrCreateChatId(providerId: String, providerName: String, onComplete: (String) -> Unit) {
        val myUid = auth.currentUser?.uid ?: return
        val myName = auth.currentUser?.displayName ?: "User"

        // Unique ID based on both UIDs
        val chatId = if (myUid < providerId) "${myUid}_$providerId" else "${providerId}_$myUid"

        val myChatRef = realtimeDb.child("user_chats").child(myUid).child(chatId)

        myChatRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                // Nijer list e add kora
                myChatRef.setValue(mapOf("otherUserId" to providerId, "userName" to providerName))

                // Provider er list e add kora
                realtimeDb.child("user_chats").child(providerId).child(chatId)
                    .setValue(mapOf("otherUserId" to myUid, "userName" to myName))
            }
            onComplete(chatId)
        }
    }

    // Message pathano ebong summary update kora
    fun sendMessage(chatId: String, message: String, otherUserId: String, otherUserName: String) {
        val uid = auth.currentUser?.uid ?: return
        val time = System.currentTimeMillis()

        val msgData = hashMapOf("message" to message, "senderId" to uid, "time" to time)

        // 1. Chat room e message pathano
        realtimeDb.child("chats").child(chatId).child("messages").push().setValue(msgData)

        // 2. Chat list summary update (for both users)
        val summary = mapOf(
            "lastMessage" to message,
            "time" to time,
            "otherUserId" to otherUserId,
            "userName" to otherUserName
        )
        realtimeDb.child("user_chats").child(uid).child(chatId).updateChildren(summary)

        val otherSummary = mapOf(
            "lastMessage" to message,
            "time" to time,
            "otherUserId" to uid,
            "userName" to (auth.currentUser?.displayName ?: "User")
        )
        realtimeDb.child("user_chats").child(otherUserId).child(chatId).updateChildren(otherSummary)
    }

    // Chat List Load kora (Sudhu current user er chats)
    fun listenToChatList(onUpdate: (List<ChatUser>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        realtimeDb.child("user_chats").child(uid).orderByChild("time")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { doc ->
                        val name = doc.child("userName").getValue(String::class.java) ?: "Unknown"
                        ChatUser(
                            chatId = doc.key ?: "",
                            otherUserId = doc.child("otherUserId").getValue(String::class.java)
                                ?: "",
                            name = name,
                            initials = name.take(1).uppercase(),
                            lastMessage = doc.child("lastMessage").getValue(String::class.java)
                                ?: "",
                            time = "Today" // Time formatting pore add korte parben
                        )
                    }.reversed()
                    onUpdate(list)
                }

                override fun onCancelled(p0: DatabaseError) {}
            })
    }

    fun getMessages(chatId: String, onUpdate: (List<ChatMessage>) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        realtimeDb.child("chats").child(chatId).child("messages").orderByChild("time")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val messages = s.children.mapNotNull { doc ->
                        val sId = doc.child("senderId").getValue(String::class.java) ?: ""
                        ChatMessage(
                            message = doc.child("message").getValue(String::class.java) ?: "",
                            senderId = sId,
                            time = doc.child("time").getValue(Long::class.java) ?: 0L,
                            isSentByMe = sId == currentUid
                        )
                    }
                    onUpdate(messages)
                }

                override fun onCancelled(e: DatabaseError) {}
            })
    }

    // AppRepository.kt-er bhetore add korun
    fun searchServices(
        query: String,
        category: String?,
        maxPrice: Int?,
        isOffering: Boolean?,
        sortBy: SearchSortOrder,
        onUpdate: (List<ServicePost>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        var firestoreQuery: com.google.firebase.firestore.Query = firestore.collection("services")

        // Category Filter
        if (!category.isNullOrEmpty()) {
            firestoreQuery = firestoreQuery.whereEqualTo("category", category)
        }

        // Offering/Looking Filter
        if (isOffering != null) {
            firestoreQuery = firestoreQuery.whereEqualTo("isOffering", isOffering)
        }

        // Sorting Logic
        firestoreQuery = when (sortBy) {
            SearchSortOrder.NEWEST -> firestoreQuery.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            SearchSortOrder.PRICE_LOW -> firestoreQuery.orderBy("price", com.google.firebase.firestore.Query.Direction.ASCENDING)
            SearchSortOrder.PRICE_HIGH -> firestoreQuery.orderBy("price", com.google.firebase.firestore.Query.Direction.DESCENDING)
            else -> firestoreQuery.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
        }

        return firestoreQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            var posts = snapshot?.documents?.mapNotNull { doc ->
                val post = doc.toObject(ServicePost::class.java)
                post?.copy(id = doc.id)
            } ?: emptyList()

            // Client-side text search (Firestore direct string contains support kore na)
            if (query.isNotBlank()) {
                posts = posts.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
            }

            // Client-side price filter (Jodi index issue thake tai ekhane kora bhalo)
            if (maxPrice != null) {
                posts = posts.filter { it.price <= maxPrice }
            }

            onUpdate(posts)
        }
    }
}