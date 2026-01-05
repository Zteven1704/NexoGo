package com.example.nexogo

import android.app.Application
import com.example.nexogo.network.FirebaseService
import javax.inject.Inject

/**
 * Clase Application principal de NexoGo
 * Inicializa Firebase y otros servicios globales
 */
class NexoGoApplication : Application() {
    
    @Inject
    lateinit var firebaseService: FirebaseService
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase
        initializeFirebase()
    }
    
    /**
     * Inicializa Firebase y todos sus servicios
     */
    private fun initializeFirebase() {
        try {
            // Crear instancia del servicio de Firebase
            val firebaseService = FirebaseService(this)
            firebaseService.initialize()
            
            // Verificar que Firebase esté inicializado correctamente
            if (firebaseService.isInitialized()) {
                android.util.Log.d("NexoGo", "Firebase inicializado correctamente")
            } else {
                android.util.Log.e("NexoGo", "Error al inicializar Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("NexoGo", "Error crítico al inicializar Firebase", e)
        }
    }
}

