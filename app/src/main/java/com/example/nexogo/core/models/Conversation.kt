package com.example.nexogo.core.models

import com.google.firebase.Timestamp

/**
 * Modelo de conversación para el chat
 */
data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantRoles: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val unreadCount: Map<String, Int> = emptyMap(),
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

