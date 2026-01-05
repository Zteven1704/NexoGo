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
import com.example.nexogo.modules.history.components.*
import com.example.nexogo.modules.history.models.ClinicalRecord
import com.example.nexogo.modules.history.models.HistoryUiState
import com.example.nexogo.modules.history.viewmodel.HistoryViewModel

/**
 * Pantalla de detalle del historial clínico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    recordId: String,
    viewModel: HistoryViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onDownloadPdf: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar historial al iniciar
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.currentRecord?.petName ?: "Historial Clínico",
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
                },
                actions = {
                    IconButton(onClick = { onDownloadPdf(recordId) }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargar PDF"
                        )
                    }
                    
                    IconButton(onClick = { onNavigateToEdit(recordId) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                
                uiState.currentRecord == null -> {
                    ErrorState(
                        message = "No se pudo cargar el historial",
                        onRetry = { viewModel.loadRecord(recordId) }
                    )
                }
                
                else -> {
                    HistoryDetailContent(
                        record = uiState.currentRecord!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Mostrar error si existe
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // TODO: Mostrar Snackbar con error
                }
            }
        }
    }
}

/**
 * Contenido del detalle del historial
 */
@Composable
private fun HistoryDetailContent(
    record: ClinicalRecord,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información del paciente
        ContactInfoCard(
            ownerName = record.ownerName,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Motivo de consulta
        if (record.visitReason.isNotEmpty()) {
            SimpleSection(
                title = "Motivo de Consulta",
                content = record.visitReason
            )
        }
        
        // Anamnesis
        SimpleSection(
            title = "Anamnesis",
            content = "Alimentación: ${record.anamnesis.feeding}\n" +
                    "Vacunaciones: ${record.anamnesis.vaccinationDates.joinToString()}\n" +
                    "Desparasitaciones: ${record.anamnesis.dewormingDates.joinToString()}\n" +
                    "Enfermedades Previas: ${record.anamnesis.previousDiseases}\n" +
                    "Hábitat: ${record.anamnesis.habitat}\n" +
                    "Coexistencia: ${record.anamnesis.coexistence}"
        )
        
        // Examen físico
        SimpleSection(
            title = "Examen Físico",
            content = "Actitud: ${record.physicalExam.attitude}\n" +
                    "Condición Corporal: ${record.physicalExam.bodyCondition}\n" +
                    "Hidratación: ${record.physicalExam.hydration}\n" +
                    "Temperatura: ${record.physicalExam.temperature}°C\n" +
                    "Frecuencia Cardíaca: ${record.physicalExam.heartRate} lpm\n" +
                    "Frecuencia Respiratoria: ${record.physicalExam.respiratoryRate} rpm\n" +
                    "Tiempo Relleno Capilar: ${record.physicalExam.capillaryRefillTime} s\n" +
                    "Piel y Pelaje: ${record.physicalExam.skinAndCoat}\n" +
                    "Ganglios Linfáticos: ${record.physicalExam.lymphNodes}"
        )
        
        // Problemas y diagnósticos
        if (record.problemsAndDiagnostics.isNotEmpty()) {
            SimpleSection(
                title = "Problemas y Diagnósticos",
                content = record.problemsAndDiagnostics.joinToString("\n\n") { pd ->
                    "Problema: ${pd.problem}\n" +
                    "Diferenciales: ${pd.differential.joinToString()}\n" +
                    "Diagnóstico Presuntivo: ${pd.presumptiveDiagnosis}"
                }
            )
        }
        
        // Exámenes paraclínicos
        if (record.paraclinical.isNotEmpty()) {
            SimpleSection(
                title = "Exámenes Paraclínicos",
                content = record.paraclinical.joinToString("\n\n") { test ->
                    "Examen: ${test.testName}\n" +
                    "Resultados: ${test.results}\n" +
                    "Archivos: ${test.fileUrls.joinToString()}"
                }
            )
        }
        
        // Plan de tratamiento
        if (record.treatmentPlan.isNotEmpty()) {
            SimpleSection(
                title = "Plan de Tratamiento",
                content = record.treatmentPlan.joinToString("\n\n") { treatment ->
                    "Medicamento: ${treatment.drug}\n" +
                    "Dosis: ${treatment.dose}\n" +
                    "Vía: ${treatment.route}\n" +
                    "Frecuencia: ${treatment.frequency}\n" +
                    "Duración: ${treatment.duration}"
                }
            )
        }
        
        // Pronóstico
        if (record.prognosis.isNotEmpty()) {
            SimpleSection(
                title = "Pronóstico",
                content = record.prognosis
            )
        }
        
        // Seguimientos
        if (record.followUps.isNotEmpty()) {
            SimpleSection(
                title = "Seguimientos",
                content = record.followUps.joinToString("\n\n") { followUp ->
                    "Fecha: ${formatDate(followUp.date)}\n" +
                    "Notas: ${followUp.notes}"
                }
            )
        }
        
        // Archivos adjuntos
        if (record.attachments.isNotEmpty()) {
            AttachmentList(
                attachments = record.attachments,
                onRemove = { /* TODO: Implementar eliminación */ },
                onDownload = { /* TODO: Implementar descarga */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Sección simple para mostrar información
 */
@Composable
private fun SimpleSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Indicador de carga
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Estado de error
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

/**
 * Formatea una fecha para mostrar
 */
private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    return try {
        val date = timestamp.toDate()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        dateFormat.format(date)
    } catch (e: Exception) {
        "Fecha no disponible"
    }
}