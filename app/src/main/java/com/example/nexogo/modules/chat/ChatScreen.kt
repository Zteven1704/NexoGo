package com.example.nexogo.modules.chat

import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.core.models.Message
import com.example.nexogo.core.models.AttachmentType
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal del chat
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val currentMessage by viewModel.currentMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Scroll automático al final cuando hay nuevos mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    // Cargar mensajes cuando se abre la conversación
    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentConversation?.participantNames?.values?.firstOrNull() ?: "Chat",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = getRoleDisplayName(
                                currentConversation?.participantRoles?.values?.firstOrNull() ?: ""
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Más opciones */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
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
            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLoading && messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(messages) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = message.senderId == "current_user_id", // TODO: Obtener del ViewModel
                            onDeleteMessage = { messageId ->
                                viewModel.deleteMessage(conversationId, messageId)
                            }
                        )
                    }
                }
            }
            
            // Indicador de carga de archivo
            if (isUploading) {
                UploadProgressIndicator(
                    isUploading = isUploading,
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Campo de entrada de mensaje
            MessageInputField(
                message = currentMessage,
                onMessageChange = viewModel::updateCurrentMessage,
                onSendMessage = { text ->
                    if (text.isNotBlank()) {
                        viewModel.sendTextMessage(conversationId, text)
                    }
                },
                onSendMedia = { uri, mediaType, name, size ->
                    viewModel.sendMediaMessage(conversationId, uri, mediaType, name, size)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
    
    // Mostrar mensajes de estado
    LaunchedEffect(message, error) {
        message?.let {
            // TODO: Mostrar snackbar
        }
        error?.let {
            // TODO: Mostrar snackbar de error
        }
    }
}

/**
 * Burbuja de mensaje
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwnMessage) {
            // Avatar del remitente
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {
            // Burbuja del mensaje
            Card(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clickable { /* TODO: Mostrar opciones */ },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                    bottomEnd = if (isOwnMessage) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOwnMessage) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Contenido del mensaje
                    when {
                        message.attachments.isEmpty() -> {
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isOwnMessage) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        message.attachments.isNotEmpty() -> {
                            // Mostrar adjuntos
                            message.attachments.forEach { attachment ->
                                MediaPreview(
                                    mediaType = attachment.type,
                                    mediaUrl = attachment.url,
                                    mediaName = attachment.name
                                )
                            }
                            if (message.text.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isOwnMessage) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Timestamp y estado
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMessageTimestamp(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isOwnMessage) {
                    if (message.isRead) {
                        Icon(
                            imageVector = Icons.Filled.DoneAll,
                            contentDescription = "Leído",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (message.isDelivered) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Enviado",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        if (isOwnMessage) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar mensaje") },
            text = { Text("¿Estás seguro de que quieres eliminar este mensaje?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMessage(message.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Campo de entrada de mensaje
 */
@Composable
fun MessageInputField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendMedia: (Uri, AttachmentType, String?, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMediaPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de adjuntar archivo
            MediaPicker(
                onMediaSelected = { uri, mediaType, name, size ->
                    onSendMedia(uri, mediaType, name, size)
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Campo de texto
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = { Text("Escribe un mensaje...") },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 4,
                trailingIcon = {
                    if (message.isNotEmpty()) {
                        IconButton(
                            onClick = { onSendMessage(message) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Enviar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botón de grabación de voz
            AudioRecorder(
                onRecordingComplete = { uri, name, size ->
                    onSendMedia(uri, AttachmentType.AUDIO, name, size)
                }
            )
        }
    }
}

/**
 * Formatear timestamp del mensaje
 */
private fun formatMessageTimestamp(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60 * 1000 -> "Ahora"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m"
        diff < 24 * 60 * 60 * 1000 -> {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.format(date)
        }
        else -> {
            val format = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
            format.format(date)
        }
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