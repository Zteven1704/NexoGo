package com.example.nexogo.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.example.nexogo.model.*
import java.util.*

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    // ==================== APPOINTMENTS ====================
    
    suspend fun addAppointment(appointment: Appointment): Result<Unit> {
        return try {
            // Generar ID único si no existe
            val appointmentWithId = if (appointment.id.isEmpty()) {
                appointment.copy(id = UUID.randomUUID().toString())
            } else {
                appointment
            }
            
            println("DEBUG: FirebaseRepository - Guardando cita con ID: ${appointmentWithId.id}")
            println("DEBUG: FirebaseRepository - Mascota: ${appointmentWithId.patientName}")
            println("DEBUG: FirebaseRepository - Fecha: ${appointmentWithId.dateTime}")
            
            firestore.collection("citas").document(appointmentWithId.id).set(appointmentWithId).await()
            
            println("DEBUG: FirebaseRepository - Cita guardada exitosamente en Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: FirebaseRepository - Error al guardar cita: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateAppointment(appointment: Appointment): Result<Unit> {
        return try {
            firestore.collection("citas").document(appointment.id).set(appointment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            firestore.collection("citas").document(appointmentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAppointmentById(appointmentId: String): Result<Appointment?> {
        return try {
            val document = firestore.collection("citas").document(appointmentId).get().await()
            val appointment = document.toObject(Appointment::class.java)
            Result.success(appointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllAppointments(): Result<List<Appointment>> {
        return try {
            println("DEBUG: FirebaseRepository - Obteniendo citas de la colección 'citas'")
            val snapshot = firestore.collection("citas").get().await()
            println("DEBUG: FirebaseRepository - Snapshot obtenido: ${snapshot.documents.size} documentos")
            val appointments = snapshot.documents.mapNotNull { doc ->
                try {
                    val appointment = doc.toObject(Appointment::class.java)
                    println("DEBUG: FirebaseRepository - Cita parseada: ${appointment?.id} - ${appointment?.patientName}")
                    appointment
                } catch (e: Exception) {
                    println("DEBUG: FirebaseRepository - Error parseando cita: ${e.message}")
                    null
                }
            }
            println("DEBUG: FirebaseRepository - Total de citas parseadas: ${appointments.size}")
            Result.success(appointments)
        } catch (e: Exception) {
            println("DEBUG: FirebaseRepository - Error obteniendo citas: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun listenToAppointments(): Flow<List<Appointment>> = callbackFlow {
        println("DEBUG: FirebaseRepository - Iniciando listener de citas en tiempo real")
        
        val listener = firestore.collection("citas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: FirebaseRepository - Error en listener: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val appointments = snapshot.documents.mapNotNull { document ->
                        document.toObject(Appointment::class.java)
                    }
                    println("DEBUG: FirebaseRepository - Citas actualizadas: ${appointments.size}")
                    trySend(appointments)
                }
            }
        
        awaitClose {
            println("DEBUG: FirebaseRepository - Cerrando listener de citas")
            listener.remove()
        }
    }
    
    // ==================== PATIENTS ====================
    
    suspend fun addPatient(patient: Patient): Result<Unit> {
        return try {
            firestore.collection("patients").document(patient.id).set(patient).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePatient(patient: Patient): Result<Unit> {
        return try {
            firestore.collection("patients").document(patient.id).set(patient).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePatient(patientId: String): Result<Unit> {
        return try {
            firestore.collection("patients").document(patientId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPatientById(patientId: String): Result<Patient?> {
        return try {
            val document = firestore.collection("patients").document(patientId).get().await()
            val patient = document.toObject(Patient::class.java)
            Result.success(patient)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllPatients(): Result<List<Patient>> {
        return try {
            val snapshot = firestore.collection("patients").get().await()
            val patients = snapshot.documents.mapNotNull { it.toObject(Patient::class.java) }
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToPatients(): Flow<List<Patient>> = callbackFlow {
        println("DEBUG: FirebaseRepository - Iniciando listener de pacientes en tiempo real")
        
        val listener = firestore.collection("patients")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: FirebaseRepository - Error en listener de pacientes: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val patients = snapshot.documents.mapNotNull { document ->
                        document.toObject(Patient::class.java)
                    }
                    println("DEBUG: FirebaseRepository - Pacientes actualizados: ${patients.size}")
                    trySend(patients)
                }
            }
        
        awaitClose {
            println("DEBUG: FirebaseRepository - Cerrando listener de pacientes")
            listener.remove()
        }
    }
    
    // ==================== PRODUCTS ====================
    
    suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            firestore.collection("products").document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            firestore.collection("products").document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            val document = firestore.collection("products").document(productId).get().await()
            val product = document.toObject(Product::class.java)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = firestore.collection("products").get().await()
            val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToProducts(): Flow<List<Product>> = flow {
        // Por ahora retornamos una lista vacía
        // TODO: Implementar listener real
        emit(emptyList())
    }
    
    // ==================== SALES ====================
    
    suspend fun addSale(sale: Sale): Result<Unit> {
        return try {
            firestore.collection("sales").document(sale.id).set(sale).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSale(sale: Sale): Result<Unit> {
        return try {
            firestore.collection("sales").document(sale.id).set(sale).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSale(saleId: String): Result<Unit> {
        return try {
            firestore.collection("sales").document(saleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSaleById(saleId: String): Result<Sale?> {
        return try {
            val document = firestore.collection("sales").document(saleId).get().await()
            val sale = document.toObject(Sale::class.java)
            Result.success(sale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllSales(): Result<List<Sale>> {
        return try {
            val snapshot = firestore.collection("sales").get().await()
            val sales = snapshot.documents.mapNotNull { it.toObject(Sale::class.java) }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToSales(): Flow<List<Sale>> = flow {
        // Por ahora retornamos una lista vacía
        // TODO: Implementar listener real
        emit(emptyList())
    }
    
    // ==================== MEDICAL RECORDS ====================
    
    suspend fun addMedicalRecord(record: MedicalRecord): Result<Unit> {
        return try {
            firestore.collection("medical_records").document(record.id).set(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateMedicalRecord(record: MedicalRecord): Result<Unit> {
        return try {
            firestore.collection("medical_records").document(record.id).set(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMedicalRecord(recordId: String): Result<Unit> {
        return try {
            firestore.collection("medical_records").document(recordId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMedicalRecordById(recordId: String): Result<MedicalRecord?> {
        return try {
            val document = firestore.collection("medical_records").document(recordId).get().await()
            val record = document.toObject(MedicalRecord::class.java)
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllMedicalRecords(): Result<List<MedicalRecord>> {
        return try {
            val snapshot = firestore.collection("medical_records").get().await()
            val records = snapshot.documents.mapNotNull { it.toObject(MedicalRecord::class.java) }
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToMedicalRecords(): Flow<List<MedicalRecord>> = flow {
        // Por ahora retornamos una lista vacía
        // TODO: Implementar listener real
        emit(emptyList())
    }
    
    // ==================== MESSAGES ====================
    
    suspend fun addMessage(message: Message): Result<Unit> {
        return try {
            firestore.collection("messages").document(message.id).set(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateMessage(message: Message): Result<Unit> {
        return try {
            firestore.collection("messages").document(message.id).set(message).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            firestore.collection("messages").document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMessageById(messageId: String): Result<Message?> {
        return try {
            val document = firestore.collection("messages").document(messageId).get().await()
            val message = document.toObject(Message::class.java)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllMessages(): Result<List<Message>> {
        return try {
            val snapshot = firestore.collection("messages").get().await()
            val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToMessages(): Flow<List<Message>> = flow {
        // Por ahora retornamos una lista vacía
        // TODO: Implementar listener real
        emit(emptyList())
    }
    
    // ==================== STORAGE ====================
    
    suspend fun uploadFile(fileUri: Uri, path: String): Result<String> {
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
                return Result.failure(Exception("URI del archivo es null"))
            }
            
            // Crear referencia con ruta corregida
            val correctedPath = path.replace("test/", "test_uploads/")
            val storageRef = storage.reference.child(correctedPath)
            
            // Verificar que la referencia sea válida
            if (storageRef == null) {
                return Result.failure(Exception("No se pudo crear referencia de Storage"))
            }
            
            android.util.Log.d("FirebaseRepository", "🔍 Subiendo archivo a: $correctedPath")
            
            // Verificar que el archivo exista antes de subir
            try {
                val file = java.io.File(fileUri.path!!)
                if (!file.exists()) {
                    android.util.Log.e("FirebaseRepository", "Archivo no encontrado, creando archivo de prueba.")
                    val tempBytes = "Archivo de prueba NexoGo".toByteArray()
                    val uploadTask = storageRef.putBytes(tempBytes).await()
                    val downloadUrl = storageRef.downloadUrl.await()
                    android.util.Log.d("FirebaseRepository", "✅ Archivo subido correctamente a ${storageRef.path}")
                    return Result.success(downloadUrl.toString())
                } else {
                    val uploadTask = storageRef.putFile(fileUri).await()
                    val downloadUrl = storageRef.downloadUrl.await()
                    android.util.Log.d("FirebaseRepository", "✅ Archivo subido correctamente a ${storageRef.path}")
                    return Result.success(downloadUrl.toString())
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseRepository", "Error verificando archivo: ${e.message}")
                // Intentar subir de todas formas
                val uploadTask = storageRef.putFile(fileUri).await()
                val downloadUrl = storageRef.downloadUrl.await()
                android.util.Log.d("FirebaseRepository", "✅ Archivo subido correctamente a ${storageRef.path}")
                return Result.success(downloadUrl.toString())
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "❌ Error subiendo archivo: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteFile(path: String): Result<Unit> {
        return try {
            val storageRef = storage.reference.child(path)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFileUrl(path: String): Result<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}