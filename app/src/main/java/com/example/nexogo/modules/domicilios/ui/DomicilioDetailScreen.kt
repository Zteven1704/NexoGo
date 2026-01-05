package com.example.nexogo.modules.domicilios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.*
import com.example.nexogo.modules.domicilios.DomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomicilioDetailScreen(
    domicilioId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { DomicilioViewModel(DomicilioRepository()) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    var domicilio by remember { mutableStateOf<Domicilio?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Cargar domicilio
    LaunchedEffect(domicilioId) {
        scope.launch {
            val repository = DomicilioRepository()
            val result = repository.getDomicilioById(domicilioId)
            result.fold(
                onSuccess = { domicilio = it },
                onFailure = { }
            )
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        successMessage?.let {
            viewModel.clearMessages()
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Solicitud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            domicilio == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Solicitud no encontrada")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Estado
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Estado",
                                style = MaterialTheme.typography.labelMedium
                            )
                            EstadoChip(estado = domicilio!!.estado)
                        }
                    }
                    
                    // Información básica
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Información",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            InfoRow("Nombre", domicilio!!.nombreUsuario)
                            InfoRow("Teléfono", domicilio!!.telefono)
                            InfoRow("Dirección", domicilio!!.direccion)
                            InfoRow(
                                "Tipo de solicitud",
                                if (domicilio!!.tipoSolicitud == TipoSolicitudDomicilio.PRODUCTO) "Domicilio de Productos" else "Domicilio de Servicios"
                            )
                            
                            // Mostrar datos según el tipo
                            when (domicilio!!.tipoSolicitud) {
                                TipoSolicitudDomicilio.PRODUCTO -> {
                                    domicilio!!.datosProducto?.let { datos ->
                                        Text(
                                            text = "Productos",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                        datos.listaProductos.forEach { producto ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = producto.nombre,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "Cantidad: ${producto.cantidad}",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    if (producto.observaciones.isNotBlank()) {
                                                        Text(
                                                            text = producto.observaciones,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (datos.observaciones.isNotBlank()) {
                                            Text(
                                                text = "Observaciones: ${datos.observaciones}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                        }
                                    }
                                }
                                TipoSolicitudDomicilio.SERVICIO -> {
                                    domicilio!!.datosServicio?.let { datos ->
                                        InfoRow(
                                            "Tipo de servicio",
                                            DomicilioUtils.getTipoServicioDisplayName(datos.tipoServicio) + 
                                                (datos.tipoServicioOtro?.let { " - $it" } ?: "")
                                        )
                                        InfoRow(
                                            "Fecha solicitada",
                                            DomicilioUtils.formatDate(datos.fechaSolicitada.toDate())
                                        )
                                        if (datos.descripcion.isNotBlank()) {
                                            Text(
                                                text = "Descripción del caso",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(top = 8.dp)
                                            )
                                            Text(
                                                text = datos.descripcion,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Motivo de rechazo
                    if (domicilio!!.motivoRechazo != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Motivo de rechazo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = domicilio!!.motivoRechazo!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    // Historial de eventos
                    if (domicilio!!.historialEventos.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Historial",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                domicilio!!.historialEventos.forEach { evento ->
                                    HistorialItem(evento = evento)
                                }
                            }
                        }
                    }
                    
                    // Botón cancelar (solo si está pendiente)
                    if (domicilio!!.estado == DomicilioEstado.PENDIENTE) {
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancelar Solicitud")
                        }
                    }
                    
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    // Dialog de confirmación
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar solicitud") },
            text = { Text("¿Estás seguro de que deseas cancelar esta solicitud?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarDomicilio(domicilioId)
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancelar solicitud", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HistorialItem(evento: com.example.nexogo.model.DomicilioEvento) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = evento.accion,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = DomicilioUtils.formatDate(evento.fecha.toDate()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        evento.observaciones?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}


