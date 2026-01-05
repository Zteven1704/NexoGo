package com.example.nexogo.ui.screens.admin

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
import com.example.nexogo.core.models.User
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserApprovalScreen(
    onNavigateBack: () -> Unit
) {
    val authRepository = remember { 
        FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) 
    }
    
    var pendingUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var scope = rememberCoroutineScope()
    
    fun loadPendingUsers() {
        scope.launch {
            isLoading = true
            println("DEBUG: UserApprovalScreen - Iniciando carga de usuarios pendientes...")
            authRepository.getPendingUsers()
                .onSuccess { users ->
                    println("DEBUG: UserApprovalScreen - Usuarios pendientes cargados: ${users.size}")
                    users.forEach { user ->
                        println("DEBUG: UserApprovalScreen - Usuario: ${user.name} (${user.email}) - Rol: ${user.role} - Aprobado: ${user.isApproved}")
                    }
                    pendingUsers = users
                }
                .onFailure { exception ->
                    println("DEBUG: UserApprovalScreen - Error cargando usuarios: ${exception.message}")
                    errorMessage = "Error al cargar usuarios: ${exception.message}"
                }
            isLoading = false
        }
    }
    
    // Cargar usuarios pendientes al iniciar
    LaunchedEffect(Unit) {
        println("DEBUG: UserApprovalScreen - Cargando usuarios pendientes...")
        loadPendingUsers()
    }
    
    fun approveUser(userId: String) {
        scope.launch {
            authRepository.approveUser(userId)
                .onSuccess {
                    successMessage = "Usuario aprobado exitosamente"
                    loadPendingUsers() // Recargar lista
                }
                .onFailure { exception ->
                    errorMessage = "Error al aprobar usuario: ${exception.message}"
                }
        }
    }
    
    fun rejectUser(userId: String) {
        scope.launch {
            authRepository.rejectUser(userId)
                .onSuccess {
                    successMessage = "Usuario rechazado"
                    loadPendingUsers() // Recargar lista
                }
                .onFailure { exception ->
                    errorMessage = "Error al rechazar usuario: ${exception.message}"
                }
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        successMessage?.let {
            // Aquí podrías mostrar un Snackbar o Toast
        }
    }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // Aquí podrías mostrar un Snackbar o Toast de error
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aprobación de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información general
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Usuarios Pendientes de Aprobación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Revisa y aprueba las solicitudes de registro de veterinarios y auxiliares.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (pendingUsers.isEmpty()) {
                // No hay usuarios pendientes
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay usuarios pendientes",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Todos los usuarios están aprobados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Lista de usuarios pendientes
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendingUsers) { user ->
                        PendingUserCard(
                            user = user,
                        onApprove = { approveUser(user.id) },
                        onReject = { rejectUser(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingUserCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    var showApprovalDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Información del usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rol: ${getRoleDisplayName(user.role)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (user.phone.isNotEmpty()) {
                        Text(
                            text = "Teléfono: ${user.phone}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Indicador de estado
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "PENDIENTE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showApprovalDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprobar")
                }
                
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar")
                }
            }
        }
    }
    
    // Diálogo de confirmación de aprobación
    if (showApprovalDialog) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            title = { Text("Aprobar Usuario") },
            text = { Text("¿Estás seguro de que quieres aprobar a ${user.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onApprove()
                        showApprovalDialog = false
                    }
                ) {
                    Text("Aprobar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación de rechazo
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Rechazar Usuario") },
            text = { Text("¿Estás seguro de que quieres rechazar a ${user.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject()
                        showRejectDialog = false
                    }
                ) {
                    Text("Rechazar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun getRoleDisplayName(role: com.example.nexogo.core.models.UserRole): String {
    return when (role) {
        com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
        com.example.nexogo.core.models.UserRole.VET -> "Médico Veterinario"
        com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        com.example.nexogo.core.models.UserRole.USER -> "Usuario"
    }
}
