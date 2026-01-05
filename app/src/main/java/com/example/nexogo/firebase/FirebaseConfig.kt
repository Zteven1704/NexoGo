package com.example.nexogo.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Configuración centralizada de Firebase para NexoGo
 * Maneja la inicialización y acceso a todos los servicios de Firebase
 */
object FirebaseConfig {
    
    // Instancias de Firebase
    private var _auth: FirebaseAuth? = null
    private var _firestore: FirebaseFirestore? = null
    private var _storage: FirebaseStorage? = null
    private var _messaging: FirebaseMessaging? = null
    
    /**
     * Inicializa Firebase si no está ya inicializado
     */
    fun initialize() {
        // Firebase se inicializa automáticamente cuando se incluye google-services.json
        // No necesitamos inicialización manual
    }
    
    /**
     * Obtiene la instancia de Firebase Auth
     */
    val auth: FirebaseAuth
        get() {
            if (_auth == null) {
                _auth = FirebaseAuth.getInstance()
            }
            return _auth!!
        }
    
    /**
     * Obtiene la instancia de Firestore
     */
    val firestore: FirebaseFirestore
        get() {
            if (_firestore == null) {
                _firestore = FirebaseFirestore.getInstance()
                // Configurar persistencia offline
                _firestore?.enableNetwork()
            }
            return _firestore!!
        }
    
    /**
     * Obtiene la instancia de Firebase Storage
     */
    val storage: FirebaseStorage
        get() {
            if (_storage == null) {
                _storage = FirebaseStorage.getInstance()
            }
            return _storage!!
        }
    
    /**
     * Obtiene la instancia de Firebase Messaging
     */
    val messaging: FirebaseMessaging
        get() {
            if (_messaging == null) {
                _messaging = FirebaseMessaging.getInstance()
            }
            return _messaging!!
        }
    
    /**
     * Verifica si Firebase está inicializado
     */
    fun isInitialized(): Boolean {
        return try {
            FirebaseApp.getInstance() != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Limpia las instancias (útil para testing)
     */
    fun clear() {
        _auth = null
        _firestore = null
        _storage = null
        _messaging = null
    }
}
