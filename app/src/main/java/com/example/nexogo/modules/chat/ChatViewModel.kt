package com.example.nexogo.modules.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.models.Message
import com.example.nexogo.core.models.MessageAttachment
import com.example.nexogo.core.models.AttachmentType
import com.example.nexogo.core.models.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel para gestión de chat
 */
class ChatViewModel : ViewModel() {
    
    private val repository = ChatRepository()
    
    // Estados del chat
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()
    
    private val _availableUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val availableUsers: StateFlow<List<Map<String, Any>>> = _availableUsers.asStateFlow()
    
    private val _availablePatients = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val availablePatients: StateFlow<List<Map<String, Any>>> = _availablePatients.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
    
    private val _typingUsers = MutableStateFlow<List<String>>(emptyList())
    val typingUsers: StateFlow<List<String>> = _typingUsers.asStateFlow()
    
    // Usuario actual
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var currentUserRole: String = ""
    
    /**
     * Inicializar con datos del usuario actual
     */
    fun initializeUser(userId: String, userName: String, userRole: String) {
        currentUserId = userId
        currentUserName = userName
        currentUserRole = userRole
        loadConversations()
        loadAvailableUsers()
        loadAvailablePatients()
    }
    
    /**
     * Cargar conversaciones del usuario
     */
    fun loadConversations() {
        if (currentUserId.isEmpty()) return
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val result = repository.getUserConversations(currentUserId)
                if (result.isSuccess) {
                    _conversations.value = result.getOrNull() ?: emptyList()
                    Log.d("NEXOGO_CHAT", "Conversaciones cargadas: ${_conversations.value.size}")
                } else {
                    _error.value = "Error cargando conversaciones: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_CHAT", "Error cargando conversaciones: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción cargando conversaciones: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar usuarios disponibles
     */
    fun loadAvailableUsers() {
        if (currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val result = repository.getAvailableUsers(currentUserId)
                if (result.isSuccess) {
                    _availableUsers.value = result.getOrNull() ?: emptyList()
                    Log.d("NEXOGO_CHAT", "Usuarios disponibles: ${_availableUsers.value.size}")
                } else {
                    Log.e("NEXOGO_CHAT", "Error cargando usuarios: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_CHAT", "Excepción cargando usuarios: ${e.message}")
            }
        }
    }
    
    /**
     * Cargar pacientes disponibles
     */
    fun loadAvailablePatients() {
        if (currentUserId.isEmpty()) {
            Log.w("NEXOGO_CHAT", "loadAvailablePatients: currentUserId está vacío")
            return
        }
        
        Log.d("NEXOGO_CHAT", "=== CHATVIEWMODEL: CARGANDO PACIENTES ===")
        Log.d("NEXOGO_CHAT", "currentUserId: $currentUserId")
        
        viewModelScope.launch {
            try {
                Log.d("NEXOGO_CHAT", "Llamando a repository.getAvailablePatients...")
                val result = repository.getAvailablePatients(currentUserId)
                Log.d("NEXOGO_CHAT", "Resultado recibido: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val patients = result.getOrNull() ?: emptyList()
                    _availablePatients.value = patients
                    Log.d("NEXOGO_CHAT", "Pacientes cargados en ViewModel: ${patients.size}")
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("NEXOGO_CHAT", "Error cargando pacientes: ${error?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_CHAT", "Excepción cargando pacientes: ${e.message}", e)
            }
        }
    }
    
    /**
     * Iniciar nueva conversación
     */
    fun startNewConversation(targetUserId: String, targetUserName: String, targetUserRole: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val result = repository.getOrCreateConversation(
                    currentUserId,
                    targetUserId,
                    currentUserName,
                    targetUserName,
                    currentUserRole,
                    targetUserRole
                )
                
                if (result.isSuccess) {
                    val conversation = result.getOrNull()
                    if (conversation != null) {
                        _currentConversation.value = conversation
                        loadMessages(conversation.id)
                        Log.d("NEXOGO_CHAT", "Nueva conversación iniciada: ${conversation.id}")
                    }
                } else {
                    _error.value = "Error iniciando conversación: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_CHAT", "Error iniciando conversación: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción iniciando conversación: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar mensajes de una conversación
     */
    fun loadMessages(conversationId: String) {
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val result = repository.getMessages(conversationId)
                if (result.isSuccess) {
                    val messages = result.getOrNull() ?: emptyList()
                    _messages.value = messages
                    
                    // Marcar mensajes como leídos
                    messages.filter { 
                        it.senderId != currentUserId && !it.isRead 
                    }.forEach { message ->
                        markMessageAsRead(conversationId, message.id)
                    }
                } else {
                    _error.value = "Error cargando mensajes: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_CHAT", "Error cargando mensajes: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción cargando mensajes: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Enviar mensaje de texto
     */
    fun sendTextMessage(conversationId: String, text: String) {
        if (text.trim().isEmpty() || currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            try {
                val conversation = _currentConversation.value
                if (conversation != null) {
                    val receiverId = conversation.participants.find { it != currentUserId } ?: return@launch
                    
                    val result = repository.sendMessage(
                        conversationId = conversationId,
                        senderId = currentUserId,
                        senderName = currentUserName,
                        receiverId = receiverId,
                        text = text.trim(),
                        attachments = emptyList()
                    )
                    
                    if (result.isSuccess) {
                        _currentMessage.value = ""
                        _message.value = "Mensaje enviado"
                        Log.d("NEXOGO_CHAT", "Mensaje de texto enviado")
                    } else {
                        _error.value = "Error enviando mensaje: ${result.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_CHAT", "Error enviando mensaje: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción enviando mensaje: ${e.message}")
            }
        }
    }
    
    /**
     * Enviar mensaje multimedia
     */
    fun sendMediaMessage(
        conversationId: String,
        uri: Uri,
        mediaType: AttachmentType,
        mediaName: String? = null,
        mediaSize: Long = 0
    ) {
        if (currentUserId.isEmpty()) return
        
        _isUploading.value = true
        _uploadProgress.value = 0f
        
        viewModelScope.launch {
            try {
                // Subir archivo
                val uploadResult = repository.uploadMedia(
                    uri = uri,
                    conversationId = conversationId,
                    mediaType = mediaType,
                    onProgress = { progress ->
                        _uploadProgress.value = progress
                    }
                )
                
                if (uploadResult.isSuccess) {
                    val mediaUrl = uploadResult.getOrNull() ?: return@launch
                    val conversation = _currentConversation.value
                    
                    if (conversation != null) {
                        val receiverId = conversation.participants.find { it != currentUserId } ?: return@launch
                        
                        val attachment = MessageAttachment(
                            url = mediaUrl,
                            type = mediaType,
                            name = mediaName ?: "Archivo",
                            size = mediaSize
                        )
                        
                        val result = repository.sendMessage(
                            conversationId = conversationId,
                            senderId = currentUserId,
                            senderName = currentUserName,
                            receiverId = receiverId,
                            text = when (mediaType) {
                                AttachmentType.IMAGE -> "📷 Imagen"
                                AttachmentType.VIDEO -> "🎥 Video"
                                AttachmentType.AUDIO -> "🎵 Audio"
                                AttachmentType.DOCUMENT -> "📄 Documento"
                                AttachmentType.PDF -> "📄 PDF"
                                else -> "📎 Archivo"
                            },
                            attachments = listOf(attachment)
                        )
                        
                        if (result.isSuccess) {
                            _message.value = "Archivo enviado"
                            Log.d("NEXOGO_CHAT", "Mensaje multimedia enviado")
                        } else {
                            _error.value = "Error enviando archivo: ${result.exceptionOrNull()?.message}"
                            Log.e("NEXOGO_CHAT", "Error enviando archivo: ${result.exceptionOrNull()?.message}")
                        }
                    }
                } else {
                    _error.value = "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_CHAT", "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción enviando archivo: ${e.message}")
            } finally {
                _isUploading.value = false
                _uploadProgress.value = 0f
            }
        }
    }
    
    /**
     * Marcar mensaje como leído
     */
    fun markMessageAsRead(conversationId: String, messageId: String) {
        viewModelScope.launch {
            try {
                repository.markMessageAsRead(conversationId, messageId)
            } catch (e: Exception) {
                Log.e("NEXOGO_CHAT", "Error marcando mensaje como leído: ${e.message}")
            }
        }
    }
    
    /**
     * Eliminar mensaje
     */
    fun deleteMessage(conversationId: String, messageId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteMessage(conversationId, messageId)
                if (result.isSuccess) {
                    _message.value = "Mensaje eliminado"
                    Log.d("NEXOGO_CHAT", "Mensaje eliminado: $messageId")
                } else {
                    _error.value = "Error eliminando mensaje: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_CHAT", "Error eliminando mensaje: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NEXOGO_CHAT", "Excepción eliminando mensaje: ${e.message}")
            }
        }
    }
    
    /**
     * Actualizar mensaje actual
     */
    fun updateCurrentMessage(message: String) {
        _currentMessage.value = message
    }
    
    /**
     * Limpiar mensaje actual
     */
    fun clearCurrentMessage() {
        _currentMessage.value = ""
    }
    
    /**
     * Limpiar mensajes de estado
     */
    fun clearMessage() {
        _message.value = ""
        _error.value = null
    }
    
    /**
     * Establecer estado de escritura
     */
    fun setTyping(isTyping: Boolean) {
        _isTyping.value = isTyping
    }
    
    /**
     * Obtener color de burbuja según rol
     */
    fun getBubbleColor(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "#2196F3" // Azul
            "vet" -> "#4CAF50" // Verde
            "assistant" -> "#9C27B0" // Morado
            "patient" -> "#FF9800" // Naranja
            else -> "#757575" // Gris
        }
    }
    
    /**
     * Obtener emoji según rol
     */
    fun getRoleEmoji(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "🧑‍💼"
            "vet" -> "🩺"
            "assistant" -> "🧑‍⚕️"
            "patient" -> "🐾"
            else -> "👤"
        }
    }
}