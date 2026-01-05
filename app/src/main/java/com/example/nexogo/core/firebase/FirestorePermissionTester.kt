package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestorePermissionTester
 * --------------------------
 * Herramienta específica para probar y diagnosticar problemas de permisos
 * en Firestore. Ayuda a identificar exactamente qué colecciones/documentos
 * están causando el error PERMISSION_DENIED.
 */
object FirestorePermissionTester {

    private const val TAG = "FirestorePermissionTester"

    /**
     * 🧪 Prueba todas las colecciones principales de NexoGo
     */
    suspend fun testAllCollections(): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "🧪 Iniciando prueba de permisos en todas las colecciones...")
        
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        
        // Verificar autenticación
        if (auth.currentUser == null) {
            Log.e(TAG, "❌ Usuario no autenticado. No se pueden probar permisos.")
            return@withContext false
        }
        
        Log.d(TAG, "✅ Usuario autenticado: ${auth.currentUser?.email}")
        
        val collections = listOf(
            "usuarios", "citas", "inventario", "patients", 
            "clinical_records", "sales", "messages", "chats", 
            "diagnostics", "test"
        )
        
        var allPassed = true
        
        for (collection in collections) {
            val result = testCollectionAccess(db, collection)
            if (!result) {
                allPassed = false
            }
        }
        
        Log.d(TAG, "🏁 Resultado final: ${if (allPassed) "TODAS LAS PRUEBAS PASARON ✅" else "ALGUNAS PRUEBAS FALLARON ❌"}")
        return@withContext allPassed
    }

    /**
     * 🔍 Prueba acceso a una colección específica
     */
    private suspend fun testCollectionAccess(db: FirebaseFirestore, collectionName: String): Boolean {
        return try {
            Log.d(TAG, "🔍 Probando acceso a colección: $collectionName")
            
            // Intentar leer la colección
            val snapshot = db.collection(collectionName).limit(1).get().await()
            Log.d(TAG, "✅ Lectura exitosa en $collectionName (${snapshot.size()} documentos)")
            
            // Intentar escribir un documento de prueba
            val testDoc = mapOf(
                "test" to true,
                "timestamp" to System.currentTimeMillis(),
                "collection" to collectionName
            )
            
            db.collection(collectionName).document("permission_test").set(testDoc).await()
            Log.d(TAG, "✅ Escritura exitosa en $collectionName")
            
            // Limpiar documento de prueba
            db.collection(collectionName).document("permission_test").delete().await()
            Log.d(TAG, "✅ Limpieza exitosa en $collectionName")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en colección $collectionName: ${e.message}", e)
            false
        }
    }

    /**
     * 🔐 Prueba específica de autenticación
     */
    suspend fun testAuthentication(): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            
            if (user == null) {
                Log.e(TAG, "❌ No hay usuario autenticado")
                return@withContext false
            }
            
            Log.d(TAG, "✅ Usuario autenticado:")
            Log.d(TAG, "   - UID: ${user.uid}")
            Log.d(TAG, "   - Email: ${user.email}")
            Log.d(TAG, "   - Verificado: ${user.isEmailVerified}")
            
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error de autenticación: ${e.message}", e)
            return@withContext false
        }
    }

    /**
     * 📊 Genera reporte detallado de permisos
     */
    suspend fun generatePermissionReport(): String = withContext(Dispatchers.IO) {
        val report = StringBuilder()
        report.appendLine("📊 REPORTE DE PERMISOS FIRESTORE")
        report.appendLine("=================================")
        
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            report.appendLine("❌ Usuario no autenticado")
            return@withContext report.toString()
        }
        
        report.appendLine("✅ Usuario: ${auth.currentUser?.email}")
        report.appendLine("✅ UID: ${auth.currentUser?.uid}")
        report.appendLine("")
        
        val db = FirebaseFirestore.getInstance()
        val collections = listOf("usuarios", "citas", "inventario", "patients", "clinical_records", "sales", "messages", "chats", "diagnostics", "test")
        
        for (collection in collections) {
            try {
                val snapshot = db.collection(collection).limit(1).get().await()
                report.appendLine("✅ $collection: Acceso permitido (${snapshot.size()} docs)")
            } catch (e: Exception) {
                report.appendLine("❌ $collection: ${e.message}")
            }
        }
        
        return@withContext report.toString()
    }
}

