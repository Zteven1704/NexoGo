package com.example.nexogo.core.models

import com.google.firebase.Timestamp

/**
 * Modelo de Usuario para NexoGo
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER,
    val isApproved: Boolean = false,
    val fcmToken: String? = null,
    val phone: String = "",
    val whatsapp: String = "",
    val profileImageUrl: String = "",
    val isProfessional: Boolean = false,
    val specialization: String = "",
    val licenseNumber: String = "",
    val language: String = "es",
    val isActive: Boolean = true,
    val address: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastLogin: Timestamp? = null
)

enum class UserRole {
    ADMIN,
    VET,
    VET_ASSISTANT,
    USER
}

/**
 * Modelo de Paciente (Mascota)
 */
data class Patient(
    val id: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val color: String = "",
    val gender: String = "",
    val imageUrl: String = "",
    val medicalNotes: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Modelo de Cita
 */
data class Appointment(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val vetId: String = "",
    val vetName: String = "",
    val dateTime: Timestamp = Timestamp.now(),
    val duration: Int = 30,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val reason: String = "",
    val notes: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    COMPLETED,
    CANCELED,
    RESCHEDULED
}

/**
 * Modelo de Historial Clínico
 */
data class ClinicalRecord(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val vetId: String = "",
    val vetName: String = "",
    val date: Timestamp = Timestamp.now(),
    val reason: String = "",
    val anamnesis: String = "",
    val physicalExam: PhysicalExam = PhysicalExam(),
    val problems: List<String> = emptyList(),
    val diagnoses: List<String> = emptyList(),
    val treatmentPlan: String = "",
    val prognosis: String = "",
    val evolution: String = "",
    val attachments: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class PhysicalExam(
    val temperature: Double = 0.0,
    val heartRate: Int = 0,
    val respiratoryRate: Int = 0,
    val weight: Double = 0.0,
    val generalAppearance: String = "",
    val cardiovascular: String = "",
    val respiratory: String = "",
    val digestive: String = "",
    val neurological: String = "",
    val musculoskeletal: String = "",
    val skin: String = "",
    val eyes: String = "",
    val ears: String = "",
    val mouth: String = ""
)

/**
 * Modelo de Producto (Inventario)
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val imageUrl: String = "",
    val lowStockThreshold: Int = 5,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

/**
 * Modelo de Venta
 */
data class Sale(
    val id: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val items: List<SaleItem> = emptyList(),
    val services: List<VeterinaryService> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val invoiceUrl: String = "",
    val notes: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class SaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

data class VeterinaryService(
    val serviceId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0
)

enum class PaymentMethod {
    CASH,
    CARD,
    TRANSFER,
    CREDIT
}

enum class PaymentStatus {
    PENDING,
    PAID,
    CANCELLED,
    REFUNDED
}

/**
 * Modelo de Chat
 */
data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val attachments: List<MessageAttachment> = emptyList(),
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val isDelivered: Boolean = false
)

data class MessageAttachment(
    val url: String = "",
    val type: AttachmentType = AttachmentType.IMAGE,
    val name: String = "",
    val size: Long = 0
)

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    PDF
}

/**
 * Modelo de Configuración
 */
data class AppConfig(
    val id: String = "",
    val language: String = "es",
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val contactPhone: String = "",
    val contactWhatsApp: String = "",
    val logoUrl: String = "",
    val categories: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class NotificationSettings(
    val appointmentReminders: Boolean = true,
    val lowStockAlerts: Boolean = true,
    val newMessages: Boolean = true,
    val systemUpdates: Boolean = true
)

