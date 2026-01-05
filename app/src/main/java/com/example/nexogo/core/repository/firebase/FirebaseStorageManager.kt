package com.example.nexogo.core.repository.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

object FirebaseStorageManager {

    private const val TAG = "FirebaseStorageManager"
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun uploadFile(uri: Uri, path: String): String? {
        return try {
            val storageRef: StorageReference = storage.reference.child(path)
            val uploadTask = storageRef.putFile(uri).await()
            val url = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "✅ Archivo subido correctamente: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir archivo: ${e.message}", e)
            null
        }
    }

    suspend fun downloadFile(path: String, localUri: Uri): Boolean {
        return try {
            val storageRef = storage.reference.child(path)
            storageRef.getFile(localUri).await()
            Log.d(TAG, "✅ Archivo descargado correctamente en $localUri")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al descargar archivo: ${e.message}", e)
            false
        }
    }

    suspend fun deleteFile(path: String): Boolean {
        return try {
            val storageRef = storage.reference.child(path)
            storageRef.delete().await()
            Log.d(TAG, "✅ Archivo eliminado correctamente: $path")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar archivo: ${e.message}", e)
            false
        }
    }

    suspend fun getFileUrl(path: String): String? {
        return try {
            val storageRef = storage.reference.child(path)
            val url = storageRef.downloadUrl.await().toString()
            Log.d(TAG, "🔗 URL obtenida: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener URL del archivo: ${e.message}", e)
            null
        }
    }
}

