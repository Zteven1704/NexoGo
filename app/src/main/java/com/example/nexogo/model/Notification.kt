package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val isRead: Boolean = false,
    val data: Map<String, String> = emptyMap(), // Additional data for navigation
    val timestamp: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp? = null
)

enum class NotificationType {
    GENERAL,
    APPOINTMENT_REMINDER,
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_RESCHEDULED,
    NEW_MESSAGE,
    PROFESSIONAL_REGISTRATION_REQUEST,
    PROFESSIONAL_APPROVED,
    PROFESSIONAL_REJECTED,
    LOW_STOCK_ALERT,
    VACCINATION_REMINDER,
    SYSTEM_UPDATE
}

