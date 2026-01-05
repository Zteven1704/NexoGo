package com.example.nexogo.model

import com.google.firebase.Timestamp

data class MedicalRecord(
    val id: String = "",
    val patientId: String = "", // ID de la mascota
    val patientName: String = "", // Nombre del paciente
    val ownerName: String = "", // Nombre del dueño
    val ownerId: String = "", // ID del dueño
    val veterinarianId: String = "", // ID del veterinario
    val veterinarian: String = "", // Nombre del veterinario
    val appointmentId: String? = null, // ID de la cita asociada
    val consultationDate: Timestamp = Timestamp.now(),
    val consultationReason: String = "", // Motivo de consulta
    val reason: String = "", // Motivo de consulta
    val anamnesis: String = "", // Anamnesis
    val physicalExam: PhysicalExam = PhysicalExam(),
    val physicalExamText: String = "", // Examen físico como texto
    val vitalSigns: VitalSigns = VitalSigns(),
    val diagnoses: List<Diagnosis> = emptyList(),
    val diagnosis: String = "", // Diagnóstico como texto
    val treatments: List<Treatment> = emptyList(),
    val treatment: String = "", // Tratamiento como texto
    val prognosis: String = "",
    val evolution: String = "", // Evolución
    val followUp: String = "", // Seguimiento
    val followUpDate: Timestamp? = null, // Fecha de seguimiento
    val attachments: List<Attachment> = emptyList(), // Archivos adjuntos
    val isEmergency: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val lastModifiedBy: String = ""
)

data class PhysicalExam(
    val generalAppearance: String = "",
    val weight: Double = 0.0,
    val temperature: Double = 0.0,
    val heartRate: Int = 0,
    val respiratoryRate: Int = 0,
    val mucousMembranes: String = "",
    val lymphNodes: String = "",
    val cardiovascular: String = "",
    val respiratory: String = "",
    val digestive: String = "",
    val urinary: String = "",
    val reproductive: String = "",
    val nervous: String = "",
    val musculoskeletal: String = "",
    val integumentary: String = "",
    val other: String = ""
)

data class VitalSigns(
    val temperature: Double = 0.0,
    val heartRate: Int = 0,
    val respiratoryRate: Int = 0,
    val bloodPressure: String = "",
    val weight: Double = 0.0,
    val bodyConditionScore: Int = 0, // 1-9 scale
    val hydrationStatus: String = "",
    val capillaryRefillTime: String = ""
)

data class Diagnosis(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val severity: DiagnosisSeverity = DiagnosisSeverity.MILD,
    val isPrimary: Boolean = false,
    val icd10Code: String = "", // Código ICD-10
    val notes: String = ""
)

enum class DiagnosisSeverity {
    MILD,       // Leve
    MODERATE,   // Moderado
    SEVERE,     // Severo
    CRITICAL    // Crítico
}

data class Treatment(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = "",
    val isPrescription: Boolean = false,
    val prescribedBy: String = "", // ID del veterinario
    val prescribedAt: Timestamp = Timestamp.now(),
    val isCompleted: Boolean = false,
    val completedAt: Timestamp? = null
)

data class Attachment(
    val id: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: AttachmentType = AttachmentType.IMAGE,
    val fileSize: Long = 0,
    val uploadedAt: Timestamp = Timestamp.now(),
    val uploadedBy: String = "",
    val description: String = ""
)

enum class AttachmentType {
    IMAGE,      // Imagen
    PDF,        // PDF
    VIDEO,      // Video
    AUDIO,      // Audio
    XRAY,       // Radiografía
    LAB_RESULT, // Resultado de laboratorio
    OTHER       // Otro
}