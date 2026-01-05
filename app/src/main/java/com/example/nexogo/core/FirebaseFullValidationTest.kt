package com.example.nexogo.core

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Test completo de validación de Firebase para NexoGo
 * Verifica Authentication, Firestore y Storage
 */
class FirebaseFullValidationTest(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    suspend fun runFullValidation(): ValidationResult {
        Log.d("NEXOGO_TEST", "🔥 Iniciando validación completa de Firebase...")
        
        val results = mutableListOf<TestResult>()
        
        // 1. Test de Authentication
        val authResult = testAuthentication()
        results.add(authResult)
        
        // 2. Test de Firestore
        val firestoreResult = testFirestore()
        results.add(firestoreResult)
        
        // 3. Test de Storage
        val storageResult = testStorage()
        results.add(storageResult)
        
        // 4. Test de integración
        val integrationResult = testIntegration()
        results.add(integrationResult)
        
        val allPassed = results.all { it.success }
        val totalTests = results.size
        val passedTests = results.count { it.success }
        
        Log.d("NEXOGO_TEST", "🔥 Validación completada: $passedTests/$totalTests tests pasaron")
        
        return ValidationResult(
            success = allPassed,
            totalTests = totalTests,
            passedTests = passedTests,
            results = results
        )
    }
    
    private suspend fun testAuthentication(): TestResult {
        Log.d("NEXOGO_TEST", "🔐 Probando Authentication...")
        
        return try {
            // Test 1: Crear usuario de prueba
            val testEmail = "test@nexogo.com"
            val testPassword = "123456"
            
            // Intentar crear usuario (puede fallar si ya existe)
            try {
                val result = auth.createUserWithEmailAndPassword(testEmail, testPassword).await()
                Log.d("NEXOGO_TEST", "✅ Usuario de prueba creado: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.d("NEXOGO_TEST", "ℹ️ Usuario de prueba ya existe o error: ${e.message}")
            }
            
            // Test 2: Iniciar sesión
            val signInResult = auth.signInWithEmailAndPassword(testEmail, testPassword).await()
            val uid = signInResult.user?.uid
            
            if (uid != null) {
                Log.d("NEXOGO_TEST", "✅ Login exitoso: $uid")
                TestResult(
                    testName = "Authentication",
                    success = true,
                    message = "Login exitoso con usuario: $testEmail",
                    details = "UID: $uid"
                )
            } else {
                TestResult(
                    testName = "Authentication",
                    success = false,
                    message = "Error: Usuario no encontrado",
                    details = "No se pudo obtener UID del usuario"
                )
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_TEST", "❌ Error en Authentication: ${e.message}")
            TestResult(
                testName = "Authentication",
                success = false,
                message = "Error: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
    
    private suspend fun testFirestore(): TestResult {
        Log.d("NEXOGO_TEST", "🔥 Probando Firestore...")
        
        return try {
            val testCollection = "test_connections"
            val testDocument = "validation_${System.currentTimeMillis()}"
            val testData = mapOf(
                "message" to "Hola NexoGo!",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "testId" to UUID.randomUUID().toString(),
                "app" to "NexoGo",
                "version" to "1.0.0"
            )
            
            // Test 1: Crear documento
            firestore.collection(testCollection).document(testDocument).set(testData).await()
            Log.d("NEXOGO_TEST", "✅ Documento creado en Firestore: $testCollection/$testDocument")
            
            // Test 2: Leer documento
            val document = firestore.collection(testCollection).document(testDocument).get().await()
            val data = document.data
            
            if (data != null && data["message"] == "Hola NexoGo!") {
                Log.d("NEXOGO_TEST", "✅ Documento leído correctamente desde Firestore")
                
                // Test 3: Actualizar documento
                val updateData = mapOf(
                    "updated" to true,
                    "updateTimestamp" to com.google.firebase.Timestamp.now()
                )
                firestore.collection(testCollection).document(testDocument).update(updateData).await()
                Log.d("NEXOGO_TEST", "✅ Documento actualizado en Firestore")
                
                // Test 4: Eliminar documento
                firestore.collection(testCollection).document(testDocument).delete().await()
                Log.d("NEXOGO_TEST", "✅ Documento eliminado de Firestore")
                
                TestResult(
                    testName = "Firestore",
                    success = true,
                    message = "CRUD completo exitoso en Firestore",
                    details = "Colección: $testCollection, Documento: $testDocument"
                )
            } else {
                TestResult(
                    testName = "Firestore",
                    success = false,
                    message = "Error: Datos no coinciden",
                    details = "Datos esperados: Hola NexoGo!, Datos obtenidos: ${data?.get("message")}"
                )
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_TEST", "❌ Error en Firestore: ${e.message}")
            TestResult(
                testName = "Firestore",
                success = false,
                message = "Error: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
    
    private suspend fun testStorage(): TestResult {
        Log.d("NEXOGO_TEST", "💾 Probando Storage...")
        
        return try {
            val testPath = "test_uploads/validation_${System.currentTimeMillis()}.txt"
            val testContent = "Archivo de prueba NexoGo - ${Date()}"
            val testBytes = testContent.toByteArray()
            
            // Test 1: Subir archivo
            val storageRef = storage.reference.child(testPath)
            val uploadTask = storageRef.putBytes(testBytes).await()
            Log.d("NEXOGO_TEST", "✅ Archivo subido a Storage: $testPath")
            
            // Test 2: Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("NEXOGO_TEST", "✅ URL de descarga obtenida: $downloadUrl")
            
            // Test 3: Eliminar archivo
            storageRef.delete().await()
            Log.d("NEXOGO_TEST", "✅ Archivo eliminado de Storage")
            
            TestResult(
                testName = "Storage",
                success = true,
                message = "Operaciones de Storage exitosas",
                details = "Ruta: $testPath, URL: $downloadUrl"
            )
        } catch (e: Exception) {
            Log.e("NEXOGO_TEST", "❌ Error en Storage: ${e.message}")
            TestResult(
                testName = "Storage",
                success = false,
                message = "Error: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
    
    private suspend fun testIntegration(): TestResult {
        Log.d("NEXOGO_TEST", "🔗 Probando integración completa...")
        
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                return TestResult(
                    testName = "Integration",
                    success = false,
                    message = "Error: Usuario no autenticado",
                    details = "No se pudo obtener UID del usuario actual"
                )
            }
            
            // Test 1: Crear perfil de usuario en Firestore
            val userProfile = mapOf(
                "uid" to uid,
                "email" to "test@nexogo.com",
                "name" to "Usuario de Prueba",
                "role" to "PATIENT",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "testProfile" to true
            )
            
            firestore.collection("users").document(uid).set(userProfile).await()
            Log.d("NEXOGO_TEST", "✅ Perfil de usuario creado en Firestore")
            
            // Test 2: Subir imagen de perfil a Storage
            val profileImagePath = "users/$uid/profile_pics/test_profile.jpg"
            val profileImageBytes = "Imagen de prueba NexoGo".toByteArray()
            val profileImageRef = storage.reference.child(profileImagePath)
            profileImageRef.putBytes(profileImageBytes).await()
            val profileImageUrl = profileImageRef.downloadUrl.await()
            Log.d("NEXOGO_TEST", "✅ Imagen de perfil subida a Storage")
            
            // Test 3: Actualizar perfil con URL de imagen
            val updateData = mapOf(
                "profileImageUrl" to profileImageUrl.toString(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(uid).update(updateData).await()
            Log.d("NEXOGO_TEST", "✅ Perfil actualizado con URL de imagen")
            
            // Test 4: Crear cita de prueba
            val appointmentId = UUID.randomUUID().toString()
            val appointment = mapOf(
                "id" to appointmentId,
                "patientId" to "test_patient",
                "patientName" to "Mascota de Prueba",
                "ownerId" to uid,
                "ownerName" to "Usuario de Prueba",
                "vetId" to "test_vet",
                "vetName" to "Veterinario de Prueba",
                "dateTime" to com.google.firebase.Timestamp.now(),
                "duration" to 30,
                "status" to "SCHEDULED",
                "reason" to "Consulta de prueba",
                "notes" to "Cita creada por test de integración",
                "createdBy" to uid,
                "testAppointment" to true
            )
            
            firestore.collection("appointments").document(appointmentId).set(appointment).await()
            Log.d("NEXOGO_TEST", "✅ Cita de prueba creada en Firestore")
            
            // Test 5: Crear paciente de prueba
            val patientId = UUID.randomUUID().toString()
            val patient = mapOf(
                "id" to patientId,
                "ownerId" to uid,
                "ownerName" to "Usuario de Prueba",
                "name" to "Mascota de Prueba",
                "species" to "Perro",
                "breed" to "Labrador",
                "age" to 3,
                "weight" to 25.5,
                "color" to "Dorado",
                "gender" to "Macho",
                "medicalNotes" to "Paciente creado por test de integración",
                "testPatient" to true
            )
            
            firestore.collection("patients").document(patientId).set(patient).await()
            Log.d("NEXOGO_TEST", "✅ Paciente de prueba creado en Firestore")
            
            // Test 6: Limpiar datos de prueba
            firestore.collection("appointments").document(appointmentId).delete().await()
            firestore.collection("patients").document(patientId).delete().await()
            profileImageRef.delete().await()
            Log.d("NEXOGO_TEST", "✅ Datos de prueba limpiados")
            
            TestResult(
                testName = "Integration",
                success = true,
                message = "Integración completa exitosa",
                details = "Usuario: $uid, Cita: $appointmentId, Paciente: $patientId"
            )
        } catch (e: Exception) {
            Log.e("NEXOGO_TEST", "❌ Error en integración: ${e.message}")
            TestResult(
                testName = "Integration",
                success = false,
                message = "Error: ${e.message}",
                details = e.stackTraceToString()
            )
        }
    }
}

data class ValidationResult(
    val success: Boolean,
    val totalTests: Int,
    val passedTests: Int,
    val results: List<TestResult>
)

data class TestResult(
    val testName: String,
    val success: Boolean,
    val message: String,
    val details: String
)

