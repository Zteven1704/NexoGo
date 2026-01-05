package com.example.nexogo.repository

import com.example.nexogo.firebase.FirebaseConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar notificaciones push con Firebase Messaging
 * Incluye suscripción a topics, envío de notificaciones y manejo de tokens
 */
@Singleton
class FirebaseMessagingRepository @Inject constructor() {
    
    private val messaging: FirebaseMessaging = FirebaseConfig.messaging
    
    /**
     * Obtiene el token FCM del dispositivo
     */
    suspend fun getFCMToken(): Result<String> {
        return try {
            val token = messaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Suscribe el dispositivo a un topic
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            messaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Desuscribe el dispositivo de un topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Suscribe a notificaciones de citas
     */
    suspend fun subscribeToAppointmentNotifications(): Result<Unit> {
        return subscribeToTopic("appointments")
    }
    
    /**
     * Suscribe a notificaciones de chat
     */
    suspend fun subscribeToChatNotifications(): Result<Unit> {
        return subscribeToTopic("chat")
    }
    
    /**
     * Suscribe a notificaciones de inventario
     */
    suspend fun subscribeToInventoryNotifications(): Result<Unit> {
        return subscribeToTopic("inventory")
    }
    
    /**
     * Suscribe a notificaciones de administración
     */
    suspend fun subscribeToAdminNotifications(): Result<Unit> {
        return subscribeToTopic("admin")
    }
    
    /**
     * Suscribe a notificaciones específicas de usuario
     */
    suspend fun subscribeToUserNotifications(userId: String): Result<Unit> {
        return subscribeToTopic("user_$userId")
    }
    
    /**
     * Desuscribe de todas las notificaciones de usuario
     */
    suspend fun unsubscribeFromUserNotifications(userId: String): Result<Unit> {
        return unsubscribeFromTopic("user_$userId")
    }
    
    /**
     * Refresca el token FCM
     */
    suspend fun refreshToken(): Result<String> {
        return try {
            messaging.deleteToken().await()
            val newToken = messaging.token.await()
            Result.success(newToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Configura notificaciones por defecto para un rol
     */
    suspend fun setupNotificationsForRole(role: String): Result<Unit> {
        return try {
            when (role.lowercase()) {
                "admin" -> {
                    subscribeToAdminNotifications()
                    subscribeToAppointmentNotifications()
                    subscribeToInventoryNotifications()
                    subscribeToChatNotifications()
                }
                "veterinario", "auxiliar" -> {
                    subscribeToAppointmentNotifications()
                    subscribeToInventoryNotifications()
                    subscribeToChatNotifications()
                }
                "paciente" -> {
                    subscribeToAppointmentNotifications()
                    subscribeToChatNotifications()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

