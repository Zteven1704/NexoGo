package com.example.nexogo.modules.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel para gestión de perfil de usuario
 */
class ProfileViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    fun loadUserProfile(userId: String) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getDocument("users", userId)
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        val user = User(
                            id = userId,
                            email = data["email"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            phone = data["phone"] as? String ?: "",
                            whatsapp = data["whatsapp"] as? String ?: "",
                            profileImageUrl = data["profileImageUrl"] as? String ?: "",
                            isApproved = data["isApproved"] as? Boolean ?: false
                        )
                        _user.value = user
                        _message.value = "Perfil cargado exitosamente"
                        Log.d("NEXOGO_PROFILE", "Perfil cargado: ${user.name}")
                    } else {
                        _message.value = "Error: No se encontró el perfil"
                        Log.e("NEXOGO_PROFILE", "No se encontró el perfil para: $userId")
                    }
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PROFILE", "Error cargando perfil: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PROFILE", "Excepción cargando perfil: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProfile(
        userId: String,
        name: String,
        phone: String,
        whatsapp: String,
        profileImageUri: Uri? = null
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                var profileImageUrl = _user.value?.profileImageUrl ?: ""
                
                // Subir imagen de perfil si se proporciona
                if (profileImageUri != null) {
                    val imagePath = "users/$userId/profile_pics/profile_${System.currentTimeMillis()}.jpg"
                    val uploadResult = repository.uploadFile(imagePath, profileImageUri)
                    if (uploadResult.isSuccess) {
                        profileImageUrl = uploadResult.getOrNull() ?: ""
                        Log.d("NEXOGO_PROFILE", "Imagen de perfil subida: $profileImageUrl")
                    } else {
                        _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_PROFILE", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                        return@launch
                    }
                }
                
                // Actualizar datos del usuario
                val updateData = mapOf(
                    "name" to name,
                    "phone" to phone,
                    "whatsapp" to whatsapp,
                    "profileImageUrl" to profileImageUrl,
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("users", userId, updateData)
                if (result.isSuccess) {
                    // Actualizar el estado local
                    _user.value = _user.value?.copy(
                        name = name,
                        phone = phone,
                        whatsapp = whatsapp,
                        profileImageUrl = profileImageUrl
                    )
                    _message.value = "Perfil actualizado exitosamente"
                    Log.d("NEXOGO_PROFILE", "Perfil actualizado: $name")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PROFILE", "Error actualizando perfil: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PROFILE", "Excepción actualizando perfil: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun uploadProfileImage(userId: String, imageUri: Uri) {
        _isLoading.value = true
        _uploadProgress.value = 0f
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val imagePath = "users/$userId/profile_pics/profile_${System.currentTimeMillis()}.jpg"
                val uploadResult = repository.uploadFile(imagePath, imageUri)
                
                if (uploadResult.isSuccess) {
                    val imageUrl = uploadResult.getOrNull() ?: ""
                    
                    // Actualizar la URL de la imagen en Firestore
                    val updateData = mapOf(
                        "profileImageUrl" to imageUrl,
                        "updatedAt" to repository.getCurrentTimestamp()
                    )
                    
                    val updateResult = repository.updateDocument("users", userId, updateData)
                    if (updateResult.isSuccess) {
                        _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                        _message.value = "Imagen de perfil actualizada exitosamente"
                        _uploadProgress.value = 1f
                        Log.d("NEXOGO_PROFILE", "Imagen de perfil actualizada: $imageUrl")
                    } else {
                        _message.value = "Error actualizando imagen: ${updateResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_PROFILE", "Error actualizando imagen: ${updateResult.exceptionOrNull()?.message}")
                    }
                } else {
                    _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PROFILE", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PROFILE", "Excepción subiendo imagen: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
    
    fun clearUploadProgress() {
        _uploadProgress.value = 0f
    }
}

