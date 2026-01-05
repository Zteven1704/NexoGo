package com.example.nexogo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.*
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de Chat conectado a Firebase
 * Mantiene la sincronización en tiempo real sin cambiar la UI
 */
class FirebaseChatViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    /**
     * Carga todos los chats del usuario
     */
    fun loadUserChats(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Filtrar chats que contengan al usuario
                val allChats = _chats.value.filter { chat ->
                    chat.participants.contains(userId)
                }
                _chats.value = allChats
                _message.value = "Chats cargados exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al cargar chats: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Carga mensajes de un chat específico
     */
    fun loadMessages(chatId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.listenToMessages().collect { messagesList ->
                    _messages.value = messagesList
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar mensajes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea un nuevo chat
     */
    fun createChat(
        participants: List<String>,
        chatType: ChatType = ChatType.ONE_TO_ONE,
        relatedPatientId: String? = null,
        relatedAppointmentId: String? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val chat = Chat(
                    id = "chat_${System.currentTimeMillis()}",
                    participants = participants,
                    chatType = chatType,
                    relatedPatientId = relatedPatientId,
                    relatedAppointmentId = relatedAppointmentId,
                    createdAt = com.google.firebase.Timestamp.now(),
                    lastActivity = com.google.firebase.Timestamp.now()
                )
                
                val result = repository.addMessage(Message(conversationId = chat.id, content = "Chat creado"))
                if (result.isSuccess) {
                    _chats.value = _chats.value + chat
                    _message.value = "Chat creado exitosamente"
                } else {
                    _message.value = "Error al crear chat: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al crear chat: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje de texto
     */
    fun sendTextMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        content: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val message = Message(
                    id = "msg_${System.currentTimeMillis()}",
                    conversationId = chatId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    messageType = MessageType.TEXT,
                    timestamp = com.google.firebase.Timestamp.now()
                )
                
                val result = repository.addMessage(message)
                if (result.isSuccess) {
                    _messages.value = _messages.value + message
                    _message.value = "Mensaje enviado"
                } else {
                    _message.value = "Error al enviar mensaje: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al enviar mensaje: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje con imagen
     */
    fun sendImageMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        imageUri: Uri,
        caption: String = ""
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Subir imagen a Firebase Storage
                val imagePath = "chat_images/${chatId}/${System.currentTimeMillis()}.jpg"
                val uploadResult = repository.uploadFile(imageUri, imagePath)
                
                if (uploadResult.isSuccess) {
                    val message = Message(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = chatId,
                        senderId = senderId,
                        receiverId = receiverId,
                        content = caption.ifEmpty { "Imagen" },
                        messageType = MessageType.IMAGE,
                        attachmentUrl = uploadResult.getOrNull(),
                        attachmentName = "imagen.jpg",
                        timestamp = com.google.firebase.Timestamp.now()
                    )
                    
                    val result = repository.addMessage(message)
                    if (result.isSuccess) {
                        _messages.value = _messages.value + message
                        _message.value = "Imagen enviada"
                    } else {
                        _message.value = "Error al enviar imagen: ${result.exceptionOrNull()?.message}"
                    }
                } else {
                    _message.value = "Error al subir imagen: ${uploadResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al enviar imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Envía un mensaje con documento
     */
    fun sendDocumentMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        documentUri: Uri,
        fileName: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Subir documento a Firebase Storage
                val documentPath = "chat_documents/${chatId}/${fileName}"
                val uploadResult = repository.uploadFile(documentUri, documentPath)
                
                if (uploadResult.isSuccess) {
                    val message = Message(
                        id = "msg_${System.currentTimeMillis()}",
                        conversationId = chatId,
                        senderId = senderId,
                        receiverId = receiverId,
                        content = "Documento: $fileName",
                        messageType = MessageType.DOCUMENT,
                        attachmentUrl = uploadResult.getOrNull(),
                        attachmentName = fileName,
                        timestamp = com.google.firebase.Timestamp.now()
                    )
                    
                    val result = repository.addMessage(message)
                    if (result.isSuccess) {
                        _messages.value = _messages.value + message
                        _message.value = "Documento enviado"
                    } else {
                        _message.value = "Error al enviar documento: ${result.exceptionOrNull()?.message}"
                    }
                } else {
                    _message.value = "Error al subir documento: ${uploadResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al enviar documento: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Marca un mensaje como leído
     */
    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                // Actualizar estado del mensaje en Firebase
                val message = _messages.value.find { it.id == messageId }
                if (message != null) {
                    val updatedMessage = message.copy(
                        status = MessageStatus.READ,
                        readAt = com.google.firebase.Timestamp.now()
                    )
                    repository.addMessage(updatedMessage)
                }
            } catch (e: Exception) {
                _message.value = "Error al marcar mensaje como leído: ${e.message}"
            }
        }
    }
    
    /**
     * Elimina un mensaje
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val message = _messages.value.find { it.id == messageId }
                if (message != null) {
                    val updatedMessage = message.copy(
                        isDeleted = true,
                        deletedAt = com.google.firebase.Timestamp.now()
                    )
                    repository.addMessage(updatedMessage)
                    _message.value = "Mensaje eliminado"
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }
    
    /**
     * Marca un chat como importante
     */
    fun toggleChatImportance(chatId: String) {
        viewModelScope.launch {
            try {
                val chat = _chats.value.find { it.id == chatId }
                if (chat != null) {
                    val updatedChat = chat.copy(isImportant = !chat.isImportant)
                    repository.addMessage(Message(conversationId = updatedChat.id, content = "Chat actualizado"))
                    _message.value = if (updatedChat.isImportant) "Chat marcado como importante" else "Chat desmarcado como importante"
                }
            } catch (e: Exception) {
                _message.value = "Error al cambiar importancia del chat: ${e.message}"
            }
        }
    }
    
    /**
     * Obtiene un chat por ID
     */
    fun getChatById(chatId: String): Chat? {
        return _chats.value.find { it.id == chatId }
    }
    
    /**
     * Obtiene el número de mensajes no leídos
     */
    fun getUnreadCount(chatId: String): Int {
        return _messages.value.count { 
            it.conversationId == chatId && 
            it.status == MessageStatus.SENT && 
            !it.isDeleted 
        }
    }
    
    /**
     * Busca mensajes por contenido
     */
    fun searchMessages(query: String): List<Message> {
        return _messages.value.filter { message ->
            message.content.contains(query, ignoreCase = true) && !message.isDeleted
        }
    }
    
    /**
     * Obtiene un mensaje por ID
     */
    fun getMessageById(messageId: String): Message? {
        return _messages.value.find { it.id == messageId }
    }
    
    /**
     * Obtiene mensajes por conversación
     */
    fun getMessagesByConversation(conversationId: String): List<Message> {
        return _messages.value.filter { it.conversationId == conversationId }
    }
    
    /**
     * Obtiene mensajes por remitente
     */
    fun getMessagesBySender(senderId: String): List<Message> {
        return _messages.value.filter { it.senderId == senderId }
    }
    
    /**
     * Marca mensajes como leídos
     */
    fun markMessagesAsRead(conversationId: String, userId: String) {
        viewModelScope.launch {
            try {
                val messages = _messages.value.filter { 
                    it.conversationId == conversationId && 
                    it.receiverId == userId && 
                    !it.isRead 
                }
                
                messages.forEach { message ->
                    val updatedMessage = message.copy(
                        isRead = true,
                        readAt = com.google.firebase.Timestamp.now()
                    )
                    repository.addMessage(updatedMessage)
                }
            } catch (e: Exception) {
                _message.value = "Error al marcar mensajes como leídos: ${e.message}"
            }
        }
    }
    
    /**
     * Obtiene estadísticas de chat
     */
    fun getChatStats(): ChatStats {
        val messages = _messages.value
        val total = messages.size
        val unread = messages.count { !it.isRead }
        val textMessages = messages.count { it.messageType == com.example.nexogo.model.MessageType.TEXT }
        val imageMessages = messages.count { it.messageType == com.example.nexogo.model.MessageType.IMAGE }
        
        return ChatStats(
            totalMessages = total,
            unreadMessages = unread,
            textMessages = textMessages,
            imageMessages = imageMessages
        )
    }
    
    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        _message.value = ""
    }
}

data class ChatStats(
    val totalMessages: Int,
    val unreadMessages: Int,
    val textMessages: Int,
    val imageMessages: Int
)
