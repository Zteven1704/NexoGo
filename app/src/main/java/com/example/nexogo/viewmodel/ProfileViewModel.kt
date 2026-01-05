package com.example.nexogo.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _state = MutableStateFlow<ProfileState>(ProfileState.Success)
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        println("DEBUG: ProfileViewModel - UID del usuario autenticado: $uid")
        
        if (uid == null) {
            println("DEBUG: ProfileViewModel - No hay usuario autenticado, intentando cargar desde DataStore...")
            loadUserFromDataStore()
            return
        }
        
        _isLoading.value = true
        _state.value = ProfileState.Loading
        
        viewModelScope.launch {
            try {
                println("DEBUG: ProfileViewModel - Cargando perfil del usuario desde Firestore: $uid")
                
                val doc = firestore.collection("usuarios").document(uid).get().await()
                
                if (doc.exists()) {
                    val data = doc.data ?: throw Exception("Datos de usuario no disponibles")
                    println("DEBUG: ProfileViewModel - Datos encontrados en Firestore: $data")
                    
                    // Debug específico para el rol
                    val rolFromFirestore = data["rol"] as? String
                    val roleFromFirestore = data["role"] as? String
                    val rolFromFirestore2 = data["Rol"] as? String
                    val roleFromFirestore2 = data["Role"] as? String
                    
                    println("DEBUG: ProfileViewModel - Campos de rol encontrados:")
                    println("  - rol: '$rolFromFirestore'")
                    println("  - role: '$roleFromFirestore'")
                    println("  - Rol: '$rolFromFirestore2'")
                    println("  - Role: '$roleFromFirestore2'")
                    
                    // Intentar obtener el rol de diferentes campos posibles
                    val finalRole = rolFromFirestore ?: roleFromFirestore ?: rolFromFirestore2 ?: roleFromFirestore2 ?: "USER"
                    println("DEBUG: ProfileViewModel - Rol final seleccionado: '$finalRole'")
                    
                    val user = User(
                        id = data["uid"] as? String ?: uid,
                        name = data["nombre"] as? String ?: "",
                        email = data["correo"] as? String ?: "",
                        phone = data["telefono"] as? String ?: "",
                        whatsapp = data["whatsapp"] as? String ?: "",
                        profileImageUrl = data["profileImageUrl"] as? String ?: "",
                        role = try {
                            com.example.nexogo.core.models.UserRole.valueOf(finalRole)
                        } catch (e: Exception) {
                            println("DEBUG: ProfileViewModel - Error parseando rol '$finalRole': ${e.message}")
                            com.example.nexogo.core.models.UserRole.USER
                        },
                        isApproved = data["isApproved"] as? Boolean ?: false,
                        fcmToken = data["token"] as? String,
                        isProfessional = data["isProfessional"] as? Boolean ?: false,
                        specialization = data["specialization"] as? String ?: "",
                        licenseNumber = data["licenseNumber"] as? String ?: "",
                        language = data["language"] as? String ?: "es",
                        isActive = data["isActive"] as? Boolean ?: true,
                        createdAt = data["fechaRegistro"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                        updatedAt = data["fechaActualizacion"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                    )
                    
                    println("DEBUG: ProfileViewModel - Usuario creado con rol: ${user.role}")
                    
                    // Si es admin@nexogo.com pero no tiene rol ADMIN, corregirlo
                    if (user.email == "admin@nexogo.com" && user.role != com.example.nexogo.core.models.UserRole.ADMIN) {
                        println("DEBUG: ProfileViewModel - Admin detectado con rol incorrecto, corrigiendo...")
                        fixAdminRole()
                        return@launch
                    }
                    
                    _user.value = user
                    _state.value = ProfileState.Success
                    println("DEBUG: ProfileViewModel - Perfil cargado exitosamente desde Firestore: ${user.name}")
                } else {
                    println("DEBUG: ProfileViewModel - Perfil no encontrado en Firestore para UID: $uid, intentando DataStore...")
                    loadUserFromDataStore()
                }
                
            } catch (e: Exception) {
                println("DEBUG: ProfileViewModel - Error cargando desde Firestore: ${e.message}, intentando DataStore...")
                loadUserFromDataStore()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadUserFromDataStore() {
        viewModelScope.launch {
            try {
                println("DEBUG: ProfileViewModel - No hay usuario autenticado en Firebase")
                _state.value = ProfileState.Error("Usuario no autenticado. Por favor, inicia sesión.")
                println("DEBUG: ProfileViewModel - Usuario no autenticado")
                
            } catch (e: Exception) {
                _state.value = ProfileState.Error("Error al cargar el perfil: ${e.message}")
                println("DEBUG: ProfileViewModel - Error cargando usuario: ${e.message}")
            }
        }
    }
    
    fun updateUserProfile(name: String, phone: String, whatsapp: String, photoUri: Uri?) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _state.value = ProfileState.Error("Usuario no autenticado")
            return
        }
        
        _isLoading.value = true
        _state.value = ProfileState.Loading
        
        viewModelScope.launch {
            try {
                println("DEBUG: ProfileViewModel - Actualizando perfil: $name, $phone, $whatsapp")
                println("DEBUG: ProfileViewModel - UID: $uid")
                
                suspend fun updateFirestore(photoUrl: String?) {
                    try {
                        val updates = hashMapOf<String, Any>(
                            "nombre" to name,
                            "telefono" to phone,
                            "whatsapp" to whatsapp,
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        )
                        if (photoUrl != null) {
                            updates["profileImageUrl"] = photoUrl
                        }
                        
                        // Primero verificar si el documento existe
                        val docRef = firestore.collection("usuarios").document(uid)
                        val doc = docRef.get().await()
                        
                        if (doc.exists()) {
                            // El documento existe, actualizarlo
                            println("DEBUG: ProfileViewModel - Documento existe, actualizando...")
                            docRef.update(updates).await()
                        } else {
                            // El documento no existe, crearlo
                            println("DEBUG: ProfileViewModel - Documento no existe, creándolo...")
                            val userData = hashMapOf<String, Any>(
                                "uid" to uid,
                                "nombre" to name,
                                "correo" to (auth.currentUser?.email ?: ""),
                                "telefono" to phone,
                                "whatsapp" to whatsapp,
                                "rol" to "USER",
                                "estado" to "aprobado",
                                "isApproved" to true,
                                "isProfessional" to false,
                                "specialization" to "",
                                "licenseNumber" to "",
                                "language" to "es",
                                "isActive" to true,
                                "fechaRegistro" to com.google.firebase.Timestamp.now(),
                                "fechaActualizacion" to com.google.firebase.Timestamp.now()
                            )
                            if (photoUrl != null) {
                                userData["profileImageUrl"] = photoUrl
                            }
                            
                            docRef.set(userData).await()
                            println("DEBUG: ProfileViewModel - Documento creado exitosamente")
                        }
                        
                        // Actualizar el usuario local
                        val updatedUser = User(
                            id = uid,
                            name = name,
                            email = auth.currentUser?.email ?: "",
                            phone = phone,
                            whatsapp = whatsapp,
                            profileImageUrl = photoUrl?.toString() ?: "",
                            role = com.example.nexogo.core.models.UserRole.USER,
                            isApproved = true,
                            fcmToken = null,
                            isProfessional = false,
                            specialization = "",
                            licenseNumber = "",
                            language = "es",
                            isActive = true,
                            createdAt = com.google.firebase.Timestamp.now(),
                            updatedAt = com.google.firebase.Timestamp.now()
                        )
                        
                        _user.value = updatedUser
                        _state.value = ProfileState.Success
                        _message.value = "Perfil actualizado exitosamente"
                        _isLoading.value = false
                        
                        println("DEBUG: ProfileViewModel - Perfil actualizado exitosamente")
                        
                    } catch (e: Exception) {
                        println("DEBUG: ProfileViewModel - Error en updateFirestore: ${e.message}")
                        _state.value = ProfileState.Error("Error al actualizar el perfil: ${e.message}")
                        _isLoading.value = false
                    }
                }
                
                if (photoUri != null) {
                    println("DEBUG: ProfileViewModel - Subiendo imagen a Storage...")
                    val ref = storage.reference.child("profile_images/$uid.jpg")
                    ref.putFile(photoUri).continueWithTask { task ->
                        if (task.isSuccessful) {
                            ref.downloadUrl
                        } else {
                            throw task.exception ?: Exception("Error subiendo imagen")
                        }
                    }.addOnSuccessListener { uri ->
                        println("DEBUG: ProfileViewModel - Imagen subida exitosamente: $uri")
                        viewModelScope.launch {
                            updateFirestore(uri.toString())
                        }
                    }.addOnFailureListener { e ->
                        println("DEBUG: ProfileViewModel - Error subiendo imagen: ${e.message}")
                        _state.value = ProfileState.Error("Error al subir la foto: ${e.message}")
                        _isLoading.value = false
                    }
                } else {
                    println("DEBUG: ProfileViewModel - Actualizando solo datos de texto...")
                    updateFirestore(null)
                }
                
            } catch (e: Exception) {
                println("DEBUG: ProfileViewModel - Error en updateUserProfile: ${e.message}")
                _state.value = ProfileState.Error("Error al actualizar el perfil: ${e.message}")
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
    
    fun clearError() {
        _state.value = ProfileState.Success
    }
    
    /**
     * Corregir el rol del usuario admin si es necesario
     */
    fun fixAdminRole() {
        val uid = auth.currentUser?.uid
        if (uid == null) return
        
        viewModelScope.launch {
            try {
                val doc = firestore.collection("usuarios").document(uid).get().await()
                if (doc.exists()) {
                    val data = doc.data ?: return@launch
                    val email = data["correo"] as? String ?: ""
                    
                    // Si es admin@nexogo.com, asegurar que tenga rol ADMIN
                    if (email == "admin@nexogo.com") {
                        println("DEBUG: ProfileViewModel - Detectado admin@nexogo.com, corrigiendo rol...")
                        
                        val updates = hashMapOf<String, Any>(
                            "rol" to "ADMIN",
                            "role" to "ADMIN",
                            "isApproved" to true,
                            "isProfessional" to false,
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        )
                        
                        firestore.collection("usuarios").document(uid).update(updates).await()
                        println("DEBUG: ProfileViewModel - Rol de admin corregido exitosamente")
                        
                        // Recargar perfil
                        loadUserProfile()
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: ProfileViewModel - Error corrigiendo rol de admin: ${e.message}")
            }
        }
    }
}

sealed class ProfileState {
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}
