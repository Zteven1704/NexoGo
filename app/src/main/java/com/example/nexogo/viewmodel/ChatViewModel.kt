package com.example.nexogo.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.data.AppDataStore
import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.model.Chat
import com.example.nexogo.model.Message
import com.example.nexogo.model.MessageType
import com.example.nexogo.model.ChatType
import com.example.nexogo.model.ChatbotResponse
import com.example.nexogo.repository.ChatRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(context: Context) : ViewModel() {

    private val appDataStore = AppDataStore(context)
    private val chatRepository = ChatRepository(
        com.google.firebase.firestore.FirebaseFirestore.getInstance(),
        com.google.firebase.storage.FirebaseStorage.getInstance()
    )

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _chatbotResponses = MutableStateFlow<List<ChatbotResponse>>(emptyList())
    val chatbotResponses: StateFlow<List<ChatbotResponse>> = _chatbotResponses.asStateFlow()

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    init {
        viewModelScope.launch {
            loadChatbotResponses()
        }
    }

    // Chat operations
    fun loadUserChats(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allChats = appDataStore.loadChats()
                if (allChats.isEmpty()) {
                    // Create sample chats for demo
                    _chats.value = createSampleChats()
                    appDataStore.saveChats(_chats.value)
                } else {
                    _chats.value = allChats.filter { it.participants.contains(userId) }
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar chats: ${e.message}"
                // Fallback to sample chats
                _chats.value = createSampleChats()
            }
            _isLoading.value = false
        }
    }

    suspend fun createChat(
        participants: List<String>,
        chatType: ChatType = ChatType.ONE_TO_ONE,
        relatedUserId: String? = null,
        relatedAppointmentId: String? = null
    ): String {
        return try {
            val chatId = "chat_${System.currentTimeMillis()}"
            val chat = Chat(
                id = chatId,
                participants = participants,
                chatType = chatType,
                // relatedUserId = relatedUserId, // Campo no disponible en Chat
                relatedAppointmentId = relatedAppointmentId,
                createdAt = Timestamp.now(),
                lastActivity = Timestamp.now()
            )
            
            // Add to local list
            val currentChats = _chats.value.toMutableList()
            currentChats.add(chat)
            _chats.value = currentChats
            
            // Save to local storage
            appDataStore.saveChats(_chats.value)
            
            _message.value = "Chat creado exitosamente"
            chatId
        } catch (e: Exception) {
            _message.value = "Error al crear chat: ${e.message}"
            ""
        }
    }

    fun getOrCreateChatWithUser(currentUserId: String, otherUserId: String): String {
        val existingChat = _chats.value.find { chat ->
            chat.participants.contains(currentUserId) && chat.participants.contains(otherUserId)
        }
        return existingChat?.id ?: ""
    }

    // Message operations
    fun loadMessages(chatId: String) {
        _currentChatId.value = chatId
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val allMessages = appDataStore.loadMessages()
                _messages.value = allMessages.filter { it.conversationId == chatId }
            } catch (e: Exception) {
                _message.value = "Error al cargar mensajes: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        attachmentUri: Uri? = null
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val message = Message(
                    conversationId = chatId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    messageType = messageType,
                    attachmentUrl = attachmentUri?.toString(),
                    attachmentName = attachmentUri?.lastPathSegment ?: "",
                    timestamp = Timestamp.now()
                )

                // Add message to local list immediately
                val currentMessages = _messages.value.toMutableList()
                currentMessages.add(message)
                _messages.value = currentMessages
                
                // Save to local storage
                appDataStore.saveMessages(_messages.value)
                
                // Update chat last message
                updateChatLastMessage(chatId, message)
                
                _message.value = "Mensaje enviado"
            } catch (e: Exception) {
                _message.value = "Error al enviar mensaje: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                chatRepository.markMessageAsRead(messageId)
            } catch (e: Exception) {
                _message.value = "Error al marcar mensaje como leído"
            }
        }
    }

    fun deleteMessage(messageId: String, userId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(messageId, userId)
                _message.value = "Mensaje eliminado"
            } catch (e: Exception) {
                _message.value = "Error al eliminar mensaje: ${e.message}"
            }
        }
    }

    fun markMessageAsImportant(messageId: String, isImportant: Boolean) {
        viewModelScope.launch {
            try {
                chatRepository.markMessageAsImportant(messageId, isImportant)
            } catch (e: Exception) {
                _message.value = "Error al marcar mensaje como importante"
            }
        }
    }

    // Chatbot operations
    private suspend fun loadChatbotResponses() {
        try {
            val responses = chatRepository.getChatbotResponses()
            _chatbotResponses.value = responses
        } catch (e: Exception) {
            _message.value = "Error al cargar respuestas del chatbot"
        }
    }

    fun processChatbotMessage(userMessage: String): String {
        val responses = _chatbotResponses.value
        val lowerMessage = userMessage.lowercase()
        
        for (response in responses) {
            if (lowerMessage.contains(response.question.lowercase())) {
                return response.answer
            }
        }
        
        return "Déjame transferirte con un veterinario para ayudarte mejor."
    }

    fun addChatbotResponse(question: String, answer: String, category: String) {
        viewModelScope.launch {
            try {
                val response = ChatbotResponse(
                    question = question,
                    answer = answer,
                    category = category,
                    createdAt = Timestamp.now()
                )
                chatRepository.addChatbotResponse(response)
                loadChatbotResponses()
                _message.value = "Respuesta del chatbot agregada"
            } catch (e: Exception) {
                _message.value = "Error al agregar respuesta del chatbot"
            }
        }
    }

    fun updateChatbotResponse(response: ChatbotResponse) {
        viewModelScope.launch {
            try {
                chatRepository.updateChatbotResponse(response)
                loadChatbotResponses()
                _message.value = "Respuesta del chatbot actualizada"
            } catch (e: Exception) {
                _message.value = "Error al actualizar respuesta del chatbot"
            }
        }
    }

    fun deleteChatbotResponse(responseId: String) {
        viewModelScope.launch {
            try {
                chatRepository.deleteChatbotResponse(responseId)
                loadChatbotResponses()
                _message.value = "Respuesta del chatbot eliminada"
            } catch (e: Exception) {
                _message.value = "Error al eliminar respuesta del chatbot"
            }
        }
    }

    // Filter operations
    fun filterChatsByUSER(USERId: String) {
        viewModelScope.launch {
            try {
                val filteredChats = emptyList<Chat>() // Simular por ahora
                _chats.value = filteredChats
            } catch (e: Exception) {
                _message.value = "Error al filtrar chats por paciente"
            }
        }
    }

    fun filterChatsByAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                val filteredChats = chatRepository.getChatsByAppointment(appointmentId)
                _chats.value = filteredChats
            } catch (e: Exception) {
                _message.value = "Error al filtrar chats por cita"
            }
        }
    }

    fun clearFilters() {
        // Reload all chats for current user
        // This would need the current user ID to be passed
    }

    // Typing indicator
    fun setTyping(isTyping: Boolean) {
        _isTyping.value = isTyping
    }

    // Utility functions
    fun getAvailableUsers(currentUserId: String, currentUserRole: UserRole): List<User> {
        return when (currentUserRole) {
            UserRole.USER -> {
                createSampleUsers().filter { 
                    it.role == UserRole.VET || it.role == UserRole.VET_ASSISTANT 
                }
            }
            UserRole.VET, UserRole.VET_ASSISTANT -> {
                createSampleUsers().filter { 
                    it.role == UserRole.USER || it.role == UserRole.VET || it.role == UserRole.VET_ASSISTANT 
                }.filter { it.id != currentUserId }
            }
            UserRole.ADMIN -> {
                createSampleUsers().filter { it.id != currentUserId }
            }
            else -> emptyList()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun refreshChats(userId: String) {
        loadUserChats(userId)
    }

    private fun updateChatLastMessage(chatId: String, message: Message) {
        val currentChats = _chats.value.toMutableList()
        val chatIndex = currentChats.indexOfFirst { it.id == chatId }
        if (chatIndex != -1) {
            currentChats[chatIndex] = currentChats[chatIndex].copy(
                lastMessage = message,
                lastActivity = message.timestamp
            )
            _chats.value = currentChats
            viewModelScope.launch {
                appDataStore.saveChats(_chats.value)
            }
        }
    }

    private fun createSampleChats(): List<Chat> {
        return listOf(
            Chat(
                id = "chat_1",
                participants = listOf("user_USER_1", "user_vet_1"),
                lastMessage = Message(
                    id = "msg_1",
                    conversationId = "chat_1",
                    senderId = "user_USER_1",
                    receiverId = "user_vet_1",
                    content = "Hola, mi perro no está comiendo bien",
                    messageType = MessageType.TEXT,
                    timestamp = Timestamp.now()
                ),
                lastActivity = Timestamp.now(),
                isActive = true,
                createdAt = Timestamp.now()
            ),
            Chat(
                id = "chat_2",
                participants = listOf("user_USER_2", "user_vet_1"),
                lastMessage = Message(
                    id = "msg_2",
                    conversationId = "chat_2",
                    senderId = "user_USER_2",
                    receiverId = "user_vet_1",
                    content = "¿Cuándo es la próxima cita?",
                    messageType = MessageType.TEXT,
                    timestamp = Timestamp.now()
                ),
                lastActivity = Timestamp.now(),
                isActive = true,
                createdAt = Timestamp.now()
            )
        )
    }

    private fun createSampleUsers(): List<User> {
        return listOf(
            User(
                id = "user_USER_1",
                name = "María García",
                email = "maria@email.com",
                role = UserRole.USER,
                profileImageUrl = "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face"
            ),
            User(
                id = "user_USER_2",
                name = "Carlos López",
                email = "carlos@email.com",
                role = UserRole.USER,
                profileImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face"
            ),
            User(
                id = "user_vet_1",
                name = "Dr. Ana Martínez",
                email = "ana@veterinaria.com",
                role = UserRole.VET,
                profileImageUrl = "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=150&h=150&fit=crop&crop=face"
            ),
            User(
                id = "user_VET_ASSISTANT_1",
                name = "Luis Rodríguez",
                email = "luis@veterinaria.com",
                role = UserRole.VET_ASSISTANT,
                profileImageUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face"
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatViewModel? = null

        fun getInstance(context: Context): ChatViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatViewModel(context).also { INSTANCE = it }
            }
        }
    }
}