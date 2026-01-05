package com.example.nexogo.modules.chat

import android.net.Uri
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.Message
import com.example.nexogo.core.models.MessageAttachment
import com.example.nexogo.core.models.AttachmentType
import com.example.nexogo.core.models.Conversation
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.util.*

/**
 * Repositorio para gestión de chat con Firebase
 */
class ChatRepository(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {
    
    /**
     * Obtener o crear una conversación entre dos usuarios
     */
    suspend fun getOrCreateConversation(
        currentUserId: String,
        targetUserId: String,
        currentUserName: String,
        targetUserName: String,
        currentUserRole: String,
        targetUserRole: String
    ): Result<Conversation> {
        return try {
            // Crear ID de conversación ordenado
            val conversationId = if (currentUserId < targetUserId) {
                "${currentUserId}_${targetUserId}"
            } else {
                "${targetUserId}_${currentUserId}"
            }
            
            // Verificar si la conversación ya existe
            val existingConversation = firebaseRepository.getDocument("conversations", conversationId)
            if (existingConversation.isSuccess) {
                val data = existingConversation.getOrNull()
                if (data != null) {
                    val conversation = Conversation(
                        id = conversationId,
                        participants = (data["participants"] as? List<String>) ?: listOf(currentUserId, targetUserId),
                        participantNames = (data["participantNames"] as? Map<String, String>) ?: mapOf(
                            currentUserId to currentUserName,
                            targetUserId to targetUserName
                        ),
                        participantRoles = (data["participantRoles"] as? Map<String, String>) ?: mapOf(
                            currentUserId to currentUserRole,
                            targetUserId to targetUserRole
                        ),
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                        lastMessageTimestamp = data["lastMessageTimestamp"] as? Timestamp ?: Timestamp.now(),
                        unreadCount = (data["unreadCount"] as? Map<String, Int>) ?: emptyMap(),
                        isActive = data["isActive"] as? Boolean ?: true,
                        createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                        updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
                    )
                    return Result.success(conversation)
                }
            }
            
            // Crear nueva conversación
            val conversation = Conversation(
                id = conversationId,
                participants = listOf(currentUserId, targetUserId),
                participantNames = mapOf(
                    currentUserId to currentUserName,
                    targetUserId to targetUserName
                ),
                participantRoles = mapOf(
                    currentUserId to currentUserRole,
                    targetUserId to targetUserRole
                ),
                lastMessage = "",
                lastMessageSenderId = "",
                lastMessageTimestamp = Timestamp.now(),
                unreadCount = mapOf(
                    currentUserId to 0,
                    targetUserId to 0
                ),
                isActive = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
                val conversationData = mapOf<String, Any>(
                    "id" to conversation.id,
                    "participants" to conversation.participants,
                    "participantNames" to conversation.participantNames,
                    "participantRoles" to conversation.participantRoles,
                    "lastMessage" to conversation.lastMessage,
                    "lastMessageSenderId" to conversation.lastMessageSenderId,
                    "lastMessageTimestamp" to conversation.lastMessageTimestamp,
                    "unreadCount" to conversation.unreadCount,
                    "isActive" to conversation.isActive,
                    "createdAt" to conversation.createdAt,
                    "updatedAt" to conversation.updatedAt
                )
            
            val result = firebaseRepository.createDocument("conversations", conversationId, conversationData)
            if (result.isSuccess) {
                Result.success(conversation)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error creando conversación"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en getOrCreateConversation: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Enviar mensaje
     */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        receiverId: String,
        text: String,
        attachments: List<MessageAttachment> = emptyList()
    ): Result<Message> {
        return try {
            val messageId = firebaseRepository.generateId()
            val message = Message(
                id = messageId,
                chatId = conversationId,
                senderId = senderId,
                senderName = senderName,
                text = text,
                attachments = attachments,
                timestamp = Timestamp.now(),
                isRead = false,
                isDelivered = true
            )
            
            val messageData = mapOf<String, Any>(
                "id" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "text" to message.text,
                "attachments" to message.attachments.map { attachment ->
                    mapOf(
                        "url" to attachment.url,
                        "type" to attachment.type.name,
                        "name" to attachment.name,
                        "size" to attachment.size
                    )
                },
                "timestamp" to message.timestamp,
                "isRead" to message.isRead,
                "isDelivered" to message.isDelivered
            )
            
            // Guardar mensaje
            val messageResult = firebaseRepository.createDocument("messages", messageId, messageData)
            if (messageResult.isSuccess) {
                // Actualizar conversación con último mensaje
                val updateData = mapOf<String, Any>(
                    "lastMessage" to text,
                    "lastMessageSenderId" to senderId,
                    "lastMessageTimestamp" to Timestamp.now(),
                    "updatedAt" to Timestamp.now()
                )
                firebaseRepository.updateDocument("conversations", conversationId, updateData)
                
                Log.d("NEXOGO_CHAT", "Mensaje enviado: $messageId")
                Result.success(message)
            } else {
                Result.failure(messageResult.exceptionOrNull() ?: Exception("Error enviando mensaje"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en sendMessage: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener mensajes de una conversación
     */
    suspend fun getMessages(conversationId: String): Result<List<Message>> {
        return try {
            val result = firebaseRepository.getCollection("messages")
            if (result.isSuccess) {
                val messagesData = result.getOrNull() ?: emptyList()
                val messages = messagesData
                    .filter { data -> data["chatId"] == conversationId }
                    .mapNotNull { data ->
                        try {
                            val attachments = (data["attachments"] as? List<Map<String, Any>>)?.map { attachmentData ->
                                MessageAttachment(
                                    url = attachmentData["url"] as? String ?: "",
                                    type = try {
                                        AttachmentType.valueOf(attachmentData["type"] as? String ?: "IMAGE")
                                    } catch (e: Exception) {
                                        AttachmentType.IMAGE
                                    },
                                    name = attachmentData["name"] as? String ?: "",
                                    size = (attachmentData["size"] as? Number)?.toLong() ?: 0
                                )
                            } ?: emptyList()
                            
                            Message(
                                id = data["id"] as? String ?: "",
                                chatId = data["chatId"] as? String ?: "",
                                senderId = data["senderId"] as? String ?: "",
                                senderName = data["senderName"] as? String ?: "",
                                text = data["text"] as? String ?: "",
                                attachments = attachments,
                                timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                                isRead = data["isRead"] as? Boolean ?: false,
                                isDelivered = data["isDelivered"] as? Boolean ?: true
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_CHAT", "Error parseando mensaje: ${e.message}")
                            null
                        }
                    }
                    .sortedBy { it.timestamp }
                
                Result.success(messages)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error obteniendo mensajes"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en getMessages: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener conversaciones del usuario
     */
    suspend fun getUserConversations(userId: String): Result<List<Conversation>> {
        return try {
            val result = firebaseRepository.getCollection("conversations")
            if (result.isSuccess) {
                val conversationsData = result.getOrNull() ?: emptyList()
                val conversations = conversationsData
                    .filter { data ->
                        val participants = data["participants"] as? List<String> ?: emptyList()
                        participants.contains(userId)
                    }
                    .mapNotNull { data ->
                        try {
                            Conversation(
                                id = data["id"] as? String ?: "",
                                participants = (data["participants"] as? List<String>) ?: emptyList(),
                                participantNames = (data["participantNames"] as? Map<String, String>) ?: emptyMap(),
                                participantRoles = (data["participantRoles"] as? Map<String, String>) ?: emptyMap(),
                                lastMessage = data["lastMessage"] as? String ?: "",
                                lastMessageSenderId = data["lastMessageSenderId"] as? String ?: "",
                                lastMessageTimestamp = data["lastMessageTimestamp"] as? Timestamp ?: Timestamp.now(),
                                unreadCount = (data["unreadCount"] as? Map<String, Int>) ?: emptyMap(),
                                isActive = data["isActive"] as? Boolean ?: true,
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_CHAT", "Error parseando conversación: ${e.message}")
                            null
                        }
                    }
                    .sortedByDescending { it.lastMessageTimestamp }
                
                Result.success(conversations)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error obteniendo conversaciones"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en getUserConversations: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Marcar mensaje como leído
     */
    suspend fun markMessageAsRead(conversationId: String, messageId: String): Result<Unit> {
        return try {
            val updateData = mapOf<String, Any>(
                "isRead" to true
            )
            val result = firebaseRepository.updateDocument("messages", messageId, updateData)
            if (result.isSuccess) {
                Log.d("NEXOGO_CHAT", "Mensaje marcado como leído: $messageId")
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error marcando mensaje como leído"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en markMessageAsRead: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar mensaje
     */
    suspend fun deleteMessage(conversationId: String, messageId: String): Result<Unit> {
        return try {
            val result = firebaseRepository.deleteDocument("messages", messageId)
            if (result.isSuccess) {
                Log.d("NEXOGO_CHAT", "Mensaje eliminado: $messageId")
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error eliminando mensaje"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en deleteMessage: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Subir archivo multimedia
     */
    suspend fun uploadMedia(
        uri: Uri,
        conversationId: String,
        mediaType: AttachmentType,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        return try {
            val fileExtension = when (mediaType) {
                AttachmentType.IMAGE -> "jpg"
                AttachmentType.VIDEO -> "mp4"
                AttachmentType.AUDIO -> "m4a"
                AttachmentType.DOCUMENT -> "pdf"
                AttachmentType.PDF -> "pdf"
            }
            
            val filePath = "chats/$conversationId/${mediaType.name.lowercase()}/${UUID.randomUUID()}.$fileExtension"
            val result = firebaseRepository.uploadFile(filePath, uri)
            
            if (result.isSuccess) {
                val url = result.getOrNull() ?: ""
                Log.d("NEXOGO_CHAT", "Archivo subido: $url")
                Result.success(url)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error subiendo archivo"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en uploadMedia: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener usuarios disponibles para chat
     */
    suspend fun getAvailableUsers(currentUserId: String): Result<List<Map<String, Any>>> {
        return try {
            val result = firebaseRepository.getCollection("users")
            if (result.isSuccess) {
                val usersData = result.getOrNull() ?: emptyList()
                val availableUsers = usersData
                    .filter { it["uid"] != currentUserId }
                    .map { userData ->
                        mapOf(
                            "uid" to (userData["uid"] as? String ?: ""),
                            "name" to (userData["name"] as? String ?: ""),
                            "role" to (userData["role"] as? String ?: ""),
                            "profileImageUrl" to (userData["profileImageUrl"] as? String ?: "")
                        )
                    }
                
                Result.success(availableUsers)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Error obteniendo usuarios"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Error en getAvailableUsers: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener pacientes disponibles para chat
     */
    suspend fun getAvailablePatients(currentUserId: String): Result<List<Map<String, Any>>> {
        return try {
            Log.d("NEXOGO_CHAT", "=== INICIANDO CARGA DE PACIENTES ===")
            Log.d("NEXOGO_CHAT", "currentUserId: $currentUserId")
            
            val result = firebaseRepository.getCollection("patients")
            Log.d("NEXOGO_CHAT", "Resultado de getCollection: ${result.isSuccess}")
            
            if (result.isSuccess) {
                val patientsData = result.getOrNull() ?: emptyList()
                Log.d("NEXOGO_CHAT", "Datos de pacientes obtenidos: ${patientsData.size}")
                
                if (patientsData.isNotEmpty()) {
                    Log.d("NEXOGO_CHAT", "Primer paciente: ${patientsData.first()}")
                }
                
                val availablePatients = patientsData
                    .map { patientData ->
                        val age = (patientData["age"] as? Number)?.toInt() ?: 0
                        val weight = (patientData["weight"] as? Number)?.toDouble() ?: 0.0
                        
                        val patient = mapOf<String, Any>(
                            "uid" to (patientData["id"] as? String ?: ""),
                            "name" to (patientData["name"] as? String ?: ""),
                            "role" to "PATIENT",
                            "profileImageUrl" to (patientData["imageUrl"] as? String ?: ""),
                            "ownerId" to (patientData["ownerId"] as? String ?: ""),
                            "ownerName" to (patientData["ownerName"] as? String ?: ""),
                            "species" to (patientData["species"] as? String ?: ""),
                            "breed" to (patientData["breed"] as? String ?: ""),
                            "age" to age,
                            "weight" to weight,
                            "color" to (patientData["color"] as? String ?: ""),
                            "gender" to (patientData["gender"] as? String ?: "")
                        )
                        Log.d("NEXOGO_CHAT", "Paciente procesado: ${patient["name"]} - ${patient["species"]}")
                        patient
                    }
                
                Log.d("NEXOGO_CHAT", "Pacientes disponibles: ${availablePatients.size}")
                Result.success(availablePatients)
            } else {
                val error = result.exceptionOrNull()
                Log.e("NEXOGO_CHAT", "Error obteniendo pacientes: ${error?.message}")
                Result.failure(error ?: Exception("Error obteniendo pacientes"))
            }
        } catch (e: Exception) {
            Log.e("NEXOGO_CHAT", "Excepción en getAvailablePatients: ${e.message}", e)
            Result.failure(e)
        }
    }
}
