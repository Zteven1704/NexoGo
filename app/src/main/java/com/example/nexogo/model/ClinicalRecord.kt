package com.example.nexogo.model

import com.google.firebase.Timestamp
import java.util.Date

data class ClinicalRecord(
    val id: String = "",
    val patientId: String = "", // ID del paciente (dueño)
    val petId: String = "", // ID de la mascota
    
    // 1. Datos del propietario
    val ownerData: OwnerData = OwnerData(),
    
    // 2. Datos del paciente (mascota)
    val petData: PetData = PetData(),
    
    // 3. Motivo de la consulta
    val consultationReason: String = "",
    
    // 4. Anamnesis
    val anamnesis: Anamnesis = Anamnesis(),
    
    // 5. Examen físico y signos vitales
    val physicalExam: ClinicalPhysicalExam = ClinicalPhysicalExam(),
    
    // 6. Diagnósticos
    val diagnoses: Diagnoses = Diagnoses(),
    
    // 7. Plan terapéutico
    val therapeuticPlan: TherapeuticPlan = TherapeuticPlan(),
    
    // 8. Pronóstico
    val prognosis: String = "",
    
    // 9. Evolución / seguimiento
    val followUp: List<FollowUpEntry> = emptyList(),
    
    // Archivos adjuntos
    val attachments: List<ClinicalAttachment> = emptyList(),
    
    // Auditoría
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastModifiedBy: String = "",
    val lastModifiedByName: String = "",
    val lastModifiedAt: Timestamp = Timestamp.now(),
    
    // Estado del registro
    val status: ClinicalRecordStatus = ClinicalRecordStatus.ACTIVE,
    val isCompleted: Boolean = false
)

// 1. Datos del propietario
data class OwnerData(
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val documentId: String = "" // Documento de identidad
)

// 2. Datos del paciente (mascota)
data class PetData(
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val color: String = "", // Color / señas particulares
    val gender: String = "", // Macho, Hembra
    val birthDate: Date? = null, // Fecha de nacimiento
    val age: Int = 0, // Edad en años
    val weight: Double = 0.0, // Peso corporal
    val reproductiveStatus: ReproductiveStatus = ReproductiveStatus.UNKNOWN,
    val origin: Origin = Origin.URBAN,
    val zootechnicalPurpose: String = "" // Fin zootécnico (si aplica)
)

enum class ReproductiveStatus {
    UNKNOWN, // Desconocido
    INTACT, // Entero
    STERILIZED, // Esterilizado
    PREGNANT, // Gestación
    LACTATING // Lactancia
}

enum class Origin {
    URBAN, // Urbana
    RURAL // Rural
}

// 4. Anamnesis
data class Anamnesis(
    val feeding: FeedingInfo = FeedingInfo(),
    val vaccinations: List<ClinicalVaccination> = emptyList(),
    val deworming: DewormingInfo = DewormingInfo(),
    val previousDiseases: List<String> = emptyList(),
    val habitat: String = "",
    val cohabitation: String = "" // Convivencia con otros animales/personas
)

data class FeedingInfo(
    val type: String = "", // Tipo de alimentación
    val appetite: String = "" // Apetito
)

data class ClinicalVaccination(
    val id: String = "",
    val name: String = "",
    val date: Date? = null,
    val nextDue: Date? = null,
    val notes: String = ""
)

data class DewormingInfo(
    val internal: List<DewormingEntry> = emptyList(),
    val external: List<DewormingEntry> = emptyList()
)

data class DewormingEntry(
    val id: String = "",
    val product: String = "",
    val date: Date? = null,
    val nextDue: Date? = null,
    val notes: String = ""
)

// 5. Examen físico y signos vitales
data class ClinicalPhysicalExam(
    val temperature: Double = 0.0, // Temperatura
    val heartRate: Int = 0, // Frecuencia cardíaca
    val respiratoryRate: Int = 0, // Frecuencia respiratoria
    val bodyCondition: BodyCondition = BodyCondition.NORMAL,
    val hydrationStatus: HydrationStatus = HydrationStatus.NORMAL,
    val skinCondition: String = "", // Piel / faneras
    val palpableLymphNodes: String = "", // Ganglios palpables
    val behavior: String = "", // Comportamiento
    val generalAppearance: String = "", // Aspecto general
    val otherObservations: String = "" // Otras observaciones
)

enum class BodyCondition {
    VERY_THIN, // Muy delgado
    THIN, // Delgado
    NORMAL, // Normal
    OVERWEIGHT, // Sobrepeso
    OBESE // Obeso
}

enum class HydrationStatus {
    DEHYDRATED, // Deshidratado
    NORMAL, // Normal
    OVERHYDRATED // Sobrehidratado
}

// 6. Diagnósticos
data class Diagnoses(
    val differentialDiagnoses: List<String> = emptyList(), // Diagnósticos diferenciales
    val presumptiveDiagnosis: String = "", // Diagnóstico presuntivo
    val paraclinicalTests: List<ParaclinicalTest> = emptyList(), // Pruebas paraclínicas
    val definitiveDiagnosis: String = "" // Diagnóstico definitivo
)

data class ParaclinicalTest(
    val id: String = "",
    val name: String = "",
    val requestedDate: Date? = null,
    val results: String = "",
    val resultsDate: Date? = null,
    val notes: String = ""
)

// 7. Plan terapéutico
data class TherapeuticPlan(
    val treatments: List<ClinicalTreatment> = emptyList(),
    val recommendations: String = "", // Recomendaciones generales
    val followUpInstructions: String = "" // Instrucciones de seguimiento
)

data class ClinicalTreatment(
    val id: String = "",
    val activeIngredient: String = "", // Principio activo
    val commercialName: String = "", // Nombre comercial
    val dose: String = "", // Dosis
    val route: String = "", // Vía (oral, intramuscular, etc.)
    val frequency: String = "", // Frecuencia
    val duration: String = "", // Duración
    val instructions: String = "", // Instrucciones específicas
    val startDate: Date? = null,
    val endDate: Date? = null
)

// 9. Evolución / seguimiento
data class FollowUpEntry(
    val id: String = "",
    val date: Date? = null,
    val observations: String = "",
    val treatmentAdjustments: String = "",
    val nextAppointment: Date? = null,
    val createdBy: String = "",
    val createdByName: String = ""
)

// Archivos adjuntos
data class ClinicalAttachment(
    val id: String = "",
    val fileName: String = "",
    val fileType: ClinicalAttachmentType = ClinicalAttachmentType.IMAGE,
    val fileUrl: String = "",
    val description: String = "",
    val uploadedBy: String = "",
    val uploadedAt: Date? = null
)

enum class ClinicalAttachmentType {
    IMAGE, // Imagen (radiografías, fotos)
    DOCUMENT, // Documento (resultados de laboratorio)
    VIDEO, // Video
    AUDIO // Audio
}

enum class ClinicalRecordStatus {
    ACTIVE, // Activo
    ARCHIVED, // Archivado
    DELETED // Eliminado
}
