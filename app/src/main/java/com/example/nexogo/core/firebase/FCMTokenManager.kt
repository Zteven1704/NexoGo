package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de tokens FCM para notificaciones push
 */
@Singleton
class FCMTokenManager @Inject constructor() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    
    companion object {
        private const val TAG = "FCMTokenManager"
        private const val COLLECTION_USERS = "users"
    }
    
    /**
     * Registra o actualiza el token FCM del usuario actual
     */
    suspend fun registerToken(): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val token = messaging.token.await()
            
            // Guardar token en Firestore
            firestore.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .update("tokenFCM", token)
                .await()
            
            Log.d(TAG, "✅ Token FCM registrado: ${currentUser.uid}")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al registrar token FCM: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el token FCM del usuario actual
     */
    suspend fun getToken(): Result<String?> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))
            
            val doc = firestore.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val token = doc.getString("tokenFCM")
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener token FCM: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina el token FCM del usuario (al hacer logout)
     */
    suspend fun deleteToken(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.success(Unit) // Si no hay usuario, no hay nada que eliminar
            
            firestore.collection(COLLECTION_USERS)
                .document(currentUser.uid)
                .update("tokenFCM", null)
                .await()
            
            Log.d(TAG, "✅ Token FCM eliminado: ${currentUser.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar token FCM: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Configura listener para cuando el token cambie
     */
    fun setupTokenRefreshListener(onTokenRefresh: (String) -> Unit) {
        messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "🔄 Token FCM refrescado: $token")
                onTokenRefresh(token)
            } else {
                Log.e(TAG, "❌ Error al obtener token FCM: ${task.exception?.message}")
            }
        }
    }
}




