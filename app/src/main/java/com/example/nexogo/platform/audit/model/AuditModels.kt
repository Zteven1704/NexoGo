package com.example.nexogo.platform.audit.model

import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.google.firebase.Timestamp

/**
 * Immutable audit trail entry.
 * Path: companies/{companyId}/audit_logs/{eventId}
 *
 * Auth events without an active company may use [PLATFORM_AUDIT_COMPANY]
 * as a sentinel tenant for platform-level logs.
 */
data class AuditEvent(
    override val id: String = "",
    override val companyId: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val userEmail: String = "",
    val action: AuditAction = AuditAction.OTHER,
    val resourceType: String = "",
    val resourceId: String = "",
    val resourceLabel: String = "",
    val summary: String = "",
    val metadata: Map<String, Any> = emptyMap(),
    /** Client public IP when provided by backend / proxy; empty if unavailable on-device. */
    val ipAddress: String = "",
    val userAgent: String = "",
    val deviceId: String = "",
    val success: Boolean = true,
    val errorMessage: String = "",
    val occurredAt: Timestamp = Timestamp.now(),
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now()
) : TenantAwareEntity

enum class AuditAction {
    LOGIN,
    LOGOUT,
    CREATE,
    UPDATE,
    DELETE,
    DOWNLOAD,
    OTHER
}

object AuditResourceTypes {
    const val AUTH = "auth"
    const val CLIENT = "client"
    const val RECORD = "record"
    const val DOCUMENT = "document"
    const val TASK = "task"
    const val OPPORTUNITY = "opportunity"
    const val LEAD = "lead"
    const val CONVERSATION = "conversation"
    const val USER = "user"
    const val COMPANY = "company"
    const val OTHER = "other"
}

/** Sentinel companyId for auth events before / without tenant bind. */
const val PLATFORM_AUDIT_COMPANY = "_platform"
