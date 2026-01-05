package com.example.nexogo.modules.domicilios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.Domicilio
import com.example.nexogo.model.DomicilioEstado
import com.example.nexogo.modules.domicilios.DomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomicilioListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val viewModel = remember { DomicilioViewModel(DomicilioRepository()) }
    val domicilios by viewModel.domicilios.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    // Mostrar mensajes
    LaunchedEffect(error) {
        error?.let {
            // Aquí podrías mostrar un Snackbar
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            // Aquí podrías mostrar un Snackbar
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes de Domicilio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva solicitud")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                domicilios.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToCreate = onNavigateToCreate
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(domicilios) { domicilio ->
                            DomicilioCard(
                                domicilio = domicilio,
                                onClick = { onNavigateToDetail(domicilio.domicilioId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DomicilioCard(
    domicilio: Domicilio,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = domicilio.tipoServicio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EstadoChip(estado = domicilio.estado)
            }
            
            Text(
                text = "Dirección: ${domicilio.direccion}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Fecha solicitada: ${DomicilioUtils.formatDate(domicilio.fechaSolicitada.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (domicilio.motivoRechazo != null) {
                Text(
                    text = "Motivo rechazo: ${domicilio.motivoRechazo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EstadoChip(estado: DomicilioEstado) {
    val (text, color) = when (estado) {
        DomicilioEstado.PENDIENTE -> "Pendiente" to MaterialTheme.colorScheme.tertiary
        DomicilioEstado.APROBADO -> "Aprobado" to MaterialTheme.colorScheme.primary
        DomicilioEstado.RECHAZADO -> "Rechazado" to MaterialTheme.colorScheme.error
        DomicilioEstado.EN_CAMINO -> "En Camino" to MaterialTheme.colorScheme.secondary
        DomicilioEstado.FINALIZADO -> "Finalizado" to MaterialTheme.colorScheme.primaryContainer
        DomicilioEstado.CANCELADO -> "Cancelado" to MaterialTheme.colorScheme.outline
    }
    
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onNavigateToCreate: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tienes solicitudes de domicilio",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToCreate) {
            Text("Crear primera solicitud")
        }
    }
}


