package com.example.nexogo.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nexogo.model.*
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.ChatViewModel
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    chatViewModel: ChatViewModel = ChatViewModel.getInstance(LocalContext.current)
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val chats by chatViewModel.chats.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") }
    
    // Load chats when screen appears
    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let { userId ->
            chatViewModel.loadUserChats(userId)
        }
    }
    
    // Refresh chats when returning to screen
    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId ->
            chatViewModel.refreshChats(userId)
        }
    }
    
    // Filter chats based on search and filter
    val filteredChats = remember(chats, searchQuery, selectedFilter) {
        chats.filter { chat ->
            val matchesSearch = searchQuery.isEmpty() || 
                chat.participants.any { participantId ->
                    // This would need to be replaced with actual user lookup
                    participantId.contains(searchQuery, ignoreCase = true)
                }
            
            val matchesFilter = when (selectedFilter) {
                "all" -> true
                "unread" -> chat.unreadCount > 0
                "important" -> chat.isImportant
                else -> true
            }
            
            matchesSearch && matchesFilter
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                    IconButton(onClick = onNavigateToNewChat) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo Chat")
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar conversaciones...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            // Filter chips
            if (showFilters) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == "all",
                            onClick = { selectedFilter = "all" },
                            label = { Text("Todos") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "unread",
                            onClick = { selectedFilter = "unread" },
                            label = { Text("No leídos") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == "important",
                            onClick = { selectedFilter = "important" },
                            label = { Text("Importantes") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Chats list
            if (filteredChats.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(filteredChats) { chat ->
                        ChatCard(
                            chat = chat,
                            currentUserId = currentUser?.id ?: "",
                            onChatClick = { onNavigateToChat(chat.id) }
                        )
                    }
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                            Icons.Default.Chat,
                            contentDescription = "Sin conversaciones",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No hay conversaciones",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inicia una nueva conversación para comenzar",
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
private fun ChatCard(
    chat: Chat,
    currentUserId: String,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val otherUserId = chat.participants.find { it != currentUserId } ?: ""
    
    // Get other user info (simplified for demo)
    val otherUserName = when (otherUserId) {
        "user_patient_1" -> "María García"
        "user_patient_2" -> "Carlos López"
        "user_vet_1" -> "Dr. Ana Martínez"
        "user_assistant_1" -> "Luis Rodríguez"
        else -> "Usuario"
    }
    
    val otherUserRole = when (otherUserId) {
        "user_patient_1", "user_patient_2" -> "Paciente"
        "user_vet_1" -> "Veterinario"
        "user_assistant_1" -> "Asistente"
        else -> "Usuario"
    }
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val now = Date()
    val lastMessageTime = chat.lastMessage?.timestamp?.toDate() ?: chat.lastActivity.toDate()
    val isToday = lastMessageTime.date == now.date && 
                  lastMessageTime.month == now.month && 
                  lastMessageTime.year == now.year
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (chat.unreadCount > 0) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onChatClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture
            Card(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
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
            
            // Chat info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.isImportant) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Importante",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = otherUserRole,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (chat.lastMessage != null) {
                    Text(
                        text = when (chat.lastMessage!!.messageType) {
                            MessageType.TEXT -> chat.lastMessage!!.content
                            MessageType.IMAGE -> "📷 Imagen"
                            MessageType.VIDEO -> "🎥 Video"
                            MessageType.DOCUMENT -> "📄 ${chat.lastMessage!!.attachmentName}"
                            else -> chat.lastMessage!!.content
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chat.unreadCount > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
            
            // Time and unread count
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (isToday) {
                        timeFormat.format(lastMessageTime)
                    } else {
                        dateFormat.format(lastMessageTime)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}