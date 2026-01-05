package com.example.nexogo.modules.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la autenticación con Firebase
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(FirebaseRepository())
) : ViewModel() {
    
    // Estados de autenticación
    private val _authState = MutableStateFlow(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // Usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Estados de formularios
    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()
    
    init {
        checkAuthState()
    }
    
    /**
     * Verificar estado de autenticación actual
     */
    fun checkAuthState() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                if (authRepository.isUserAuthenticated()) {
                    val userResult = authRepository.getCurrentUserData()
                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()
                        if (user != null) {
                            _currentUser.value = user
                            _authState.value = AuthState.Authenticated
                        } else {
                            _authState.value = AuthState.Unauthenticated
                        }
                    } else {
                        _authState.value = AuthState.Error
                        _errorMessage.value = userResult.exceptionOrNull()?.message
                    }
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error
                _errorMessage.value = e.message
            }
        }
    }
    
    /**
     * Iniciar sesión
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = ""
            _errorMessage.value = null
            
            try {
                val result = authRepository.login(email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    _message.value = "¡Bienvenido, ${user?.name}!"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Registrar nuevo usuario
     */
    fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        phone: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = ""
            _errorMessage.value = null
            
            try {
                val result = authRepository.register(email, password, name, role, phone)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                    _message.value = "¡Cuenta creada exitosamente, ${user?.name}!"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al crear cuenta"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cerrar sesión
     */
    fun logout() {
        viewModelScope.launch {
            try {
                val result = authRepository.logout()
                if (result.isSuccess) {
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                    _message.value = "Sesión cerrada exitosamente"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al cerrar sesión"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error inesperado"
            }
        }
    }
    
    /**
     * Enviar email de recuperación de contraseña
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = ""
            _errorMessage.value = null
            
            try {
                val result = authRepository.resetPassword(email)
                if (result.isSuccess) {
                    _message.value = "Se ha enviado un email de recuperación a $email"
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error al enviar email"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error inesperado"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cambiar modo de formulario (login/registro)
     */
    fun toggleLoginMode() {
        _isLoginMode.value = !_isLoginMode.value
        clearMessages()
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _message.value = ""
        _errorMessage.value = null
    }
    
    /**
     * Obtener rol del usuario actual
     */
    fun getCurrentUserRole(): UserRole? {
        return _currentUser.value?.role
    }
    
    /**
     * Verificar si el usuario es administrador
     */
    fun isAdmin(): Boolean {
        return _currentUser.value?.role == UserRole.ADMIN
    }
    
    /**
     * Verificar si el usuario es veterinario
     */
    fun isVeterinarian(): Boolean {
        return _currentUser.value?.role == UserRole.VET
    }
    
    /**
     * Verificar si el usuario es auxiliar
     */
    fun isVET_ASSISTANT(): Boolean {
        return _currentUser.value?.role == UserRole.VET_ASSISTANT
    }
    
    /**
     * Verificar si el usuario es paciente
     */
    fun isUSER(): Boolean {
        return _currentUser.value?.role == UserRole.USER
    }
    
    /**
     * Obtener nombre del usuario actual
     */
    fun getCurrentUserName(): String {
        return _currentUser.value?.name ?: "Usuario"
    }
    
    /**
     * Obtener email del usuario actual
     */
    fun getCurrentUserEmail(): String {
        return _currentUser.value?.email ?: ""
    }
}