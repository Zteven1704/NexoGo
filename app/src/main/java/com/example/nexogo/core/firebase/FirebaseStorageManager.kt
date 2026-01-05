package com.example.nexogo.core.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * FirebaseStorageManager
 * ----------------------
 * Clase utilitaria para manejar la subida, descarga y eliminación de archivos en Firebase Storage.
 * Este módulo es independiente de Firestore.
 * Ideal para operaciones rápidas (por ejemplo, subir imágenes o PDF sin necesidad de registrar URLs en Firestore).
 */
object FirebaseStorageManager {

    private const val TAG = "FirebaseStorageManager"
    private val storage = FirebaseStorage.getInstance()

    /**
     * 📤 Sube un archivo a Firebase Storage y devuelve la URL de descarga.
     * @param fileUri URI del archivo a subir.
     * @param path Ruta donde se almacenará en Firebase Storage (por ejemplo "documentos/citas/{id}.pdf").
     * @return String URL de descarga o null si ocurre un error.
     */
    suspend fun uploadFile(fileUri: Uri, path: String): String? {
        return try {
            val storageRef = storage.reference.child(path)
            storageRef.putFile(fileUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "✅ Archivo subido correctamente: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir archivo: ${e.message}", e)
            null
        }
    }

    /**
     * 📥 Descarga la URL de un archivo almacenado en Firebase Storage.
     * @param path Ruta del archivo en el Storage (por ejemplo "imagenes/perfiles/{userId}.jpg").
     * @return String URL de descarga o null si falla.
     */
    suspend fun getFileDownloadUrl(path: String): String? {
        return try {
            val storageRef = storage.reference.child(path)
            val url = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "🔗 URL obtenida correctamente: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener URL: ${e.message}", e)
            null
        }
    }

    /**
     * 🗑️ Elimina un archivo del Storage.
     * @param path Ruta completa del archivo en Firebase Storage.
     * @return true si se elimina correctamente, false si hay error.
     */
    suspend fun deleteFile(path: String): Boolean {
        return try {
            storage.reference.child(path).delete().await()
            Log.d(TAG, "🗑️ Archivo eliminado correctamente: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar archivo: ${e.message}", e)
            false
        }
    }
}

