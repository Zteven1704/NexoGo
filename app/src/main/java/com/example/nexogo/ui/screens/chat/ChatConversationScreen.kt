package com.example.nexogo.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nexogo.model.*
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    chatViewModel: ChatViewModel = ChatViewModel.getInstance(LocalContext.current)
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val isTyping by chatViewModel.isTyping.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }
    var selectedAttachmentType by remember { mutableStateOf(MessageType.IMAGE) }
    
    val listState = rememberLazyListState()
    val context = LocalContext.current
    
    // File picker launchers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendMessage(
                chatId = chatId,
                senderId = currentUser?.id ?: "",
                receiverId = "", // Will be determined by chat participants
                content = "",
                messageType = MessageType.IMAGE,
                attachmentUri = it
            )
        }
    }
    
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendMessage(
                chatId = chatId,
                senderId = currentUser?.id ?: "",
                receiverId = "",
                content = "",
                messageType = MessageType.VIDEO,
                attachmentUri = it
            )
        }
    }
    
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            chatViewModel.sendMessage(
                chatId = chatId,
                senderId = currentUser?.id ?: "",
                receiverId = "",
                content = "",
                messageType = MessageType.DOCUMENT,
                attachmentUri = it
            )
        }
    }
    
    // Load messages when chat changes
    LaunchedEffect(chatId) {
        chatViewModel.loadMessages(chatId)
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Get chat info
    val chat = chatViewModel.chats.value.find { it.id == chatId }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile picture
                        Card(
                            modifier = Modifier.size(40.dp),
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
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
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
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Video call */ }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Llamada de video")
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
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
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.senderId == currentUser?.id,
                        onMessageLongClick = { /* Show message options */ },
                        onAttachmentClick = { /* Open attachment */ }
                    )
                }
                
                // Typing indicator
                if (isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Message input
            MessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        chatViewModel.sendMessage(
                            chatId = chatId,
                            senderId = currentUser?.id ?: "",
                            receiverId = otherUserId ?: "",
                            content = messageText.trim()
                        )
                        messageText = ""
                    }
                },
                onAttachmentClick = { 
                    selectedAttachmentType = it
                    showAttachmentDialog = true 
                }
            )
        }
    }
    
    // Attachment dialog
    if (showAttachmentDialog) {
        AttachmentDialog(
            onDismiss = { showAttachmentDialog = false },
            onImageClick = {
                showAttachmentDialog = false
                imagePicker.launch("image/*")
            },
            onVideoClick = {
                showAttachmentDialog = false
                videoPicker.launch("video/*")
            },
            onDocumentClick = {
                showAttachmentDialog = false
                documentPicker.launch("*/*")
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    onMessageLongClick: () -> Unit,
    onAttachmentClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMessageLongClick() },
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
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
            horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    when (message.messageType) {
                        MessageType.TEXT -> {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFromCurrentUser) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        MessageType.IMAGE -> {
                            if (message.attachmentUrl != null) {
                                AsyncImage(
                                    model = message.attachmentUrl,
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onAttachmentClick() },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "📷 Imagen",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFromCurrentUser) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        MessageType.VIDEO -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable { onAttachmentClick() },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Reproducir video",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        MessageType.DOCUMENT -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onAttachmentClick() }
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Documento",
                                    tint = if (isFromCurrentUser) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.attachmentName.ifEmpty { "Documento" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isFromCurrentUser) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFromCurrentUser) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
            
            // Timestamp
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentClick: (MessageType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment button
            IconButton(onClick = { onAttachmentClick(MessageType.IMAGE) }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Adjuntar")
            }
            
            // Message input
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 4
            )
            
            // Send button
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (messageText.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AttachmentDialog(
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onDocumentClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar archivo") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Imagen",
                        onClick = onImageClick
                    )
                    AttachmentOption(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        onClick = onVideoClick
                    )
                    AttachmentOption(
                        icon = Icons.Default.AttachFile,
                        label = "Documento",
                        onClick = onDocumentClick
                    )
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

@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    contentDescription = "Escribiendo",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Escribiendo...",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
