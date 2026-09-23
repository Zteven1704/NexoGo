package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.data.AppDataStore
import com.example.nexogo.platform.audit.AuditLogger
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.UsageAnalytics
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PersistentAuthViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        @Volatile
        private var INSTANCE: PersistentAuthViewModel? = null
        
        fun getInstance(context: Context): PersistentAuthViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PersistentAuthViewModel(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Sample profile images for different roles
        private fun getSampleProfileImage(role: UserRole): String {
            return when (role) {
                UserRole.ADMIN -> "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=150&h=150&fit=crop&crop=face"
                UserRole.VET -> "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=150&h=150&fit=crop&crop=face"
                UserRole.VET_ASSISTANT -> "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face"
                UserRole.USER -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face"
            }
        }
    }
    
    private val dataStore = AppDataStore(context)
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseRepository = FirebaseAuthRepository(firebaseAuth, firestore)
    private val companySessionManager = CompanySessionManager.getInstance(context)
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _pendingUsers = MutableStateFlow<List<User>>(emptyList())
    val pendingUsers: StateFlow<List<User>> = _pendingUsers.asStateFlow()

    init {
        // Load user data from DataStore on initialization
        loadUserFromDataStore()
    }
    
    private fun loadUserFromDataStore() {
        viewModelScope.launch {
            try {
                println("DEBUG: Cargando usuario...")
                
                // Primero verificar si hay un usuario autenticado en Firebase
                val currentFirebaseUser = firebaseAuth.currentUser
                if (currentFirebaseUser != null) {
                    println("DEBUG: Usuario autenticado en Firebase: ${currentFirebaseUser.uid}")
                    
                    // Cargar datos desde Firebase
                    val result = firebaseRepository.getUserById(currentFirebaseUser.uid)
                    if (result.isSuccess) {
                        val firebaseUser = result.getOrThrow()
                        println("DEBUG: Usuario cargado desde Firebase: ${firebaseUser.name}")
                        
                        _currentUser.value = firebaseUser
                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            currentUser = firebaseUser
                        )
                        
                        // Actualizar caché local
                        dataStore.saveUser(firebaseUser)
                        bindCompanySession(firebaseUser)
                        println("DEBUG: Usuario cargado exitosamente desde Firebase")
                        return@launch
                    } else {
                        println("DEBUG: Error cargando usuario desde Firebase: ${result.exceptionOrNull()?.message}")
                    }
                }
                
                // Si no hay usuario autenticado o falló la carga desde Firebase, intentar desde DataStore
                println("DEBUG: Intentando cargar desde DataStore...")
                val user = dataStore.getUser().first()
                println("DEBUG: Usuario encontrado en DataStore: ${user?.name}")
                
                if (user != null) {
                    _currentUser.value = user
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUser = user
                    )
                    bindCompanySession(user)
                    println("DEBUG: Usuario cargado desde DataStore exitosamente")
                } else {
                    println("DEBUG: No hay usuario en DataStore")
                    // S0 Secure: do not auto-create admin; leave unauthenticated
                }
            } catch (e: Exception) {
                println("DEBUG: Error cargando usuario: ${e.message}")
                // Intentar recuperar usuario existente desde DataStore
                val existingUser = try {
                    dataStore.getUser().first()
                } catch (ex: Exception) {
                    null
                }
                
                if (existingUser != null) {
                    _currentUser.value = existingUser
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUser = existingUser
                    )
                    bindCompanySession(existingUser)
                    println("DEBUG: Usuario recuperado después del error: ${existingUser.name}")
                } else {
                    println("DEBUG: Sin sesión recuperable — usuario no autenticado")
                }
            }
        }
    }

    fun register(email: String, password: String, name: String, role: UserRole) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(1500)

                // Simple validation
                if (email.isBlank() || password.isBlank() || name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Todos los campos son requeridos"
                    )
                    return@launch
                }

                if (!email.contains("@")) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email inválido"
                    )
                    return@launch
                }

                if (password.length < 6) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "La contraseña debe tener al menos 6 caracteres"
                    )
                    return@launch
                }

                if (name.length < 2) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "El nombre debe tener al menos 2 caracteres"
                    )
                    return@launch
                }

                // Local/sim register: Auth session only. Tenant access = membership ACTIVE + company session.
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = name,
                    profileImageUrl = getSampleProfileImage(role),
                    role = role
                )
                _currentUser.value = user
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = user,
                    isAuthenticated = true,
                    message = "¡Registro exitoso!"
                )
                dataStore.saveUser(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun login(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                // Simulate network delay
                delay(1000)

                // Simple validation
                if (email.isBlank() || password.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email y contraseña son requeridos"
                    )
                    return@launch
                }

                if (!email.contains("@")) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Email inválido"
                    )
                    return@launch
                }

                if (password.length < 6) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "La contraseña debe tener al menos 6 caracteres"
                    )
                    return@launch
                }

                // Local/sim login: no isApproved gate. Platform hub usa company session + membership ACTIVE.
                val savedUser = dataStore.getUser().first()
                if (savedUser != null && savedUser.email == email) {
                    _currentUser.value = savedUser
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = savedUser,
                        isAuthenticated = true,
                        message = "¡Bienvenido ${savedUser.name}!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuario no encontrado. Por favor, regístrate primero."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val userSnapshot = _currentUser.value
            val companyIdSnapshot = companySessionManager.activeCompanyId
                ?: TenantContext.companyId
            try {
                println("DEBUG: Realizando logout...")

                if (userSnapshot != null) {
                    AuditLogger.logout(
                        userId = userSnapshot.id,
                        companyId = companyIdSnapshot,
                        userDisplayName = userSnapshot.name,
                        userEmail = userSnapshot.email
                    )
                }
                
                // Logout de Firebase
                val result = firebaseRepository.logout()
                if (result.isSuccess) {
                    println("DEBUG: Logout de Firebase exitoso")
                } else {
                    println("DEBUG: Error en logout de Firebase: ${result.exceptionOrNull()?.message}")
                }
                
                companySessionManager.clearSession()
                _currentUser.value = null
                _uiState.value = AuthUiState()
                
                // Solo limpiar autenticación, mantener datos del usuario como caché
                dataStore.clearUser()
                println("DEBUG: Logout completado, datos del usuario mantenidos como caché")
                
            } catch (e: Exception) {
                println("DEBUG: Error durante logout: ${e.message}")
                // Aún así limpiar el estado local
                companySessionManager.clearSession()
                _currentUser.value = null
                _uiState.value = AuthUiState()
                dataStore.clearUser()
            }
        }
    }

    fun resetPassword(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                // Simulate password reset
                delay(1000)
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Password reset email sent to $email.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Function to switch user role for testing purposes
    fun switchUserRole(role: UserRole) {
        val currentUser = _currentUser.value
        if (currentUser != null) {
            val updatedUser = currentUser.copy(role = role)
            _currentUser.value = updatedUser
            _uiState.value = _uiState.value.copy(currentUser = updatedUser)
            
            // Save updated user to DataStore
            viewModelScope.launch {
                dataStore.saveUser(updatedUser)
            }
        }
    }

    // LEGACY admin helpers — UI UserApproval eliminada del grafo Platform.
    // Autorización viva: FirebaseAuth + membership ACTIVE + company session.
    @Deprecated("Platform auth ignores isApproved; use company User Management")
    fun approveUser(userId: String) {
        val user = _pendingUsers.value.find { it.id == userId }
        if (user != null) {
            val updatedPending = _pendingUsers.value.filter { it.id != userId }
            _pendingUsers.value = updatedPending
            viewModelScope.launch {
                val gson = com.google.gson.Gson()
                dataStore.savePendingUsers(gson.toJson(updatedPending))
            }
            _uiState.value = _uiState.value.copy(
                message = "Usuario ${user.name} marcado (legacy isApproved; no afecta acceso Platform)"
            )
        }
    }

    @Deprecated("Platform auth ignores isApproved; use membership REVOKED/SUSPENDED")
    fun rejectUser(userId: String) {
        val updatedPending = _pendingUsers.value.filter { it.id != userId }
        _pendingUsers.value = updatedPending
        
        // Save updated pending users to DataStore
        viewModelScope.launch {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(updatedPending)
            dataStore.savePendingUsers(json)
        }
        
        _uiState.value = _uiState.value.copy(
            message = "Usuario rechazado (legacy; no afecta Auth Platform)"
        )
    }

    fun updateUserProfile(updatedUser: User) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                println("DEBUG: Actualizando perfil del usuario: ${updatedUser.name}")
                println("DEBUG: Datos del usuario: ${updatedUser.phone}, ${updatedUser.whatsapp}")
                println("DEBUG: Foto de perfil: ${updatedUser.profileImageUrl}")

                // Actualizar en Firebase
                val result = firebaseRepository.updateUserProfile(updatedUser)
                
                if (result.isSuccess) {
                    val firebaseUser = result.getOrThrow()
                    println("DEBUG: Perfil actualizado en Firebase exitosamente")
                    
                    // Update the current user
                    _currentUser.value = firebaseUser
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = firebaseUser,
                        message = "Perfil actualizado exitosamente"
                    )
                    
                    // Save updated user to DataStore as cache
                    dataStore.saveUser(firebaseUser)
                    println("DEBUG: Usuario guardado en DataStore como caché")
                    
                } else {
                    throw result.exceptionOrNull() ?: Exception("Error desconocido al actualizar perfil")
                }
                
            } catch (e: Exception) {
                println("DEBUG: Error al actualizar perfil: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al actualizar el perfil"
                )
            }
        }
    }
    
    fun setCurrentUser(user: User) {
        _currentUser.value = user
        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isAuthenticated = true,
            message = "Usuario cargado exitosamente"
        )
        println("DEBUG: Usuario establecido en ViewModel: ${user.name}")
        println("DEBUG: Foto de perfil establecida: ${user.profileImageUrl}")
        viewModelScope.launch {
            bindCompanySession(user)
        }
    }

    /**
     * Binds SaaS company session (S1: hub requires active company; failure is surfaced in Home).
     */
    private suspend fun bindCompanySession(user: User) {
        try {
            companySessionManager.ensureSessionForUser(
                userId = user.id,
                displayName = user.name.ifBlank { user.email }
            )
            AuditLogger.login(
                userId = user.id,
                companyId = companySessionManager.activeCompanyId ?: TenantContext.companyId,
                userDisplayName = user.name,
                userEmail = user.email
            )
            UsageAnalytics.login(
                userId = user.id,
                companyId = companySessionManager.activeCompanyId ?: TenantContext.companyId
            )
        } catch (e: Exception) {
            println("DEBUG: Company session bind skipped: ${e.message}")
            AuditLogger.login(
                userId = user.id,
                companyId = TenantContext.companyId,
                userDisplayName = user.name,
                userEmail = user.email,
                metadata = mapOf("companyBindError" to (e.message ?: "unknown"))
            )
            UsageAnalytics.login(
                userId = user.id,
                companyId = TenantContext.companyId,
                metadata = mapOf("companyBindError" to (e.message ?: "unknown"))
            )
        }
    }
    
    fun refreshUserFromDataStore() {
        viewModelScope.launch {
            try {
                println("DEBUG: Refrescando usuario desde DataStore...")
                val user = dataStore.getUser().first()
                println("DEBUG: Usuario encontrado al refrescar: ${user?.name}")
                
                if (user != null) {
                    // Solo actualizar si el usuario actual es diferente
                    val currentUser = _currentUser.value
                    if (currentUser == null || currentUser.id != user.id || 
                        currentUser.name != user.name || currentUser.phone != user.phone ||
                        currentUser.whatsapp != user.whatsapp || currentUser.profileImageUrl != user.profileImageUrl) {
                        
                        println("DEBUG: Actualizando usuario con datos más recientes")
                        _currentUser.value = user
                        _uiState.value = _uiState.value.copy(
                            isAuthenticated = true,
                            currentUser = user
                        )
                    } else {
                        println("DEBUG: Usuario ya está actualizado, no hay cambios")
                    }
                    bindCompanySession(user)
                } else {
                    println("DEBUG: No hay usuario en DataStore al refrescar")
                }
            } catch (e: Exception) {
                println("DEBUG: Error al refrescar usuario: ${e.message}")
            }
        }
    }
    
    // Método de prueba para verificar la persistencia
    fun testPersistence() {
        viewModelScope.launch {
            try {
                println("DEBUG: === PRUEBA DE PERSISTENCIA ===")
                val user = dataStore.getUser().first()
                if (user != null) {
                    println("DEBUG: Usuario encontrado en DataStore:")
                    println("  - Nombre: ${user.name}")
                    println("  - Teléfono: ${user.phone}")
                    println("  - WhatsApp: ${user.whatsapp}")
                    println("  - Foto: ${user.profileImageUrl}")
                } else {
                    println("DEBUG: NO HAY USUARIO EN DATASTORE")
                }
                println("DEBUG: === FIN PRUEBA ===")
            } catch (e: Exception) {
                println("DEBUG: Error en prueba de persistencia: ${e.message}")
            }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val message: String? = null
)
