package com.example.nexogo.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.ChatViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    chatViewModel: ChatViewModel = ChatViewModel.getInstance(LocalContext.current)
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var creatingChat by remember { mutableStateOf(false) }
    
    // Get available users based on current user role
    val availableUsers = remember(currentUser?.role) {
        currentUser?.let { user ->
            chatViewModel.getAvailableUsers(user.id, user.role)
        } ?: emptyList()
    }
    
    // State for creating chat
    var creatingChatForUser by remember { mutableStateOf<User?>(null) }
    
    // Handle chat creation when user is selected
    LaunchedEffect(creatingChatForUser) {
        creatingChatForUser?.let { user ->
            val chatId = chatViewModel.createChat(
                listOf(currentUser?.id ?: "", user.id)
            )
            if (chatId.isNotEmpty()) {
                // Refresh chats list
                chatViewModel.refreshChats(currentUser?.id ?: "")
                onNavigateToChat(chatId)
            }
            creatingChatForUser = null
        }
    }
    
    // Filter users based on search query
    val filteredUsers = remember(availableUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            availableUsers
        } else {
            availableUsers.filter { user ->
                user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (showSearchBar) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar usuarios...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    showSearchBar = false 
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Limpiar")
                                }
                            }
                        )
                    } else {
                        Text("Nuevo Chat")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!showSearchBar) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Elige con quién quieres iniciar una conversación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Users list
            if (filteredUsers.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onUserClick = {
                                // Check if chat already exists
                                val existingChat = chatViewModel.chats.value.find { chat ->
                                    chat.participants.contains(currentUser?.id) && chat.participants.contains(user.id)
                                }
                                
                                if (existingChat != null) {
                                    // Navigate to existing chat
                                    onNavigateToChat(existingChat.id)
                                } else {
                                    // Create new chat
                                    creatingChatForUser = user
                                }
                            }
                        )
                    }
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Sin usuarios",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron usuarios" else "No hay usuarios disponibles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) 
                                "Intenta con otros términos de búsqueda" 
                            else "No hay usuarios disponibles para chatear",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roleDisplayName = when (user.role) {
        UserRole.ADMIN -> "Administrador"
        UserRole.VET -> "Médico Veterinario"
        UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        UserRole.USER -> "Paciente"
        UserRole.VET -> "Profesional Pendiente"
        else -> "Usuario"
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onUserClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture placeholder
            Card(
                modifier = Modifier.size(50.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = roleDisplayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
