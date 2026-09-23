package com.example.nexogo.model

/**
 * Preferencias locales (ex-viewmodel.Settings) usadas por AppDataStore.
 * No es UI de settings Platform.
 */
data class AppSettings(
    val language: String = "Español",
    val notificationsEnabled: Boolean = true,
    val appointmentReminders: Boolean = true,
    val messageNotifications: Boolean = true,
    val biometricAuth: Boolean = false,
    val autoBackup: Boolean = true,
    val privacyMode: Boolean = false,
    val darkMode: Boolean = false
)

/**
 * Categoría de producto para preferencias locales (compatible JSON legacy Long createdAt).
 */
data class LocalProductCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
