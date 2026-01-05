package com.example.nexogo.modules.history.ui

import androidx.compose.foundation.layout.*
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope

/**
 * Versión ultra-simplificada de la lista de historiales
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHistoryListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onDeleteRecord: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var records by remember { mutableStateOf<List<ClinicalRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<ClinicalRecord?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // Firebase
    val firebaseRepository = remember { FirebaseRepository() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    // Cargar historiales
    LaunchedEffect(Unit) {
        try {
            val result = firebaseRepository.getCollection("clinical_records")
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val loadedRecords = dataList.mapNotNull { data ->
                    try {
                        ClinicalRecord(
                            recordId = data["recordId"] as? String ?: "",
                            petName = data["petName"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            visitReason = data["visitReason"] as? String ?: "",
                            prognosis = data["prognosis"] as? String ?: "",
                            createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                records = loadedRecords
                isLoading = false
            } else {
                errorMessage = "Error cargando historiales: ${result.exceptionOrNull()?.message}"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Historiales Clínicos",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Historial"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                        Text("Cargando historiales...")
                    }
                }
            } else if (errorMessage.isNotEmpty()) {
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
            } else if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Sin historiales",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "No hay historiales clínicos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Crea el primer historial clínico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(onClick = onNavigateToCreate) {
                            Text("Crear Historial")
                        }
                    }
                }
            } else {
                // Mostrar lista de historiales
                Text(
                    text = "Historiales Clínicos (${records.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                records.forEach { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                                    text = record.petName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Row {
                                    IconButton(
                                        onClick = { onNavigateToEdit(record.recordId) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = { 
                                            recordToDelete = record
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Propietario: ${record.ownerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Motivo: ${record.visitReason}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (record.prognosis.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pronóstico: ${record.prognosis}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Creado: ${record.createdAt.toDate().toString().substring(0, 19)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && recordToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    recordToDelete = null
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Advertencia",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Confirmar Eliminación",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text("¿Estás seguro de que quieres eliminar el historial de ${recordToDelete?.petName}? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Eliminar de Firebase
                            GlobalScope.launch {
                                try {
                                    isDeleting = true
                                    val result = firebaseRepository.deleteDocument("clinical_records", recordToDelete?.recordId ?: "")
                                    if (result.isSuccess) {
                                        // Recargar la lista
                                        val reloadResult = firebaseRepository.getCollection("clinical_records")
                                        if (reloadResult.isSuccess) {
                                            val dataList = reloadResult.getOrNull() ?: emptyList()
                                            val loadedRecords = dataList.mapNotNull { data ->
                                                try {
                                                    ClinicalRecord(
                                                        recordId = data["recordId"] as? String ?: "",
                                                        petName = data["petName"] as? String ?: "",
                                                        ownerName = data["ownerName"] as? String ?: "",
                                                        visitReason = data["visitReason"] as? String ?: "",
                                                        prognosis = data["prognosis"] as? String ?: "",
                                                        createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                                                    )
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                            records = loadedRecords
                                        }
                                        showDeleteDialog = false
                                        recordToDelete = null
                                        isDeleting = false
                                    } else {
                                        errorMessage = "Error eliminando historial: ${result.exceptionOrNull()?.message}"
                                        isDeleting = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                    isDeleting = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isDeleting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Eliminando...")
                            }
                        } else {
                            Text("Eliminar")
                        }
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
}
