package com.example.nexogo.core.repository.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * FileUploadRepository
 * --------------------
 * Repositorio que maneja la subida de archivos (imágenes, documentos, etc.)
 * a Firebase Storage y guarda automáticamente la URL en Firestore.
 */
object FileUploadRepository {

    private const val TAG = "FileUploadRepository"
    private val storage = FirebaseStorage.getInstance()

    /**
     * Sube un archivo a Firebase Storage y guarda su URL en Firestore.
     * @param fileUri URI del archivo local a subir.
     * @param path Ruta dentro del Storage (por ejemplo "imagenes/perfiles/{userId}.jpg").
     * @param collection Colección de Firestore donde se guardará la URL (por ejemplo "usuarios").
     * @param documentId ID del documento en Firestore donde se guardará la URL.
     * @param fieldName Nombre del campo dentro del documento donde se guardará la URL (por ejemplo "fotoPerfil").
     */
    suspend fun uploadFileAndSaveUrl(
        fileUri: Uri,
        path: String,
        collection: String,
        documentId: String,
        fieldName: String
    ): Boolean {
        return try {
            val storageRef = storage.reference.child(path)
            val uploadTask = storageRef.putFile(fileUri).await()

            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "✅ Archivo subido correctamente. URL: $downloadUrl")

            val firestoreSuccess = FirebaseFirestoreRepository.saveFileUrl(
                collection = collection,
                documentId = documentId,
                fieldName = fieldName,
                downloadUrl = downloadUrl
            )

            if (firestoreSuccess) {
                Log.d(TAG, "✅ URL guardada correctamente en Firestore ($collection/$documentId)")
                true
            } else {
                Log.w(TAG, "⚠️ Archivo subido pero la URL no se guardó en Firestore")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir archivo y guardar URL: ${e.message}", e)
            false
        }
    }

    /**
     * Elimina un archivo del Storage.
     * @param path Ruta completa del archivo en Storage.
     */
    suspend fun deleteFile(path: String): Boolean {
        return try {
            storage.reference.child(path).delete().await()
            Log.d(TAG, "🗑️ Archivo eliminado correctamente de Storage: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar archivo: ${e.message}", e)
            false
        }
    }
}

