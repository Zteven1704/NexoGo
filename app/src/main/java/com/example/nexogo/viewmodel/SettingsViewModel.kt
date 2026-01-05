package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.data.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {

    private val appDataStore = AppDataStore(context)

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            loadSettings()
        }
    }

    fun updateLanguage(language: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(language = language)
            appDataStore.saveSettings(_settings.value)
            _message.value = "Idioma actualizado a $language"
            _isLoading.value = false
        }
    }

    fun updateNotifications(enabled: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(notificationsEnabled = enabled)
            appDataStore.saveSettings(_settings.value)
            _message.value = if (enabled) "Notificaciones activadas" else "Notificaciones desactivadas"
            _isLoading.value = false
        }
    }

    fun updateAppointmentReminders(enabled: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(appointmentReminders = enabled)
            appDataStore.saveSettings(_settings.value)
            _message.value = if (enabled) "Recordatorios de citas activados" else "Recordatorios de citas desactivados"
            _isLoading.value = false
        }
    }

    fun updateMessageNotifications(enabled: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(messageNotifications = enabled)
            appDataStore.saveSettings(_settings.value)
            _message.value = if (enabled) "Notificaciones de mensajes activadas" else "Notificaciones de mensajes desactivadas"
            _isLoading.value = false
        }
    }

    fun updateBiometricAuth(enabled: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(biometricAuth = enabled)
            appDataStore.saveSettings(_settings.value)
            _message.value = if (enabled) "Autenticación biométrica activada" else "Autenticación biométrica desactivada"
            _isLoading.value = false
        }
    }

    fun updateAutoBackup(enabled: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentSettings = _settings.value
            _settings.value = currentSettings.copy(autoBackup = enabled)
            appDataStore.saveSettings(_settings.value)
            _message.value = if (enabled) "Respaldo automático activado" else "Respaldo automático desactivado"
            _isLoading.value = false
        }
    }

    fun clearCache() {
        _isLoading.value = true
        viewModelScope.launch {
            // Simulate cache clearing
            kotlinx.coroutines.delay(1000)
            _message.value = "Caché limpiado exitosamente"
            _isLoading.value = false
        }
    }

    fun getStorageInfo(): StorageInfo {
        // Simulate storage info
        return StorageInfo(
            totalSpace = 8.0, // GB
            usedSpace = 2.5, // GB
            availableSpace = 5.5, // GB
            cacheSize = 0.3 // GB
        )
    }

    private suspend fun loadSettings() {
        _isLoading.value = true
        val loadedSettings = appDataStore.loadSettings()
        if (loadedSettings == null) {
            _settings.value = Settings() // Default settings
            appDataStore.saveSettings(_settings.value)
        } else {
            _settings.value = loadedSettings
        }
        _isLoading.value = false
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsViewModel? = null

        fun getInstance(context: Context): SettingsViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsViewModel(context).also { INSTANCE = it }
            }
        }
    }
}

data class Settings(
    val language: String = "Español",
    val notificationsEnabled: Boolean = true,
    val appointmentReminders: Boolean = true,
    val messageNotifications: Boolean = true,
    val biometricAuth: Boolean = false,
    val autoBackup: Boolean = true,
    val privacyMode: Boolean = false,
    val darkMode: Boolean = false
)

data class StorageInfo(
    val totalSpace: Double, // GB
    val usedSpace: Double, // GB
    val availableSpace: Double, // GB
    val cacheSize: Double // GB
)

