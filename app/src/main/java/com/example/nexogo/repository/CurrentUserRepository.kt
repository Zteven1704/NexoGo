package com.example.nexogo.repository

import android.util.Log
import com.example.nexogo.core.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio para obtener y observar el perfil del usuario actual
 * Incluye soporte offline con listeners en tiempo real
 */
class CurrentUserRepository {
    companion object {
        private const val TAG = "CurrentUserRepository"
    }
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile.asStateFlow()
    
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    
    /**
     * Carga el perfil del usuario actual desde Firestore
     * Funciona offline: usa cache si no hay conexión
     */
    suspend fun loadCurrentProfile(): Result<User?> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(null)
            
            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(User::class.java)?.copy(id = doc.id)
            
            _profile.value = user
            Log.d(TAG, "✅ Perfil cargado: ${user?.name} (${user?.role})")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cargar perfil: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Observa cambios en el perfil del usuario en tiempo real
     * Funciona offline: usa cache cuando no hay conexión
     * 
     * @param onUpdate Callback que se ejecuta cuando el perfil se actualiza
     * @return Función para cancelar el listener
     */
    fun observeProfileRealtime(onUpdate: (User) -> Unit): () -> Unit {
        val uid = auth.currentUser?.uid ?: return { }
        
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error en listener de perfil: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.let { doc ->
                    if (doc.exists()) {
                        val user = doc.toObject(User::class.java)?.copy(id = doc.id)
                        user?.let {
                            _profile.value = it
                            onUpdate(it)
                            
                            // Log metadata para detectar escrituras pendientes
                            val hasPendingWrites = doc.metadata.hasPendingWrites()
                            if (hasPendingWrites) {
                                Log.d(TAG, "⏳ Perfil tiene escrituras pendientes (offline)")
                            }
                        }
                    }
                }
            }
        
        listenerRegistration = listener
        
        // Retorna función para cancelar
        return {
            listener.remove()
            listenerRegistration = null
        }
    }
    
    /**
     * Cancela el listener de perfil en tiempo real
     */
    fun cancelProfileListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
    
    /**
     * Actualiza el perfil del usuario actual
     * Funciona offline: se encola si no hay conexión
     */
    suspend fun updateProfile(user: User): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Usuario no autenticado"))
            
            firestore.collection("users").document(uid)
                .set(user.copy(id = uid))
                .await()
            
            _profile.value = user.copy(id = uid)
            Log.d(TAG, "✅ Perfil actualizado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar perfil: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Limpia el perfil (logout)
     */
    fun clearProfile() {
        _profile.value = null
        cancelProfileListener()
    }
}

