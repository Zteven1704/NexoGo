package com.example.nexogo.modules.sales.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Sale(
    @DocumentId val id: String = "",
    val saleNumber: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val status: SaleStatus = SaleStatus.COMPLETED,
    val notes: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val pdfUrl: String = ""
)

data class SaleItem(
    val id: String = "",
    val type: SaleItemType = SaleItemType.PRODUCT,
    val itemId: String = "", // ID del producto o servicio
    val itemName: String = "",
    val description: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

enum class SaleItemType {
    PRODUCT, SERVICE
}

enum class PaymentMethod {
    CASH, CARD, TRANSFER, CHECK
}

enum class SaleStatus {
    PENDING, COMPLETED, CANCELLED, REFUNDED
}

