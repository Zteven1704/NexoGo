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
import com.example.nexogo.modules.domicilios.VetDomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VetDomicilioListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit = {}
) {
    val viewModel = remember { VetDomicilioViewModel(DomicilioRepository()) }
    val domiciliosAsignados by viewModel.domiciliosAsignados.collectAsStateWithLifecycle()
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
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Domicilios Asignados", fontWeight = FontWeight.Bold) },
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
                domiciliosAsignados.isEmpty() -> {
                    EmptyStateVet(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(domiciliosAsignados) { domicilio ->
                            VetDomicilioCard(
                                domicilio = domicilio,
                                onClick = { onNavigateToDetail(domicilio.domicilioId) },
                                onMarcarEnCamino = {
                                    viewModel.marcarEnCamino(domicilio.domicilioId)
                                },
                                onMarcarFinalizado = {
                                    viewModel.marcarFinalizado(domicilio.domicilioId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VetDomicilioCard(
    domicilio: Domicilio,
    onClick: () -> Unit,
    onMarcarEnCamino: () -> Unit,
    onMarcarFinalizado: () -> Unit
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
                text = "Cliente: ${domicilio.nombreUsuario}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Dirección: ${domicilio.direccion}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Fecha: ${DomicilioUtils.formatDate(domicilio.fechaSolicitada.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Botones de acción
            if (domicilio.estado == DomicilioEstado.APROBADO) {
                Button(
                    onClick = onMarcarEnCamino,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Marcar como En Camino")
                }
            }
            
            if (domicilio.estado == DomicilioEstado.EN_CAMINO) {
                Button(
                    onClick = onMarcarFinalizado,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("Marcar como Finalizado")
                }
            }
        }
    }
}

@Composable
fun EmptyStateVet(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No tienes domicilios asignados",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

