package com.example.nexogo.modules.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de configuración
 */
class ConfigViewModel : ViewModel() {
    
    private val firebaseRepository = FirebaseRepository()
    
    private val _config = MutableStateFlow<Config?>(null)
    val config: StateFlow<Config?> = _config.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()
    
    private val _services = MutableStateFlow<List<String>>(emptyList())
    val services: StateFlow<List<String>> = _services.asStateFlow()
    
    init {
        loadConfig()
        loadCategories()
        loadServices()
    }
    
    /**
     * Cargar configuración actual
     */
    fun loadConfig() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val config = firebaseRepository.getConfig()
                _config.value = config
                
                if (config == null) {
                    _message.value = "No hay configuración disponible"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar configuración: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Guardar configuración
     */
    fun saveConfig(config: Config) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                firebaseRepository.saveConfig(config)
                _config.value = config
                _message.value = "Configuración guardada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al guardar configuración: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Actualizar idioma
     */
    fun updateLanguage(language: String) {
        val currentConfig = _config.value ?: Config()
        val updatedConfig = currentConfig.copy(language = language)
        saveConfig(updatedConfig)
    }
    
    /**
     * Actualizar notificaciones
     */
    fun updateNotifications(enabled: Boolean) {
        val currentConfig = _config.value ?: Config()
        val updatedConfig = currentConfig.copy(notificationsEnabled = enabled)
        saveConfig(updatedConfig)
    }
    
    /**
     * Cargar categorías de productos
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = firebaseRepository.getCategories()
                _categories.value = categories
            } catch (e: Exception) {
                _message.value = "Error al cargar categorías: ${e.message}"
            }
        }
    }
    
    /**
     * Agregar categoría
     */
    fun addCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                firebaseRepository.addCategory(category)
                loadCategories()
                _message.value = "Categoría agregada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al agregar categoría: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Eliminar categoría
     */
    fun deleteCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                firebaseRepository.deleteCategory(category)
                loadCategories()
                _message.value = "Categoría eliminada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar categoría: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar servicios
     */
    fun loadServices() {
        viewModelScope.launch {
            try {
                val services = firebaseRepository.getServices()
                _services.value = services
            } catch (e: Exception) {
                _message.value = "Error al cargar servicios: ${e.message}"
            }
        }
    }
    
    /**
     * Agregar servicio
     */
    fun addService(service: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                firebaseRepository.addService(service)
                loadServices()
                _message.value = "Servicio agregado exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al agregar servicio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Eliminar servicio
     */
    fun deleteService(service: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                firebaseRepository.deleteService(service)
                loadServices()
                _message.value = "Servicio eliminado exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar servicio: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpiar mensaje
     */
    fun clearMessage() {
        _message.value = ""
    }
}
