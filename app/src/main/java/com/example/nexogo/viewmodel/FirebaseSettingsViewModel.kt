package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.manager.FirebaseAuthManager
import com.example.nexogo.core.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseSettingsViewModel(private val context: Context) : ViewModel() {
    
    private val authManager = FirebaseAuthManager(context)
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        val firebaseUser = authManager.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                try {
                    val result = authManager.getUserFromFirestore(firebaseUser.uid)
                    if (result.isSuccess) {
                        _currentUser.value = result.getOrNull()
                    }
                } catch (e: Exception) {
                    _message.value = "Error al cargar usuario: ${e.message}"
                }
            }
        }
    }
    
    fun updateUser(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authManager.updateUserInFirestore(user)
                if (result.isSuccess) {
                    _currentUser.value = user
                    _message.value = "Usuario actualizado exitosamente"
                } else {
                    _message.value = "Error al actualizar usuario: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePassword(currentPassword: String, newPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Primero reautenticar
                val reauthResult = authManager.reauthenticateWithPassword(currentPassword)
                if (reauthResult.isSuccess) {
                    // Luego actualizar la contraseña
                    val updateResult = authManager.updatePassword(newPassword)
                    if (updateResult.isSuccess) {
                        _message.value = "Contraseña actualizada exitosamente"
                    } else {
                        _message.value = "Error al actualizar contraseña: ${updateResult.exceptionOrNull()?.message}"
                    }
                } else {
                    _message.value = "Error de autenticación: ${reauthResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar contraseña: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateEmail(newEmail: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authManager.updateEmail(newEmail)
                if (result.isSuccess) {
                    _message.value = "Email actualizado exitosamente"
                } else {
                    _message.value = "Error al actualizar email: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar email: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun sendPasswordResetEmail(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authManager.sendPasswordResetEmail(email)
                if (result.isSuccess) {
                    _message.value = "Email de recuperación enviado"
                } else {
                    _message.value = "Error al enviar email: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al enviar email: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAccount() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    // Eliminar de Firestore
                    val deleteFromFirestoreResult = authManager.deleteUserFromFirestore(currentUser.id)
                    if (deleteFromFirestoreResult.isSuccess) {
                        // Eliminar de Firebase Auth
                        val deleteFromAuthResult = authManager.deleteUser()
                        if (deleteFromAuthResult.isSuccess) {
                            _currentUser.value = null
                            _message.value = "Cuenta eliminada exitosamente"
                        } else {
                            _message.value = "Error al eliminar cuenta: ${deleteFromAuthResult.exceptionOrNull()?.message}"
                        }
                    } else {
                        _message.value = "Error al eliminar cuenta: ${deleteFromFirestoreResult.exceptionOrNull()?.message}"
                    }
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar cuenta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}

