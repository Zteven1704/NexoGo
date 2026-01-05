package com.example.nexogo.modules.inventory.model

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val productCount: Int = 0
)

