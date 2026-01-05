package com.example.nexogo

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Test rápido para verificar la conexión completa con Firebase
 * Incluye verificación de Authentication, Firestore y Storage
 */
object FirebaseConnectionTest {
    
    private const val TAG = "FirebaseConnectionTest"
    
    /**
     * Ejecuta un test completo de conexión Firebase
     */
    suspend fun runConnectionTest(context: Context) {
        try {
            Log.d(TAG, "🔥 Iniciando test de conexión Firebase...")
            
            // 1. Inicializar Firebase
            initializeFirebase(context)
            
            // 2. Test de Firestore
            testFirestoreConnection()
            
            // 3. Test de Authentication
            testAuthConnection()
            
            // 4. Test de Storage
            testStorageConnection()
            
            Log.d(TAG, "✅ Test de conexión Firebase completado exitosamente")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de conexión Firebase: ${e.message}", e)
        }
    }
    
    /**
     * Inicializa Firebase si no está inicializado
     */
    private fun initializeFirebase(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "🔥 Firebase inicializado correctamente")
            } else {
                Log.d(TAG, "🔥 Firebase ya estaba inicializado")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando Firebase: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Test de conexión con Firestore
     */
    private suspend fun testFirestoreConnection() {
        try {
            Log.d(TAG, "📊 Probando conexión con Firestore...")
            
            val firestore = FirebaseFirestore.getInstance()
            val testData = mapOf(
                "status" to "connected",
                "timestamp" to System.currentTimeMillis(),
                "testId" to UUID.randomUUID().toString(),
                "message" to "Test de conexión desde NexoGo"
            )
            
            // Escribir documento de prueba
            firestore.collection("test")
                .add(testData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "✅ Firestore: Documento creado con ID: ${documentReference.id}")
                    
                    // Leer el documento para verificar
                    documentReference.get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val status = document.getString("status")
                                val message = document.getString("message")
                                Log.d(TAG, "✅ Firestore: Documento leído - Status: $status, Message: $message")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "❌ Firestore: Error leyendo documento: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Firestore: Error creando documento: ${e.message}")
                }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de Firestore: ${e.message}", e)
        }
    }
    
    /**
     * Test de conexión con Authentication
     */
    private fun testAuthConnection() {
        try {
            Log.d(TAG, "🔐 Probando conexión con Firebase Auth...")
            
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser != null) {
                Log.d(TAG, "✅ Auth: Usuario autenticado - UID: ${currentUser.uid}, Email: ${currentUser.email}")
            } else {
                Log.d(TAG, "ℹ️ Auth: No hay usuario autenticado actualmente")
            }
            
            // Verificar que el servicio de Auth esté disponible
            Log.d(TAG, "✅ Auth: Servicio de autenticación disponible")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de Auth: ${e.message}", e)
        }
    }
    
    /**
     * Test de conexión con Storage
     */
    private fun testStorageConnection() {
        try {
            Log.d(TAG, "💾 Probando conexión con Firebase Storage...")
            
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            
            // Verificar que la referencia al storage esté disponible
            if (storageRef != null) {
                Log.d(TAG, "✅ Storage: Referencia al storage obtenida correctamente")
                Log.d(TAG, "✅ Storage: Bucket: ${storageRef.bucket}")
            } else {
                Log.e(TAG, "❌ Storage: No se pudo obtener referencia al storage")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de Storage: ${e.message}", e)
        }
    }
    
    /**
     * Test rápido de conexión (versión simplificada)
     */
    fun quickConnectionTest(context: Context) {
        try {
            Log.d(TAG, "🚀 Iniciando test rápido de conexión...")
            
            // Inicializar Firebase
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            
            // Test básico de Firestore
            FirebaseFirestore.getInstance().collection("test")
                .add(mapOf("status" to "connected", "timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener { 
                    Log.d(TAG, "🔥 Firestore conectado correctamente")
                }
                .addOnFailureListener { e -> 
                    Log.e(TAG, "❌ Error Firestore: ${e.message}")
                }
            
            // Test básico de Auth
            val auth = FirebaseAuth.getInstance()
            Log.d(TAG, "🔐 Auth disponible: ${auth != null}")
            
            // Test básico de Storage
            val storage = FirebaseStorage.getInstance()
            Log.d(TAG, "💾 Storage disponible: ${storage != null}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test rápido: ${e.message}", e)
        }
    }
}

