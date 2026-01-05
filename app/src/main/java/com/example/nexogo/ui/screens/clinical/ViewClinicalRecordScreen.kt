package com.example.nexogo.ui.screens.clinical

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.ClinicalRecord
import com.example.nexogo.model.ClinicalAttachment
import com.example.nexogo.model.ClinicalAttachmentType
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.viewmodel.ClinicalRecordViewModel
import com.example.nexogo.viewmodel.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewClinicalRecordScreen(
    recordId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    clinicalRecordViewModel: ClinicalRecordViewModel = ClinicalRecordViewModel.getInstance(LocalContext.current),
    patientViewModel: PatientViewModel = PatientViewModel.getInstance(LocalContext.current)
) {
    val clinicalRecords by clinicalRecordViewModel.clinicalRecords.collectAsStateWithLifecycle()
    val patients by patientViewModel.patients.collectAsStateWithLifecycle()
    
    val record = clinicalRecords.find { it.id == recordId }
    val patient = record?.let { patients.find { p -> p.id == it.patientId } }
    val pet = patient?.pets?.find { it.id == record?.petId }
    
    if (record == null) {
        // Mostrar error si no se encuentra el registro
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Registro no encontrado") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Registro clínico no encontrado")
            }
        }
        return
    }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Clínico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(recordId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = "Historial Clínico",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historial Clínico",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Mascota: ${pet?.name ?: "No encontrada"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dueño: ${patient?.name ?: "No encontrado"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Fecha: ${dateFormat.format(record.createdAt.toDate())}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Veterinario: Dr. ${record.createdByName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 1. Datos del propietario
            if (record.ownerData.name.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "👤 Datos del Propietario",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("Nombre", record.ownerData.name)
                        InfoRow("Dirección", record.ownerData.address)
                        InfoRow("Teléfono", record.ownerData.phone)
                        InfoRow("Email", record.ownerData.email)
                        if (record.ownerData.documentId.isNotBlank()) {
                            InfoRow("Documento", record.ownerData.documentId)
                        }
                    }
                }
            }
            
            // 2. Datos del paciente (mascota)
            if (record.petData.name.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🐾 Datos del Paciente",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("Nombre", record.petData.name)
                        InfoRow("Especie", record.petData.species)
                        InfoRow("Raza", record.petData.breed)
                        InfoRow("Color/Señas", record.petData.color)
                        InfoRow("Sexo", record.petData.gender)
                        if (record.petData.age > 0) {
                            InfoRow("Edad", "${record.petData.age} años")
                        }
                        if (record.petData.weight > 0) {
                            InfoRow("Peso", "${record.petData.weight} kg")
                        }
                        InfoRow("Estado Reproductivo", record.petData.reproductiveStatus.name)
                        InfoRow("Procedencia", record.petData.origin.name)
                        if (record.petData.zootechnicalPurpose.isNotBlank()) {
                            InfoRow("Fin Zootécnico", record.petData.zootechnicalPurpose)
                        }
                    }
                }
            }
            
            // 3. Motivo de consulta
            if (record.consultationReason.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🏥 Motivo de Consulta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = record.consultationReason,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // 4. Anamnesis
            if (record.anamnesis.feeding.type.isNotBlank() || 
                record.anamnesis.feeding.appetite.isNotBlank() ||
                record.anamnesis.habitat.isNotBlank() ||
                record.anamnesis.cohabitation.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📝 Anamnesis",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (record.anamnesis.feeding.type.isNotBlank()) {
                            InfoRow("Alimentación", record.anamnesis.feeding.type)
                        }
                        if (record.anamnesis.feeding.appetite.isNotBlank()) {
                            InfoRow("Apetito", record.anamnesis.feeding.appetite)
                        }
                        if (record.anamnesis.habitat.isNotBlank()) {
                            InfoRow("Hábitat", record.anamnesis.habitat)
                        }
                        if (record.anamnesis.cohabitation.isNotBlank()) {
                            InfoRow("Convivencia", record.anamnesis.cohabitation)
                        }
                        
                        // Vacunas
                        if (record.anamnesis.vaccinations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Vacunas:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            record.anamnesis.vaccinations.forEach { vaccination ->
                                Text(
                                    text = "• ${vaccination.name} - ${dateFormat.format(vaccination.date ?: Date())}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // 5. Examen físico
            if (record.physicalExam.temperature > 0 || 
                record.physicalExam.heartRate > 0 ||
                record.physicalExam.respiratoryRate > 0 ||
                record.physicalExam.generalAppearance.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🩺 Examen Físico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (record.physicalExam.temperature > 0) {
                            InfoRow("Temperatura", "${record.physicalExam.temperature}°C")
                        }
                        if (record.physicalExam.heartRate > 0) {
                            InfoRow("Frecuencia Cardíaca", "${record.physicalExam.heartRate} lpm")
                        }
                        if (record.physicalExam.respiratoryRate > 0) {
                            InfoRow("Frecuencia Respiratoria", "${record.physicalExam.respiratoryRate} rpm")
                        }
                        if (record.physicalExam.generalAppearance.isNotBlank()) {
                            InfoRow("Aspecto General", record.physicalExam.generalAppearance)
                        }
                        if (record.physicalExam.skinCondition.isNotBlank()) {
                            InfoRow("Piel/Faneras", record.physicalExam.skinCondition)
                        }
                        if (record.physicalExam.behavior.isNotBlank()) {
                            InfoRow("Comportamiento", record.physicalExam.behavior)
                        }
                    }
                }
            }
            
            // 6. Diagnósticos
            if (record.diagnoses.presumptiveDiagnosis.isNotBlank() || 
                record.diagnoses.definitiveDiagnosis.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🔍 Diagnósticos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (record.diagnoses.presumptiveDiagnosis.isNotBlank()) {
                            InfoRow("Diagnóstico Presuntivo", record.diagnoses.presumptiveDiagnosis)
                        }
                        if (record.diagnoses.definitiveDiagnosis.isNotBlank()) {
                            InfoRow("Diagnóstico Definitivo", record.diagnoses.definitiveDiagnosis)
                        }
                        
                        // Diagnósticos diferenciales
                        if (record.diagnoses.differentialDiagnoses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Diagnósticos Diferenciales:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            record.diagnoses.differentialDiagnoses.forEach { diagnosis ->
                                Text(
                                    text = "• $diagnosis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // 7. Plan terapéutico
            if (record.therapeuticPlan.treatments.isNotEmpty() || 
                record.therapeuticPlan.recommendations.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💊 Plan Terapéutico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tratamientos
                        if (record.therapeuticPlan.treatments.isNotEmpty()) {
                            Text(
                                text = "Tratamientos:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            record.therapeuticPlan.treatments.forEach { treatment ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = treatment.activeIngredient,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (treatment.commercialName.isNotBlank()) {
                                            Text(
                                                text = "(${treatment.commercialName})",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                        Text(
                                            text = "Dosis: ${treatment.dose}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Vía: ${treatment.route}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Frecuencia: ${treatment.frequency}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Duración: ${treatment.duration}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (treatment.instructions.isNotBlank()) {
                                            Text(
                                                text = "Instrucciones: ${treatment.instructions}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (record.therapeuticPlan.recommendations.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            InfoRow("Recomendaciones", record.therapeuticPlan.recommendations)
                        }
                    }
                }
            }
            
            // 8. Pronóstico
            if (record.prognosis.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🔮 Pronóstico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = record.prognosis,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // 9. Evolución / Seguimiento
            if (record.followUp.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📈 Evolución / Seguimiento",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        record.followUp.forEach { entry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = dateFormat.format(entry.date ?: Date()),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.observations.isNotBlank()) {
                                        Text(
                                            text = "Observaciones: ${entry.observations}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    if (entry.treatmentAdjustments.isNotBlank()) {
                                        Text(
                                            text = "Ajustes: ${entry.treatmentAdjustments}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = "Por: ${entry.createdByName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Archivos adjuntos
            if (record.attachments.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📎 Archivos Adjuntos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Mostrar archivos en una fila horizontal
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(record.attachments) { attachment ->
                                AttachmentPreviewCard(attachment = attachment)
                            }
                        }
                        
                        // Lista detallada de archivos
                        Spacer(modifier = Modifier.height(12.dp))
                        record.attachments.forEach { attachment ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📄 ${attachment.fileName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = attachment.fileType.name.lowercase(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    if (attachment.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = attachment.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    if (attachment.uploadedAt != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Subido: ${dateFormat.format(attachment.uploadedAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewCard(
    attachment: ClinicalAttachment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(100.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            when (attachment.fileType) {
                ClinicalAttachmentType.IMAGE -> {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Imagen",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                ClinicalAttachmentType.DOCUMENT -> {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = "Documento",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                ClinicalAttachmentType.VIDEO -> {
                    Icon(
                        Icons.Default.VideoFile,
                        contentDescription = "Video",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                ClinicalAttachmentType.AUDIO -> {
                    Icon(
                        Icons.Default.AudioFile,
                        contentDescription = "Audio",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    if (value.isNotBlank()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(120.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
