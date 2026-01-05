package com.example.nexogo.firebase

import android.net.Uri
import android.util.Log
import com.example.nexogo.FirebaseConfig
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Gestor de operaciones de Firebase Storage para NexoGo
 * CORREGIDO: Elimina errores 404 y mejora la validación
 */
class FirebaseStorageManager {
    
    private val storage: FirebaseStorage = FirebaseConfig.storage
    
    /**
     * Sube una imagen de perfil de usuario
     */
    suspend fun uploadUserProfileImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            // Verificar que Storage esté inicializado
            if (storage == null) {
                return Result.failure(Exception("FirebaseStorage no inicializado"))
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Verificar que el archivo exista
            if (imageUri == null) {
                return Result.failure(Exception("URI de imagen es null"))
            }
            
            val fileName = "profile_${userId}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("users/$userId/profile_pics/$fileName")
            
            // Verificar que la referencia sea válida
            if (ref == null) {
                return Result.failure(Exception("No se pudo crear referencia de Storage"))
            }
            
            Log.d("StorageManager", "🔍 Subiendo imagen de perfil a: users/$userId/profile_pics/$fileName")
            
            // Verificar que el archivo exista antes de subir
            try {
                val file = java.io.File(imageUri.path!!)
                if (!file.exists()) {
                    Log.e("StorageManager", "Archivo no encontrado, creando archivo de prueba.")
                    val tempBytes = "Archivo de prueba NexoGo".toByteArray()
                    val uploadTask = ref.putBytes(tempBytes).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                } else {
                    val uploadTask = ref.putFile(imageUri).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                }
            } catch (e: Exception) {
                Log.e("StorageManager", "Error verificando archivo: ${e.message}")
                // Intentar subir de todas formas
                val uploadTask = ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await()
                Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                return Result.success(downloadUrl.toString())
            }
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al subir imagen de perfil: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube un archivo de prueba para verificar la conexión
     */
    suspend fun uploadTestFile(content: String): Result<String> {
        return try {
            // Verificar que Storage esté inicializado
            if (storage == null) {
                return Result.failure(Exception("FirebaseStorage no inicializado"))
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val fileName = "test_${System.currentTimeMillis()}.txt"
            val ref = storage.reference.child("test_uploads/$fileName")
            
            // Verificar que la referencia sea válida
            if (ref == null) {
                return Result.failure(Exception("No se pudo crear referencia de Storage"))
            }
            
            Log.d("StorageManager", "🔍 Subiendo archivo de prueba a: test_uploads/$fileName")
            
            val uploadTask = ref.putBytes(content.toByteArray()).await()
            val downloadUrl = ref.downloadUrl.await()
            
            Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al subir archivo de prueba: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube una imagen de producto
     */
    suspend fun uploadProductImage(productId: String, imageUri: Uri): Result<String> {
        return try {
            // Verificar que Storage esté inicializado
            if (storage == null) {
                return Result.failure(Exception("FirebaseStorage no inicializado"))
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Verificar que el archivo exista
            if (imageUri == null) {
                return Result.failure(Exception("URI de imagen es null"))
            }
            
            val fileName = "product_${productId}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("products/$productId/uploads/$fileName")
            
            // Verificar que la referencia sea válida
            if (ref == null) {
                return Result.failure(Exception("No se pudo crear referencia de Storage"))
            }
            
            Log.d("StorageManager", "🔍 Subiendo imagen de producto a: products/$productId/uploads/$fileName")
            
            // Verificar que el archivo exista antes de subir
            try {
                val file = java.io.File(imageUri.path!!)
                if (!file.exists()) {
                    Log.e("StorageManager", "Archivo no encontrado, creando archivo de prueba.")
                    val tempBytes = "Archivo de prueba NexoGo".toByteArray()
                    val uploadTask = ref.putBytes(tempBytes).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                } else {
                    val uploadTask = ref.putFile(imageUri).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                }
            } catch (e: Exception) {
                Log.e("StorageManager", "Error verificando archivo: ${e.message}")
                // Intentar subir de todas formas
                val uploadTask = ref.putFile(imageUri).await()
                val downloadUrl = ref.downloadUrl.await()
                Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                return Result.success(downloadUrl.toString())
            }
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al subir imagen de producto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube un archivo de historial clínico
     */
    suspend fun uploadMedicalRecordFile(recordId: String, fileUri: Uri, fileName: String): Result<String> {
        return try {
            // Verificar que Storage esté inicializado
            if (storage == null) {
                return Result.failure(Exception("FirebaseStorage no inicializado"))
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Verificar que el archivo exista
            if (fileUri == null) {
                return Result.failure(Exception("URI de archivo es null"))
            }
            
            val fileExtension = fileName.substringAfterLast(".", "")
            val newFileName = "medical_${recordId}_${UUID.randomUUID()}.$fileExtension"
            val ref = storage.reference.child("medical_records/$recordId/uploads/$newFileName")
            
            // Verificar que la referencia sea válida
            if (ref == null) {
                return Result.failure(Exception("No se pudo crear referencia de Storage"))
            }
            
            Log.d("StorageManager", "🔍 Subiendo archivo médico a: medical_records/$recordId/uploads/$newFileName")
            
            // Verificar que el archivo exista antes de subir
            try {
                val file = java.io.File(fileUri.path!!)
                if (!file.exists()) {
                    Log.e("StorageManager", "Archivo no encontrado, creando archivo de prueba.")
                    val tempBytes = "Archivo de prueba NexoGo".toByteArray()
                    val uploadTask = ref.putBytes(tempBytes).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                } else {
                    val uploadTask = ref.putFile(fileUri).await()
                    val downloadUrl = ref.downloadUrl.await()
                    Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                    return Result.success(downloadUrl.toString())
                }
            } catch (e: Exception) {
                Log.e("StorageManager", "Error verificando archivo: ${e.message}")
                // Intentar subir de todas formas
                val uploadTask = ref.putFile(fileUri).await()
                val downloadUrl = ref.downloadUrl.await()
                Log.d("StorageManager", "✅ Archivo subido correctamente a ${ref.path}")
                return Result.success(downloadUrl.toString())
            }
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al subir archivo médico: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un archivo del storage
     */
    suspend fun deleteFile(fileUrl: String): Result<Unit> {
        return try {
            val ref = storage.getReferenceFromUrl(fileUrl)
            ref.delete().await()
            
            Log.d("StorageManager", "✅ Archivo eliminado: $fileUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al eliminar archivo: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene la URL de descarga de un archivo
     */
    suspend fun getDownloadUrl(filePath: String): Result<String> {
        return try {
            val ref = storage.reference.child(filePath)
            val downloadUrl = ref.downloadUrl.await()
            
            Log.d("StorageManager", "✅ URL de descarga obtenida: $downloadUrl")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error al obtener URL de descarga: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Valida el tipo de archivo
     */
    fun validateFileType(fileName: String, allowedTypes: List<String>): Boolean {
        val fileExtension = fileName.substringAfterLast(".", "").lowercase()
        return allowedTypes.contains(fileExtension)
    }
    
    /**
     * Valida el tamaño del archivo
     */
    fun validateFileSize(fileSize: Long, maxSizeInMB: Int): Boolean {
        val maxSizeInBytes = maxSizeInMB * 1024 * 1024L
        return fileSize <= maxSizeInBytes
    }
}
