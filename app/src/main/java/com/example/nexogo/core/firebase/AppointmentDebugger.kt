package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * AppointmentDebugger
 * -------------------
 * Herramienta de depuración para verificar el estado de las citas en Firestore
 */
object AppointmentDebugger {
    
    private const val TAG = "AppointmentDebugger"
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Verifica si existen citas en Firestore y muestra información detallada
     */
    suspend fun debugAppointmentsInFirestore() {
        try {
            Log.d(TAG, "🔍 Iniciando diagnóstico de citas en Firestore...")
            
            // Verificar colección "citas"
            val citasSnapshot = firestore.collection("citas").get().await()
            Log.d(TAG, "📊 Colección 'citas': ${citasSnapshot.documents.size} documentos")
            
            citasSnapshot.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "📄 Documento $index:")
                Log.d(TAG, "   ID: ${doc.id}")
                Log.d(TAG, "   Datos: ${doc.data}")
            }
            
            // Verificar colección "appointments" (por si acaso)
            val appointmentsSnapshot = firestore.collection("appointments").get().await()
            Log.d(TAG, "📊 Colección 'appointments': ${appointmentsSnapshot.documents.size} documentos")
            
            appointmentsSnapshot.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "📄 Documento $index:")
                Log.d(TAG, "   ID: ${doc.id}")
                Log.d(TAG, "   Datos: ${doc.data}")
            }
            
            // Verificar colección "usuarios" para ver si hay usuarios
            val usuariosSnapshot = firestore.collection("usuarios").get().await()
            Log.d(TAG, "👥 Colección 'usuarios': ${usuariosSnapshot.documents.size} documentos")
            
            // Verificar colección "users" (por si acaso)
            val usersSnapshot = firestore.collection("users").get().await()
            Log.d(TAG, "👥 Colección 'users': ${usersSnapshot.documents.size} documentos")
            
            Log.d(TAG, "✅ Diagnóstico completado")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en diagnóstico: ${e.message}", e)
        }
    }
    
    /**
     * Crea una cita de prueba si no existen citas
     */
    suspend fun createTestAppointmentIfNeeded() {
        try {
            val citasSnapshot = firestore.collection("citas").get().await()
            
            if (citasSnapshot.documents.isEmpty()) {
                Log.d(TAG, "🆕 No hay citas, creando cita de prueba...")
                
                val testAppointment = mapOf(
                    "id" to "test_appointment_001",
                    "patientId" to "test_patient_001",
                    "patientName" to "Max (Prueba)",
                    "ownerId" to "test_owner_001",
                    "ownerName" to "Dueño de Prueba",
                    "vetId" to "test_vet_001",
                    "vetName" to "Dr. Prueba",
                    "dateTime" to com.google.firebase.Timestamp.now(),
                    "duration" to 30,
                    "status" to "SCHEDULED",
                    "reason" to "Consulta de prueba",
                    "notes" to "Esta es una cita de prueba",
                    "createdBy" to "test_user_001",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                
                firestore.collection("citas").document("test_appointment_001").set(testAppointment).await()
                Log.d(TAG, "✅ Cita de prueba creada exitosamente")
            } else {
                Log.d(TAG, "ℹ️ Ya existen ${citasSnapshot.documents.size} citas, no se creará cita de prueba")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando cita de prueba: ${e.message}", e)
        }
    }
}

