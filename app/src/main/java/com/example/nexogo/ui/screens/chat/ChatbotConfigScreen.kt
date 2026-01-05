package com.example.nexogo.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.ChatbotResponse
import com.example.nexogo.viewmodel.ChatViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatbotConfigScreen(
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = ChatViewModel.getInstance(LocalContext.current)
) {
    val chatbotResponses by chatViewModel.chatbotResponses.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val message by chatViewModel.message.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingResponse by remember { mutableStateOf<ChatbotResponse?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración del Chatbot") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar respuesta")
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
            // Header info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = "Chatbot",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Configuración del Chatbot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Configura las respuestas automáticas del chatbot para preguntas frecuentes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Responses list
            if (chatbotResponses.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(chatbotResponses) { response ->
                        ChatbotResponseCard(
                            response = response,
                            onEdit = { editingResponse = response },
                            onDelete = { chatViewModel.deleteChatbotResponse(response.id) }
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
                            Icons.Default.SmartToy,
                            contentDescription = "Sin respuestas",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "No hay respuestas configuradas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Agrega respuestas para que el chatbot pueda ayudar a los usuarios",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Add/Edit dialog
    if (showAddDialog || editingResponse != null) {
        ChatbotResponseDialog(
            response = editingResponse,
            onDismiss = { 
                showAddDialog = false
                editingResponse = null
            },
            onSave = { question, answer, category ->
                if (editingResponse != null) {
                    chatViewModel.updateChatbotResponse(
                        editingResponse!!.copy(
                            question = question,
                            answer = answer,
                            category = category
                        )
                    )
                } else {
                    chatViewModel.addChatbotResponse(question, answer, category)
                }
                showAddDialog = false
                editingResponse = null
            }
        )
    }
    
    // Show messages
    message?.let { msg ->
        LaunchedEffect(msg) {
            // Show snackbar or toast
            chatViewModel.clearMessage()
        }
    }
}

@Composable
private fun ChatbotResponseCard(
    response: ChatbotResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = response.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = response.answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = response.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (response.isActive) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Activo",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar respuesta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta respuesta del chatbot?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
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

@Composable
private fun ChatbotResponseDialog(
    response: ChatbotResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var question by remember { mutableStateOf(response?.question ?: "") }
    var answer by remember { mutableStateOf(response?.answer ?: "") }
    var category by remember { mutableStateOf(response?.category ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (response != null) "Editar respuesta" else "Nueva respuesta") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pregunta o palabra clave") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: horarios, servicios, pago") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Respuesta") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Respuesta que dará el chatbot") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: horarios, servicios, contacto") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (question.isNotBlank() && answer.isNotBlank() && category.isNotBlank()) {
                        onSave(question, answer, category)
                    }
                },
                enabled = question.isNotBlank() && answer.isNotBlank() && category.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

