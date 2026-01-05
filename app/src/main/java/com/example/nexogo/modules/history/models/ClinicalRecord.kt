package com.example.nexogo.modules.history.models

import com.google.firebase.Timestamp
import java.util.Date

/**
 * Modelo principal para el historial clínico de una mascota
 */
data class ClinicalRecord(
    val recordId: String = "",
    val petId: String = "",
    val petName: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val visitReason: String = "",
    val anamnesis: Anamnesis = Anamnesis(),
    val physicalExam: PhysicalExam = PhysicalExam(),
    val problemsAndDiagnostics: List<ProblemDiagnostic> = emptyList(),
    val paraclinical: List<ParaclinicalTest> = emptyList(),
    val treatmentPlan: List<Treatment> = emptyList(),
    val prognosis: String = "",
    val followUps: List<FollowUp> = emptyList(),
    val attachments: List<Attachment> = emptyList()
)

/**
 * Información de anamnesis del paciente
 */
data class Anamnesis(
    val feeding: String = "",
    val vaccinationDates: List<String> = emptyList(),
    val dewormingDates: List<String> = emptyList(),
    val previousDiseases: String = "",
    val habitat: String = "",
    val coexistence: String = ""
)

/**
 * Examen físico del paciente
 */
data class PhysicalExam(
    val attitude: String = "",
    val bodyCondition: String = "",
    val hydration: String = "",
    val temperature: Double = 0.0,
    val heartRate: Int = 0,
    val respiratoryRate: Int = 0,
    val capillaryRefillTime: Double = 0.0,
    val skinAndCoat: String = "",
    val lymphNodes: String = ""
)

/**
 * Problema y diagnóstico
 */
data class ProblemDiagnostic(
    val problem: String = "",
    val differential: List<String> = emptyList(),
    val presumptiveDiagnosis: String = ""
)

/**
 * Examen paraclínico
 */
data class ParaclinicalTest(
    val testName: String = "",
    val results: String = "",
    val fileUrls: List<String> = emptyList()
)

/**
 * Plan de tratamiento
 */
data class Treatment(
    val drug: String = "",
    val dose: String = "",
    val route: String = "",
    val frequency: String = "",
    val duration: String = ""
)

/**
 * Seguimiento del paciente
 */
data class FollowUp(
    val date: Timestamp = Timestamp.now(),
    val notes: String = ""
)

/**
 * Archivo adjunto
 */
data class Attachment(
    val name: String = "",
    val url: String = "",
    val type: AttachmentType = AttachmentType.IMAGE,
    val size: Long = 0L,
    val uploadedAt: Timestamp = Timestamp.now()
)

/**
 * Tipos de archivos adjuntos
 */
enum class AttachmentType {
    IMAGE, PDF, DOCUMENT, VIDEO, AUDIO
}

/**
 * Estado de la UI para el historial clínico
 */
data class HistoryUiState(
    val records: List<ClinicalRecord> = emptyList(),
    val currentRecord: ClinicalRecord? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val pdfUrl: String? = null
)
