package com.example.nexogo.core

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import android.util.Log
import java.util.*

/**
 * Repository central de Firebase para NexoGo
 * Maneja Authentication, Firestore y Storage
 */
class FirebaseRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    // ==================== AUTHENTICATION ====================
    
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Usuario no encontrado")
            Log.d("NEXOGO_AUTH", "Login exitoso: $email")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("NEXOGO_AUTH", "Error en login: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun signUpWithEmail(email: String, password: String, userData: Map<String, Any>): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Usuario no creado")
            
            // Guardar datos del usuario en Firestore
            firestore.collection("users").document(uid).set(userData).await()
            
            Log.d("NEXOGO_AUTH", "Registro exitoso: $email")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("NEXOGO_AUTH", "Error en registro: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Log.d("NEXOGO_AUTH", "Logout exitoso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NEXOGO_AUTH", "Error en logout: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    
    // ==================== FIRESTORE ====================
    
    suspend fun createDocument(collection: String, documentId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).set(data).await()
            Log.d("NEXOGO_FIRESTORE", "Documento creado: $collection/$documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NEXOGO_FIRESTORE", "Error creando documento: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateDocument(collection: String, documentId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).update(data).await()
            Log.d("NEXOGO_FIRESTORE", "Documento actualizado: $collection/$documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NEXOGO_FIRESTORE", "Error actualizando documento: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).delete().await()
            Log.d("NEXOGO_FIRESTORE", "Documento eliminado: $collection/$documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NEXOGO_FIRESTORE", "Error eliminando documento: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getDocument(collection: String, documentId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection(collection).document(documentId).get().await()
            val data = document.data
            Log.d("NEXOGO_FIRESTORE", "Documento obtenido: $collection/$documentId")
            Result.success(data)
        } catch (e: Exception) {
            Log.e("NEXOGO_FIRESTORE", "Error obteniendo documento: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getCollection(collection: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(collection).get().await()
            val documents = snapshot.documents.map { it.data ?: emptyMap() }
            Log.d("NEXOGO_FIRESTORE", "Colección obtenida: $collection (${documents.size} documentos)")
            Result.success(documents)
        } catch (e: Exception) {
            Log.e("NEXOGO_FIRESTORE", "Error obteniendo colección: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun listenToCollection(collection: String): Flow<List<Map<String, Any>>> = callbackFlow {
        Log.d("NEXOGO_FIRESTORE", "Iniciando listener para: $collection")
        
        val listener = firestore.collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NEXOGO_FIRESTORE", "Error en listener: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val documents = snapshot.documents.map { it.data ?: emptyMap() }
                    Log.d("NEXOGO_FIRESTORE", "Listener actualizado: $collection (${documents.size} documentos)")
                    trySend(documents)
                }
            }
        
        awaitClose {
            Log.d("NEXOGO_FIRESTORE", "Cerrando listener para: $collection")
            listener.remove()
        }
    }
    
    fun listenToDocument(collection: String, documentId: String): Flow<Map<String, Any>?> = callbackFlow {
        Log.d("NEXOGO_FIRESTORE", "Iniciando listener para documento: $collection/$documentId")
        
        val listener = firestore.collection(collection).document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NEXOGO_FIRESTORE", "Error en listener de documento: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val data = snapshot?.data
                Log.d("NEXOGO_FIRESTORE", "Documento actualizado: $collection/$documentId")
                trySend(data)
            }
        
        awaitClose {
            Log.d("NEXOGO_FIRESTORE", "Cerrando listener para documento: $collection/$documentId")
            listener.remove()
        }
    }
    
    // ==================== STORAGE ====================
    
    suspend fun uploadFile(path: String, uri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("NEXOGO_STORAGE", "Archivo subido: $path")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("NEXOGO_STORAGE", "Error subiendo archivo: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun uploadBytes(path: String, bytes: ByteArray): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val uploadTask = storageRef.putBytes(bytes).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("NEXOGO_STORAGE", "Bytes subidos: $path")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("NEXOGO_STORAGE", "Error subiendo bytes: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            val storageRef = storage.reference.child(path)
            storageRef.delete().await()
            Log.d("NEXOGO_STORAGE", "Archivo eliminado: $path")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NEXOGO_STORAGE", "Error eliminando archivo: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getDownloadUrl(path: String): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("NEXOGO_STORAGE", "URL obtenida: $path")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("NEXOGO_STORAGE", "Error obteniendo URL: ${e.message}")
            Result.failure(e)
        }
    }
    
    // ==================== CONFIGURACIÓN ====================
    
    /**
     * Obtener configuración del sistema
     */
    suspend fun getConfig(): com.example.nexogo.core.models.Config? {
        return try {
            val document = firestore.collection("config").document("config_001").get().await()
            if (document.exists()) {
                document.toObject(com.example.nexogo.core.models.Config::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting config: ${e.message}")
            null
        }
    }
    
    /**
     * Guardar configuración del sistema
     */
    suspend fun saveConfig(config: com.example.nexogo.core.models.Config) {
        try {
            firestore.collection("config").document("config_001").set(config).await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving config: ${e.message}")
            throw e
        }
    }
    
    /**
     * Obtener categorías de productos
     */
    suspend fun getCategories(): List<String> {
        return try {
            val config = getConfig()
            config?.categories ?: emptyList()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting categories: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Agregar categoría
     */
    suspend fun addCategory(category: String) {
        try {
            val config = getConfig() ?: com.example.nexogo.core.models.Config()
            val updatedCategories = config.categories + category
            val updatedConfig = config.copy(categories = updatedCategories)
            saveConfig(updatedConfig)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding category: ${e.message}")
            throw e
        }
    }
    
    /**
     * Eliminar categoría
     */
    suspend fun deleteCategory(category: String) {
        try {
            val config = getConfig() ?: com.example.nexogo.core.models.Config()
            val updatedCategories = config.categories.filter { it != category }
            val updatedConfig = config.copy(categories = updatedCategories)
            saveConfig(updatedConfig)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting category: ${e.message}")
            throw e
        }
    }
    
    /**
     * Obtener servicios
     */
    suspend fun getServices(): List<String> {
        return try {
            val config = getConfig()
            config?.services ?: emptyList()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting services: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Agregar servicio
     */
    suspend fun addService(service: String) {
        try {
            val config = getConfig() ?: com.example.nexogo.core.models.Config()
            val updatedServices = config.services + service
            val updatedConfig = config.copy(services = updatedServices)
            saveConfig(updatedConfig)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error adding service: ${e.message}")
            throw e
        }
    }
    
    /**
     * Eliminar servicio
     */
    suspend fun deleteService(service: String) {
        try {
            val config = getConfig() ?: com.example.nexogo.core.models.Config()
            val updatedServices = config.services.filter { it != service }
            val updatedConfig = config.copy(services = updatedServices)
            saveConfig(updatedConfig)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error deleting service: ${e.message}")
            throw e
        }
    }
    
    // ==================== UTILIDADES ====================
    
    fun generateId(): String = UUID.randomUUID().toString()
    
    fun getCurrentTimestamp() = com.google.firebase.Timestamp.now()
    
    fun isUserAuthenticated(): Boolean = auth.currentUser != null
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // ==================== DASHBOARD ====================
    
    /**
     * Obtener usuario por ID
     */
    suspend fun getUserById(userId: String): com.example.nexogo.core.models.User? {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                document.toObject(com.example.nexogo.core.models.User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user by ID: ${e.message}")
            null
        }
    }
    
    /**
     * Obtener pacientes por propietario
     */
    suspend fun getPatientsByOwner(ownerId: String): List<com.example.nexogo.core.models.Patient> {
        return try {
            val query = if (ownerId == "all") {
                firestore.collection("patients").get().await()
            } else {
                firestore.collection("patients")
                    .whereEqualTo("ownerId", ownerId)
                    .get().await()
            }
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.Patient::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting patients by owner: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener citas por propietario
     */
    suspend fun getAppointmentsByOwner(ownerId: String): List<com.example.nexogo.core.models.Appointment> {
        return try {
            val query = if (ownerId == "all") {
                firestore.collection("appointments").get().await()
            } else {
                firestore.collection("appointments")
                    .whereEqualTo("ownerId", ownerId)
                    .get().await()
            }
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.Appointment::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting appointments by owner: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener historial clínico por propietario
     */
    suspend fun getClinicalHistoryByOwner(ownerId: String): List<com.example.nexogo.core.models.ClinicalRecord> {
        return try {
            val query = firestore.collection("clinical_history")
                .whereEqualTo("ownerId", ownerId)
                .get().await()
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.ClinicalRecord::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting clinical history by owner: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener chats del usuario
     */
    suspend fun getChats(): List<com.example.nexogo.core.models.Chat> {
        return try {
            val query = firestore.collection("chats")
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.Chat::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting chats: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener inventario
     */
    suspend fun getInventory(): List<com.example.nexogo.core.models.Product> {
        return try {
            val query = firestore.collection("inventory")
                .whereEqualTo("isActive", true)
                .get().await()
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.Product::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting inventory: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtener ventas
     */
    suspend fun getSales(): List<com.example.nexogo.core.models.Sale> {
        return try {
            val query = firestore.collection("sales")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get().await()
            
            query.documents.mapNotNull { document ->
                document.toObject(com.example.nexogo.core.models.Sale::class.java)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting sales: ${e.message}")
            emptyList()
        }
    }
}
