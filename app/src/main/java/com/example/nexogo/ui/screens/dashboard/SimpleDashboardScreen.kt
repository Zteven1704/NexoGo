package com.example.nexogo.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.modules.dashboard.DashboardViewModel

/**
 * Dashboard simplificado para evitar crashes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToClinicalHistory: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToConfig: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    // Fondo con gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "NexoGo Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mensaje de estado
                if (message.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.contains("Error")) 
                                    MaterialTheme.colorScheme.errorContainer 
                                else 
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(12.dp),
                                color = if (message.contains("Error")) 
                                    MaterialTheme.colorScheme.onErrorContainer 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Indicador de carga
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                // Saludo
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "¡Bienvenido a NexoGo!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Sistema de Gestión Veterinaria",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF424242)
                            )
                            
                            if (user != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Usuario: ${user!!.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                                Text(
                                    text = "Rol: ${user!!.role.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
                
                // Módulos disponibles
                val modules = listOf(
                    ModuleItem("Perfil", Icons.Default.Person, onNavigateToProfile),
                    ModuleItem("Citas", Icons.Default.Event, onNavigateToAppointments),
                    ModuleItem("Pacientes", Icons.Default.Pets, onNavigateToPatients),
                    ModuleItem("Historial Clínico", Icons.Default.MedicalServices, onNavigateToClinicalHistory),
                    ModuleItem("Inventario", Icons.Default.Inventory, onNavigateToInventory),
                    ModuleItem("Ventas", Icons.Default.PointOfSale, onNavigateToSales),
                    ModuleItem("Chat", Icons.Default.Chat, onNavigateToChat),
                    ModuleItem("Configuración", Icons.Default.Settings, onNavigateToConfig)
                )
                
                items(modules) { module ->
                    ModuleCard(
                        title = module.title,
                        icon = module.icon,
                        onClick = module.onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ir a $title",
                tint = Color(0xFF757575),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private data class ModuleItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

