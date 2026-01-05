package com.example.nexogo.core.repository.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * FirebaseFirestoreRepository
 * ---------------------------
 * Repositorio central para interactuar con Firestore en la app NexoGo.
 * Permite guardar, leer y eliminar datos de manera asíncrona y segura.
 */
object FirebaseFirestoreRepository {

    private const val TAG = "FirebaseFirestoreRepo"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Guarda un documento en una colección específica.
     * @param collection Nombre de la colección (por ejemplo: "usuarios", "citas", "inventario")
     * @param documentId ID del documento. Si es null, Firestore generará uno automáticamente.
     * @param data Mapa con los datos a guardar.
     */
    suspend fun saveDocument(collection: String, documentId: String? = null, data: Map<String, Any>): Boolean {
        return try {
            if (documentId != null) {
                db.collection(collection).document(documentId).set(data).await()
                Log.d(TAG, "✅ Documento guardado con ID: $documentId en colección: $collection")
            } else {
                db.collection(collection).add(data).await()
                Log.d(TAG, "✅ Documento agregado correctamente en colección: $collection")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar documento: ${e.message}", e)
            false
        }
    }

    /**
     * Obtiene un documento desde una colección.
     */
    suspend fun getDocument(collection: String, documentId: String): Map<String, Any>? {
        return try {
            val snapshot = db.collection(collection).document(documentId).get().await()
            if (snapshot.exists()) {
                Log.d(TAG, "📄 Documento obtenido correctamente: ${snapshot.id}")
                snapshot.data
            } else {
                Log.w(TAG, "⚠️ Documento no encontrado: $documentId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener documento: ${e.message}", e)
            null
        }
    }

    /**
     * Elimina un documento desde una colección.
     */
    suspend fun deleteDocument(collection: String, documentId: String): Boolean {
        return try {
            db.collection(collection).document(documentId).delete().await()
            Log.d(TAG, "✅ Documento eliminado correctamente: $documentId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar documento: ${e.message}", e)
            false
        }
    }

    /**
     * Guarda la URL de un archivo subido al Storage dentro de Firestore.
     * @param collection colección donde guardar la referencia (por ejemplo "archivos" o "imagenesMascotas")
     * @param documentId ID del documento (puede ser el ID del usuario o mascota)
     * @param fieldName nombre del campo donde se guardará la URL (por ejemplo "fotoPerfil")
     * @param downloadUrl URL de descarga obtenida del Storage
     */
    suspend fun saveFileUrl(collection: String, documentId: String, fieldName: String, downloadUrl: String): Boolean {
        return try {
            db.collection(collection).document(documentId)
                .update(fieldName, downloadUrl)
                .await()
            Log.d(TAG, "🔗 URL guardada correctamente en $collection/$documentId: $downloadUrl")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar URL del archivo: ${e.message}", e)
            false
        }
    }
}

