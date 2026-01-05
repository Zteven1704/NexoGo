package com.example.nexogo.modules.auth

import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Repositorio para operaciones de autenticación con Firebase
 */
class AuthRepository(
    private val firebaseRepository: FirebaseRepository
) {
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Iniciar sesión con email y contraseña
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Obtener datos del usuario desde Firestore
                val user = firebaseRepository.getUserById(firebaseUser.uid)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("No se encontraron datos del usuario"))
                }
            } else {
                Result.failure(Exception("Error al iniciar sesión"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registrar nuevo usuario
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        phone: String? = null
    ): Result<User> {
        return try {
            // Crear usuario en Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Crear documento de usuario en Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    phone = phone ?: "",
                    role = role,
                    isApproved = role == UserRole.USER || role == UserRole.ADMIN, // Pacientes y admins aprobados automáticamente
                    createdAt = com.google.firebase.Timestamp.now()
                )
                
                // Guardar en Firestore
                val userData = mapOf(
                    "id" to user.id,
                    "email" to user.email,
                    "name" to user.name,
                    "phone" to user.phone,
                    "whatsapp" to user.whatsapp,
                    "role" to user.role.name,
                    "profileImageUrl" to user.profileImageUrl,
                    "isApproved" to user.isApproved,
                    "createdAt" to user.createdAt,
                    "updatedAt" to user.updatedAt
                )
                
                val saveResult = firebaseRepository.createDocument("users", firebaseUser.uid, userData)
                if (saveResult.isSuccess) {
                    Result.success(user)
                } else {
                    // Si falla al guardar en Firestore, eliminar el usuario de Auth
                    firebaseUser.delete().await()
                    Result.failure(saveResult.exceptionOrNull() ?: Exception("Error al guardar datos del usuario"))
                }
            } else {
                Result.failure(Exception("Error al crear usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cerrar sesión
     */
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Enviar email de recuperación de contraseña
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener usuario actual autenticado
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Verificar si hay un usuario autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Obtener datos del usuario actual desde Firestore
     */
    suspend fun getCurrentUserData(): Result<User?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val user = firebaseRepository.getUserById(currentUser.uid)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("No se encontraron datos del usuario"))
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener rol del usuario actual
     */
    suspend fun getCurrentUserRole(): Result<UserRole?> {
        return try {
            val userResult = getCurrentUserData()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                Result.success(user?.role)
            } else {
                Result.failure(userResult.exceptionOrNull() ?: Exception("Error al obtener rol"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verificar estado de autenticación
     */
    fun checkAuthState(): Flow<AuthState> = flow {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Verificar que el usuario existe en Firestore
                val userData = firebaseRepository.getUserById(currentUser.uid)
                if (userData != null) {
                    emit(AuthState.Authenticated)
                } else {
                    emit(AuthState.Unauthenticated)
                }
            } else {
                emit(AuthState.Unauthenticated)
            }
        } catch (e: Exception) {
            emit(AuthState.Error)
        }
    }
    
    /**
     * Eliminar cuenta del usuario actual
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Eliminar datos de Firestore primero
                firebaseRepository.deleteDocument("users", currentUser.uid)
                
                // Eliminar cuenta de Auth
                currentUser.delete().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No hay usuario autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar perfil del usuario
     */
    suspend fun updateProfile(
        name: String? = null,
        phone: String? = null,
        whatsApp: String? = null
    ): Result<User> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userData = firebaseRepository.getUserById(currentUser.uid)
                if (userData != null) {
                    val updatedUser = userData.copy(
                        name = name ?: userData.name,
                        phone = phone ?: userData.phone,
                        whatsapp = whatsApp ?: userData.whatsapp
                    )
                    
                    val userData = mapOf(
                        "name" to updatedUser.name,
                        "phone" to updatedUser.phone,
                        "whatsapp" to updatedUser.whatsapp,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                    
                    val saveResult = firebaseRepository.updateDocument("users", currentUser.uid, userData)
                    if (saveResult.isSuccess) {
                        Result.success(updatedUser)
                    } else {
                        Result.failure(saveResult.exceptionOrNull() ?: Exception("Error al actualizar perfil"))
                    }
                } else {
                    Result.failure(Exception("No se encontraron datos del usuario"))
                }
            } else {
                Result.failure(Exception("No hay usuario autenticado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Estados de autenticación
 */
enum class AuthState {
    Loading,
    Authenticated,
    Unauthenticated,
    Error
}
