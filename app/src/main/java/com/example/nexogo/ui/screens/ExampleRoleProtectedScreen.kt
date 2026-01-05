package com.example.nexogo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.core.network.NetworkStatus
import com.example.nexogo.core.session.SessionManager
import com.example.nexogo.repository.CurrentUserRepository
import com.example.nexogo.ui.components.OfflineIndicator
import com.example.nexogo.ui.components.PendingSyncBadge
import com.example.nexogo.ui.components.RoleAware

/**
 * Ejemplo de pantalla protegida por roles con soporte offline
 * 
 * Esta pantalla demuestra:
 * - Control de acceso por roles
 * - Indicadores de estado offline
 * - Uso de RoleAware para mostrar contenido condicional
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleRoleProtectedScreen() {
    val context = LocalContext.current
    val userRepository = remember { CurrentUserRepository() }
    
    // Obtener rol del usuario (bloqueante para inicialización rápida)
    val userRole = remember { SessionManager.getRoleBlocking(context) }
    
    // Observar perfil en tiempo real (con soporte offline)
    val profile by userRepository.profile.collectAsStateWithLifecycle()
    
    // Observar estado de conexión
    val isOnline by NetworkStatus.isOnline.collectAsState()
    
    // Cargar perfil al iniciar
    LaunchedEffect(Unit) {
        userRepository.loadCurrentProfile()
        
        // Observar cambios en tiempo real
        userRepository.observeProfileRealtime { updatedProfile ->
            // El perfil se actualiza automáticamente en el StateFlow
        }
    }
    
    // Limpiar listener al desmontar
    DisposableEffect(Unit) {
        onDispose {
            userRepository.cancelProfileListener()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantalla Protegida por Roles") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de estado offline
            OfflineIndicator()
            
            // Información del usuario
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información del Usuario",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Nombre: ${profile?.name ?: "Cargando..."}")
                    Text("Email: ${profile?.email ?: "Cargando..."}")
                    Text("Rol: ${userRole.name}")
                    Text("Estado: ${if (isOnline) "Online" else "Offline"}")
                }
            }
            
            // Contenido solo para ADMIN
            RoleAware(role = userRole, allowedRole = UserRole.ADMIN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Panel de Administrador",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Solo los administradores pueden ver esto")
                        Button(onClick = { /* Acción admin */ }) {
                            Text("Gestionar Usuarios")
                        }
                    }
                }
            }
            
            // Contenido para VET y ASSISTANT
            RoleAware(
                role = userRole,
                allowedRoles = listOf(UserRole.VET, UserRole.VET_ASSISTANT)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Panel de Veterinario",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Solo veterinarios y asistentes pueden ver esto")
                        Button(onClick = { /* Acción vet */ }) {
                            Text("Ver Pacientes")
                        }
                    }
                }
            }
            
            // Contenido para todos los usuarios autenticados
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Panel General",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Todos los usuarios autenticados pueden ver esto")
                    Button(onClick = { /* Acción general */ }) {
                        Text("Ver Mis Datos")
                    }
                }
            }
            
            // Ejemplo de badge de sincronización pendiente
            if (!isOnline) {
                PendingSyncBadge(hasPendingWrites = true)
            }
        }
    }
}




