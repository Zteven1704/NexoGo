package com.example.nexogo.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.SettingsViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun StorageDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current)
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val isLoading by settingsViewModel.isLoading.collectAsStateWithLifecycle()
    val message by settingsViewModel.message.collectAsStateWithLifecycle()
    
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    val storageInfo = remember { settingsViewModel.getStorageInfo() }
    
    // Clear message when it changes
    LaunchedEffect(message) {
        message?.let {
            settingsViewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Almacenamiento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Storage overview
                    Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Storage,
                            contentDescription = "Almacenamiento",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Uso de Almacenamiento",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Storage bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Espacio usado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", storageInfo.usedSpace)} GB de ${String.format("%.1f", storageInfo.totalSpace)} GB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = (storageInfo.usedSpace / storageInfo.totalSpace).toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            // Storage details
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detalles de Almacenamiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StorageDetailItem(
                        icon = Icons.Default.Storage,
                        title = "Espacio total",
                        value = "${String.format("%.1f", storageInfo.totalSpace)} GB",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    StorageDetailItem(
                        icon = Icons.Default.Folder,
                        title = "Espacio usado",
                        value = "${String.format("%.1f", storageInfo.usedSpace)} GB",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    StorageDetailItem(
                        icon = Icons.Default.FolderOpen,
                        title = "Espacio disponible",
                        value = "${String.format("%.1f", storageInfo.availableSpace)} GB",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    StorageDetailItem(
                        icon = Icons.Default.Cached,
                        title = "Caché de la aplicación",
                        value = "${String.format("%.1f", storageInfo.cacheSize)} GB",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Actions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Acciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Clear cache button
                    Button(
                        onClick = { showClearCacheDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            Icons.Default.Cached,
                            contentDescription = "Limpiar caché",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar Caché")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Backup data button
                    OutlinedButton(
                        onClick = { /* TODO: Implement backup */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = "Respaldar datos",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Respaldar Datos")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Restore data button
                    OutlinedButton(
                        onClick = { /* TODO: Implement restore */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = "Restaurar datos",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar Datos")
                    }
                }
            }
                }
            }
        }
    }
    
    // Clear cache confirmation dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Limpiar Caché") },
            text = { 
                Text("¿Estás seguro de que quieres limpiar el caché de la aplicación? Esto liberará ${String.format("%.1f", storageInfo.cacheSize)} GB de espacio.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.clearCache()
                        showClearCacheDialog = false
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Limpiar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

