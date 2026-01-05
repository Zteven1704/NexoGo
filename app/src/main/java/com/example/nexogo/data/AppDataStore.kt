package com.example.nexogo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.model.ClinicalRecord
import com.example.nexogo.model.Product
import com.example.nexogo.model.Message
import com.example.nexogo.model.Chat
import com.example.nexogo.viewmodel.Settings
import com.example.nexogo.viewmodel.ProductCategory
import com.example.nexogo.model.Sale
import com.example.nexogo.model.VeterinaryService
import com.example.nexogo.model.Invoice
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppDataStore(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nexogo_preferences")
        
        // User preferences keys
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PHONE = stringPreferencesKey("user_phone")
        private val USER_WHATSAPP = stringPreferencesKey("user_whatsapp")
        private val USER_PROFILE_IMAGE_URL = stringPreferencesKey("user_profile_image_url")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_IS_APPROVED = booleanPreferencesKey("user_is_approved")
        private val USER_IS_PROFESSIONAL = booleanPreferencesKey("user_is_professional")
        private val USER_SPECIALIZATION = stringPreferencesKey("user_specialization")
        private val USER_LICENSE_NUMBER = stringPreferencesKey("user_license_number")
        private val USER_LANGUAGE = stringPreferencesKey("user_language")
        private val USER_IS_ACTIVE = booleanPreferencesKey("user_is_active")
        
        // App state keys
        private val IS_AUTHENTICATED = booleanPreferencesKey("is_authenticated")
        private val LAST_LOGIN_TIME = longPreferencesKey("last_login_time")
    }
    
    // Save user data
    suspend fun saveUser(user: User) {
        println("DEBUG: AppDataStore - Guardando usuario: ${user.name}")
        println("DEBUG: AppDataStore - Datos a guardar:")
        println("  - ID: ${user.id}")
        println("  - Email: ${user.email}")
        println("  - Nombre: ${user.name}")
        println("  - Teléfono: ${user.phone}")
        println("  - WhatsApp: ${user.whatsapp}")
        println("  - Foto: ${user.profileImageUrl}")
        println("  - Rol: ${user.role}")
        
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_EMAIL] = user.email
            preferences[USER_NAME] = user.name
            preferences[USER_PHONE] = user.phone
            preferences[USER_WHATSAPP] = user.whatsapp
            preferences[USER_PROFILE_IMAGE_URL] = user.profileImageUrl
            preferences[USER_ROLE] = user.role.name
            preferences[USER_IS_APPROVED] = user.isApproved
            preferences[USER_IS_PROFESSIONAL] = user.isProfessional
            preferences[USER_SPECIALIZATION] = user.specialization
            preferences[USER_LICENSE_NUMBER] = user.licenseNumber
            preferences[USER_LANGUAGE] = user.language
            preferences[USER_IS_ACTIVE] = user.isActive
            preferences[IS_AUTHENTICATED] = true
            preferences[LAST_LOGIN_TIME] = System.currentTimeMillis()
        }
        
        println("DEBUG: AppDataStore - Usuario guardado exitosamente en DataStore")
    }
    
    // Get user data
    fun getUser(): Flow<User?> = context.dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        val email = preferences[USER_EMAIL] ?: ""
        val name = preferences[USER_NAME] ?: ""
        val phone = preferences[USER_PHONE] ?: ""
        val whatsapp = preferences[USER_WHATSAPP] ?: ""
        val profileImageUrl = preferences[USER_PROFILE_IMAGE_URL] ?: ""
        val roleString = preferences[USER_ROLE] ?: "USER"
        val role = try { UserRole.valueOf(roleString) } catch (e: Exception) { UserRole.USER }
        val isApproved = preferences[USER_IS_APPROVED] ?: true
        val isProfessional = preferences[USER_IS_PROFESSIONAL] ?: false
        val specialization = preferences[USER_SPECIALIZATION] ?: ""
        val licenseNumber = preferences[USER_LICENSE_NUMBER] ?: ""
        val language = preferences[USER_LANGUAGE] ?: "es"
        val isActive = preferences[USER_IS_ACTIVE] ?: true
        
        println("DEBUG: AppDataStore - Cargando usuario desde DataStore:")
        println("  - ID: $id")
        println("  - Email: $email")
        println("  - Nombre: $name")
        println("  - Teléfono: $phone")
        println("  - WhatsApp: $whatsapp")
        println("  - Foto: $profileImageUrl")
        println("  - Rol: $role")
        
        User(
            id = id,
            email = email,
            name = name,
            phone = phone,
            whatsapp = whatsapp,
            profileImageUrl = profileImageUrl,
            role = role,
            isApproved = isApproved,
            isProfessional = isProfessional,
            specialization = specialization,
            licenseNumber = licenseNumber,
            language = language,
            isActive = isActive,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    }
    
    // Check if user is authenticated
    fun isAuthenticated(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTHENTICATED] ?: false
    }
    
    // Check if there's any user data saved (regardless of authentication status)
    fun hasUserData(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] != null
    }
    
    // Clear user data (logout) - Solo limpiar autenticación, mantener datos del usuario
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            // Solo limpiar la autenticación, mantener los datos del usuario
            preferences.remove(IS_AUTHENTICATED)
            preferences.remove(LAST_LOGIN_TIME)
            // NO limpiar los datos del usuario (USER_ID, USER_NAME, etc.)
        }
    }
    
    // Función para limpiar completamente todos los datos (solo para casos especiales)
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    // Update specific user fields
    suspend fun updateUserField(key: String, value: String) {
        context.dataStore.edit { preferences ->
            when (key) {
                "name" -> preferences[USER_NAME] = value
                "phone" -> preferences[USER_PHONE] = value
                "whatsapp" -> preferences[USER_WHATSAPP] = value
                "profileImageUrl" -> preferences[USER_PROFILE_IMAGE_URL] = value
                "specialization" -> preferences[USER_SPECIALIZATION] = value
                "licenseNumber" -> preferences[USER_LICENSE_NUMBER] = value
                "language" -> preferences[USER_LANGUAGE] = value
            }
        }
    }
    
    // Save appointments data (simple JSON string for now)
    suspend fun saveAppointments(appointmentsJson: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("appointments")] = appointmentsJson
        }
    }
    
    // Get appointments data
    fun getAppointments(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("appointments")]
    }
    
    // Save pending users data
    suspend fun savePendingUsers(pendingUsersJson: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("pending_users")] = pendingUsersJson
        }
    }
    
    // Get pending users data
    fun getPendingUsers(): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[stringPreferencesKey("pending_users")]
    }
    
    // USERs functions
    suspend fun saveUsers(users: List<SimplePatient>) {
        context.dataStore.edit { preferences ->
            val gson = com.google.gson.Gson()
            val usersJson = gson.toJson(users)
            preferences[stringPreferencesKey("users")] = usersJson
        }
    }
    
    suspend fun loadUsers(): List<SimplePatient> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("users")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<SimplePatient>>() {}.type
                gson.fromJson<List<SimplePatient>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Clinical Records functions
    suspend fun saveClinicalRecords(records: List<ClinicalRecord>) {
        context.dataStore.edit { preferences ->
            val gson = com.google.gson.Gson()
            val recordsJson = gson.toJson(records)
            preferences[stringPreferencesKey("clinical_records")] = recordsJson
        }
    }
    
    suspend fun loadClinicalRecords(): List<ClinicalRecord> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("clinical_records")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<ClinicalRecord>>() {}.type
                gson.fromJson<List<ClinicalRecord>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Products functions
    suspend fun saveProducts(products: List<Product>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(products)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("products")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadProducts(): List<Product> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("products")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<Product>>() {}.type
                gson.fromJson<List<Product>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Chats functions
    suspend fun saveChats(chats: List<Chat>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(chats)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("chats")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadChats(): List<Chat> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("chats")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<Chat>>() {}.type
                gson.fromJson<List<Chat>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Messages functions
    suspend fun saveMessages(messages: List<Message>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(messages)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("messages")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadMessages(): List<Message> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("messages")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<Message>>() {}.type
                gson.fromJson<List<Message>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Settings functions
    suspend fun saveSettings(settings: Settings) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(settings)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("settings")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadSettings(): Settings? {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("settings")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                gson.fromJson<Settings>(json, Settings::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Product Categories functions
    suspend fun saveProductCategories(categories: List<ProductCategory>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(categories)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("product_categories")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadProductCategories(): List<ProductCategory> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("product_categories")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<ProductCategory>>() {}.type
                gson.fromJson<List<ProductCategory>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Sales functions
    suspend fun saveSales(sales: List<Sale>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(sales)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("sales")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadSales(): List<Sale> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("sales")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<Sale>>() {}.type
                gson.fromJson<List<Sale>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Veterinary Services functions
    suspend fun saveVeterinaryServices(services: List<VeterinaryService>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(services)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("veterinary_services")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadVeterinaryServices(): List<VeterinaryService> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("veterinary_services")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<VeterinaryService>>() {}.type
                gson.fromJson<List<VeterinaryService>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Invoices functions
    suspend fun saveInvoices(invoices: List<Invoice>) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(invoices)
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey("invoices")] = json
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun loadInvoices(): List<Invoice> {
        return try {
            val json = context.dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("invoices")]
            }.first()
            if (json != null) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<List<Invoice>>() {}.type
                gson.fromJson<List<Invoice>>(json, type) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
