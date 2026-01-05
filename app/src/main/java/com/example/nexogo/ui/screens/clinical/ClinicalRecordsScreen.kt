package com.example.nexogo.ui.screens.clinical

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.ClinicalRecord
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.model.SimplePet
import com.example.nexogo.viewmodel.ClinicalRecordViewModel
import com.example.nexogo.viewmodel.PatientViewModel
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalRecordsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateRecord: () -> Unit,
    onNavigateToViewRecord: (String) -> Unit,
    onNavigateToEditRecord: (String) -> Unit,
    clinicalRecordViewModel: ClinicalRecordViewModel = ClinicalRecordViewModel.getInstance(LocalContext.current),
    patientViewModel: PatientViewModel = PatientViewModel.getInstance(LocalContext.current)
) {
    val clinicalRecords by clinicalRecordViewModel.clinicalRecords.collectAsStateWithLifecycle()
    val patients by patientViewModel.patients.collectAsStateWithLifecycle()
    val isLoading by clinicalRecordViewModel.isLoading.collectAsStateWithLifecycle()
    val message by clinicalRecordViewModel.message.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<ClinicalRecord?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    
    // Filtrar registros clínicos basado en la búsqueda
    val filteredRecords = remember(clinicalRecords, patients, searchQuery) {
        if (searchQuery.isBlank()) {
            clinicalRecords
        } else {
            clinicalRecords.filter { record ->
                val patient = patients.find { it.id == record.patientId }
                val pet = patient?.pets?.find { it.id == record.petId }
                
                // Buscar por nombre de mascota o teléfono del dueño
                val petName = pet?.name?.lowercase() ?: ""
                val ownerPhone = patient?.phone?.lowercase() ?: ""
                val query = searchQuery.lowercase()
                
                petName.contains(query) || ownerPhone.contains(query)
            }
        }
    }
    
    // Función para obtener el nombre de la mascota
    fun getPetName(record: ClinicalRecord): String {
        val patient = patients.find { it.id == record.patientId }
        val pet = patient?.pets?.find { it.id == record.petId }
        return pet?.name ?: "Mascota no encontrada"
    }
    
    // Función para obtener el nombre del dueño
    fun getOwnerName(record: ClinicalRecord): String {
        val patient = patients.find { it.id == record.patientId }
        return patient?.name ?: "Dueño no encontrado"
    }
    
    LaunchedEffect(message) {
        message?.let {
            // Mostrar mensaje si es necesario
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por mascota o teléfono...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        searchQuery = ""
                                        showSearchBar = false
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    } else {
                        Text("Historial Clínico")
                    }
                },
                navigationIcon = {
                    if (showSearchBar) {
                        IconButton(onClick = { showSearchBar = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar búsqueda")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                    IconButton(onClick = onNavigateToCreateRecord) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Registro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = "Historial Clínico",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Historial Clínico",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Registro completo de consultas veterinarias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                }
            }
            
            // Indicador de búsqueda activa
            if (searchQuery.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Búsqueda activa",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buscando: \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = { 
                                searchQuery = ""
                                showSearchBar = false
                            }
                        ) {
                            Text("Limpiar")
                        }
                    }
                    }
                }
            }
            
            // Estadísticas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${filteredRecords.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Resultados" else "Registros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    // Separador vertical
                    VerticalDivider(
                        modifier = Modifier.height(40.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${filteredRecords.filter { it.isCompleted }.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Completados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                }
            }
            
            // Lista de registros clínicos
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredRecords.isNotEmpty()) {
                items(filteredRecords) { record ->
                    ClinicalRecordCard(
                        record = record,
                        petName = getPetName(record),
                        ownerName = getOwnerName(record),
                        onView = { onNavigateToViewRecord(record.id) },
                        onEdit = { onNavigateToEditRecord(record.id) },
                        onDelete = {
                            recordToDelete = record
                            showDeleteDialog = true
                        }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = "Sin registros",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No se encontraron resultados" else "No hay registros clínicos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) 
                                "Intenta con otro término de búsqueda" 
                            else 
                                "Agrega el primer registro clínico para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                recordToDelete = null
            },
            title = { Text("Eliminar Registro Clínico") },
            text = { 
                Text("¿Estás seguro de que quieres eliminar este registro clínico? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete?.let { record ->
                            clinicalRecordViewModel.deleteClinicalRecord(record.id)
                        }
                        showDeleteDialog = false
                        recordToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        recordToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ClinicalRecordCard(
    record: ClinicalRecord,
    petName: String,
    ownerName: String,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = petName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Dueño: $ownerName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Fecha: ${dateFormat.format(record.createdAt.toDate())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onView) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Ver registro",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar registro",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar registro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del registro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Motivo de consulta:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.consultationReason.ifEmpty { "No especificado" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Diagnóstico:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.diagnoses.presumptiveDiagnosis.ifEmpty { "No especificado" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Pronóstico:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.prognosis.ifEmpty { "No especificado" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Estado del registro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (record.isCompleted) "✅ Completado" else "⏳ En progreso",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (record.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                
                Text(
                    text = "Dr. ${record.createdByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
