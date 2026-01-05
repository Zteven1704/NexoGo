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
import com.example.nexogo.modules.domicilios.AdminDomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDomicilioListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit = {}
) {
    val viewModel = remember { AdminDomicilioViewModel(DomicilioRepository()) }
    val domiciliosPendientes by viewModel.domiciliosPendientes.collectAsStateWithLifecycle()
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
                title = { Text("Solicitudes Pendientes", fontWeight = FontWeight.Bold) },
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
                domiciliosPendientes.isEmpty() -> {
                    EmptyStateAdmin(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(domiciliosPendientes) { domicilio ->
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
fun EmptyStateAdmin(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay solicitudes pendientes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

