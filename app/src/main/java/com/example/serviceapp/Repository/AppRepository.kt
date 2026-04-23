package com.example.serviceapp.Repository

import com.example.serviceapp.Model.Category
import com.example.serviceapp.R
import com.example.serviceapp.Model.ServicePost

class AppRepository {
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

    fun getServiceById(serviceId: Int): ServicePost? {
        return getAllServices().find { it.id == serviceId }
    }

    fun toggleBookmark(serviceId: Int): Boolean {
        // In real app: update Firestore user bookmarks collection
        // Returns new bookmark state
        return true // placeholder
    }

    // Services fetch korar function
    private fun getAllServices(): List<ServicePost> {
        return listOf(
            ServicePost(
                id = 1,
                title = "I will do professional House Cleaning",
                providerName = "John Doe",
                providerUsername = "@johndoe",
                rating = 4.8f,
                reviewCount = 120,
                price = 500,
                imageRes = R.drawable.img3,
                isOffering = true,
                description = "I provide professional house cleaning services. " +
                        "I will clean your home, kitchen, bathroom perfectly. " +
                        "All cleaning supplies included. Satisfaction guaranteed.",
                location = "Dhaka, Bangladesh",
                phone = "+880 1700-000000",
                skills = listOf("Cleaning", "Kitchen", "Bathroom", "Deep Clean")
            ),
            ServicePost(
                id = 2,
                title = "Need a Plumber for Pipe Repair",
                providerName = "Alex Smith",
                providerUsername = "@alexsmith",
                rating = 4.6f,
                reviewCount = 80,
                price = 700,
                imageRes = R.drawable.img2,
                isOffering = true,
                description = "Expert plumber with 10+ years experience. " +
                        "All pipe-related issues fixed quickly and professionally.",
                location = "Dhaka, Bangladesh",
                phone = "+880 1800-111111",
                skills = listOf("Plumbing", "Pipe Repair", "Water Leak", "Installation")
            ),
            ServicePost(
                id = 3,
                title = "Need Electrician for Office Wiring",
                providerName = "Sarah Ahmed",
                providerUsername = "@sarahahmed",
                rating = 4.7f,
                reviewCount = 60,
                price = 1000,
                imageRes = R.drawable.img_2,
                isOffering = false,
                description = "Looking for a licensed electrician to set up full office wiring. " +
                        "Must have experience with commercial installations.",
                location = "Dhaka, Bangladesh",
                phone = "+880 1900-222222",
                skills = listOf("Electrician", "Wiring", "Office Setup")
            ),
            ServicePost(
                id = 4,
                title = "Carpentry Work (Shelves, Furniture)",
                providerName = "Mike Johnson",
                providerUsername = "@mikejohnson",
                rating = 4.9f,
                reviewCount = 150,
                price = 1500,
                imageRes = R.drawable.img_3,
                isOffering = true,
                description = "Custom furniture and shelf building. All wood types handled. " +
                        "Precision craftsmanship with years of experience.",
                location = "Dhaka, Bangladesh",
                phone = "+880 1600-333333",
                skills = listOf("Carpentry", "Furniture", "Wood Work", "Shelves")
            )
        )
    }

    fun getServices(): List<ServicePost> {
        return getAllServices()
    }



}