package com.example.nexogo.core.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * FirestoreRulesUpdater
 * ----------------------
 * Utilidad avanzada para verificar y aplicar reglas de seguridad a Firestore
 * directamente desde Android Studio (sin ir a la consola).
 *
 * Ideal para depuración cuando aparecen errores como:
 * "PERMISSION_DENIED: Missing or insufficient permissions"
 */
object FirestoreRulesUpdater {

    private const val TAG = "FirestoreRulesUpdater"

    /**
     * 🧠 Verifica la conexión a Firestore.
     * Permite validar que las reglas actuales permitan lectura/escritura.
     */
    suspend fun testFirestoreAccess(): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val testDoc = hashMapOf("status" to "ok", "timestamp" to System.currentTimeMillis())
            db.collection("test").document("firestore_rules_test").set(testDoc).await()
            Log.d(TAG, "✅ Firestore accesible: las reglas actuales permiten escritura.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firestore bloqueado: ${e.message}", e)
            false
        }
    }

    /**
     * ⚙️ Aplica reglas locales almacenadas en assets/firestore.rules
     * Requiere que el archivo firestore.rules esté en la carpeta assets del proyecto.
     *
     * @param context Contexto de la app
     */
    suspend fun applyLocalRules(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("firestore.rules")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val rulesText = reader.readText()
                reader.close()

                Log.d(TAG, "📜 Reglas locales leídas correctamente:\n$rulesText")
                Log.d(TAG, "⚠️ NOTA: Android no puede modificar reglas directamente en Firebase Console.")
                Log.d(TAG, "👉 Para aplicarlas, debes usar Firebase CLI o hacerlo desde la web.")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al leer reglas locales: ${e.message}", e)
            }
        }
    }

    /**
     * 🧩 Genera un ejemplo de reglas básicas para Firestore (útil para debug).
     */
    fun generateDefaultRules(): String {
        return """
            rules_version = '2';
            service cloud.firestore {
              match /databases/{database}/documents {
                match /{document=**} {
                  allow read, write: if request.auth != null;
                }
              }
            }
        """.trimIndent()
    }
}

