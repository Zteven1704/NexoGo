package com.example.nexogo

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Configuración completa de Firebase para NexoGo
 * Inicializa todos los servicios de Firebase automáticamente
 */
class FirebaseConfig : Application() {
    
    companion object {
        lateinit var firebaseAuth: FirebaseAuth
        lateinit var firestore: FirebaseFirestore
        lateinit var storage: FirebaseStorage
        lateinit var analytics: FirebaseAnalytics
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Inicializar servicios de Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        analytics = FirebaseAnalytics.getInstance(this)
        
        // Configurar Firestore para persistencia offline
        firestore.enableNetwork()
        
        // Log de confirmación
        android.util.Log.d("NexoGo", "🔥 Firebase inicializado correctamente")
        android.util.Log.d("NexoGo", "📊 Analytics: ${analytics.appInstanceId}")
        android.util.Log.d("NexoGo", "🔐 Auth: ${firebaseAuth.currentUser?.uid ?: "No user"}")
    }
}
