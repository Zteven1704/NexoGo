package com.example.nexogo.modules.history.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.history.models.ClinicalRecord
import com.example.nexogo.modules.history.models.Anamnesis
import com.example.nexogo.modules.history.models.PhysicalExam
import com.example.nexogo.modules.history.components.AttachmentUploader
import com.example.nexogo.modules.history.utils.SimplePdfGenerator
import com.example.nexogo.modules.history.utils.PdfDownloader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.compose.ui.platform.LocalContext

/**
 * Versión ultra-simplificada para crear/editar historial clínico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHistoryEditScreen(
    recordId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var petName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var visitReason by remember { mutableStateOf("") }
    var prognosis by remember { mutableStateOf("") }
    
    // Anamnesis
    var feeding by remember { mutableStateOf("") }
    var previousDiseases by remember { mutableStateOf("") }
    var habitat by remember { mutableStateOf("") }
    var coexistence by remember { mutableStateOf("") }
    
    // Examen Físico
    var attitude by remember { mutableStateOf("") }
    var bodyCondition by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var respiratoryRate by remember { mutableStateOf("") }
    
    // Tratamientos
    var treatments by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(recordId != null) }
    var attachments by remember { mutableStateOf<List<String>>(emptyList()) }
    var showPdfDialog by remember { mutableStateOf(false) }
    var showPdfOptions by remember { mutableStateOf(false) }
    var pdfBytes by remember { mutableStateOf<ByteArray?>(null) }
    
    val context = LocalContext.current
    val pdfDownloader = remember { PdfDownloader(context) }
    
    // Firebase
    val firebaseRepository = remember { FirebaseRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Cargar datos si es edición
    LaunchedEffect(recordId) {
        if (recordId != null) {
            try {
                val result = firebaseRepository.getDocument("clinical_records", recordId)
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    data?.let {
                        petName = it["petName"] as? String ?: ""
                        ownerName = it["ownerName"] as? String ?: ""
                        visitReason = it["visitReason"] as? String ?: ""
                        prognosis = it["prognosis"] as? String ?: ""
                        
                        // Cargar anamnesis
                        val anamnesis = it["anamnesis"] as? Map<String, Any>
                        feeding = anamnesis?.get("feeding") as? String ?: ""
                        previousDiseases = anamnesis?.get("previousDiseases") as? String ?: ""
                        habitat = anamnesis?.get("habitat") as? String ?: ""
                        coexistence = anamnesis?.get("coexistence") as? String ?: ""
                        
                        // Cargar examen físico
                        val physicalExam = it["physicalExam"] as? Map<String, Any>
                        attitude = physicalExam?.get("attitude") as? String ?: ""
                        bodyCondition = physicalExam?.get("bodyCondition") as? String ?: ""
                        temperature = physicalExam?.get("temperature")?.toString() ?: ""
                        heartRate = physicalExam?.get("heartRate")?.toString() ?: ""
                        respiratoryRate = physicalExam?.get("respiratoryRate")?.toString() ?: ""
                        
                        // Cargar tratamientos
                        val treatmentPlan = it["treatmentPlan"] as? List<Map<String, Any>>
                        treatments = treatmentPlan?.joinToString("\n") { treatment ->
                            "${treatment["drug"]} - ${treatment["dose"]} - ${treatment["frequency"]}"
                        } ?: ""
                    }
                } else {
                    errorMessage = "Error cargando historial: ${result.exceptionOrNull()?.message}"
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (recordId != null) "Editar Historial" else "Nuevo Historial",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando historial...")
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información básica
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información Básica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = petName,
                            onValueChange = { petName = it },
                            label = { Text("Nombre de la mascota *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Nombre del propietario *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = visitReason,
                            onValueChange = { visitReason = it },
                            label = { Text("Motivo de consulta *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = prognosis,
                            onValueChange = { prognosis = it },
                            label = { Text("Pronóstico") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
                
                // Anamnesis
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Anamnesis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = feeding,
                            onValueChange = { feeding = it },
                            label = { Text("Alimentación") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = previousDiseases,
                            onValueChange = { previousDiseases = it },
                            label = { Text("Enfermedades previas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = habitat,
                            onValueChange = { habitat = it },
                            label = { Text("Hábitat") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = coexistence,
                            onValueChange = { coexistence = it },
                            label = { Text("Convivencia") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Examen Físico
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Examen Físico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = attitude,
                                onValueChange = { attitude = it },
                                label = { Text("Actitud") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = bodyCondition,
                                onValueChange = { bodyCondition = it },
                                label = { Text("Condición corporal") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = temperature,
                                onValueChange = { temperature = it },
                                label = { Text("Temperatura (°C)") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = heartRate,
                                onValueChange = { heartRate = it },
                                label = { Text("Frecuencia cardíaca") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = respiratoryRate,
                            onValueChange = { respiratoryRate = it },
                            label = { Text("Frecuencia respiratoria") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Tratamientos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tratamientos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = treatments,
                            onValueChange = { treatments = it },
                            label = { Text("Plan de tratamiento") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            placeholder = { Text("Ej: Medicamento - Dosis - Frecuencia") }
                        )
                    }
                }
                
                // Adjuntos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        AttachmentUploader(
                            recordId = recordId ?: UUID.randomUUID().toString(),
                            onUploadComplete = { url ->
                                attachments = attachments + url
                            },
                            onUploadError = { error ->
                                errorMessage = error
                            }
                        )
                    }
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    OutlinedButton(
                        onClick = { showPdfDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generar PDF")
                    }
                    
                    Button(
                        onClick = {
                            if (petName.isNotBlank() && ownerName.isNotBlank() && visitReason.isNotBlank()) {
                                isSaving = true
                                errorMessage = ""
                                
                                // Guardar en Firebase
                                GlobalScope.launch {
                                    try {
                                        val recordData = mapOf(
                                            "petName" to petName,
                                            "ownerName" to ownerName,
                                            "visitReason" to visitReason,
                                            "prognosis" to prognosis,
                                            "anamnesis" to mapOf<String, Any>(
                                                "feeding" to feeding,
                                                "previousDiseases" to previousDiseases,
                                                "habitat" to habitat,
                                                "coexistence" to coexistence
                                            ),
                                            "physicalExam" to mapOf<String, Any>(
                                                "attitude" to attitude,
                                                "bodyCondition" to bodyCondition,
                                                "temperature" to (temperature.toDoubleOrNull() ?: 0.0),
                                                "heartRate" to (heartRate.toIntOrNull() ?: 0),
                                                "respiratoryRate" to (respiratoryRate.toIntOrNull() ?: 0)
                                            ),
                                            "treatmentPlan" to if (treatments.isNotEmpty()) {
                                                treatments.split("\n").map { treatment ->
                                                    val parts = treatment.split(" - ")
                                                    mapOf<String, Any>(
                                                        "drug" to (parts.getOrNull(0) ?: ""),
                                                        "dose" to (parts.getOrNull(1) ?: ""),
                                                        "frequency" to (parts.getOrNull(2) ?: ""),
                                                        "route" to "",
                                                        "duration" to ""
                                                    )
                                                }
                                            } else emptyList<Map<String, Any>>(),
                                            "updatedAt" to com.google.firebase.Timestamp.now()
                                        )
                                        
                                        val result = if (recordId != null) {
                                            // Actualizar historial existente
                                            firebaseRepository.updateDocument("clinical_records", recordId, recordData)
                                        } else {
                                            // Crear nuevo historial
                                            val newRecordId = UUID.randomUUID().toString()
                                            val fullRecordData = recordData + mapOf(
                                                "recordId" to newRecordId,
                                                "petId" to UUID.randomUUID().toString(),
                                                "ownerId" to (currentUser?.uid ?: "unknown"),
                                                "createdBy" to (currentUser?.uid ?: "unknown"),
                                                "createdByName" to (currentUser?.displayName ?: "Usuario"),
                                                "createdAt" to com.google.firebase.Timestamp.now(),
                                                "anamnesis" to mapOf<String, Any>(),
                                                "physicalExam" to mapOf<String, Any>(),
                                                "problemsAndDiagnostics" to emptyList<Map<String, Any>>(),
                                                "paraclinical" to emptyList<Map<String, Any>>(),
                                                "treatmentPlan" to emptyList<Map<String, Any>>(),
                                                "followUps" to emptyList<Map<String, Any>>(),
                                                "attachments" to emptyList<Map<String, Any>>()
                                            )
                                            firebaseRepository.createDocument("clinical_records", newRecordId, fullRecordData)
                                        }
                                        
                                        if (result.isSuccess) {
                                            isSaving = false
                                            showSuccess = true
                                        } else {
                                            isSaving = false
                                            errorMessage = "Error guardando: ${result.exceptionOrNull()?.message}"
                                        }
                                    } catch (e: Exception) {
                                        isSaving = false
                                        errorMessage = "Error: ${e.message}"
                                    }
                                }
                            }
                        },
                        enabled = !isSaving && petName.isNotBlank() && ownerName.isNotBlank() && visitReason.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Guardando...")
                            }
                        } else {
                            Text(if (recordId != null) "Actualizar" else "Crear")
                        }
                    }
                }
                
                // Mostrar error si existe
                if (errorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // Diálogo de éxito
        if (showSuccess) {
            AlertDialog(
                onDismissRequest = { showSuccess = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (recordId != null) "Historial Actualizado" else "Historial Creado",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(if (recordId != null) "El historial clínico ha sido actualizado exitosamente." else "El historial clínico ha sido creado exitosamente.")
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            showSuccess = false
                            onNavigateBack()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
        
        // Diálogo de generación de PDF
        if (showPdfDialog) {
            AlertDialog(
                onDismissRequest = { showPdfDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Generar PDF",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text("¿Deseas generar un PDF del historial clínico?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPdfDialog = false
                            // Generar PDF
                            GlobalScope.launch {
                                try {
                                    val clinicalRecord = ClinicalRecord(
                                        recordId = recordId ?: UUID.randomUUID().toString(),
                                        petName = petName,
                                        ownerName = ownerName,
                                        visitReason = visitReason,
                                        prognosis = prognosis,
                                        anamnesis = Anamnesis(
                                            feeding = feeding,
                                            previousDiseases = previousDiseases,
                                            habitat = habitat,
                                            coexistence = coexistence
                                        ),
                                        physicalExam = PhysicalExam(
                                            attitude = attitude,
                                            bodyCondition = bodyCondition,
                                            temperature = temperature.toDoubleOrNull() ?: 0.0,
                                            heartRate = heartRate.toIntOrNull() ?: 0,
                                            respiratoryRate = respiratoryRate.toIntOrNull() ?: 0
                                        ),
                                        treatmentPlan = if (treatments.isNotEmpty()) {
                                            treatments.split("\n").map { treatment ->
                                                val parts = treatment.split(" - ")
                                                com.example.nexogo.modules.history.models.Treatment(
                                                    drug = parts.getOrNull(0) ?: "",
                                                    dose = parts.getOrNull(1) ?: "",
                                                    frequency = parts.getOrNull(2) ?: ""
                                                )
                                            }
                                        } else emptyList(),
                                        attachments = attachments.map { url ->
                                            com.example.nexogo.modules.history.models.Attachment(
                                                name = "Adjunto",
                                                url = url,
                                                type = com.example.nexogo.modules.history.models.AttachmentType.DOCUMENT
                                            )
                                        }
                                    )
                                    
                                    val pdfGenerator = SimplePdfGenerator(context)
                                    val generatedPdfBytes = pdfGenerator.generateSimplePdf(clinicalRecord)
                                    pdfBytes = generatedPdfBytes
                                    
                                    // Mostrar opciones de PDF inmediatamente (sin subir a Firebase por ahora)
                                    showPdfOptions = true
                                    
                                    // Intentar subir PDF a Firebase Storage (opcional)
                                    try {
                                        val pdfPath = "medical_records/${clinicalRecord.recordId}/pdfs/historial_${clinicalRecord.petName}_${System.currentTimeMillis()}.pdf"
                                        val uploadResult = firebaseRepository.uploadBytes(pdfPath, generatedPdfBytes)
                                        
                                        if (uploadResult.isSuccess) {
                                            val pdfUrl = uploadResult.getOrNull() ?: ""
                                            android.util.Log.d("NEXOGO_PDF", "PDF subido exitosamente a Firebase: $pdfUrl")
                                        } else {
                                            android.util.Log.w("NEXOGO_PDF", "Error subiendo PDF a Firebase: ${uploadResult.exceptionOrNull()?.message}")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.w("NEXOGO_PDF", "Error subiendo PDF a Firebase: ${e.message}")
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Text("Generar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPdfDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        // Diálogo de opciones de PDF
        if (showPdfOptions && pdfBytes != null) {
            AlertDialog(
                onDismissRequest = { showPdfOptions = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "PDF Generado",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text("El PDF ha sido generado exitosamente. ¿Qué deseas hacer?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPdfOptions = false
                            pdfBytes?.let { bytes ->
                                pdfDownloader.downloadPdf(bytes, "historial_${petName}_${System.currentTimeMillis()}.pdf")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargar",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Descargar")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                showPdfOptions = false
                                pdfBytes?.let { bytes ->
                                    pdfDownloader.sharePdf(bytes, "historial_${petName}_${System.currentTimeMillis()}.pdf")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartir")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        TextButton(
                            onClick = { showPdfOptions = false }
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            )
        }
    }
}