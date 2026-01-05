package com.example.nexogo.modules.history.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.nexogo.modules.history.models.ClinicalRecord
import com.example.nexogo.modules.history.models.HistoryUiState
import com.example.nexogo.modules.history.models.Pet
import com.example.nexogo.modules.history.components.PetSelector
import com.example.nexogo.modules.history.components.ConfirmationDialog
import com.example.nexogo.modules.history.components.SuccessDialog
import com.example.nexogo.modules.history.components.ErrorDialog
import com.example.nexogo.modules.history.repo.PetRepository
import com.example.nexogo.modules.history.viewmodel.HistoryViewModel
import com.example.nexogo.core.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla para crear o editar un historial clínico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryEditScreen(
    recordId: String?,
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = recordId != null
    
    // Cargar historial si está editando
    LaunchedEffect(recordId) {
        if (recordId != null) {
            try {
                viewModel.loadRecord(recordId)
            } catch (e: Exception) {
                // Manejar error silenciosamente
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isEditing) "Editar Historial" else "Nuevo Historial",
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                
                else -> {
                    SimpleHistoryEditForm(
                        record = uiState.currentRecord,
                        isEditing = isEditing,
                        onSave = { record ->
                            try {
                                if (isEditing) {
                                    viewModel.updateRecord(recordId!!, record)
                                } else {
                                    viewModel.createRecord(record)
                                }
                                onNavigateBack()
                            } catch (e: Exception) {
                                // Manejar error silenciosamente
                            }
                        },
                        onNavigateBack = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Formulario simplificado de edición del historial
 */
@Composable
private fun SimpleHistoryEditForm(
    record: ClinicalRecord?,
    isEditing: Boolean,
    onSave: (ClinicalRecord) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var petName by remember { mutableStateOf(record?.petName ?: "") }
    var ownerName by remember { mutableStateOf(record?.ownerName ?: "") }
    var visitReason by remember { mutableStateOf(record?.visitReason ?: "") }
    var prognosis by remember { mutableStateOf(record?.prognosis ?: "") }
    
    // Estados para anamnesis
    var feeding by remember { mutableStateOf(record?.anamnesis?.feeding ?: "") }
    var previousDiseases by remember { mutableStateOf(record?.anamnesis?.previousDiseases ?: "") }
    var habitat by remember { mutableStateOf(record?.anamnesis?.habitat ?: "") }
    var coexistence by remember { mutableStateOf(record?.anamnesis?.coexistence ?: "") }
    
    // Estados para examen físico
    var attitude by remember { mutableStateOf(record?.physicalExam?.attitude ?: "") }
    var bodyCondition by remember { mutableStateOf(record?.physicalExam?.bodyCondition ?: "") }
    var hydration by remember { mutableStateOf(record?.physicalExam?.hydration ?: "") }
    var temperature by remember { mutableStateOf(record?.physicalExam?.temperature?.toString() ?: "") }
    var heartRate by remember { mutableStateOf(record?.physicalExam?.heartRate?.toString() ?: "") }
    var respiratoryRate by remember { mutableStateOf(record?.physicalExam?.respiratoryRate?.toString() ?: "") }
    
    // Estados de validación
    var showValidationErrors by remember { mutableStateOf(false) }
    var validationErrors by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Estados para diálogos y animaciones
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    
    // Obtener usuario actual
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: "anonymous"
    val currentUserName = currentUser?.displayName ?: "Usuario"
    
    // Estados para selección de mascota
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingPets by remember { mutableStateOf(false) }
    
    // Repositorio de mascotas
    val petRepository = remember { PetRepository(FirebaseRepository()) }
    
    // Cargar mascotas al iniciar
    LaunchedEffect(Unit) {
        try {
            isLoadingPets = true
            petRepository.getPets().collect { petsList ->
                pets = petsList
                isLoadingPets = false
            }
        } catch (e: Exception) {
            isLoadingPets = false
        }
    }
    
    // Buscar mascotas cuando cambia la query
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            try {
                isLoadingPets = true
                petRepository.searchPets(searchQuery).collect { petsList ->
                    pets = petsList
                    isLoadingPets = false
                }
            } catch (e: Exception) {
                isLoadingPets = false
            }
        }
    }
    
    // Función de validación
    fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        
        if (petName.isBlank()) {
            errors.add("El nombre de la mascota es obligatorio")
        }
        
        if (ownerName.isBlank()) {
            errors.add("El nombre del propietario es obligatorio")
        }
        
        if (visitReason.isBlank()) {
            errors.add("El motivo de consulta es obligatorio")
        }
        
        if (feeding.isBlank()) {
            errors.add("La información de alimentación es obligatoria")
        }
        
        if (attitude.isBlank()) {
            errors.add("La actitud del animal es obligatoria")
        }
        
        if (bodyCondition.isBlank()) {
            errors.add("La condición corporal es obligatoria")
        }
        
        if (selectedPet == null) {
            errors.add("Debe seleccionar una mascota")
        }
        
        validationErrors = errors
        return errors.isEmpty()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
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
                    isError = showValidationErrors && petName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Selector de mascota
                PetSelector(
                    pets = pets,
                    selectedPet = selectedPet,
                    onPetSelected = { pet ->
                        selectedPet = pet
                        petName = pet.name
                        ownerName = pet.ownerName
                    },
                    onSearchChanged = { query ->
                        searchQuery = query
                    },
                    searchQuery = searchQuery,
                    isLoading = isLoadingPets,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Nombre del propietario *") },
                    isError = showValidationErrors && ownerName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = visitReason,
                    onValueChange = { visitReason = it },
                    label = { Text("Motivo de consulta *") },
                    isError = showValidationErrors && visitReason.isBlank(),
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
                    label = { Text("Alimentación *") },
                    isError = showValidationErrors && feeding.isBlank(),
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
        
        // Examen físico
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
                        label = { Text("Actitud *") },
                        isError = showValidationErrors && attitude.isBlank(),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = bodyCondition,
                        onValueChange = { bodyCondition = it },
                        label = { Text("Condición corporal *") },
                        isError = showValidationErrors && bodyCondition.isBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = hydration,
                    onValueChange = { hydration = it },
                    label = { Text("Hidratación") },
                    modifier = Modifier.fillMaxWidth()
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
        
        // Mostrar errores de validación
        if (showValidationErrors && validationErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Por favor, corrige los siguientes errores:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    validationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
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
            
            Button(
                onClick = {
                    if (!validateForm()) {
                        showValidationErrors = true
                        return@Button
                    }
                    
                    showValidationErrors = false
                    showConfirmDialog = true
                },
                enabled = !isSaving,
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
                    Text(if (isEditing) "Actualizar" else "Crear")
                }
            }
        }
        
        // Función para guardar el historial
        fun saveRecord() {
            isSaving = true
            showConfirmDialog = false
            
            try {
                val updatedRecord = if (record != null) {
                    // Editar historial existente
                    record.copy(
                        petName = petName,
                        ownerName = ownerName,
                        visitReason = visitReason,
                        prognosis = prognosis,
                        anamnesis = record.anamnesis.copy(
                            feeding = feeding,
                            previousDiseases = previousDiseases,
                            habitat = habitat,
                            coexistence = coexistence
                        ),
                        physicalExam = record.physicalExam.copy(
                            attitude = attitude,
                            bodyCondition = bodyCondition,
                            hydration = hydration,
                            temperature = temperature.toDoubleOrNull() ?: 0.0,
                            heartRate = heartRate.toIntOrNull() ?: 0,
                            respiratoryRate = respiratoryRate.toIntOrNull() ?: 0
                        )
                    )
                } else {
                    // Crear nuevo historial
                    ClinicalRecord(
                        petId = selectedPet?.petId ?: "temp_pet_${System.currentTimeMillis()}",
                        petName = petName,
                        ownerId = selectedPet?.ownerId ?: currentUserId,
                        ownerName = ownerName,
                        createdBy = currentUserId,
                        createdByName = currentUserName,
                        visitReason = visitReason,
                        prognosis = prognosis,
                        anamnesis = com.example.nexogo.modules.history.models.Anamnesis(
                            feeding = feeding,
                            previousDiseases = previousDiseases,
                            habitat = habitat,
                            coexistence = coexistence
                        ),
                        physicalExam = com.example.nexogo.modules.history.models.PhysicalExam(
                            attitude = attitude,
                            bodyCondition = bodyCondition,
                            hydration = hydration,
                            temperature = temperature.toDoubleOrNull() ?: 0.0,
                            heartRate = heartRate.toIntOrNull() ?: 0,
                            respiratoryRate = respiratoryRate.toIntOrNull() ?: 0
                        )
                    )
                }
                
                onSave(updatedRecord)
                
                // Mostrar diálogo de éxito
                showSuccessDialog = true
                isSaving = false
            } catch (e: Exception) {
                errorMessage = "Error al guardar: ${e.message}"
                showErrorDialog = true
                isSaving = false
            }
        }
        
        // Diálogos
        ConfirmationDialog(
            title = if (isEditing) "Actualizar Historial" else "Crear Historial",
            message = if (isEditing) {
                "¿Está seguro de que desea actualizar este historial clínico?"
            } else {
                "¿Está seguro de que desea crear este historial clínico?"
            },
            confirmText = if (isEditing) "Actualizar" else "Crear",
            onConfirm = { saveRecord() },
            onCancel = { showConfirmDialog = false },
            isVisible = showConfirmDialog
        )
        
        SuccessDialog(
            title = if (isEditing) "Historial Actualizado" else "Historial Creado",
            message = if (isEditing) {
                "El historial clínico ha sido actualizado exitosamente."
            } else {
                "El historial clínico ha sido creado exitosamente."
            },
            onDismiss = { 
                showSuccessDialog = false
                onNavigateBack()
            },
            isVisible = showSuccessDialog
        )
        
        ErrorDialog(
            title = "Error",
            message = errorMessage,
            onDismiss = { showErrorDialog = false },
            isVisible = showErrorDialog
        )
    }
}

/**
 * Estado de carga
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Cargando historial...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}