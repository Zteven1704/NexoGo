package com.example.nexogo.modules.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el Dashboard principal de NexoGo
 */
class DashboardViewModel(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    // Estadísticas del dashboard
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()
    
    // Notificaciones en tiempo real
    private val _notifications = MutableStateFlow<List<DashboardNotification>>(emptyList())
    val notifications: StateFlow<List<DashboardNotification>> = _notifications.asStateFlow()
    
    // Módulos disponibles según el rol
    private val _availableModules = MutableStateFlow<List<DashboardModule>>(emptyList())
    val availableModules: StateFlow<List<DashboardModule>> = _availableModules.asStateFlow()
    
    init {
        loadUserData()
        loadStats()
        loadNotifications()
    }
    
    /**
     * Cargar datos del usuario actual
     */
    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = ""
                
                val currentUserId = firebaseRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val userData = firebaseRepository.getUserById(currentUserId)
                    _user.value = userData
                    
                    // Determinar módulos disponibles según el rol
                    updateAvailableModules(userData?.role?.name)
                } else {
                    _message.value = "Usuario no autenticado"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar datos del usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar estadísticas del dashboard
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                val currentUser = _user.value
                if (currentUser != null) {
                    // Cargar estadísticas según el rol
                    when (currentUser.role) {
                        com.example.nexogo.core.models.UserRole.ADMIN, 
                        com.example.nexogo.core.models.UserRole.VET, 
                        com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> {
                            loadProfessionalStats()
                        }
                        com.example.nexogo.core.models.UserRole.USER -> {
                            loadUserstats()
                        }
                        else -> {
                            // Para roles pendientes, no cargar estadísticas
                        }
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar estadísticas: ${e.message}"
            }
        }
    }
    
    /**
     * Cargar estadísticas para profesionales
     */
    private suspend fun loadProfessionalStats() {
        try {
            val patients = firebaseRepository.getPatientsByOwner("all") // Obtener todos los pacientes
            val appointments = firebaseRepository.getAppointmentsByOwner("all") // Obtener todas las citas
            val inventory = firebaseRepository.getInventory()
            val sales = firebaseRepository.getSales()
            
            val todayAppointments = appointments.filter { appointment ->
                val appointmentDate = appointment.dateTime.toDate()
                val today = java.util.Date()
                java.text.SimpleDateFormat("yyyy-MM-dd").format(appointmentDate) == 
                java.text.SimpleDateFormat("yyyy-MM-dd").format(today)
            }
            
            val todaySales = sales.filter { sale ->
                val saleDate = sale.createdAt.toDate()
                val today = java.util.Date()
                java.text.SimpleDateFormat("yyyy-MM-dd").format(saleDate) == 
                java.text.SimpleDateFormat("yyyy-MM-dd").format(today)
            }
            
            _stats.value = DashboardStats(
                totalPatients = patients.size,
                todayAppointments = todayAppointments.size,
                totalInventory = inventory.size,
                todaySales = todaySales.size,
                lowStockItems = inventory.count { it.quantity <= it.lowStockThreshold }
            )
        } catch (e: Exception) {
            _message.value = "Error al cargar estadísticas profesionales: ${e.message}"
        }
    }
    
    /**
     * Cargar estadísticas para pacientes
     */
    private suspend fun loadUserstats() {
        try {
            val currentUserId = firebaseRepository.getCurrentUserId()
            if (currentUserId != null) {
                val userPatients = firebaseRepository.getPatientsByOwner(currentUserId)
                val userAppointments = firebaseRepository.getAppointmentsByOwner(currentUserId)
                val userClinicalHistory = firebaseRepository.getClinicalHistoryByOwner(currentUserId)
                
                _stats.value = DashboardStats(
                    totalPatients = userPatients.size,
                    todayAppointments = userAppointments.count { appointment ->
                        val appointmentDate = appointment.dateTime.toDate()
                        val today = java.util.Date()
                        java.text.SimpleDateFormat("yyyy-MM-dd").format(appointmentDate) == 
                        java.text.SimpleDateFormat("yyyy-MM-dd").format(today)
                    },
                    totalInventory = 0, // Los pacientes no ven inventario
                    todaySales = 0, // Los pacientes no ven ventas
                    lowStockItems = 0
                )
            }
        } catch (e: Exception) {
            _message.value = "Error al cargar estadísticas del paciente: ${e.message}"
        }
    }
    
    /**
     * Cargar notificaciones en tiempo real
     */
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                val currentUser = _user.value
                if (currentUser != null) {
                    val notificationsList = mutableListOf<DashboardNotification>()
                    
                    // Notificaciones de citas
                    val appointments = firebaseRepository.getAppointmentsByOwner("all")
                    val upcomingAppointments = appointments.filter { appointment ->
                        val appointmentDate = appointment.dateTime.toDate()
                        val now = java.util.Date()
                        appointmentDate.after(now) && appointment.status == com.example.nexogo.core.models.AppointmentStatus.SCHEDULED
                    }
                    
                    if (upcomingAppointments.isNotEmpty()) {
                        notificationsList.add(
                            DashboardNotification(
                                id = "appointments_${upcomingAppointments.size}",
                                title = "Citas Programadas",
                                message = "Tienes ${upcomingAppointments.size} citas programadas",
                                type = NotificationType.APPOINTMENT,
                                timestamp = java.util.Date(),
                                isRead = false
                            )
                        )
                    }
                    
                    // Notificaciones de chat
                    val chats = firebaseRepository.getChats()
                    val unreadMessages = chats.count { chat ->
                        chat.lastMessageTime.toDate().after(java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
                    }
                    
                    if (unreadMessages > 0) {
                        notificationsList.add(
                            DashboardNotification(
                                id = "messages_$unreadMessages",
                                title = "Mensajes Nuevos",
                                message = "Tienes $unreadMessages mensajes sin leer",
                                type = NotificationType.MESSAGE,
                                timestamp = java.util.Date(),
                                isRead = false
                            )
                        )
                    }
                    
                    // Notificaciones de stock bajo (solo para profesionales)
                    if (currentUser.role in listOf(com.example.nexogo.core.models.UserRole.ADMIN, com.example.nexogo.core.models.UserRole.VET, com.example.nexogo.core.models.UserRole.VET_ASSISTANT)) {
                        val inventory = firebaseRepository.getInventory()
                        val lowStockItems = inventory.filter { it.quantity <= it.lowStockThreshold }
                        
                        if (lowStockItems.isNotEmpty()) {
                            notificationsList.add(
                                DashboardNotification(
                                    id = "low_stock_${lowStockItems.size}",
                                    title = "Stock Bajo",
                                    message = "${lowStockItems.size} productos con stock bajo",
                                    type = NotificationType.INVENTORY,
                                    timestamp = java.util.Date(),
                                    isRead = false
                                )
                            )
                        }
                    }
                    
                    _notifications.value = notificationsList
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar notificaciones: ${e.message}"
            }
        }
    }
    
    /**
     * Actualizar módulos disponibles según el rol
     */
    private fun updateAvailableModules(role: String?) {
        val modules = when (role) {
            "ADMIN" -> listOf(
                DashboardModule.PROFILE,
                DashboardModule.APPOINTMENTS,
                DashboardModule.USERS,
                DashboardModule.CLINICAL_HISTORY,
                DashboardModule.INVENTORY,
                DashboardModule.SALES,
                DashboardModule.CHAT,
                DashboardModule.CONFIG
            )
            "VET" -> listOf(
                DashboardModule.PROFILE,
                DashboardModule.APPOINTMENTS,
                DashboardModule.USERS,
                DashboardModule.CLINICAL_HISTORY,
                DashboardModule.CHAT
            )
            "VET_ASSISTANT" -> listOf(
                DashboardModule.PROFILE,
                DashboardModule.APPOINTMENTS,
                DashboardModule.USERS,
                DashboardModule.INVENTORY,
                DashboardModule.SALES,
                DashboardModule.CHAT
            )
            "USER" -> listOf(
                DashboardModule.PROFILE,
                DashboardModule.APPOINTMENTS,
                DashboardModule.CLINICAL_HISTORY,
                DashboardModule.CHAT
            )
            else -> emptyList()
        }
        
        _availableModules.value = modules
    }
    
    /**
     * Marcar notificación como leída
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            val currentNotifications = _notifications.value.toMutableList()
            val notificationIndex = currentNotifications.indexOfFirst { it.id == notificationId }
            if (notificationIndex != -1) {
                currentNotifications[notificationIndex] = currentNotifications[notificationIndex].copy(isRead = true)
                _notifications.value = currentNotifications
            }
        }
    }
    
    /**
     * Limpiar mensaje
     */
    fun clearMessage() {
        _message.value = ""
    }
    
    /**
     * Refrescar datos del dashboard
     */
    fun refreshDashboard() {
        loadUserData()
        loadStats()
        loadNotifications()
    }
}

