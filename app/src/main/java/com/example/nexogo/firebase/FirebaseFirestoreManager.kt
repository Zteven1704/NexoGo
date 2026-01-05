package com.example.nexogo.firebase

import android.util.Log
import com.example.nexogo.FirebaseConfig
import com.example.nexogo.core.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Gestor de operaciones de Firestore para NexoGo
 */
class FirebaseFirestoreManager {
    
    private val firestore: FirebaseFirestore = FirebaseConfig.firestore
    
    /**
     * Crea una colección de prueba para verificar la conexión
     */
    suspend fun createTestCollection(): Result<Unit> {
        return try {
            val testData = mapOf(
                "mensaje" to "Hola NexoGo!",
                "timestamp" to System.currentTimeMillis(),
                "app_version" to "1.0.0",
                "firebase_connected" to true
            )
            
            firestore.collection("test")
                .add(testData)
                .await()
            
            Log.d("FirestoreManager", "✅ Colección de prueba creada exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al crear colección de prueba: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Guarda un usuario en Firestore
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            
            Log.d("FirestoreManager", "✅ Usuario guardado: ${user.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al guardar usuario: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene un usuario por ID
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                Log.d("FirestoreManager", "✅ Usuario obtenido: ${user?.name}")
                Result.success(user)
            } else {
                Log.d("FirestoreManager", "⚠️ Usuario no encontrado: $userId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al obtener usuario: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los usuarios
     */
    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
            
            Log.d("FirestoreManager", "✅ Usuarios obtenidos: ${users.size}")
            Result.success(users)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al obtener usuarios: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un usuario
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            
            Log.d("FirestoreManager", "✅ Usuario actualizado: ${user.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al actualizar usuario: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un usuario
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .delete()
                .await()
            
            Log.d("FirestoreManager", "✅ Usuario eliminado: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al eliminar usuario: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Escucha cambios en tiempo real de usuarios
     */
    fun listenToUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreManager", "❌ Error en listener: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                } ?: emptyList()
                
                Log.d("FirestoreManager", "📡 Usuarios actualizados: ${users.size}")
                trySend(users)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Crea datos de prueba para la aplicación
     */
    suspend fun createTestData(): Result<Unit> {
        return try {
            // Crear usuarios de prueba
            val testUsers = listOf(
                User(
                    id = "admin_test",
                    name = "Dr. María González",
                    email = "admin@nexogo.com",
                    role = com.example.nexogo.core.models.UserRole.ADMIN,
                    phone = "+52 55 1234 5678",
                    address = "Av. Reforma 123, CDMX",
                    isApproved = true,
                    createdAt = com.google.firebase.Timestamp.now()
                ),
                User(
                    id = "vet_test",
                    name = "Dr. Carlos Martínez",
                    email = "veterinario@nexogo.com",
                    role = com.example.nexogo.core.models.UserRole.VET,
                    phone = "+52 55 2345 6789",
                    address = "Calle Insurgentes 456, CDMX",
                    isApproved = true,
                    createdAt = com.google.firebase.Timestamp.now()
                ),
                User(
                    id = "USER_test",
                    name = "María García",
                    email = "maria.garcia@email.com",
                    role = com.example.nexogo.core.models.UserRole.USER,
                    phone = "+52 55 4567 8901",
                    address = "Calle Roma Norte 321, CDMX",
                    isApproved = true,
                    createdAt = com.google.firebase.Timestamp.now()
                )
            )
            
            // Guardar usuarios de prueba
            for (user in testUsers) {
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .await()
            }
            
            Log.d("FirestoreManager", "✅ Datos de prueba creados exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "❌ Error al crear datos de prueba: ${e.message}")
            Result.failure(e)
        }
    }
}

