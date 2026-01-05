package com.example.nexogo.core.models

/**
 * Modelo de configuración del sistema
 */
data class Config(
    val id: String = "",
    val language: String = "es",
    val notificationsEnabled: Boolean = true,
    val categories: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val contactPhone: String = "",
    val contactWhatsApp: String = "",
    val logoUrl: String = "",
    val updatedAt: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
)