/**
 * Datos de estadísticas del dashboard
 */
data class DashboardStats(
    val totalPatients: Int = 0,
    val todayAppointments: Int = 0,
    val totalInventory: Int = 0,
    val todaySales: Int = 0,
    val lowStockItems: Int = 0
)

/**
 * Notificación del dashboard
 */
data class DashboardNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: java.util.Date,
    val isRead: Boolean = false
)

/**
 * Tipos de notificaciones
 */
enum class NotificationType {
    APPOINTMENT,
    MESSAGE,
    INVENTORY,
    SALE,
    SYSTEM
}

/**
 * Módulos del dashboard
 */
enum class DashboardModule(
    val title: String,
    val icon: String,
    val color: Long,
    val route: String
) {
    PROFILE("Perfil", "👤", 0xFF6200EE, "profile"),
    APPOINTMENTS("Citas", "🗓️", 0xFF03DAC6, "appointments"),
    USERS("Pacientes", "🐾", 0xFF018786, "users"),
    CLINICAL_HISTORY("Historial Clínico", "📋", 0xFFBB86FC, "clinical_records"),
    INVENTORY("Inventario", "💊", 0xFFFF9800, "inventory"),
    SALES("Ventas", "🧾", 0xFF4CAF50, "sales"),
    CHAT("Chat", "💬", 0xFF2196F3, "chat_list"),
    CONFIG("Configuración", "⚙️", 0xFF9E9E9E, "settings")
}
