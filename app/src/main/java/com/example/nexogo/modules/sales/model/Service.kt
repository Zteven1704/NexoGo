package com.example.nexogo.modules.sales.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Service(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val duration: Int = 0, // en minutos
    val isActive: Boolean = true,
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class ServiceCategory(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)

