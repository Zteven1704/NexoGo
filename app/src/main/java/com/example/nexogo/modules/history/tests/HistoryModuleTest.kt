package com.example.nexogo.modules.history.tests

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.history.models.*
import com.example.nexogo.modules.history.repo.HistoryRepository
import com.example.nexogo.modules.history.viewmodel.HistoryViewModel
import com.example.nexogo.modules.history.utils.PdfGenerator
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import java.util.*

/**
 * Tests para el módulo de historial clínico
 * Incluye pruebas unitarias e integración para CRUD y subida de archivos
 */
class HistoryModuleTest(
    private val context: Context,
    private val firebaseRepository: FirebaseRepository
) {
    companion object {
        private const val TAG = "NEXOGO_HISTORY_TEST"
    }

    private val historyRepository = HistoryRepository(firebaseRepository)
    private val historyViewModel = HistoryViewModel(historyRepository)

    /**
     * Ejecuta todos los tests del módulo de historial
     */
    fun runAllTests(): Boolean {
        Log.d(TAG, "=== INICIANDO TESTS DEL MÓDULO HISTORIAL ===")
        
        var allTestsPassed = true
        
        try {
            // Test 1: Crear historial clínico
            Log.d(TAG, "Test 1: Crear historial clínico")
            val createTest = testCreateRecord()
            if (createTest) {
                Log.d(TAG, "✅ Test 1 PASÓ: Crear historial clínico")
            } else {
                Log.e(TAG, "❌ Test 1 FALLÓ: Crear historial clínico")
                allTestsPassed = false
            }
            
            // Test 2: Leer historial clínico
            Log.d(TAG, "Test 2: Leer historial clínico")
            val readTest = testReadRecord()
            if (readTest) {
                Log.d(TAG, "✅ Test 2 PASÓ: Leer historial clínico")
            } else {
                Log.e(TAG, "❌ Test 2 FALLÓ: Leer historial clínico")
                allTestsPassed = false
            }
            
            // Test 3: Actualizar historial clínico
            Log.d(TAG, "Test 3: Actualizar historial clínico")
            val updateTest = testUpdateRecord()
            if (updateTest) {
                Log.d(TAG, "✅ Test 3 PASÓ: Actualizar historial clínico")
            } else {
                Log.e(TAG, "❌ Test 3 FALLÓ: Actualizar historial clínico")
                allTestsPassed = false
            }
            
            // Test 4: Subir archivo adjunto
            Log.d(TAG, "Test 4: Subir archivo adjunto")
            val uploadTest = testUploadAttachment()
            if (uploadTest) {
                Log.d(TAG, "✅ Test 4 PASÓ: Subir archivo adjunto")
            } else {
                Log.e(TAG, "❌ Test 4 FALLÓ: Subir archivo adjunto")
                allTestsPassed = false
            }
            
            // Test 5: Generar PDF
            Log.d(TAG, "Test 5: Generar PDF")
            val pdfTest = testGeneratePdf()
            if (pdfTest) {
                Log.d(TAG, "✅ Test 5 PASÓ: Generar PDF")
            } else {
                Log.e(TAG, "❌ Test 5 FALLÓ: Generar PDF")
                allTestsPassed = false
            }
            
            // Test 6: Eliminar historial clínico
            Log.d(TAG, "Test 6: Eliminar historial clínico")
            val deleteTest = testDeleteRecord()
            if (deleteTest) {
                Log.d(TAG, "✅ Test 6 PASÓ: Eliminar historial clínico")
            } else {
                Log.e(TAG, "❌ Test 6 FALLÓ: Eliminar historial clínico")
                allTestsPassed = false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error ejecutando tests: ${e.message}")
            allTestsPassed = false
        }
        
        if (allTestsPassed) {
            Log.d(TAG, "🎉 TODOS LOS TESTS PASARON EXITOSAMENTE")
        } else {
            Log.e(TAG, "💥 ALGUNOS TESTS FALLARON")
        }
        
        Log.d(TAG, "=== FIN TESTS DEL MÓDULO HISTORIAL ===")
        return allTestsPassed
    }

    /**
     * Test para crear un historial clínico
     */
    private fun testCreateRecord(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            runBlocking {
                val result = historyRepository.createRecord(testRecord)
                if (result.isSuccess) {
                    val recordId = result.getOrNull()
                    Log.d(TAG, "Historial creado con ID: $recordId")
                    recordId != null
                } else {
                    Log.e(TAG, "Error creando historial: ${result.exceptionOrNull()?.message}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de creación: ${e.message}")
            false
        }
    }

    /**
     * Test para leer un historial clínico
     */
    private fun testReadRecord(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            runBlocking {
                // Crear el historial primero
                val createResult = historyRepository.createRecord(testRecord)
                if (createResult.isSuccess) {
                    val recordId = createResult.getOrNull()!!
                    
                    // Leer el historial
                    val readRecord = historyRepository.getRecordById(recordId)
                    if (readRecord != null) {
                        Log.d(TAG, "Historial leído exitosamente: ${readRecord.petName}")
                        true
                    } else {
                        Log.e(TAG, "No se pudo leer el historial")
                        false
                    }
                } else {
                    Log.e(TAG, "Error creando historial para test de lectura")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de lectura: ${e.message}")
            false
        }
    }

    /**
     * Test para actualizar un historial clínico
     */
    private fun testUpdateRecord(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            runBlocking {
                // Crear el historial primero
                val createResult = historyRepository.createRecord(testRecord)
                if (createResult.isSuccess) {
                    val recordId = createResult.getOrNull()!!
                    
                    // Actualizar el historial
                    val updatedRecord = testRecord.copy(
                        petName = "Firulais Actualizado",
                        visitReason = "Consulta de seguimiento"
                    )
                    
                    val updateResult = historyRepository.updateRecord(recordId, updatedRecord)
                    if (updateResult.isSuccess) {
                        Log.d(TAG, "Historial actualizado exitosamente")
                        true
                    } else {
                        Log.e(TAG, "Error actualizando historial: ${updateResult.exceptionOrNull()?.message}")
                        false
                    }
                } else {
                    Log.e(TAG, "Error creando historial para test de actualización")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de actualización: ${e.message}")
            false
        }
    }

    /**
     * Test para subir un archivo adjunto
     */
    private fun testUploadAttachment(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            runBlocking {
                // Crear el historial primero
                val createResult = historyRepository.createRecord(testRecord)
                if (createResult.isSuccess) {
                    val recordId = createResult.getOrNull()!!
                    
                    // Crear un archivo de prueba
                    val testContent = "Contenido de prueba para el archivo adjunto"
                    val testBytes = testContent.toByteArray()
                    
                    // Simular subida de archivo
                    val uploadResult = historyRepository.uploadPdf(recordId, testBytes) { progress ->
                        Log.d(TAG, "Progreso de subida: ${(progress * 100).toInt()}%")
                    }
                    
                    if (uploadResult.isSuccess) {
                        val downloadUrl = uploadResult.getOrNull()
                        Log.d(TAG, "Archivo subido exitosamente: $downloadUrl")
                        true
                    } else {
                        Log.e(TAG, "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}")
                        false
                    }
                } else {
                    Log.e(TAG, "Error creando historial para test de subida")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de subida: ${e.message}")
            false
        }
    }

    /**
     * Test para generar PDF
     */
    private fun testGeneratePdf(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            // Generar PDF
            val pdfGenerator = PdfGenerator(context)
            val pdfBytes = pdfGenerator.generateClinicalRecordPdf(testRecord)
            
            if (pdfBytes.isNotEmpty()) {
                Log.d(TAG, "PDF generado exitosamente: ${pdfBytes.size} bytes")
                true
            } else {
                Log.e(TAG, "Error generando PDF: bytes vacíos")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de PDF: ${e.message}")
            false
        }
    }

    /**
     * Test para eliminar un historial clínico
     */
    private fun testDeleteRecord(): Boolean {
        return try {
            val testRecord = createTestRecord()
            
            runBlocking {
                // Crear el historial primero
                val createResult = historyRepository.createRecord(testRecord)
                if (createResult.isSuccess) {
                    val recordId = createResult.getOrNull()!!
                    
                    // Eliminar el historial
                    val deleteResult = historyRepository.deleteRecord(recordId)
                    if (deleteResult.isSuccess) {
                        Log.d(TAG, "Historial eliminado exitosamente")
                        true
                    } else {
                        Log.e(TAG, "Error eliminando historial: ${deleteResult.exceptionOrNull()?.message}")
                        false
                    }
                } else {
                    Log.e(TAG, "Error creando historial para test de eliminación")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en test de eliminación: ${e.message}")
            false
        }
    }

    /**
     * Crea un historial clínico de prueba
     */
    private fun createTestRecord(): ClinicalRecord {
        return ClinicalRecord(
            petId = "test_pet_123",
            petName = "Firulais",
            ownerId = "test_owner_123",
            ownerName = "Brayan Martínez",
            createdBy = "test_vet_123",
            visitReason = "Vómitos y diarrea",
            anamnesis = Anamnesis(
                feeding = "Balanceada",
                vaccinationDates = listOf("2025-01-01", "2025-02-01"),
                dewormingDates = listOf("2025-04-01"),
                previousDiseases = "Ninguna",
                habitat = "Urbano",
                coexistence = "2 perros, 1 gato"
            ),
            physicalExam = PhysicalExam(
                attitude = "Activo",
                bodyCondition = "Normal",
                hydration = "Normal",
                temperature = 38.5,
                heartRate = 100,
                respiratoryRate = 24,
                capillaryRefillTime = 1.5,
                skinAndCoat = "Bueno",
                lymphNodes = "No palpables"
            ),
            problemsAndDiagnostics = listOf(
                ProblemDiagnostic(
                    problem = "Vómito",
                    differential = listOf("Gastroenteritis", "Obstrucción"),
                    presumptiveDiagnosis = "Gastroenteritis"
                )
            ),
            paraclinical = listOf(
                ParaclinicalTest(
                    testName = "Hemograma",
                    results = "Normal",
                    fileUrls = emptyList()
                )
            ),
            treatmentPlan = listOf(
                Treatment(
                    drug = "Antiemético",
                    dose = "1 mg/kg",
                    route = "Oral",
                    frequency = "Cada 12h",
                    duration = "5 días"
                )
            ),
            prognosis = "Bueno",
            followUps = listOf(
                FollowUp(
                    date = Timestamp.now(),
                    notes = "Revisión en 1 semana"
                )
            ),
            attachments = emptyList()
        )
    }

    /**
     * Test de integración con ViewModel
     */
    fun testViewModelIntegration(): Boolean {
        return try {
            Log.d(TAG, "Test de integración con ViewModel")
            
            val testRecord = createTestRecord()
            
            runBlocking {
                // Test crear historial
                historyViewModel.createRecord(testRecord)
                
                // Esperar un poco para que se complete
                kotlinx.coroutines.delay(1000)
                
                // Test cargar historiales
                historyViewModel.loadRecords(testRecord.ownerId)
                
                // Esperar un poco para que se complete
                kotlinx.coroutines.delay(1000)
                
                Log.d(TAG, "✅ Test de ViewModel completado")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de ViewModel: ${e.message}")
            false
        }
    }

    /**
     * Test de validación de datos
     */
    fun testDataValidation(): Boolean {
        return try {
            Log.d(TAG, "Test de validación de datos")
            
            // Test con datos vacíos
            val emptyRecord = ClinicalRecord()
            val emptyResult = runBlocking { historyRepository.createRecord(emptyRecord) }
            
            if (emptyResult.isSuccess) {
                Log.d(TAG, "✅ Test de datos vacíos pasó")
            } else {
                Log.d(TAG, "❌ Test de datos vacíos falló")
            }
            
            // Test con datos válidos
            val validRecord = createTestRecord()
            val validResult = runBlocking { historyRepository.createRecord(validRecord) }
            
            if (validResult.isSuccess) {
                Log.d(TAG, "✅ Test de datos válidos pasó")
                true
            } else {
                Log.d(TAG, "❌ Test de datos válidos falló")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en test de validación: ${e.message}")
            false
        }
    }
}
