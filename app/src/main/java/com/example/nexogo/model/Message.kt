package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val messageType: MessageType = MessageType.TEXT,
    val attachmentUrl: String? = null,
    val attachmentName: String = "",
    val attachmentSize: Long = 0, // Size in bytes
    val isRead: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT, // Estado del mensaje
    val readAt: Timestamp? = null, // Fecha de lectura
    val timestamp: Timestamp = Timestamp.now(),
    val replyToMessageId: String = "",
    val isDeleted: Boolean = false,
    val deletedAt: Timestamp? = null,
    val isImportant: Boolean = false
)

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val lastActivity: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val chatType: ChatType = ChatType.ONE_TO_ONE,
    val relatedPatientId: String? = null, // For patient-related chats
    val relatedAppointmentId: String? = null, // For appointment-related chats
    val isImportant: Boolean = false,
    val unreadCount: Int = 0
)

data class ChatbotResponse(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val category: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    SYSTEM
}

enum class ChatType {
    ONE_TO_ONE,
    GROUP,
    PATIENT_CHAT,
    APPOINTMENT_CHAT
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}
