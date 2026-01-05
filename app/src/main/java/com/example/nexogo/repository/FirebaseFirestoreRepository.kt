package com.example.nexogo.repository

import com.example.nexogo.firebase.FirebaseConfig
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.model.Appointment
import com.example.nexogo.core.models.Patient
import com.example.nexogo.core.models.ClinicalRecord
import com.example.nexogo.core.models.Product
import com.example.nexogo.core.models.Sale
import com.example.nexogo.core.models.Chat
import com.example.nexogo.core.models.Message
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.model.AppointmentStatus
import com.example.nexogo.model.PaymentMethod
import com.example.nexogo.model.PaymentStatus
import com.example.nexogo.model.AttachmentType
import com.example.nexogo.model.RecurringType
import com.example.nexogo.model.VeterinaryService
import com.example.nexogo.model.Pet
import com.example.nexogo.model.Vaccination
import com.example.nexogo.model.PhysicalExam
import com.example.nexogo.model.SaleItem
import com.example.nexogo.model.MedicalRecord
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar operaciones de Firestore
 * Incluye CRUD para todas las colecciones principales
 */
@Singleton
class FirebaseFirestoreRepository @Inject constructor() {
    
    private val firestore: FirebaseFirestore = FirebaseConfig.firestore
    
    // ==================== USUARIOS ====================
    
    /**
     * Obtiene un usuario por ID
     */
    suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val doc = firestore.collection("usuarios")
                .document(userId)
                .get()
                .await()
            
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los usuarios con un rol específico
     */
    suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("usuarios")
                .whereEqualTo("role", role.toString())
                .whereEqualTo("isApproved", true)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }
            
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== CITAS ====================
    
    /**
     * Crea una nueva cita
     */
    suspend fun createAppointment(appointment: Appointment): Result<String> {
        return try {
            val docRef = firestore.collection("citas")
                .add(appointment)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene citas por usuario
     */
    suspend fun getAppointmentsByUser(userId: String): Result<List<Appointment>> {
        return try {
            val snapshot = firestore.collection("citas")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val appointments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
            
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene citas por veterinario
     */
    suspend fun getAppointmentsByVeterinarian(veterinarianId: String): Result<List<Appointment>> {
        return try {
            val snapshot = firestore.collection("citas")
                .whereEqualTo("veterinarianId", veterinarianId)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val appointments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Appointment::class.java)?.copy(id = doc.id)
            }
            
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza una cita
     */
    suspend fun updateAppointment(appointmentId: String, appointment: Appointment): Result<Unit> {
        return try {
            firestore.collection("citas")
                .document(appointmentId)
                .set(appointment)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina una cita
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.collection("citas")
                .document(appointmentId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== INVENTARIO ====================
    
    /**
     * Crea un nuevo producto
     */
    suspend fun createProduct(product: Product): Result<String> {
        return try {
            val docRef = firestore.collection("inventario")
                .add(product)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los productos
     */
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("inventario")
                .orderBy("name")
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene productos con stock bajo
     */
    suspend fun getLowStockProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("inventario")
                .whereLessThan("currentStock", 10) // Menos de 10 unidades
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
            
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un producto
     */
    suspend fun updateProduct(productId: String, product: Product): Result<Unit> {
        return try {
            firestore.collection("inventario")
                .document(productId)
                .set(product)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Elimina un producto
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection("inventario")
                .document(productId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crea un servicio veterinario
     */
    suspend fun createVeterinaryService(service: com.example.nexogo.model.VeterinaryService): Result<String> {
        return try {
            val docRef = firestore.collection("servicios_veterinarios")
                .add(service)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== HISTORIAL CLÍNICO ====================
    
    /**
     * Crea un nuevo historial clínico
     */
    suspend fun createMedicalRecord(record: MedicalRecord): Result<String> {
        return try {
            val docRef = firestore.collection("historial_clinico")
                .add(record)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene historial clínico por paciente
     */
    suspend fun getMedicalRecordsByPatient(patientId: String): Result<List<MedicalRecord>> {
        return try {
            val snapshot = firestore.collection("historial_clinico")
                .whereEqualTo("patientId", patientId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val records = snapshot.documents.mapNotNull { doc ->
                doc.toObject(MedicalRecord::class.java)?.copy(id = doc.id)
            }
            
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza un historial clínico
     */
    suspend fun updateMedicalRecord(recordId: String, record: MedicalRecord): Result<Unit> {
        return try {
            firestore.collection("historial_clinico")
                .document(recordId)
                .set(record)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== VENTAS ====================
    
    /**
     * Crea una nueva venta
     */
    suspend fun createSale(sale: Sale): Result<String> {
        return try {
            val docRef = firestore.collection("ventas")
                .add(sale)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene ventas por fecha
     */
    suspend fun getSalesByDateRange(startDate: com.google.firebase.Timestamp, endDate: com.google.firebase.Timestamp): Result<List<Sale>> {
        return try {
            val snapshot = firestore.collection("ventas")
                .whereGreaterThanOrEqualTo("saleDate", startDate)
                .whereLessThanOrEqualTo("saleDate", endDate)
                .orderBy("saleDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val sales = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Sale::class.java)?.copy(id = doc.id)
            }
            
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== MENSAJES/CHAT ====================
    
    /**
     * Crea un nuevo chat
     */
    suspend fun createChat(chat: Chat): Result<String> {
        return try {
            val docRef = firestore.collection("chats")
                .add(chat)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene chats de un usuario
     */
    suspend fun getUserChats(userId: String): Result<List<Chat>> {
        return try {
            val snapshot = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                .orderBy("lastActivity", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chats = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Chat::class.java)?.copy(id = doc.id)
            }
            
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Crea un nuevo mensaje
     */
    suspend fun createMessage(message: Message): Result<String> {
        return try {
            val docRef = firestore.collection("mensajes")
                .add(message)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene mensajes de un chat
     */
    suspend fun getChatMessages(chatId: String): Result<List<Message>> {
        return try {
            val snapshot = firestore.collection("mensajes")
                .whereEqualTo("conversationId", chatId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val messages = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)?.copy(id = doc.id)
            }
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== LISTENERS EN TIEMPO REAL ====================
    
    /**
     * Escucha cambios en tiempo real de citas
     */
    fun listenToAppointments(userId: String): Flow<List<Appointment>> = callbackFlow {
        val listener = firestore.collection("citas")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val appointments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Appointment::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(appointments)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Escucha cambios en tiempo real de mensajes
     */
    fun listenToMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("mensajes")
            .whereEqualTo("conversationId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
}
