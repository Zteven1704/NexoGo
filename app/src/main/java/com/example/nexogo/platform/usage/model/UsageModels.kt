package com.example.nexogo.platform.usage.model

import com.google.firebase.Timestamp

/**
 * Immutable usage analytics event.
 * Path: companies/{companyId}/usage_events/{eventId}
 */
data class UsageEvent(
    val id: String = "",
    val companyId: String = "",
    val type: String = UsageEventType.OTHER,
    val userId: String = "",
    val resourceType: String = "",
    val resourceId: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    val occurredAt: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * Event type codes stored as strings for Firestore stability.
 */
object UsageEventType {
    const val LOGIN = "login"
    const val CLIENT_CREATED = "client_created"
    const val RECORD_CREATED = "record_created"
    const val DOCUMENT_UPLOADED = "document_uploaded"
    const val ACTIVE_USER = "active_user"
    const val OTHER = "other"
}

/**
 * Aggregated counters for a period (typically one calendar day UTC).
 * Path: companies/{companyId}/usage_metrics/{periodKey}
 * periodKey format: day_yyyy-MM-dd
 */
data class UsageMetrics(
    val id: String = "",
    val companyId: String = "",
    val periodKey: String = "",
    val periodType: String = UsagePeriodType.DAY,
    val loginCount: Long = 0,
    val clientsCreated: Long = 0,
    val recordsCreated: Long = 0,
    val documentsUploaded: Long = 0,
    /** Distinct users marked active in this period. */
    val activeUsers: Long = 0,
    val updatedAt: Timestamp = Timestamp.now()
)

object UsagePeriodType {
    const val DAY = "day"
    const val MONTH = "month"
}

/**
 * Marker that [userId] was active on [dayKey] (yyyy-MM-dd).
 * Path: companies/{companyId}/usage_active_users/{dayKey}_{userId}
 */
data class UsageActiveUser(
    val id: String = "",
    val companyId: String = "",
    val userId: String = "",
    val dayKey: String = "",
    val lastSeenAt: Timestamp = Timestamp.now()
)
