package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    private val authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    
    companion object {
        @Volatile
        private var INSTANCE: AuthViewModel? = null
        
        fun getInstance(): AuthViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthViewModel().also { INSTANCE = it }
            }
        }
    }

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _pendingUsers = MutableStateFlow<List<User>>(emptyList())
    val pendingUsers: StateFlow<List<User>> = _pendingUsers.asStateFlow()

    init {
        // Create default admin user
        val adminUser = User(
            id = "admin_uid_123",
            email = "admin@nexogo.com",
            name = "Administrador",
            phone = "1234567890",
            whatsapp = "1234567890",
            role = UserRole.ADMIN,
            isApproved = true
        )
        _currentUser.value = adminUser
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            currentUser = adminUser
        )
    }

    fun register(email: String, password: String, name: String, role: UserRole, phone: String = "") {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
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
                
                // Register with Firebase
                val result = authRepository.registerUser(
                    email = email,
                    password = password,
                    name = name,
                    role = role,
                    phone = phone,
                    // address = address // Campo no disponible en User
                )
                
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    if (role == UserRole.USER) {
                        // USERs are approved immediately
                        _currentUser.value = user
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            currentUser = user,
                            isAuthenticated = true,
                            message = "¡Registro exitoso!"
                        )
                    } else {
                        // Professionals need approval
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "¡Registro exitoso! Tu cuenta está pendiente de aprobación del administrador."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al registrar usuario"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun login(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
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
                
                // Login with Firebase
                val result = authRepository.loginUser(email, password)
                
                if (result.isSuccess) {
                    val user = result.getOrThrow()
                    _currentUser.value = user
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        isAuthenticated = true,
                        message = "¡Bienvenido!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val result = authRepository.logout()
                if (result.isSuccess) {
                    _currentUser.value = null
                    _uiState.value = AuthUiState() // Reset state on logout
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Error al cerrar sesión"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error al cerrar sesión"
                )
            }
        }
    }

    fun resetPassword(email: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                // Simular reset de contraseña por ahora
                val result = Result.success(Unit)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        message = "Se ha enviado un email de restablecimiento a $email"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al enviar email de restablecimiento"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
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
        }
    }
    
    // Admin functions
    fun approveUser(userId: String) {
        val user = _pendingUsers.value.find { it.id == userId }
        if (user != null) {
            // Remove from pending and add as approved
            _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
            val approvedUser = user.copy(
                role = if (user.role == UserRole.VET) UserRole.VET else user.role,
                isApproved = true
            )
            _uiState.value = _uiState.value.copy(
                message = "Usuario ${approvedUser.name} aprobado exitosamente"
            )
        }
    }
    
    fun rejectUser(userId: String) {
        _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
        _uiState.value = _uiState.value.copy(
            message = "Usuario rechazado"
        )
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun updateUserProfile(updatedUser: User) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
        viewModelScope.launch {
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(500)
                
                // Update the current user
                _currentUser.value = updatedUser
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = updatedUser,
                    message = "Perfil actualizado exitosamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    error = e.message ?: "Error al actualizar el perfil"
                )
            }
        }
    }
}
