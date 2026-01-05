package com.example.nexogo.modules.history.repo

import android.net.Uri
import android.util.Log
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.history.models.ClinicalRecord
import com.example.nexogo.modules.history.models.Attachment
import com.example.nexogo.modules.history.models.AttachmentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Repositorio para operaciones del historial clínico
 * Utiliza el FirebaseRepository central para todas las operaciones
 */
class HistoryRepository(
    private val firebaseRepository: FirebaseRepository
) {
    companion object {
        private const val TAG = "NEXOGO_HISTORY"
        private const val COLLECTION_NAME = "clinical_records"
        private const val STORAGE_PATH = "medical_records"
    }

    /**
     * Obtiene todos los historiales clínicos de un propietario
     */
    suspend fun getRecordsByOwner(ownerId: String): Flow<List<ClinicalRecord>> = flow {
        try {
            Log.d(TAG, "Obteniendo historiales para propietario: $ownerId")
            val result = firebaseRepository.getCollection(COLLECTION_NAME)
            
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val records = dataList.mapNotNull { data ->
                    try {
                        // Filtrar por ownerId
                        if (data["ownerId"] == ownerId) {
                            ClinicalRecord(
                                recordId = data["recordId"] as? String ?: "",
                                petId = data["petId"] as? String ?: "",
                                petName = data["petName"] as? String ?: "",
                                ownerId = data["ownerId"] as? String ?: "",
                                ownerName = data["ownerName"] as? String ?: "",
                                createdBy = data["createdBy"] as? String ?: "",
                                createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                                updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                                visitReason = data["visitReason"] as? String ?: "",
                                anamnesis = parseAnamnesis(data["anamnesis"] as? Map<String, Any>),
                                physicalExam = parsePhysicalExam(data["physicalExam"] as? Map<String, Any>),
                                problemsAndDiagnostics = parseProblemsAndDiagnostics(data["problemsAndDiagnostics"] as? List<Map<String, Any>>),
                                paraclinical = parseParaclinical(data["paraclinical"] as? List<Map<String, Any>>),
                                treatmentPlan = parseTreatmentPlan(data["treatmentPlan"] as? List<Map<String, Any>>),
                                prognosis = data["prognosis"] as? String ?: "",
                                followUps = parseFollowUps(data["followUps"] as? List<Map<String, Any>>),
                                attachments = parseAttachments(data["attachments"] as? List<Map<String, Any>>)
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando historial: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "Historiales obtenidos: ${records.size}")
                emit(records)
            } else {
                Log.e(TAG, "Error obteniendo historiales: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo historiales: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Obtiene un historial clínico específico
     */
    suspend fun getRecordById(recordId: String): ClinicalRecord? {
        return try {
            Log.d(TAG, "Obteniendo historial: $recordId")
            val result = firebaseRepository.getDocument(COLLECTION_NAME, recordId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    ClinicalRecord(
                        recordId = it["recordId"] as? String ?: recordId,
                        petId = it["petId"] as? String ?: "",
                        petName = it["petName"] as? String ?: "",
                        ownerId = it["ownerId"] as? String ?: "",
                        ownerName = it["ownerName"] as? String ?: "",
                        createdBy = it["createdBy"] as? String ?: "",
                        createdAt = it["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                        updatedAt = it["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                        visitReason = it["visitReason"] as? String ?: "",
                        anamnesis = parseAnamnesis(it["anamnesis"] as? Map<String, Any>),
                        physicalExam = parsePhysicalExam(it["physicalExam"] as? Map<String, Any>),
                        problemsAndDiagnostics = parseProblemsAndDiagnostics(it["problemsAndDiagnostics"] as? List<Map<String, Any>>),
                        paraclinical = parseParaclinical(it["paraclinical"] as? List<Map<String, Any>>),
                        treatmentPlan = parseTreatmentPlan(it["treatmentPlan"] as? List<Map<String, Any>>),
                        prognosis = it["prognosis"] as? String ?: "",
                        followUps = parseFollowUps(it["followUps"] as? List<Map<String, Any>>),
                        attachments = parseAttachments(it["attachments"] as? List<Map<String, Any>>)
                    )
                }
            } else {
                Log.e(TAG, "Error obteniendo historial: ${result.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo historial: ${e.message}")
            null
        }
    }

    /**
     * Crea un nuevo historial clínico
     */
    suspend fun createRecord(record: ClinicalRecord): Result<String> {
        return try {
            Log.d(TAG, "Creando historial para mascota: ${record.petName}")
            val recordId = UUID.randomUUID().toString()
            val recordData = mapOf(
                "recordId" to recordId,
                "petId" to record.petId,
                "petName" to record.petName,
                "ownerId" to record.ownerId,
                "ownerName" to record.ownerName,
                "createdBy" to record.createdBy,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "visitReason" to record.visitReason,
                "anamnesis" to mapAnamnesis(record.anamnesis),
                "physicalExam" to mapPhysicalExam(record.physicalExam),
                "problemsAndDiagnostics" to mapProblemsAndDiagnostics(record.problemsAndDiagnostics),
                "paraclinical" to mapParaclinical(record.paraclinical),
                "treatmentPlan" to mapTreatmentPlan(record.treatmentPlan),
                "prognosis" to record.prognosis,
                "followUps" to mapFollowUps(record.followUps),
                "attachments" to mapAttachments(record.attachments)
            )
            
            val result = firebaseRepository.createDocument(COLLECTION_NAME, recordId, recordData)
            if (result.isSuccess) {
                Log.d(TAG, "Historial creado exitosamente: $recordId")
                Result.success(recordId)
            } else {
                Log.e(TAG, "Error creando historial: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando historial: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Actualiza un historial clínico existente
     */
    suspend fun updateRecord(recordId: String, record: ClinicalRecord): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando historial: $recordId")
            val updateData = mapOf(
                "petName" to record.petName,
                "ownerId" to record.ownerId,
                "ownerName" to record.ownerName,
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "visitReason" to record.visitReason,
                "anamnesis" to mapAnamnesis(record.anamnesis),
                "physicalExam" to mapPhysicalExam(record.physicalExam),
                "problemsAndDiagnostics" to mapProblemsAndDiagnostics(record.problemsAndDiagnostics),
                "paraclinical" to mapParaclinical(record.paraclinical),
                "treatmentPlan" to mapTreatmentPlan(record.treatmentPlan),
                "prognosis" to record.prognosis,
                "followUps" to mapFollowUps(record.followUps),
                "attachments" to mapAttachments(record.attachments)
            )
            
            val result = firebaseRepository.updateDocument(COLLECTION_NAME, recordId, updateData)
            if (result.isSuccess) {
                Log.d(TAG, "Historial actualizado exitosamente: $recordId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error actualizando historial: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando historial: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Elimina un historial clínico
     */
    suspend fun deleteRecord(recordId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando historial: $recordId")
            val result = firebaseRepository.deleteDocument(COLLECTION_NAME, recordId)
            if (result.isSuccess) {
                Log.d(TAG, "Historial eliminado exitosamente: $recordId")
                // TODO: Eliminar archivos adjuntos del Storage
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error eliminando historial: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando historial: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sube un archivo adjunto al Storage
     */
    suspend fun uploadAttachment(
        recordId: String,
        fileUri: Uri,
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            Log.d(TAG, "Subiendo archivo: $fileName para historial: $recordId")
            val storagePath = "$STORAGE_PATH/$recordId/files/$fileName"
            val result = firebaseRepository.uploadFile(storagePath, fileUri)
            if (result.isSuccess) {
                val downloadUrl = result.getOrNull() ?: ""
                Log.d(TAG, "Archivo subido exitosamente: $downloadUrl")
                Result.success(downloadUrl)
            } else {
                Log.e(TAG, "Error subiendo archivo: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error subiendo archivo: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sube un PDF generado al Storage
     */
    suspend fun uploadPdf(
        recordId: String,
        pdfBytes: ByteArray,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            Log.d(TAG, "Subiendo PDF para historial: $recordId")
            val storagePath = "$STORAGE_PATH/$recordId/pdfs/$recordId.pdf"
            val result = firebaseRepository.uploadBytes(storagePath, pdfBytes)
            if (result.isSuccess) {
                val downloadUrl = result.getOrNull() ?: ""
                Log.d(TAG, "PDF subido exitosamente: $downloadUrl")
                Result.success(downloadUrl)
            } else {
                Log.e(TAG, "Error subiendo PDF: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error subiendo PDF: ${e.message}")
            Result.failure(e)
        }
    }

    // Métodos auxiliares para parsear datos de Firestore
    private fun parseAnamnesis(data: Map<String, Any>?): com.example.nexogo.modules.history.models.Anamnesis {
        if (data == null) return com.example.nexogo.modules.history.models.Anamnesis()
        return com.example.nexogo.modules.history.models.Anamnesis(
            feeding = data["feeding"] as? String ?: "",
            vaccinationDates = (data["vaccinationDates"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            dewormingDates = (data["dewormingDates"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            previousDiseases = data["previousDiseases"] as? String ?: "",
            habitat = data["habitat"] as? String ?: "",
            coexistence = data["coexistence"] as? String ?: ""
        )
    }

    private fun parsePhysicalExam(data: Map<String, Any>?): com.example.nexogo.modules.history.models.PhysicalExam {
        if (data == null) return com.example.nexogo.modules.history.models.PhysicalExam()
        return com.example.nexogo.modules.history.models.PhysicalExam(
            attitude = data["attitude"] as? String ?: "",
            bodyCondition = data["bodyCondition"] as? String ?: "",
            hydration = data["hydration"] as? String ?: "",
            temperature = (data["temperature"] as? Number)?.toDouble() ?: 0.0,
            heartRate = (data["heartRate"] as? Number)?.toInt() ?: 0,
            respiratoryRate = (data["respiratoryRate"] as? Number)?.toInt() ?: 0,
            capillaryRefillTime = (data["capillaryRefillTime"] as? Number)?.toDouble() ?: 0.0,
            skinAndCoat = data["skinAndCoat"] as? String ?: "",
            lymphNodes = data["lymphNodes"] as? String ?: ""
        )
    }

    private fun parseProblemsAndDiagnostics(data: List<Map<String, Any>>?): List<com.example.nexogo.modules.history.models.ProblemDiagnostic> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                com.example.nexogo.modules.history.models.ProblemDiagnostic(
                    problem = item["problem"] as? String ?: "",
                    differential = (item["differential"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    presumptiveDiagnosis = item["presumptiveDiagnosis"] as? String ?: ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando problema diagnóstico: ${e.message}")
                null
            }
        }
    }

    private fun parseParaclinical(data: List<Map<String, Any>>?): List<com.example.nexogo.modules.history.models.ParaclinicalTest> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                com.example.nexogo.modules.history.models.ParaclinicalTest(
                    testName = item["testName"] as? String ?: "",
                    results = item["results"] as? String ?: "",
                    fileUrls = (item["fileUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando examen paraclínico: ${e.message}")
                null
            }
        }
    }

    private fun parseTreatmentPlan(data: List<Map<String, Any>>?): List<com.example.nexogo.modules.history.models.Treatment> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                com.example.nexogo.modules.history.models.Treatment(
                    drug = item["drug"] as? String ?: "",
                    dose = item["dose"] as? String ?: "",
                    route = item["route"] as? String ?: "",
                    frequency = item["frequency"] as? String ?: "",
                    duration = item["duration"] as? String ?: ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando tratamiento: ${e.message}")
                null
            }
        }
    }

    private fun parseFollowUps(data: List<Map<String, Any>>?): List<com.example.nexogo.modules.history.models.FollowUp> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                com.example.nexogo.modules.history.models.FollowUp(
                    date = item["date"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                    notes = item["notes"] as? String ?: ""
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando seguimiento: ${e.message}")
                null
            }
        }
    }

    private fun parseAttachments(data: List<Map<String, Any>>?): List<Attachment> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                Attachment(
                    name = item["name"] as? String ?: "",
                    url = item["url"] as? String ?: "",
                    type = try {
                        AttachmentType.valueOf(item["type"] as? String ?: "IMAGE")
                    } catch (e: Exception) {
                        AttachmentType.IMAGE
                    },
                    size = (item["size"] as? Number)?.toLong() ?: 0L,
                    uploadedAt = item["uploadedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando adjunto: ${e.message}")
                null
            }
        }
    }

    // Métodos auxiliares para mapear datos a Firestore
    private fun mapAnamnesis(anamnesis: com.example.nexogo.modules.history.models.Anamnesis): Map<String, Any> {
        return mapOf(
            "feeding" to anamnesis.feeding,
            "vaccinationDates" to anamnesis.vaccinationDates,
            "dewormingDates" to anamnesis.dewormingDates,
            "previousDiseases" to anamnesis.previousDiseases,
            "habitat" to anamnesis.habitat,
            "coexistence" to anamnesis.coexistence
        )
    }

    private fun mapPhysicalExam(physicalExam: com.example.nexogo.modules.history.models.PhysicalExam): Map<String, Any> {
        return mapOf(
            "attitude" to physicalExam.attitude,
            "bodyCondition" to physicalExam.bodyCondition,
            "hydration" to physicalExam.hydration,
            "temperature" to physicalExam.temperature,
            "heartRate" to physicalExam.heartRate,
            "respiratoryRate" to physicalExam.respiratoryRate,
            "capillaryRefillTime" to physicalExam.capillaryRefillTime,
            "skinAndCoat" to physicalExam.skinAndCoat,
            "lymphNodes" to physicalExam.lymphNodes
        )
    }

    private fun mapProblemsAndDiagnostics(problems: List<com.example.nexogo.modules.history.models.ProblemDiagnostic>): List<Map<String, Any>> {
        return problems.map { problem ->
            mapOf(
                "problem" to problem.problem,
                "differential" to problem.differential,
                "presumptiveDiagnosis" to problem.presumptiveDiagnosis
            )
        }
    }

    private fun mapParaclinical(paraclinical: List<com.example.nexogo.modules.history.models.ParaclinicalTest>): List<Map<String, Any>> {
        return paraclinical.map { test ->
            mapOf(
                "testName" to test.testName,
                "results" to test.results,
                "fileUrls" to test.fileUrls
            )
        }
    }

    private fun mapTreatmentPlan(treatments: List<com.example.nexogo.modules.history.models.Treatment>): List<Map<String, Any>> {
        return treatments.map { treatment ->
            mapOf(
                "drug" to treatment.drug,
                "dose" to treatment.dose,
                "route" to treatment.route,
                "frequency" to treatment.frequency,
                "duration" to treatment.duration
            )
        }
    }

    private fun mapFollowUps(followUps: List<com.example.nexogo.modules.history.models.FollowUp>): List<Map<String, Any>> {
        return followUps.map { followUp ->
            mapOf(
                "date" to followUp.date,
                "notes" to followUp.notes
            )
        }
    }

    private fun mapAttachments(attachments: List<Attachment>): List<Map<String, Any>> {
        return attachments.map { attachment ->
            mapOf(
                "name" to attachment.name,
                "url" to attachment.url,
                "type" to attachment.type.name,
                "size" to attachment.size,
                "uploadedAt" to attachment.uploadedAt
            )
        }
    }
}
