package com.example.nexogo.network

import android.app.Application
import com.example.nexogo.firebase.FirebaseConfig
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.nexogo.repository.FirebaseFirestoreRepository
import com.example.nexogo.repository.FirebaseMessagingRepository
import com.example.nexogo.repository.FirebaseStorageRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio principal de Firebase para NexoGo
 * Maneja la inicialización y configuración de todos los servicios de Firebase
 */
@Singleton
class FirebaseService @Inject constructor(
    private val application: Application
) {
    
    // Repositorios de Firebase
    val authRepository: FirebaseAuthRepository by lazy { 
        FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) 
    }
    val firestoreRepository: FirebaseFirestoreRepository by lazy { FirebaseFirestoreRepository() }
    val storageRepository: FirebaseStorageRepository by lazy { FirebaseStorageRepository() }
    val messagingRepository: FirebaseMessagingRepository by lazy { FirebaseMessagingRepository() }
    
    /**
     * Inicializa Firebase y todos sus servicios
     */
    fun initialize() {
        try {
            // Inicializar Firebase
            FirebaseConfig.initialize()
            
            // Configurar Firestore para persistencia offline
            configureFirestoreOfflinePersistence()
            
            // Configurar notificaciones
            configureMessaging()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Configura la persistencia offline de Firestore
     */
    private fun configureFirestoreOfflinePersistence() {
        try {
            val firestore = FirebaseConfig.firestore
            // Firestore ya tiene persistencia offline habilitada por defecto
            // Solo necesitamos asegurarnos de que esté habilitada
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Configura Firebase Messaging
     */
    private fun configureMessaging() {
        try {
            // Obtener token FCM
            // El token se obtendrá cuando sea necesario
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Verifica si Firebase está correctamente inicializado
     */
    fun isInitialized(): Boolean {
        return FirebaseConfig.isInitialized()
    }
    
    /**
     * Limpia todos los recursos de Firebase
     */
    fun cleanup() {
        FirebaseConfig.clear()
    }
}

