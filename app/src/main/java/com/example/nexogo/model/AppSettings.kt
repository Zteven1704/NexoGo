package com.example.nexogo.model

data class AppSettings(
    val userId: String = "",
    val language: String = "es", // es, en
    val notificationsEnabled: Boolean = true,
    val appointmentReminders: Boolean = true,
    val chatNotifications: Boolean = true,
    val marketingNotifications: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
    val autoBackup: Boolean = true,
    val biometricAuth: Boolean = false
)

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}

