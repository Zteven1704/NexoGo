package com.example.nexogo.core.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("nexogo_prefs")

/**
 * Gestor de sesión y caché local de perfil de usuario
 * Singleton que gestiona el rol y datos del usuario autenticado
 */
object SessionManager {
    private const val TAG = "SessionManager"
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Keys para DataStore
    private val KEY_ROLE = stringPreferencesKey("user_role")
    private val KEY_UID = stringPreferencesKey("user_uid")
    private val KEY_NAME = stringPreferencesKey("user_name")
    private val KEY_EMAIL = stringPreferencesKey("user_email")
    
    /**
     * Guarda el perfil del usuario localmente para acceso rápido y verificación offline
     */
    suspend fun saveUserProfileLocally(context: Context, user: User) {
        try {
            context.dataStore.edit { prefs ->
                prefs[KEY_ROLE] = user.role.name
                prefs[KEY_UID] = user.id
                prefs[KEY_NAME] = user.name
                prefs[KEY_EMAIL] = user.email
            }
            Log.d(TAG, "✅ Perfil guardado localmente: ${user.name} (${user.role.name})")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al guardar perfil localmente: ${e.message}", e)
        }
    }
    
    /**
     * Obtiene el rol del usuario desde caché local (bloqueante)
     */
    fun getRoleBlocking(context: Context): UserRole {
        return runBlocking {
            try {
                val prefs = context.dataStore.data.first()
                val roleString = prefs[KEY_ROLE] ?: "USER"
                UserRole.valueOf(roleString)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al obtener rol: ${e.message}", e)
                UserRole.USER
            }
        }
    }
    
    /**
     * Obtiene el rol del usuario como Flow (reactivo)
     */
    fun getRoleFlow(context: Context): Flow<UserRole> {
        return context.dataStore.data.map { prefs ->
            try {
                val roleString = prefs[KEY_ROLE] ?: "USER"
                UserRole.valueOf(roleString)
            } catch (e: Exception) {
                UserRole.USER
            }
        }
    }
    
    /**
     * Obtiene el UID del usuario desde caché local (bloqueante)
     */
    fun getUidBlocking(context: Context): String {
        return runBlocking {
            try {
                val prefs = context.dataStore.data.first()
                prefs[KEY_UID] ?: (auth.currentUser?.uid ?: "")
            } catch (e: Exception) {
                auth.currentUser?.uid ?: ""
            }
        }
    }
    
    /**
     * Obtiene el nombre del usuario desde caché local (bloqueante)
     */
    fun getNameBlocking(context: Context): String {
        return runBlocking {
            try {
                val prefs = context.dataStore.data.first()
                prefs[KEY_NAME] ?: ""
            } catch (e: Exception) {
                ""
            }
        }
    }
    
    /**
     * Carga el perfil desde Firestore y lo guarda localmente
     */
    suspend fun fetchAndSaveProfile(context: Context, uid: String) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    saveUserProfileLocally(context, user)
                } else {
                    Log.w(TAG, "⚠️ No se pudo parsear el perfil del usuario")
                }
            } else {
                Log.w(TAG, "⚠️ Usuario no encontrado en Firestore: $uid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cargar perfil desde Firestore: ${e.message}", e)
        }
    }
    
    /**
     * Limpia la sesión local (logout)
     */
    suspend fun clearSession(context: Context) {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(KEY_ROLE)
                prefs.remove(KEY_UID)
                prefs.remove(KEY_NAME)
                prefs.remove(KEY_EMAIL)
            }
            Log.d(TAG, "✅ Sesión local limpiada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al limpiar sesión: ${e.message}", e)
        }
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Obtiene el UID del usuario actual autenticado
     */
    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }
}




