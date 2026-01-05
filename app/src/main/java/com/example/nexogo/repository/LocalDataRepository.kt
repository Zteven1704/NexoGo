package com.example.nexogo.repository

import android.content.Context
import com.example.nexogo.data.AppDataStore
import com.example.nexogo.model.Appointment
import com.example.nexogo.core.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface LocalDataRepository {
    suspend fun saveUser(user: User)
    fun getUser(): Flow<User?>
    fun isAuthenticated(): Flow<Boolean>
    suspend fun clearUser()
    suspend fun updateUserField(key: String, value: String)
    suspend fun saveAppointments(appointments: List<Appointment>)
    fun getAppointments(): Flow<List<Appointment>>
    suspend fun savePendingUsers(users: List<User>)
    fun getPendingUsers(): Flow<List<User>>
}

@Singleton
class LocalDataRepositoryImpl @Inject constructor(
    private val context: Context
) : LocalDataRepository {
    
    private val dataStore = AppDataStore(context)
    private val gson = Gson()
    
    override suspend fun saveUser(user: User) {
        dataStore.saveUser(user)
    }
    
    override fun getUser(): Flow<User?> = dataStore.getUser()
    
    override fun isAuthenticated(): Flow<Boolean> = dataStore.isAuthenticated()
    
    override suspend fun clearUser() {
        dataStore.clearUser()
    }
    
    override suspend fun updateUserField(key: String, value: String) {
        dataStore.updateUserField(key, value)
    }
    
    override suspend fun saveAppointments(appointments: List<Appointment>) {
        val json = gson.toJson(appointments)
        dataStore.saveAppointments(json)
    }
    
    override fun getAppointments(): Flow<List<Appointment>> = dataStore.getAppointments().map { json ->
        if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<Appointment>>() {}.type
                gson.fromJson<List<Appointment>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    override suspend fun savePendingUsers(users: List<User>) {
        val json = gson.toJson(users)
        dataStore.savePendingUsers(json)
    }
    
    override fun getPendingUsers(): Flow<List<User>> = dataStore.getPendingUsers().map { json ->
        if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<User>>() {}.type
                gson.fromJson<List<User>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

