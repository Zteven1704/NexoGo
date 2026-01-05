package com.example.nexogo.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.Message
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.ChatViewModel
import com.example.nexogo.model.MessageType
import com.example.nexogo.ui.components.ChatFilePickerDialog
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    chatViewModel: ChatViewModel = ChatViewModel.getInstance(LocalContext.current)
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    
    // Load messages when chat changes
    LaunchedEffect(conversationId) {
        chatViewModel.loadMessages(conversationId)
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Get chat info
    val chat = chatViewModel.chats.value.find { it.id == conversationId }
    val otherUserId = chat?.participants?.find { it != currentUser?.id }
    
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = otherUserName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = otherUserRole,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.senderId == currentUser?.id
                    )
                }
            }
            
            // Message input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Attachment button
                    IconButton(
                        onClick = { showAttachmentDialog = true }
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Adjuntar archivo"
                        )
                    }
                    
                    // Message input
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        singleLine = false
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Send button
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                chatViewModel.sendMessage(
                    chatId = conversationId,
                    senderId = currentUser?.id ?: "",
                    receiverId = otherUserId ?: "",
                    content = messageText.trim()
                )
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Enviar"
                        )
                    }
                }
            }
        }
    }
    
    // File picker dialog
    ChatFilePickerDialog(
        isVisible = showAttachmentDialog,
        onDismiss = { showAttachmentDialog = false },
        onFileSelected = { fileName, fileType, fileUri ->
            chatViewModel.sendMessage(
                chatId = conversationId,
                senderId = currentUser?.id ?: "",
                receiverId = otherUserId ?: "",
                content = "Archivo adjunto: $fileName",
                messageType = when (fileType) {
                    MessageType.IMAGE.name -> MessageType.IMAGE
                    MessageType.VIDEO.name -> MessageType.VIDEO
                    MessageType.AUDIO.name -> MessageType.AUDIO
                    MessageType.DOCUMENT.name -> MessageType.DOCUMENT
                    else -> MessageType.TEXT
                },
                attachmentUri = Uri.parse(fileUri)
            )
        }
    )
}

@Composable
private fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        if (!isFromCurrentUser) {
            // Profile picture for received messages
            Card(
                modifier = Modifier.size(32.dp),
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
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isFromCurrentUser) {
                Alignment.End
            } else {
                Alignment.Start
            }
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Message content
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isFromCurrentUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    // Attachment info
                    if (message.attachmentUrl != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (message.messageType) {
                                    MessageType.IMAGE -> "📷"
                                    MessageType.VIDEO -> "🎥"
                                    MessageType.AUDIO -> "🎵"
                                    MessageType.DOCUMENT -> "📄"
                                    else -> "📎"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.attachmentName.ifBlank { "Archivo adjunto" },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isFromCurrentUser) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
            
            // Time
            Text(
                text = timeFormat.format(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Profile picture for sent messages
            Card(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
