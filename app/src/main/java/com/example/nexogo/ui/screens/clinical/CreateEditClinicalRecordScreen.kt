package com.example.nexogo.ui.screens.clinical

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import com.example.nexogo.ui.components.AttachmentManager
import com.example.nexogo.ui.components.FilePickerDialog
import com.example.nexogo.model.ClinicalAttachment
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.*
import com.example.nexogo.viewmodel.ClinicalRecordViewModel
import com.example.nexogo.viewmodel.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditClinicalRecordScreen(
    recordId: String? = null,
    onNavigateBack: () -> Unit,
    clinicalRecordViewModel: ClinicalRecordViewModel = ClinicalRecordViewModel.getInstance(LocalContext.current),
    patientViewModel: PatientViewModel = PatientViewModel.getInstance(LocalContext.current)
) {
    val clinicalRecords by clinicalRecordViewModel.clinicalRecords.collectAsStateWithLifecycle()
    val patients by patientViewModel.patients.collectAsStateWithLifecycle()
    val isLoading by clinicalRecordViewModel.isLoading.collectAsStateWithLifecycle()
    val message by clinicalRecordViewModel.message.collectAsStateWithLifecycle()
    
    val isEditing = recordId != null
    val recordToEdit = if (isEditing && recordId != null) {
        clinicalRecords.find { it.id == recordId }
    } else null
    
    // Estados para los datos del formulario
    var selectedPatientId by remember { mutableStateOf(recordToEdit?.patientId ?: "") }
    var selectedPetId by remember { mutableStateOf(recordToEdit?.petId ?: "") }
    var consultationReason by remember { mutableStateOf(recordToEdit?.consultationReason ?: "") }
    var prognosis by remember { mutableStateOf(recordToEdit?.prognosis ?: "") }
    
    // Estados para anamnesis
    var feedingType by remember { mutableStateOf(recordToEdit?.anamnesis?.feeding?.type ?: "") }
    var appetite by remember { mutableStateOf(recordToEdit?.anamnesis?.feeding?.appetite ?: "") }
    var habitat by remember { mutableStateOf(recordToEdit?.anamnesis?.habitat ?: "") }
    var cohabitation by remember { mutableStateOf(recordToEdit?.anamnesis?.cohabitation ?: "") }
    
    // Estados para examen físico
    var temperature by remember { mutableStateOf(recordToEdit?.physicalExam?.temperature?.toString() ?: "") }
    var heartRate by remember { mutableStateOf(recordToEdit?.physicalExam?.heartRate?.toString() ?: "") }
    var respiratoryRate by remember { mutableStateOf(recordToEdit?.physicalExam?.respiratoryRate?.toString() ?: "") }
    var generalAppearance by remember { mutableStateOf(recordToEdit?.physicalExam?.generalAppearance ?: "") }
    
    // Estados para diagnósticos
    var presumptiveDiagnosis by remember { mutableStateOf(recordToEdit?.diagnoses?.presumptiveDiagnosis ?: "") }
    var definitiveDiagnosis by remember { mutableStateOf(recordToEdit?.diagnoses?.definitiveDiagnosis ?: "") }
    
    // Estados para plan terapéutico
    var treatmentActiveIngredient by remember { mutableStateOf("") }
    var treatmentDose by remember { mutableStateOf("") }
    var treatmentRoute by remember { mutableStateOf("") }
    var treatmentFrequency by remember { mutableStateOf("") }
    var treatmentDuration by remember { mutableStateOf("") }
    var recommendations by remember { mutableStateOf(recordToEdit?.therapeuticPlan?.recommendations ?: "") }
    
    // Estados para UI
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAddTreatmentDialog by remember { mutableStateOf(false) }
    var treatments by remember { mutableStateOf(recordToEdit?.therapeuticPlan?.treatments ?: emptyList()) }
    var showPatientDropdown by remember { mutableStateOf(false) }
    var showPetDropdown by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf(recordToEdit?.attachments ?: emptyList()) }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    
    // Obtener mascotas del paciente seleccionado
    val selectedPatient = patients.find { it.id == selectedPatientId }
    val availablePets = selectedPatient?.pets ?: emptyList()
    
    // Debug: Log para verificar datos
    LaunchedEffect(patients) {
        println("DEBUG: Pacientes cargados: ${patients.size}")
        patients.forEach { patient ->
            println("DEBUG: Paciente: ${patient.name} (ID: ${patient.id}) - Mascotas: ${patient.pets.size}")
        }
    }
    
    LaunchedEffect(selectedPatientId) {
        if (selectedPatientId.isNotBlank()) {
            println("DEBUG: Paciente seleccionado: $selectedPatientId")
            println("DEBUG: Paciente encontrado: ${selectedPatient?.name}")
            println("DEBUG: Mascotas disponibles: ${availablePets.size}")
            availablePets.forEach { pet ->
                println("DEBUG: Mascota: ${pet.name} (${pet.species})")
            }
        }
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(message) {
        message?.let { msg ->
            if (msg.contains("exitosamente")) {
                showSuccessDialog = true
            } else {
                snackbarHostState.showSnackbar(msg)
            }
            clinicalRecordViewModel.clearMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Registro Clínico" else "Nuevo Registro Clínico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Validar campos requeridos
                            if (selectedPatientId.isBlank()) {
                                clinicalRecordViewModel._message.value = "Debe seleccionar un paciente"
                                return@IconButton
                            }
                            if (selectedPetId.isBlank()) {
                                clinicalRecordViewModel._message.value = "Debe seleccionar una mascota"
                                return@IconButton
                            }
                            if (consultationReason.isBlank()) {
                                clinicalRecordViewModel._message.value = "Debe ingresar el motivo de consulta"
                                return@IconButton
                            }
                            
                            val newRecord = ClinicalRecord(
                                id = if (isEditing) recordId!! else "",
                                patientId = selectedPatientId,
                                petId = selectedPetId,
                                ownerData = selectedPatient?.let { patient ->
                                    OwnerData(
                                        name = patient.name,
                                        address = patient.address,
                                        phone = patient.phone,
                                        email = patient.email
                                    )
                                } ?: OwnerData(),
                                petData = availablePets.find { it.id == selectedPetId }?.let { pet ->
                                    PetData(
                                        name = pet.name,
                                        species = pet.species,
                                        breed = pet.breed,
                                        age = pet.age
                                    )
                                } ?: PetData(),
                                consultationReason = consultationReason,
                                anamnesis = Anamnesis(
                                    feeding = FeedingInfo(
                                        type = feedingType,
                                        appetite = appetite
                                    ),
                                    habitat = habitat,
                                    cohabitation = cohabitation
                                ),
                                physicalExam = ClinicalPhysicalExam(
                                    temperature = temperature.toDoubleOrNull() ?: 0.0,
                                    heartRate = heartRate.toIntOrNull() ?: 0,
                                    respiratoryRate = respiratoryRate.toIntOrNull() ?: 0,
                                    generalAppearance = generalAppearance
                                ),
                                diagnoses = Diagnoses(
                                    presumptiveDiagnosis = presumptiveDiagnosis,
                                    definitiveDiagnosis = definitiveDiagnosis
                                ),
                                    therapeuticPlan = TherapeuticPlan(
                                        treatments = treatments,
                                        recommendations = recommendations
                                    ),
                                    prognosis = prognosis,
                                    attachments = attachments,
                                    isCompleted = true
                            )
                            
                            if (isEditing) {
                                clinicalRecordViewModel.updateClinicalRecord(newRecord)
                            } else {
                                clinicalRecordViewModel.addClinicalRecord(newRecord)
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
            // Selección de paciente y mascota
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📋 Información del Paciente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Debug info para pacientes
                    Text(
                        text = "Pacientes disponibles: ${patients.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (patients.isNotEmpty()) {
                        patients.take(3).forEach { patient ->
                            Text(
                                text = "- ${patient.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de paciente
                    if (patients.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = showPatientDropdown,
                            onExpandedChange = { showPatientDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedPatient?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Paciente (Dueño)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPatientDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showPatientDropdown,
                                onDismissRequest = { showPatientDropdown = false }
                            ) {
                                patients.forEach { patient ->
                                    DropdownMenuItem(
                                        text = { Text("${patient.name} (${patient.pets.size} mascotas)") },
                                        onClick = {
                                            selectedPatientId = patient.id
                                            selectedPetId = "" // Reset pet selection
                                            showPatientDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = "No hay pacientes disponibles",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Paciente (Dueño)") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Debug info (temporal)
                    if (selectedPatientId.isNotBlank()) {
                        Column {
                            Text(
                                text = "Paciente: ${selectedPatient?.name ?: "No encontrado"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Mascotas disponibles: ${availablePets.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (availablePets.isNotEmpty()) {
                                availablePets.forEach { pet ->
                                    Text(
                                        text = "- ${pet.name} (${pet.species})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Selector de mascota
                    if (selectedPatientId.isNotBlank()) {
                        if (availablePets.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = showPetDropdown,
                                onExpandedChange = { showPetDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = availablePets.find { it.id == selectedPetId }?.name ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Mascota") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showPetDropdown,
                                    onDismissRequest = { showPetDropdown = false }
                                ) {
                                    availablePets.forEach { pet ->
                                        DropdownMenuItem(
                                            text = { Text("${pet.name} (${pet.species})") },
                                            onClick = { 
                                                selectedPetId = pet.id
                                                showPetDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = "No hay mascotas registradas para este paciente",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Mascota") },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = "Seleccione primero un paciente",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Mascota") },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Motivo de consulta
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
                    
                    OutlinedTextField(
                        value = consultationReason,
                        onValueChange = { consultationReason = it },
                        label = { Text("Descripción del problema") },
                        placeholder = { Text("Ingrese el motivo de la consulta...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = true
                    )
                }
            }
            
            // Anamnesis
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = feedingType,
                        onValueChange = { feedingType = it },
                        label = { Text("Tipo de alimentación") },
                        placeholder = { Text("Ej: Alimento balanceado, casero...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = appetite,
                        onValueChange = { appetite = it },
                        label = { Text("Apetito") },
                        placeholder = { Text("Ej: Normal, disminuido, aumentado...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = habitat,
                        onValueChange = { habitat = it },
                        label = { Text("Hábitat") },
                        placeholder = { Text("Ej: Casa con jardín, apartamento...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cohabitation,
                        onValueChange = { cohabitation = it },
                        label = { Text("Convivencia con otros animales/personas") },
                        placeholder = { Text("Ej: Vive con 2 personas adultas, otros perros...") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                }
            }
            
            // Examen físico
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = temperature,
                            onValueChange = { temperature = it },
                            label = { Text("Temperatura (°C)") },
                            placeholder = { Text("37.5") },
                            modifier = Modifier.weight(1f),
                            enabled = true
                        )
                        
                        OutlinedTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it },
                            label = { Text("FC (lpm)") },
                            placeholder = { Text("80") },
                            modifier = Modifier.weight(1f),
                            enabled = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = respiratoryRate,
                        onValueChange = { respiratoryRate = it },
                        label = { Text("FR (rpm)") },
                        placeholder = { Text("20") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = generalAppearance,
                        onValueChange = { generalAppearance = it },
                        label = { Text("Aspecto general") },
                        placeholder = { Text("Describa el aspecto general del animal...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                }
            }
            
            // Diagnósticos
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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = presumptiveDiagnosis,
                        onValueChange = { presumptiveDiagnosis = it },
                        label = { Text("Diagnóstico presuntivo") },
                        placeholder = { Text("Ingrese el diagnóstico presuntivo...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = definitiveDiagnosis,
                        onValueChange = { definitiveDiagnosis = it },
                        label = { Text("Diagnóstico definitivo") },
                        placeholder = { Text("Ingrese el diagnóstico definitivo...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                }
            }
            
            // Plan terapéutico
            Card(
                modifier = Modifier.fillMaxWidth()
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
                            text = "💊 Plan Terapéutico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(onClick = { showAddTreatmentDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar tratamiento")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Lista de tratamientos
                    treatments.forEach { treatment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = treatment.activeIngredient,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${treatment.dose} - ${treatment.route} - ${treatment.frequency}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                IconButton(onClick = {
                                    treatments = treatments.filter { it.id != treatment.id }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar tratamiento",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = recommendations,
                        onValueChange = { recommendations = it },
                        label = { Text("Recomendaciones generales") },
                        placeholder = { Text("Ingrese recomendaciones generales...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                }
            }
            
            // Pronóstico
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
                    
                    OutlinedTextField(
                        value = prognosis,
                        onValueChange = { prognosis = it },
                        label = { Text("Pronóstico") },
                        placeholder = { Text("Ingrese el pronóstico del caso...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                }
            }
            
            // Archivos adjuntos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    AttachmentManager(
                        attachments = attachments,
                        onAddAttachment = { 
                            println("DEBUG: Abriendo diálogo de archivos adjuntos")
                            showAttachmentDialog = true 
                        },
                        onRemoveAttachment = { attachmentId ->
                            attachments = attachments.filter { it.id != attachmentId }
                        }
                    )
                }
            }
        }
    }
    
    // Diálogo para agregar tratamiento
    if (showAddTreatmentDialog) {
        AlertDialog(
            onDismissRequest = { showAddTreatmentDialog = false },
            title = { Text("Agregar Tratamiento") },
            text = {
                Column {
                    OutlinedTextField(
                        value = treatmentActiveIngredient,
                        onValueChange = { treatmentActiveIngredient = it },
                        label = { Text("Principio activo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = treatmentDose,
                        onValueChange = { treatmentDose = it },
                        label = { Text("Dosis") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = treatmentRoute,
                        onValueChange = { treatmentRoute = it },
                        label = { Text("Vía") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = treatmentFrequency,
                        onValueChange = { treatmentFrequency = it },
                        label = { Text("Frecuencia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = treatmentDuration,
                        onValueChange = { treatmentDuration = it },
                        label = { Text("Duración") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (treatmentActiveIngredient.isNotBlank()) {
                            val newTreatment = ClinicalTreatment(
                                id = "t_${System.currentTimeMillis()}",
                                activeIngredient = treatmentActiveIngredient,
                                dose = treatmentDose,
                                route = treatmentRoute,
                                frequency = treatmentFrequency,
                                duration = treatmentDuration
                            )
                            treatments = treatments + newTreatment
                            
                            // Limpiar campos
                            treatmentActiveIngredient = ""
                            treatmentDose = ""
                            treatmentRoute = ""
                            treatmentFrequency = ""
                            treatmentDuration = ""
                            
                            showAddTreatmentDialog = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTreatmentDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para agregar archivo adjunto
    FilePickerDialog(
        isVisible = showAttachmentDialog,
        onDismiss = { showAttachmentDialog = false },
        onConfirm = { attachment ->
            attachments = attachments + attachment
            showAttachmentDialog = false
        }
    )
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onNavigateBack()
            },
            title = { Text("¡Éxito!") },
            text = { 
                Text(
                    if (isEditing) "Registro clínico actualizado exitosamente" 
                    else "Registro clínico creado exitosamente"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}
