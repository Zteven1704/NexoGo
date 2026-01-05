package com.example.nexogo.modules.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.core.models.Conversation
import com.example.nexogo.core.models.Message
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de lista de conversaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val availableUsers by viewModel.availableUsers.collectAsState()
    val availablePatients by viewModel.availablePatients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showNewChatDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Inicializar el ViewModel con datos del usuario actual
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Obtener datos del usuario desde Firestore
            val firebaseRepository = com.example.nexogo.core.FirebaseRepository()
            val userResult = firebaseRepository.getDocument("users", currentUser.uid)
            if (userResult.isSuccess) {
                val userData = userResult.getOrNull()
                val userName = userData?.get("name") as? String ?: "Usuario"
                val userRole = userData?.get("role") as? String ?: "PATIENT"
                
                viewModel.initializeUser(currentUser.uid, userName, userRole)
            } else {
                // Si no se puede obtener datos del usuario, usar datos básicos
                viewModel.initializeUser(currentUser.uid, "Usuario", "PATIENT")
            }
        }
    }
    
    // Filtrar conversaciones por búsqueda
    val filteredConversations = conversations.filter { conversation ->
        val participantNames = conversation.participantNames.values.joinToString(" ")
        participantNames.contains(searchQuery, ignoreCase = true) ||
        conversation.lastMessage.contains(searchQuery, ignoreCase = true)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 Mensajes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Nueva conversación")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Chat, contentDescription = "Nuevo chat")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar conversaciones...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // Lista de conversaciones
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Error cargando conversaciones",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { viewModel.loadConversations() }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = "Sin conversaciones",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isEmpty()) "No tienes conversaciones" else "No se encontraron conversaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isEmpty()) {
                            Button(onClick = { showNewChatDialog = true }) {
                                Text("Iniciar conversación")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConversations) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = { onNavigateToChat(conversation.id) }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para nueva conversación
    if (showNewChatDialog) {
        NewChatDialog(
            availableUsers = availableUsers,
            availablePatients = availablePatients,
            onUserSelected = { userId, userName, userRole ->
                viewModel.startNewConversation(userId, userName, userRole)
                showNewChatDialog = false
            },
            onDismiss = { showNewChatDialog = false }
        )
    }
}

/**
 * Item de conversación
 */
@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getRoleEmoji(conversation.participantRoles.values.firstOrNull() ?: ""),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre del participante
                Text(
                    text = conversation.participantNames.values.firstOrNull() ?: "Usuario",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Último mensaje
                Text(
                    text = conversation.lastMessage.ifEmpty { "Sin mensajes" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Timestamp
            Text(
                text = formatTimestamp(conversation.lastMessageTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Diálogo para nueva conversación
 */
@Composable
fun NewChatDialog(
    availableUsers: List<Map<String, Any>>,
    availablePatients: List<Map<String, Any>>,
    onUserSelected: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    
    val filteredUsers = availableUsers.filter { user ->
        val name = user["name"] as? String ?: ""
        val role = user["role"] as? String ?: ""
        name.contains(searchQuery, ignoreCase = true) ||
        role.contains(searchQuery, ignoreCase = true)
    }
    
    val filteredPatients = availablePatients.filter { patient ->
        val name = patient["name"] as? String ?: ""
        val ownerName = patient["ownerName"] as? String ?: ""
        val species = patient["species"] as? String ?: ""
        val breed = patient["breed"] as? String ?: ""
        name.contains(searchQuery, ignoreCase = true) ||
        ownerName.contains(searchQuery, ignoreCase = true) ||
        species.contains(searchQuery, ignoreCase = true) ||
        breed.contains(searchQuery, ignoreCase = true)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva conversación") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Pestañas para usuarios y pacientes
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("👥 Usuarios") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🐾 Pacientes") }
                    )
                }
                
                // Contenido según la pestaña seleccionada
                when (selectedTab) {
                    0 -> {
                        // Lista de usuarios
                        if (filteredUsers.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron usuarios",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredUsers) { user ->
                                    UserItem(
                                        user = user,
                                        onClick = {
                                            val userId = user["uid"] as? String ?: ""
                                            val userName = user["name"] as? String ?: ""
                                            val userRole = user["role"] as? String ?: ""
                                            onUserSelected(userId, userName, userRole)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Lista de pacientes
                        if (filteredPatients.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron pacientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredPatients) { patient ->
                                    PatientItem(
                                        patient = patient,
                                        onClick = {
                                            val patientId = patient["uid"] as? String ?: ""
                                            val patientName = patient["name"] as? String ?: ""
                                            val patientRole = patient["role"] as? String ?: ""
                                            onUserSelected(patientId, patientName, patientRole)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Item de usuario
 */
@Composable
fun UserItem(
    user: Map<String, Any>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = user["name"] as? String ?: "Usuario"
    val role = user["role"] as? String ?: ""
    val profileImageUrl = user["profileImageUrl"] as? String ?: ""
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getRoleEmoji(role),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del usuario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = getRoleDisplayName(role),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Item de paciente
 */
@Composable
fun PatientItem(
    patient: Map<String, Any>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = patient["name"] as? String ?: "Paciente"
    val ownerName = patient["ownerName"] as? String ?: ""
    val species = patient["species"] as? String ?: ""
    val breed = patient["breed"] as? String ?: ""
    val age = patient["age"] as? Int ?: 0
    val gender = patient["gender"] as? String ?: ""
    val profileImageUrl = patient["profileImageUrl"] as? String ?: ""
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐾",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del paciente
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$species - $breed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Dueño: $ownerName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (age > 0) {
                    Text(
                        text = "Edad: ${age} años - $gender",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Obtener emoji según rol
 */
private fun getRoleEmoji(role: String): String {
    return when (role.lowercase()) {
        "admin" -> "🧑‍💼"
        "vet" -> "🩺"
        "assistant" -> "🧑‍⚕️"
        "patient" -> "🐾"
        else -> "👤"
    }
}

/**
 * Obtener nombre de rol para mostrar
 */
private fun getRoleDisplayName(role: String): String {
    return when (role.lowercase()) {
        "admin" -> "Administrador"
        "vet" -> "Veterinario"
        "assistant" -> "Asistente"
        "patient" -> "Paciente"
        else -> "Usuario"
    }
}

/**
 * Formatear timestamp
 */
private fun formatTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60 * 1000 -> "Ahora"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h"
        else -> {
            val format = SimpleDateFormat("dd/MM", Locale.getDefault())
            format.format(date)
        }
    }
}
