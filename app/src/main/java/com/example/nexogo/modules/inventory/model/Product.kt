package com.example.nexogo.modules.inventory.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    val totalValue: Double
        get() = unitPrice * quantity
    
    val isLowStock: Boolean
        get() = quantity <= 10 // Umbral de stock bajo
    
    val stockStatus: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity <= 10 -> StockStatus.LOW_STOCK
            quantity <= 50 -> StockStatus.MEDIUM_STOCK
            else -> StockStatus.HIGH_STOCK
        }
}

enum class StockStatus {
    OUT_OF_STOCK,
    LOW_STOCK,
    MEDIUM_STOCK,
    HIGH_STOCK
}

