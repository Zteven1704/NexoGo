package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Invoice(
    val id: String = "",
    val invoiceNumber: String = "",
    val saleId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientEmail: String = "",
    val clientPhone: String = "",
    val professionalId: String = "",
    val professionalName: String = "",
    val items: List<InvoiceItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val invoiceDate: Timestamp = Timestamp.now(),
    val dueDate: Timestamp? = null,
    val notes: String = "",
    val pdfUrl: String = "",
    val qrCodeUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class InvoiceItem(
    val id: String = "",
    val type: InvoiceItemType = InvoiceItemType.PRODUCT,
    val productId: String? = null,
    val serviceId: String? = null,
    val name: String = "",
    val description: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val subtotal: Double = 0.0
)

enum class InvoiceItemType {
    PRODUCT,
    SERVICE
}

