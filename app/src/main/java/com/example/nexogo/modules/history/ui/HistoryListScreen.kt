package com.example.nexogo.modules.history.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.nexogo.modules.history.viewmodel.HistoryViewModel

/**
 * Pantalla principal que muestra la lista de historiales clínicos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(
    viewModel: HistoryViewModel,
    ownerId: String,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar historiales al iniciar
    LaunchedEffect(ownerId) {
        try {
            viewModel.loadRecords(ownerId)
        } catch (e: Exception) {
            // Manejar error silenciosamente
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                
                uiState.records.isEmpty() -> {
                    EmptyState(
                        onNavigateToCreate = onNavigateToCreate
                    )
                }
                
                else -> {
                    SimpleHistoryList(
                        records = uiState.records,
                        onNavigateToDetail = onNavigateToDetail,
                        onNavigateToEdit = onNavigateToEdit,
                        onDelete = { record ->
                            try {
                                viewModel.deleteRecord(record.recordId, record.ownerId)
                            } catch (e: Exception) {
                                // Manejar error silenciosamente
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Lista simplificada de historiales clínicos
 */
@Composable
private fun SimpleHistoryList(
    records: List<ClinicalRecord>,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDelete: (ClinicalRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = records,
            key = { it.recordId }
        ) { record ->
            SimpleHistoryCard(
                record = record,
                onClick = { onNavigateToDetail(record.recordId) },
                onEdit = { onNavigateToEdit(record.recordId) },
                onDelete = { onDelete(record) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Tarjeta simplificada para mostrar un historial clínico
 */
@Composable
private fun SimpleHistoryCard(
    record: ClinicalRecord,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header con nombre de mascota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.petName.ifEmpty { "Sin nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información del propietario
            Text(
                text = "Propietario: ${record.ownerName.ifEmpty { "No especificado" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Motivo de consulta
            if (record.visitReason.isNotEmpty()) {
                Text(
                    text = "Motivo: ${record.visitReason}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Pronóstico
            if (record.prognosis.isNotEmpty()) {
                Text(
                    text = "Pronóstico: ${record.prognosis}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }
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
                text = "Cargando historiales...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Estado vacío
 */
@Composable
private fun EmptyState(
    onNavigateToCreate: () -> Unit
) {
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
}