package com.example.nexogo.modules.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay

/**
 * Pantalla principal del Dashboard de NexoGo
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToUSERs: () -> Unit,
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
    val stats by viewModel.stats.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val availableModules by viewModel.availableModules.collectAsState()
    
    // Limpiar mensaje automáticamente
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(3000)
            viewModel.clearMessage()
        }
    }
    
    // Fondo con gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                DashboardTopBar(
                    user = user,
                    onProfileClick = onNavigateToProfile,
                    onLogout = onLogout
                )
            },
            floatingActionButton = {
                DashboardFAB(
                    onNavigateToAppointments = onNavigateToAppointments
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
                
                // Saludo personalizado
                item {
                    DashboardGreeting(user = user)
                }
                
                // Notificaciones
                if (notifications.isNotEmpty()) {
                    item {
                        DashboardNotifications(
                            notifications = notifications,
                            onNotificationClick = { notification ->
                                viewModel.markNotificationAsRead(notification.id)
                                // Navegar según el tipo de notificación
                                when (notification.type) {
                                    NotificationType.APPOINTMENT -> onNavigateToAppointments()
                                    NotificationType.MESSAGE -> onNavigateToChat()
                                    NotificationType.INVENTORY -> onNavigateToInventory()
                                    NotificationType.SALE -> onNavigateToSales()
                                    else -> {}
                                }
                            }
                        )
                    }
                }
                
                // Estadísticas (solo para profesionales)
                if (user?.role in listOf(com.example.nexogo.core.models.UserRole.ADMIN, com.example.nexogo.core.models.UserRole.VET, com.example.nexogo.core.models.UserRole.VET_ASSISTANT)) {
                    item {
                        DashboardStats(stats = stats)
                    }
                }
                
                // Módulos disponibles
                item {
                    DashboardModules(
                        modules = availableModules,
                        onModuleClick = { module ->
                            when (module) {
                                DashboardModule.PROFILE -> onNavigateToProfile()
                                DashboardModule.APPOINTMENTS -> onNavigateToAppointments()
                                DashboardModule.USERS -> onNavigateToUSERs()
                                DashboardModule.CLINICAL_HISTORY -> onNavigateToClinicalHistory()
                                DashboardModule.INVENTORY -> onNavigateToInventory()
                                DashboardModule.SALES -> onNavigateToSales()
                                DashboardModule.CHAT -> onNavigateToChat()
                                DashboardModule.CONFIG -> onNavigateToConfig()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Barra superior del dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    user: com.example.nexogo.core.models.User?,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "NexoGo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (user != null) {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            // Avatar del usuario
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profileImageUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.profileImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Usuario",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Menú de opciones
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
            }
        }
    )
}

/**
 * Saludo personalizado
 */
@Composable
fun DashboardGreeting(user: com.example.nexogo.core.models.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = when {
                    user?.name?.isNotEmpty() == true -> "¡Hola, ${user.name}!"
                    else -> "¡Bienvenido a NexoGo!"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = when (user?.role) {
                    com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
                    com.example.nexogo.core.models.UserRole.VET -> "Veterinario"
                    com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
                    com.example.nexogo.core.models.UserRole.USER -> "Paciente"
                    else -> "Usuario"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Gestiona tu clínica veterinaria de manera eficiente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Notificaciones del dashboard
 */
@Composable
fun DashboardNotifications(
    notifications: List<DashboardNotification>,
    onNotificationClick: (DashboardNotification) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { onNotificationClick(notification) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de notificación individual
 */
@Composable
fun NotificationCard(
    notification: DashboardNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = java.text.SimpleDateFormat("HH:mm").format(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Estadísticas del dashboard
 */
@Composable
fun DashboardStats(stats: DashboardStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Resumen del Día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Pacientes",
                        value = stats.totalPatients.toString(),
                        icon = "🐾",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item {
                    StatCard(
                        title = "Citas Hoy",
                        value = stats.todayAppointments.toString(),
                        icon = "🗓️",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                item {
                    StatCard(
                        title = "Inventario",
                        value = stats.totalInventory.toString(),
                        icon = "💊",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                item {
                    StatCard(
                        title = "Ventas Hoy",
                        value = stats.todaySales.toString(),
                        icon = "💰",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (stats.lowStockItems > 0) {
                    item {
                        StatCard(
                            title = "Stock Bajo",
                            value = stats.lowStockItems.toString(),
                            icon = "⚠️",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de estadística individual
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Módulos del dashboard
 */
@Composable
fun DashboardModules(
    modules: List<DashboardModule>,
    onModuleClick: (DashboardModule) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🚀 Módulos Disponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modules) { module ->
                    ModuleCard(
                        module = module,
                        onClick = { onModuleClick(module) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de módulo individual
 */
@Composable
fun ModuleCard(
    module: DashboardModule,
    onClick: () -> Unit
) {
    val color = Color(module.color)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = module.icon,
                style = MaterialTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}

/**
 * Botón flotante del dashboard
 */
@Composable
fun DashboardFAB(
    onNavigateToAppointments: () -> Unit
) {
    FloatingActionButton(
        onClick = onNavigateToAppointments,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Nueva cita"
        )
    }
}
