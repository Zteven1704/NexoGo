package com.example.nexogo.modules.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.core.models.PhysicalExam
import java.util.*

/**
 * Pantalla principal de historial clínico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateRecord: () -> Unit,
    viewModel: ClinicalHistoryViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    
    val records by viewModel.filteredRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    // Actualizar búsqueda cuando cambie el query
    LaunchedEffect(searchQuery) {
        viewModel.searchRecords(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Historial Clínico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onNavigateToCreateRecord) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Historial")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRecord,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Historial")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de búsqueda
            if (showSearchBar) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar historiales...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Indicador de carga
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.contains("Error")) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = if (message.contains("Error")) 
                                MaterialTheme.colorScheme.onErrorContainer 
                            else 
                                MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Lista de historiales
            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📋 No hay historiales clínicos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Crea el primer historial clínico",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(records) { record ->
                    ClinicalRecordCard(
                        record = record,
                        onEdit = { 
                            // TODO: Implementar edición
                        },
                        onDelete = { 
                            viewModel.deleteRecord(record.id)
                        },
                        onDownloadPDF = {
                            viewModel.generatePDF(record.id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de historial clínico individual
 */
@Composable
fun ClinicalRecordCard(
    record: com.example.nexogo.core.models.ClinicalRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDownloadPDF: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.patientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onDownloadPDF) {
                        Icon(Icons.Default.Download, contentDescription = "Descargar PDF")
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Dueño: ${record.ownerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Veterinario: ${record.vetName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Fecha: ${record.date.toDate().date}/${record.date.toDate().month + 1}/${record.date.toDate().year + 1900}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Motivo: ${record.reason}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (record.diagnoses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Diagnósticos: ${record.diagnoses.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (record.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Archivos adjuntos",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${record.attachments.size} archivo(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Pantalla de creación/edición de historial clínico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditClinicalRecordScreen(
    recordId: String? = null,
    patientId: String,
    patientName: String,
    ownerId: String,
    ownerName: String,
    vetId: String,
    vetName: String,
    onNavigateBack: () -> Unit,
    onRecordSaved: () -> Unit,
    viewModel: ClinicalHistoryViewModel = viewModel()
) {
    var reason by remember { mutableStateOf("") }
    var anamnesis by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var respiratoryRate by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var generalAppearance by remember { mutableStateOf("") }
    var cardiovascular by remember { mutableStateOf("") }
    var respiratory by remember { mutableStateOf("") }
    var digestive by remember { mutableStateOf("") }
    var neurological by remember { mutableStateOf("") }
    var musculoskeletal by remember { mutableStateOf("") }
    var skin by remember { mutableStateOf("") }
    var eyes by remember { mutableStateOf("") }
    var ears by remember { mutableStateOf("") }
    var mouth by remember { mutableStateOf("") }
    var problems by remember { mutableStateOf("") }
    var diagnoses by remember { mutableStateOf("") }
    var treatmentPlan by remember { mutableStateOf("") }
    var prognosis by remember { mutableStateOf("") }
    var evolution by remember { mutableStateOf("") }
    var selectedAttachments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    // Launcher para seleccionar archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedAttachments = selectedAttachments + uris
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId == null) "Nuevo Historial" else "Editar Historial") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del paciente
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información del Paciente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Paciente: $patientName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Dueño: $ownerName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Veterinario: $vetName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Motivo de consulta
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Motivo de Consulta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo de la consulta") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = anamnesis,
                        onValueChange = { anamnesis = it },
                        label = { Text("Anamnesis") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            }
            
            // Examen físico
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Examen Físico",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = temperature,
                            onValueChange = { temperature = it },
                            label = { Text("Temperatura (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it },
                            label = { Text("Frecuencia Cardíaca") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = respiratoryRate,
                            onValueChange = { respiratoryRate = it },
                            label = { Text("Frecuencia Respiratoria") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Peso (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = generalAppearance,
                        onValueChange = { generalAppearance = it },
                        label = { Text("Aspecto General") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                    
                    OutlinedTextField(
                        value = cardiovascular,
                        onValueChange = { cardiovascular = it },
                        label = { Text("Sistema Cardiovascular") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                    
                    OutlinedTextField(
                        value = respiratory,
                        onValueChange = { respiratory = it },
                        label = { Text("Sistema Respiratorio") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }
            
            // Diagnóstico y tratamiento
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Diagnóstico y Tratamiento",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = problems,
                        onValueChange = { problems = it },
                        label = { Text("Problemas Identificados") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    OutlinedTextField(
                        value = diagnoses,
                        onValueChange = { diagnoses = it },
                        label = { Text("Diagnósticos") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    OutlinedTextField(
                        value = treatmentPlan,
                        onValueChange = { treatmentPlan = it },
                        label = { Text("Plan de Tratamiento") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )
                    
                    OutlinedTextField(
                        value = prognosis,
                        onValueChange = { prognosis = it },
                        label = { Text("Pronóstico") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                    
                    OutlinedTextField(
                        value = evolution,
                        onValueChange = { evolution = it },
                        label = { Text("Evolución y Seguimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
            
            // Archivos adjuntos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Archivos Adjuntos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") }
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar archivos")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adjuntar Archivos")
                    }
                    
                    if (selectedAttachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Archivos seleccionados: ${selectedAttachments.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Botón de guardar
            Button(
                onClick = {
                    if (reason.isNotEmpty() && anamnesis.isNotEmpty()) {
                        val physicalExam = PhysicalExam(
                            temperature = temperature.toDoubleOrNull() ?: 0.0,
                            heartRate = heartRate.toIntOrNull() ?: 0,
                            respiratoryRate = respiratoryRate.toIntOrNull() ?: 0,
                            weight = weight.toDoubleOrNull() ?: 0.0,
                            generalAppearance = generalAppearance,
                            cardiovascular = cardiovascular,
                            respiratory = respiratory,
                            digestive = digestive,
                            neurological = neurological,
                            musculoskeletal = musculoskeletal,
                            skin = skin,
                            eyes = eyes,
                            ears = ears,
                            mouth = mouth
                        )
                        
                        val problemsList = problems.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val diagnosesList = diagnoses.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        if (recordId == null) {
                            // Crear nuevo historial
                            viewModel.createRecord(
                                patientId = patientId,
                                patientName = patientName,
                                ownerId = ownerId,
                                ownerName = ownerName,
                                vetId = vetId,
                                vetName = vetName,
                                reason = reason,
                                anamnesis = anamnesis,
                                physicalExam = physicalExam,
                                problems = problemsList,
                                diagnoses = diagnosesList,
                                treatmentPlan = treatmentPlan,
                                prognosis = prognosis,
                                evolution = evolution,
                                attachments = selectedAttachments
                            )
                        } else {
                            // Actualizar historial existente
                            viewModel.updateRecord(
                                recordId = recordId,
                                reason = reason,
                                anamnesis = anamnesis,
                                physicalExam = physicalExam,
                                problems = problemsList,
                                diagnoses = diagnosesList,
                                treatmentPlan = treatmentPlan,
                                prognosis = prognosis,
                                evolution = evolution,
                                newAttachments = selectedAttachments
                            )
                        }
                        
                        onRecordSaved()
                    }
                },
                enabled = !isLoading && reason.isNotEmpty() && anamnesis.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (recordId == null) "Crear Historial" else "Actualizar Historial")
            }
        }
    }
}

