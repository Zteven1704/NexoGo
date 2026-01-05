package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * AppointmentDiagnostics
 * ----------------------
 * Herramienta de diagnóstico específica para verificar el estado de las citas
 * en Firebase Firestore y identificar problemas de visualización.
 */
object AppointmentDiagnostics {

    private const val TAG = "AppointmentDiagnostics"
    private val db = FirebaseFirestore.getInstance()

    /**
     * 🔍 Verifica el estado de las citas en Firestore
     */
    suspend fun checkAppointmentsInFirestore(): Boolean {
        return try {
            Log.d(TAG, "🔍 Verificando citas en Firestore...")
            
            // Verificar colección "citas"
            val citasSnapshot = db.collection("citas").get().await()
            Log.d(TAG, "📊 Colección 'citas': ${citasSnapshot.size()} documentos")
            
            // Verificar colección "appointments" (por si acaso)
            val appointmentsSnapshot = db.collection("appointments").get().await()
            Log.d(TAG, "📊 Colección 'appointments': ${appointmentsSnapshot.size()} documentos")
            
            // Mostrar detalles de las citas
            citasSnapshot.documents.forEachIndexed { index, doc ->
                val data = doc.data
                Log.d(TAG, "📋 Cita $index:")
                Log.d(TAG, "   - ID: ${doc.id}")
                Log.d(TAG, "   - Patient: ${data?.get("patientName")}")
                Log.d(TAG, "   - Owner: ${data?.get("ownerName")}")
                Log.d(TAG, "   - Date: ${data?.get("dateTime")}")
                Log.d(TAG, "   - Status: ${data?.get("status")}")
            }
            
            val totalCitas = citasSnapshot.size() + appointmentsSnapshot.size()
            Log.d(TAG, "✅ Total de citas encontradas: $totalCitas")
            
            totalCitas > 0
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error verificando citas: ${e.message}", e)
            false
        }
    }

    /**
     * 🧪 Prueba la creación de una cita de prueba
     */
    suspend fun createTestAppointment(): Boolean {
        return try {
            Log.d(TAG, "🧪 Creando cita de prueba...")
            
            val testAppointment = mapOf(
                "id" to "test_appointment_${System.currentTimeMillis()}",
                "patientId" to "test_patient",
                "patientName" to "Mascota de Prueba",
                "ownerId" to "test_owner",
                "ownerName" to "Dueño de Prueba",
                "vetId" to "test_vet",
                "vetName" to "Veterinario de Prueba",
                "dateTime" to com.google.firebase.Timestamp.now(),
                "duration" to 30,
                "status" to "SCHEDULED",
                "reason" to "Consulta de prueba",
                "notes" to "Cita creada para diagnóstico",
                "createdBy" to "system",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            db.collection("citas").document(testAppointment["id"] as String).set(testAppointment).await()
            Log.d(TAG, "✅ Cita de prueba creada exitosamente")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creando cita de prueba: ${e.message}", e)
            false
        }
    }

    /**
     * 📊 Genera reporte completo de citas
     */
    suspend fun generateAppointmentReport(): String {
        val report = StringBuilder()
        report.appendLine("📊 REPORTE DE CITAS - NEXOGO")
        report.appendLine("=============================")
        
        try {
            // Verificar colección "citas"
            val citasSnapshot = db.collection("citas").get().await()
            report.appendLine("📋 Colección 'citas': ${citasSnapshot.size()} documentos")
            
            citasSnapshot.documents.forEachIndexed { index, doc ->
                val data = doc.data
                report.appendLine("   ${index + 1}. ${data?.get("patientName")} - ${data?.get("ownerName")} - ${data?.get("status")}")
            }
            
            // Verificar colección "appointments"
            val appointmentsSnapshot = db.collection("appointments").get().await()
            report.appendLine("📋 Colección 'appointments': ${appointmentsSnapshot.size()} documentos")
            
            appointmentsSnapshot.documents.forEachIndexed { index, doc ->
                val data = doc.data
                report.appendLine("   ${index + 1}. ${data?.get("patientName")} - ${data?.get("ownerName")} - ${data?.get("status")}")
            }
            
            val totalCitas = citasSnapshot.size() + appointmentsSnapshot.size()
            report.appendLine("")
            report.appendLine("✅ Total de citas: $totalCitas")
            
            if (totalCitas == 0) {
                report.appendLine("⚠️ No se encontraron citas en ninguna colección")
                report.appendLine("💡 Verificar que las citas se estén guardando correctamente")
            }
            
        } catch (e: Exception) {
            report.appendLine("❌ Error generando reporte: ${e.message}")
        }
        
        return report.toString()
    }
}
