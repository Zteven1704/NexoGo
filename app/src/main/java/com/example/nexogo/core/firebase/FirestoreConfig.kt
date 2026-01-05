package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Configuración global de Firestore
 * Habilita persistencia offline (cache + cola de escrituras)
 */
object FirestoreConfig {
    private const val TAG = "FirestoreConfig"
    
    /**
     * Habilita persistencia offline en Firestore
     * Debe llamarse antes de cualquier operación de Firestore
     * Idealmente en Application.onCreate() o MainActivity.onCreate()
     */
    fun enablePersistence() {
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Habilita modo offline (cache + cola)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED) // Cache ilimitado
                .build()
            
            db.firestoreSettings = settings
            Log.d(TAG, "✅ Persistencia offline habilitada en Firestore")
        } catch (e: Exception) {
            // Si la persistencia ya está habilitada, esto lanzará una excepción
            // Es seguro ignorarla
            Log.w(TAG, "⚠️ Persistencia ya habilitada o error al habilitar: ${e.message}")
        }
    }
    
    /**
     * Obtiene la instancia de Firestore configurada
     */
    fun getInstance(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}




