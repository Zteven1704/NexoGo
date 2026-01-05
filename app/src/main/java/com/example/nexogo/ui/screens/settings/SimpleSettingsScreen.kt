package com.example.nexogo.ui.screens.settings

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
import com.example.nexogo.core.models.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToProductCategories: () -> Unit,
    onNavigateToChatbotConfig: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            // Sección de Perfil
            item {
                SimpleSettingsSection(
                    title = "Perfil",
                    content = {
                        SimpleSettingsItem(
                            icon = Icons.Default.Person,
                            title = "Mi Perfil",
                            subtitle = "Ver y editar información personal",
                            onClick = onNavigateToProfile
                        )
                    }
                )
            }
            
            // Sección de Aplicación
            item {
                SimpleSettingsSection(
                    title = "Aplicación",
                    content = {
                        SimpleSettingsItem(
                            icon = Icons.Default.Storage,
                            title = "Gestión de Almacenamiento",
                            subtitle = "Administrar espacio de almacenamiento",
                            onClick = onNavigateToStorage
                        )
                        
                        SimpleSettingsItem(
                            icon = Icons.Default.Category,
                            title = "Categorías de Productos",
                            subtitle = "Gestionar categorías de inventario",
                            onClick = onNavigateToProductCategories
                        )
                        
                        SimpleSettingsItem(
                            icon = Icons.Default.Chat,
                            title = "Configuración del Chatbot",
                            subtitle = "Personalizar respuestas del asistente",
                            onClick = onNavigateToChatbotConfig
                        )
                    }
                )
            }
            
            // Sección de Soporte
            item {
                SimpleSettingsSection(
                    title = "Soporte",
                    content = {
                        SimpleSettingsItem(
                            icon = Icons.Default.Help,
                            title = "Ayuda",
                            subtitle = "Preguntas frecuentes y soporte",
                            onClick = onNavigateToHelp
                        )
                        
                        SimpleSettingsItem(
                            icon = Icons.Default.Info,
                            title = "Acerca de",
                            subtitle = "Información de la aplicación",
                            onClick = onNavigateToAbout
                        )
                    }
                )
            }
            
            // Sección de Cuenta
            item {
                SimpleSettingsSection(
                    title = "Cuenta",
                    content = {
                        SimpleSettingsItem(
                            icon = Icons.Default.Logout,
                            title = "Cerrar Sesión",
                            subtitle = "Salir de la aplicación",
                            onClick = { showLogoutDialog = true }
                        )
                    }
                )
            }
        }
    }
    
    // Dialog de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SimpleSettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SimpleSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = { if (enabled && onClick != null) onClick() },
        enabled = enabled && onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            if (onClick != null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
