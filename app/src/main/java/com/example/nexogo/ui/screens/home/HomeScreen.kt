package com.example.nexogo.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
// import androidx.hilt.navigation.compose.hiltViewModel // Temporarily disabled
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import android.util.Log
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToClinicalRecords: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMedicalRecords: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSales: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFirebaseTest: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: PersistentAuthViewModel = PersistentAuthViewModel.getInstance(LocalContext.current)
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Refrescar usuario desde DataStore al iniciar la pantalla
    LaunchedEffect(Unit) {
        println("DEBUG: HomeScreen - Refrescando usuario desde DataStore...")
        viewModel.refreshUserFromDataStore()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = currentUser,
                scope = scope,
                drawerState = drawerState,
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onNavigateToAppointments = {
                    scope.launch { drawerState.close() }
                    onNavigateToAppointments()
                },
                onNavigateToPatients = {
                    scope.launch { drawerState.close() }
                    onNavigateToPatients()
                },
                onNavigateToClinicalRecords = {
                    scope.launch { drawerState.close() }
                    onNavigateToClinicalRecords()
                },
                onNavigateToInventory = {
                    scope.launch { drawerState.close() }
                    onNavigateToInventory()
                },
                onNavigateToMedicalRecords = {
                    scope.launch { drawerState.close() }
                    onNavigateToMedicalRecords()
                },
                onNavigateToChat = {
                    scope.launch { drawerState.close() }
                    onNavigateToChat()
                },
                onNavigateToSales = {
                    scope.launch { drawerState.close() }
                    onNavigateToSales()
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        gesturesEnabled = true
    ) {
         HomeContent(
             currentUser = currentUser,
             onOpenDrawer = { 
                 scope.launch { drawerState.open() }
             },
             onNavigateToProfile = onNavigateToProfile,
             onNavigateToAppointments = onNavigateToAppointments,
             onNavigateToClinicalRecords = onNavigateToClinicalRecords,
             onNavigateToInventory = onNavigateToInventory,
             onNavigateToMedicalRecords = onNavigateToMedicalRecords,
             onNavigateToChat = onNavigateToChat,
             onNavigateToSales = onNavigateToSales,
             onNavigateToReports = onNavigateToReports,
             onNavigateToAdmin = onNavigateToAdmin,
             onNavigateToFirebaseTest = onNavigateToFirebaseTest,
             onLogout = onLogout
         )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    currentUser: com.example.nexogo.core.models.User?,
    onOpenDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToClinicalRecords: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMedicalRecords: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSales: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFirebaseTest: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Estado para el modo desarrollador
    var isDeveloperMode by remember { mutableStateOf(false) }
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    
    // Efecto para resetear el modo desarrollador después de 30 segundos
    LaunchedEffect(isDeveloperMode) {
        if (isDeveloperMode) {
            delay(30000) // 30 segundos
            isDeveloperMode = false
            tapCount = 0
            Log.d("DeveloperMode", "⏰ Modo desarrollador desactivado automáticamente")
        }
    }
    
    // Efecto para resetear el contador de toques después de 3 segundos sin actividad
    LaunchedEffect(lastTapTime) {
        if (lastTapTime > 0) {
            delay(3000) // 3 segundos
            tapCount = 0
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "NexoGo",
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastTapTime < 1000) {
                                        tapCount++
                                    } else {
                                        tapCount = 1
                                    }
                                    lastTapTime = currentTime
                                    
                                    // Mostrar progreso en logs
                                    Log.d("DeveloperMode", "🔢 Toques detectados: $tapCount/7")
                                    
                                    // Activar modo desarrollador con 7 toques
                                    if (tapCount >= 7) {
                                        isDeveloperMode = true
                                        tapCount = 0
                                        Log.d("DeveloperMode", "✅ Modo desarrollador activado. Prueba de Firebase disponible.")
                                    }
                                }
                            )
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // User Profile Section - Prominent at top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile image - Larger and more prominent
                    if (currentUser?.profileImageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = currentUser.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder when no image
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Usuario",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // User info - More prominent
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Bienvenido!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = currentUser?.name ?: "Usuario",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = getRoleDisplayName(currentUser?.role),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Modules Section - Organized in a grid
            Text(
                text = "Módulos Disponibles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Quick actions based on user role
            currentUser?.let { user ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getQuickActions(user.role, onNavigateToAppointments, onNavigateToClinicalRecords, onNavigateToInventory, onNavigateToMedicalRecords, onNavigateToChat, onNavigateToSales, onNavigateToReports, onNavigateToAdmin, onNavigateToFirebaseTest)) { action ->
                        QuickActionCard(
                            title = action.title,
                            description = action.description,
                            icon = action.icon,
                            onClick = action.onClick
                        )
                    }
                }
            }
        }
        
        // Indicador de progreso para desarrolladores (solo visible si hay toques)
        if (tapCount > 0 && tapCount < 7) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Dev: $tapCount/7",
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Botón flotante oculto para desarrolladores
        if (isDeveloperMode) {
            FloatingActionButton(
                onClick = {
                    Log.d("DeveloperMode", "✅ Abriendo FirebaseFullTestActivity…")
                    onNavigateToFirebaseTest()
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(56.dp),
                containerColor = Color.Gray.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Text(
                    text = "🔥",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    currentUser: com.example.nexogo.core.models.User?,
    scope: kotlinx.coroutines.CoroutineScope,
    drawerState: androidx.compose.material3.DrawerState,
    onNavigateToProfile: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToClinicalRecords: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMedicalRecords: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSales: () -> Unit = {},
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User info
            currentUser?.let { user ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getRoleDisplayName(user.role),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation items
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Inicio") },
                selected = true, // Mark as selected since we're on home
                onClick = { 
                    scope.launch { drawerState.close() }
                    // Stay on home screen
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Perfil") },
                selected = false,
                onClick = onNavigateToProfile
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                label = { Text("Citas") },
                selected = false,
                onClick = onNavigateToAppointments
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                label = { Text("Pacientes") },
                selected = false,
                onClick = onNavigateToPatients
            )

            // Show clinical records only for professionals
            currentUser?.let { user ->
                if (user.role == UserRole.VET || user.role == UserRole.VET_ASSISTANT || user.role == UserRole.ADMIN) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                        label = { Text("Historial Clínico") },
                        selected = false,
                        onClick = onNavigateToClinicalRecords
                    )
                }
            }

             // Show inventory only for professionals
             currentUser?.let { user ->
                 if (user.role == UserRole.VET || user.role == UserRole.VET_ASSISTANT || user.role == UserRole.ADMIN) {
                     NavigationDrawerItem(
                         icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                         label = { Text("Inventario") },
                         selected = false,
                         onClick = onNavigateToInventory
                     )
                 }
             }

             // Show sales only for professionals
             currentUser?.let { user ->
                 if (user.role == UserRole.VET || user.role == UserRole.VET_ASSISTANT || user.role == UserRole.ADMIN) {
                     NavigationDrawerItem(
                         icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
                         label = { Text("Ventas y Servicios") },
                         selected = false,
                         onClick = onNavigateToSales
                     )
                 }
             }

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                label = { Text("Chat") },
                selected = false,
                onClick = onNavigateToChat
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Configuración") },
                selected = false,
                onClick = onNavigateToSettings
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                label = { Text("Cerrar Sesión") },
                selected = false,
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getRoleDisplayName(role: UserRole?): String {
    return when (role) {
        UserRole.ADMIN -> "Administrador"
        UserRole.VET -> "Médico Veterinario"
        UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        UserRole.USER -> "Usuario"
        null -> "Usuario"
    }
}

private data class QuickAction(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

private fun getQuickActions(
    role: UserRole?,
    onNavigateToAppointments: () -> Unit,
    onNavigateToClinicalRecords: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToMedicalRecords: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSales: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToFirebaseTest: () -> Unit = {}
): List<QuickAction> {
    return when (role) {
        UserRole.ADMIN -> listOf(
            QuickAction(
                "Gestión de Usuarios",
                "Aprobar profesionales y gestionar usuarios",
                Icons.Default.SupervisorAccount
            ) { onNavigateToAdmin() },
            QuickAction(
                "Ventas y Servicios",
                "Gestionar ventas, facturación y pagos",
                Icons.Default.PointOfSale
            ) { onNavigateToSales() },
            QuickAction(
                "Inventario",
                "Gestionar productos y stock",
                Icons.Default.Inventory
            ) { onNavigateToInventory() },
            QuickAction(
                "Reportes",
                "Ver estadísticas y reportes",
                Icons.Default.Analytics
            ) { onNavigateToReports() },
            QuickAction(
                "Prueba Firebase",
                "Verificar conexión completa a Firebase",
                Icons.Default.BugReport
            ) { onNavigateToFirebaseTest() }
        )
        UserRole.VET, UserRole.VET_ASSISTANT -> listOf(
            QuickAction(
                "Citas del Día",
                "Ver y gestionar citas programadas",
                Icons.Default.CalendarToday
            ) { onNavigateToAppointments() },
            QuickAction(
                "Historial Clínico",
                "Crear y gestionar historiales clínicos",
                Icons.Default.MedicalServices
            ) { onNavigateToClinicalRecords() },
            QuickAction(
                "Ventas y Servicios",
                "Registrar ventas y servicios veterinarios",
                Icons.Default.PointOfSale
            ) { onNavigateToSales() },
            QuickAction(
                "Inventario",
                "Gestionar productos y alertas de stock",
                Icons.Default.Inventory
            ) { onNavigateToInventory() }
        )
        UserRole.USER -> listOf(
            QuickAction(
                "Agendar Cita",
                "Solicitar nueva cita veterinaria",
                Icons.Default.CalendarToday
            ) { onNavigateToAppointments() },
            QuickAction(
                "Mis Mascotas",
                "Ver información de mis mascotas",
                Icons.Default.Pets
            ) { /* TODO: Implement pets management */ },
            QuickAction(
                "Historial Clínico",
                "Ver historial médico de mis mascotas",
                Icons.Default.MedicalServices
            ) { onNavigateToMedicalRecords() },
            QuickAction(
                "Chat",
                "Comunicarse con profesionales",
                Icons.Default.Chat
            ) { onNavigateToChat() }
        )
        null -> emptyList()
    }
}
