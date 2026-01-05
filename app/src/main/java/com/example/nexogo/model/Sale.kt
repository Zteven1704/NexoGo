package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Sale(
    val id: String = "",
    val saleNumber: String = "", // Número de venta único
    val customerId: String = "", // ID del cliente
    val clientId: String = "", // ID del cliente para compatibilidad
    val customerName: String = "",
    val clientName: String = "", // Nombre del cliente para compatibilidad
    val customerEmail: String = "",
    val clientEmail: String = "", // Email del cliente para compatibilidad
    val customerPhone: String = "",
    val clientPhone: String = "", // Teléfono del cliente para compatibilidad
    val professionalId: String = "", // ID del profesional
    val professionalName: String = "", // Nombre del profesional
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val tax: Double = 0.0, // Impuesto para compatibilidad
    val discountAmount: Double = 0.0,
    val discount: Double = 0.0, // Descuento para compatibilidad
    val totalAmount: Double = 0.0,
    val total: Double = 0.0, // Total para compatibilidad
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val saleDate: Timestamp = Timestamp.now(),
    val date: Timestamp = Timestamp.now(), // Fecha para compatibilidad
    val cashierId: String = "", // ID del cajero
    val notes: String = "",
    val invoiceUrl: String = "", // URL del PDF de la factura
    val qrCodeUrl: String = "", // URL del código QR
    val isRefunded: Boolean = false,
    val refundedAt: Timestamp? = null,
    val refundReason: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class SaleItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productType: SaleItemType = SaleItemType.PRODUCT,
    val type: SaleItemType = SaleItemType.PRODUCT, // Tipo para compatibilidad
    val name: String = "", // Nombre para compatibilidad
    val description: String = "", // Descripción para compatibilidad
    val serviceId: String = "", // ID del servicio
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val basePrice: Double = 0.0, // Precio base para compatibilidad
    val totalPrice: Double = 0.0,
    val subtotal: Double = 0.0, // Subtotal para compatibilidad
    val discount: Double = 0.0,
    val notes: String = ""
)

enum class SaleItemType {
    PRODUCT,        // Producto físico
    SERVICE,        // Servicio veterinario
    CONSULTATION,   // Consulta
    VACCINATION,    // Vacunación
    SURGERY,        // Cirugía
    LABORATORY,     // Laboratorio
    IMAGING,        // Imagenología
    HOSPITALIZATION, // Hospitalización
    XRAY,           // Radiografía
    STERILIZATION,  // Esterilización
    GROOMING        // Peluquería
}

enum class PaymentMethod {
    CASH,           // Efectivo
    CARD,           // Tarjeta
    TRANSFER,       // Transferencia
    CHECK,          // Cheque
    CREDIT,         // Crédito
    DIGITAL_WALLET  // Billetera digital
}

enum class PaymentStatus {
    PENDING,        // Pendiente
    PAID,           // Pagado
    PARTIAL,        // Pago parcial
    REFUNDED,       // Reembolsado
    CANCELLED       // Cancelado
}

data class VeterinaryService(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: ServiceCategory = ServiceCategory.CONSULTATION,
    val price: Double = 0.0,
    val duration: Int = 30, // Duración en minutos
    val isActive: Boolean = true,
    val requiresAppointment: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)

enum class ServiceCategory {
    CONSULTATION,   // Consulta
    VACCINATION,    // Vacunación
    SURGERY,        // Cirugía
    DENTAL,         // Dental
    DERMATOLOGY,    // Dermatología
    CARDIOLOGY,     // Cardiología
    NEUROLOGY,      // Neurología
    ONCOLOGY,       // Oncología
    EMERGENCY,      // Emergencia
    LABORATORY,     // Laboratorio
    IMAGING,        // Imagenología
    HOSPITALIZATION, // Hospitalización
    EUTHANASIA,     // Eutanasia
    OTHER           // Otro
}