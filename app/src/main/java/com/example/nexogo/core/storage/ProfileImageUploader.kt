package com.example.nexogo.core.storage

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Helper centralizado para subir fotos de perfil
 * Maneja la subida de forma consistente y con mejor manejo de errores
 */
object ProfileImageUploader {
    private const val TAG = "ProfileImageUploader"
    
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Sube una foto de perfil a Firebase Storage
     * 
     * @param userId ID del usuario
     * @param imageUri URI de la imagen a subir
     * @return Result con la URL de descarga de la imagen
     */
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            // Verificar que el usuario esté autenticado
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Verificar que el userId coincida con el usuario autenticado
            if (currentUser.uid != userId) {
                return Result.failure(Exception("No tienes permiso para actualizar este perfil"))
            }
            
            // Generar nombre único para la imagen
            val fileName = "profile_${userId}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val imagePath = "users/$userId/profile/$fileName"
            
            Log.d(TAG, "📤 Subiendo foto de perfil: $imagePath")
            
            // Subir la imagen
            val storageRef = storage.reference.child(imagePath)
            val uploadTask = storageRef.putFile(imageUri)
            
            // Esperar a que termine la subida
            uploadTask.await()
            
            // Obtener la URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            val imageUrl = downloadUrl.toString()
            
            Log.d(TAG, "✅ Foto de perfil subida exitosamente: $imageUrl")
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("permission") == true || 
                e.message?.contains("Permission denied") == true -> {
                    "Error de permisos. Verifica las reglas de Storage en Firebase Console."
                }
                e.message?.contains("network") == true || 
                e.message?.contains("Network") == true -> {
                    "Error de conexión. Verifica tu conexión a internet."
                }
                e.message?.contains("canceled") == true -> {
                    "Subida cancelada."
                }
                else -> {
                    "Error al subir la foto: ${e.message ?: "Error desconocido"}"
                }
            }
            
            Log.e(TAG, "❌ Error al subir foto de perfil: ${e.message}", e)
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Elimina una foto de perfil de Firebase Storage
     */
    suspend fun deleteProfileImage(imageUrl: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Extraer la ruta del Storage desde la URL
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            
            Log.d(TAG, "✅ Foto de perfil eliminada: $imageUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar foto de perfil: ${e.message}", e)
            Result.failure(e)
        }
    }
}




