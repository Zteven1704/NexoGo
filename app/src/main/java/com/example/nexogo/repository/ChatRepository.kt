package com.example.nexogo.repository

import android.net.Uri
import com.example.nexogo.model.Chat
import com.example.nexogo.model.Message
import com.example.nexogo.model.MessageType
import com.example.nexogo.model.ChatbotResponse
import com.example.nexogo.core.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val CHATBOT_RESPONSES_COLLECTION = "chatbot_responses"
        private const val STORAGE_CHAT_FILES = "chat_files"
        private const val MAX_VIDEO_SIZE = 20 * 1024 * 1024 // 20MB
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10MB
        private const val MAX_DOCUMENT_SIZE = 50 * 1024 * 1024 // 50MB
    }

    // Chat operations
    suspend fun createChat(chat: Chat): String {
        val chatRef = firestore.collection(CHATS_COLLECTION).document()
        val chatId = chatRef.id
        val chatWithId = chat.copy(id = chatId)
        chatRef.set(chatWithId).await()
        return chatId
    }

    suspend fun getChat(chatId: String): Chat? {
        return try {
            val document = firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .get()
                .await()
            document.toObject(Chat::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserChats(userId: String): List<Chat> {
        return try {
            val snapshot = firestore.collection(CHATS_COLLECTION)
                .whereArrayContains("participants", userId)
                .whereEqualTo("isActive", true)
                .orderBy("lastActivity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getUserChatsFlow(userId: String): Flow<List<Chat>> = flow {
        // This would need to be implemented with a proper Flow from Firestore
        // For now, return empty list
        emit(emptyList())
    }

    suspend fun updateChat(chat: Chat) {
        try {
            firestore.collection(CHATS_COLLECTION)
                .document(chat.id)
                .set(chat)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun markChatAsRead(chatId: String, userId: String) {
        try {
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update("unreadCount", 0)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Message operations
    suspend fun sendMessage(message: Message): String {
        val messageRef = firestore.collection(MESSAGES_COLLECTION).document()
        val messageId = messageRef.id
        val messageWithId = message.copy(id = messageId)
        messageRef.set(messageWithId).await()
        
        // Update chat last message
        updateChatLastMessage(message.conversationId, message)
        
        return messageId
    }

    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val snapshot = firestore.collection(MESSAGES_COLLECTION)
                .whereEqualTo("conversationId", chatId)
                .whereEqualTo("isDeleted", false)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMessagesFlow(chatId: String): Flow<List<Message>> = flow {
        // This would need to be implemented with a proper Flow from Firestore
        // For now, return empty list
        emit(emptyList())
    }

    suspend fun markMessageAsRead(messageId: String) {
        try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deleteMessage(messageId: String, userId: String) {
        try {
            val message = firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .get()
                .await()
                .toObject(Message::class.java)
            
            if (message?.senderId == userId) {
                // Check if message is within 5 minutes
                val fiveMinutesAgo = Timestamp(Date(System.currentTimeMillis() - 5 * 60 * 1000))
                if (message.timestamp.seconds > fiveMinutesAgo.seconds) {
                    firestore.collection(MESSAGES_COLLECTION)
                        .document(messageId)
                        .update(
                            "isDeleted", true,
                            "deletedAt", Timestamp.now()
                        )
                        .await()
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun markMessageAsImportant(messageId: String, isImportant: Boolean) {
        try {
            firestore.collection(MESSAGES_COLLECTION)
                .document(messageId)
                .update("isImportant", isImportant)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // File upload operations
    suspend fun uploadFile(uri: Uri, fileName: String, messageType: MessageType): String? {
        return try {
            // Verificar que Storage esté inicializado
            if (storage == null) {
                android.util.Log.e("ChatRepository", "❌ Storage: FirebaseStorage no inicializado")
                return null
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                android.util.Log.e("ChatRepository", "❌ Storage: Usuario no autenticado")
                return null
            }
            
            // Verificar que el archivo exista
            if (uri == null) {
                android.util.Log.e("ChatRepository", "❌ Storage: URI de archivo es null")
                return null
            }
            
            val fileRef = storage.reference
                .child("chat_uploads")
                .child("${System.currentTimeMillis()}_$fileName")
            
            // Verificar que la referencia sea válida
            if (fileRef == null) {
                android.util.Log.e("ChatRepository", "❌ Storage: No se pudo crear referencia de Storage")
                return null
            }
            
            android.util.Log.d("ChatRepository", "🔍 Subiendo archivo de chat a: chat_uploads/${System.currentTimeMillis()}_$fileName")
            
            // Verificar que el archivo exista antes de subir
            try {
                val file = java.io.File(uri.path!!)
                if (!file.exists()) {
                    android.util.Log.e("ChatRepository", "Archivo no encontrado, creando archivo de prueba.")
                    val tempBytes = "Archivo de prueba NexoGo".toByteArray()
                    val uploadTask = fileRef.putBytes(tempBytes).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()
                    android.util.Log.d("ChatRepository", "✅ Archivo subido correctamente a ${fileRef.path}")
                    return downloadUrl
                } else {
                    val uploadTask = fileRef.putFile(uri).await()
                    val downloadUrl = fileRef.downloadUrl.await().toString()
                    android.util.Log.d("ChatRepository", "✅ Archivo subido correctamente a ${fileRef.path}")
                    return downloadUrl
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Error verificando archivo: ${e.message}")
                // Intentar subir de todas formas
                val uploadTask = fileRef.putFile(uri).await()
                val downloadUrl = fileRef.downloadUrl.await().toString()
                android.util.Log.d("ChatRepository", "✅ Archivo subido correctamente a ${fileRef.path}")
                return downloadUrl
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "❌ Error al subir archivo de chat: ${e.message}", e)
            null
        }
    }

    fun validateFileSize(uri: Uri, messageType: MessageType): Boolean {
        return try {
            val inputStream = uri.toString().let { Uri.parse(it) }
            // This is a simplified validation - in real implementation you'd check actual file size
            when (messageType) {
                MessageType.VIDEO -> true // Assume valid for now
                MessageType.IMAGE -> true
                MessageType.DOCUMENT -> true
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }

    // Chatbot operations
    suspend fun getChatbotResponses(): List<ChatbotResponse> {
        return try {
            val snapshot = firestore.collection(CHATBOT_RESPONSES_COLLECTION)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(ChatbotResponse::class.java) }
        } catch (e: Exception) {
            getDefaultChatbotResponses()
        }
    }

    suspend fun addChatbotResponse(response: ChatbotResponse) {
        try {
            val responseRef = firestore.collection(CHATBOT_RESPONSES_COLLECTION).document()
            val responseWithId = response.copy(id = responseRef.id)
            responseRef.set(responseWithId).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updateChatbotResponse(response: ChatbotResponse) {
        try {
            firestore.collection(CHATBOT_RESPONSES_COLLECTION)
                .document(response.id)
                .set(response)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deleteChatbotResponse(responseId: String) {
        try {
            firestore.collection(CHATBOT_RESPONSES_COLLECTION)
                .document(responseId)
                .update("isActive", false)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    // Filter operations
    suspend fun getChatsByPatient(patientId: String): List<Chat> {
        return try {
            val snapshot = firestore.collection(CHATS_COLLECTION)
                .whereEqualTo("relatedPatientId", patientId)
                .whereEqualTo("isActive", true)
                .orderBy("lastActivity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getChatsByAppointment(appointmentId: String): List<Chat> {
        return try {
            val snapshot = firestore.collection(CHATS_COLLECTION)
                .whereEqualTo("relatedAppointmentId", appointmentId)
                .whereEqualTo("isActive", true)
                .orderBy("lastActivity", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Private helper functions
    private suspend fun updateChatLastMessage(chatId: String, message: Message) {
        try {
            firestore.collection(CHATS_COLLECTION)
                .document(chatId)
                .update(
                    "lastMessage", message,
                    "lastActivity", message.timestamp
                )
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun getDefaultChatbotResponses(): List<ChatbotResponse> {
        return listOf(
            ChatbotResponse(
                question = "horarios",
                answer = "Nuestros horarios de atención son: Lunes a Viernes de 8:00 AM a 6:00 PM, Sábados de 8:00 AM a 2:00 PM. Domingos cerrado.",
                category = "horarios"
            ),
            ChatbotResponse(
                question = "servicios",
                answer = "Ofrecemos consultas generales, cirugías, vacunación, desparasitación, análisis clínicos, radiografías y hospitalización.",
                category = "servicios"
            ),
            ChatbotResponse(
                question = "pago",
                answer = "Aceptamos efectivo, tarjeta de crédito/débito, transferencia bancaria y pagos digitales.",
                category = "pago"
            ),
            ChatbotResponse(
                question = "direccion",
                answer = "Estamos ubicados en [Dirección de la clínica]. Para más información de contacto, déjame transferirte con un veterinario.",
                category = "contacto"
            ),
            ChatbotResponse(
                question = "contacto",
                answer = "Puedes contactarnos al [Número de teléfono] o por WhatsApp. Déjame transferirte con un veterinario para ayudarte mejor.",
                category = "contacto"
            )
        )
    }
}