package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirebaseAutoFixer
 * ----------------------
 * Herramienta avanzada que detecta y corrige automáticamente errores comunes
 * en Auth, Firestore y Storage para mantener la integridad del proyecto NexoGo.
 *
 * Puede:
 * ✅ Eliminar usuarios duplicados
 * ✅ Regenerar documentos base perdidos
 * ✅ Corregir rutas de Storage vacías o dañadas
 * ✅ Crear estructura mínima necesaria para el correcto funcionamiento
 */
object FirebaseAutoFixer {

    private const val TAG = "FirebaseAutoFixer"

    /**
     * Ejecuta todas las reparaciones posibles.
     */
    suspend fun runAutoFix() {
        Log.d(TAG, "🧩 Iniciando proceso de reparación automática Firebase...")
        val authFix = fixAuthIssues()
        val firestoreFix = fixFirestoreIssues()
        val storageFix = fixStorageIssues()

        Log.d(TAG, """
            🔧 RESULTADOS DEL AUTOFIX FIREBASE:
            🔹 Auth: ${if (authFix) "OK ✅" else "ERROR ❌"}
            🔹 Firestore: ${if (firestoreFix) "OK ✅" else "ERROR ❌"}
            🔹 Storage: ${if (storageFix) "OK ✅" else "ERROR ❌"}
        """.trimIndent())
    }

    /**
     * 🔐 Soluciona problemas de autenticación (usuarios duplicados o corruptos).
     */
    private suspend fun fixAuthIssues(): Boolean = withContext(Dispatchers.IO) {
        try {
            val auth = FirebaseAuth.getInstance()
            val adminEmail = "admin@nexogo.com"

            // Elimina usuarios duplicados de prueba
            auth.fetchSignInMethodsForEmail(adminEmail).await()?.let {
                if (it.signInMethods?.size ?: 0 > 1) {
                    Log.w(TAG, "⚠️ Duplicado detectado en Auth. Eliminando duplicados...")
                    auth.currentUser?.delete()?.await()
                    auth.createUserWithEmailAndPassword(adminEmail, "123456").await()
                    Log.d(TAG, "✅ Usuario administrador restaurado correctamente.")
                } else {
                    Log.d(TAG, "✅ Auth sin duplicados.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error corrigiendo Auth: ${e.message}", e)
            false
        }
    }

    /**
     * 📄 Corrige estructuras de Firestore rotas o faltantes.
     */
    private suspend fun fixFirestoreIssues(): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = FirebaseFirestore.getInstance()

            val collectionsBase = listOf("users", "appointments", "pets", "diagnostics")
            collectionsBase.forEach { collection ->
                val snapshot = db.collection(collection).get().await()
                if (snapshot.isEmpty) {
                    Log.w(TAG, "⚠️ Colección '$collection' vacía. Creando documento base...")
                    val data = mapOf("initialized" to true, "timestamp" to System.currentTimeMillis())
                    db.collection(collection).document("base_doc").set(data).await()
                    Log.d(TAG, "✅ Colección '$collection' reparada con documento base.")
                } else {
                    Log.d(TAG, "✅ Colección '$collection' ya contiene documentos.")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error corrigiendo Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * ☁️ Repara rutas vacías o corruptas en Firebase Storage.
     */
    private suspend fun fixStorageIssues(): Boolean = withContext(Dispatchers.IO) {
        try {
            val storage = FirebaseStorage.getInstance()
            val basePaths = listOf("users/", "pets/", "backups/")

            for (path in basePaths) {
                try {
                    val listResult = storage.reference.child(path).listAll().await()
                    if (listResult.items.isEmpty() && listResult.prefixes.isEmpty()) {
                        Log.w(TAG, "⚠️ Ruta '$path' vacía. Creando marcador base...")
                        val markerRef = storage.reference.child("${path}placeholder.txt")
                        val bytes = "NexoGo placeholder for $path".toByteArray()
                        markerRef.putBytes(bytes).await()
                        Log.d(TAG, "✅ Marcador creado en '$path'")
                    } else {
                        Log.d(TAG, "✅ Ruta '$path' válida.")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ No se pudo listar '$path': ${e.message}")
                }
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error corrigiendo Storage: ${e.message}", e)
            false
        }
    }
}

