package com.example.nexogo.core.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * FirebaseDiagnostics
 * ----------------------
 * Utilidad avanzada para verificar la conexión completa con Firebase:
 * ✅ Autenticación (Auth)
 * ✅ Base de datos (Firestore)
 * ✅ Almacenamiento (Storage)
 *
 * Muestra logs detallados en Logcat para asegurar que todo esté correctamente conectado.
 */
object FirebaseDiagnostics {

    private const val TAG = "FirebaseDiagnostics"

    /**
     * 🔍 Ejecuta todas las pruebas de diagnóstico de Firebase.
     */
    suspend fun runFullDiagnostics(context: Context) {
        Log.d(TAG, "🚀 Iniciando diagnóstico completo de Firebase...")
        ensureFirebaseInitialized(context)

        val authResult = testAuth()
        val firestoreResult = testFirestore()
        val storageResult = testStorage(context)

        Log.d(TAG, """
            ✅ RESULTADOS DEL DIAGNÓSTICO FIREBASE:
            🔹 Auth: ${if (authResult) "OK ✅" else "ERROR ❌"}
            🔹 Firestore: ${if (firestoreResult) "OK ✅" else "ERROR ❌"}
            🔹 Storage: ${if (storageResult) "OK ✅" else "ERROR ❌"}
        """.trimIndent())
    }

    /**
     * 🧠 Asegura que Firebase esté inicializado correctamente.
     */
    private fun ensureFirebaseInitialized(context: Context) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
            Log.d(TAG, "🔥 Firebase inicializado correctamente.")
        } else {
            Log.d(TAG, "🔥 Firebase ya está inicializado.")
        }
    }

    /**
     * 🔐 Prueba la conexión de Firebase Authentication.
     */
    private suspend fun testAuth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInWithEmailAndPassword("admin@nexogo.com", "123456").await()
                Log.d(TAG, "✅ Autenticación exitosa con admin@nexogo.com")
            } else {
                Log.d(TAG, "✅ Usuario autenticado: ${auth.currentUser?.email}")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error de autenticación: ${e.message}", e)
            false
        }
    }

    /**
     * 📄 Prueba de lectura/escritura en Firestore.
     */
    private suspend fun testFirestore(): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()
            val testData = hashMapOf("diagnostic" to "ok", "timestamp" to System.currentTimeMillis())
            db.collection("diagnostics").document("firestore_test").set(testData).await()
            val snapshot = db.collection("diagnostics").document("firestore_test").get().await()
            Log.d(TAG, "✅ Firestore operativo. Documento leído: ${snapshot.data}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error de Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * ☁️ Prueba de subida y descarga de archivos en Firebase Storage.
     */
    private suspend fun testStorage(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("diagnostics/test_file.txt")

            // Crear archivo temporal
            val localFile = File(context.cacheDir, "test_file.txt")
            FileOutputStream(localFile).use { it.write("NexoGo Firebase Diagnostics Test".toByteArray()) }

            // Subir archivo
            storageRef.putFile(Uri.fromFile(localFile)).await()
            Log.d(TAG, "✅ Archivo subido exitosamente a Firebase Storage.")

            // Descargar archivo
            val tempFile = File.createTempFile("test_download", ".txt")
            storageRef.getFile(tempFile).await()
            Log.d(TAG, "✅ Archivo descargado correctamente desde Firebase Storage.")

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en Firebase Storage: ${e.message}", e)
            false
        }
    }
}

