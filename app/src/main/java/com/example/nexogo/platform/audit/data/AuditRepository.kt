package com.example.nexogo.platform.audit.data

import com.example.nexogo.platform.audit.ip.AuditIpProvider
import com.example.nexogo.platform.audit.ip.EmptyAuditIpProvider
import com.example.nexogo.platform.audit.model.AuditAction
import com.example.nexogo.platform.audit.model.AuditEvent
import com.example.nexogo.platform.audit.model.AuditResourceTypes
import com.example.nexogo.platform.audit.model.PLATFORM_AUDIT_COMPANY
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.data.TenantAwareRepository
import com.example.nexogo.platform.tenant.data.TenantCollections
import com.example.nexogo.platform.tenant.guard.TenantIsolationGuard
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Writes and queries company-scoped audit events.
 * Failures in write should be swallowed by callers (audit must not break UX).
 */
class AuditRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ipProvider: AuditIpProvider = EmptyAuditIpProvider
) : TenantAwareRepository(firestore) {

    override val collectionName: String = TenantCollections.AUDIT_LOGS

    private fun resolveCompanyId(explicit: String?): String {
        val fromArg = explicit?.takeIf { it.isNotBlank() }
        val fromCtx = TenantContext.companyId?.takeIf { it.isNotBlank() }
        return fromArg ?: fromCtx ?: PLATFORM_AUDIT_COMPANY
    }

    /**
     * Low-level append. Prefer [record] helpers for standard actions.
     */
    suspend fun append(event: AuditEvent): Result<AuditEvent> {
        return try {
            val companyId = resolveCompanyId(event.companyId.takeIf { it.isNotBlank() })
            if (companyId != PLATFORM_AUDIT_COMPANY) {
                TenantIsolationGuard.requireCompanyId(companyId)
            }
            val now = Timestamp.now()
            val ip = event.ipAddress.ifBlank { runCatching { ipProvider.currentIp() }.getOrDefault("") }
            val id = event.id.ifBlank { newId("aud") }
            val toSave = event.copy(
                id = id,
                companyId = companyId,
                ipAddress = ip,
                occurredAt = if (event.occurredAt.seconds == 0L) now else event.occurredAt,
                createdAt = now,
                updatedAt = now
            )
            if (companyId == PLATFORM_AUDIT_COMPANY) {
                firestore.collection("platform")
                    .document("audit")
                    .collection(TenantCollections.AUDIT_LOGS)
                    .document(id)
                    .set(toSave)
                    .await()
            } else {
                collection(companyId).document(id).set(toSave).await()
            }
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun record(
        action: AuditAction,
        userId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        userEmail: String = "",
        resourceType: String = AuditResourceTypes.OTHER,
        resourceId: String = "",
        resourceLabel: String = "",
        summary: String = "",
        metadata: Map<String, Any> = emptyMap(),
        ipAddress: String = "",
        success: Boolean = true,
        errorMessage: String = ""
    ): Result<AuditEvent> {
        val autoSummary = summary.ifBlank {
            defaultSummary(action, resourceType, resourceLabel.ifBlank { resourceId })
        }
        return append(
            AuditEvent(
                companyId = companyId.orEmpty(),
                userId = userId,
                userDisplayName = userDisplayName,
                userEmail = userEmail,
                action = action,
                resourceType = resourceType,
                resourceId = resourceId,
                resourceLabel = resourceLabel,
                summary = autoSummary,
                metadata = metadata,
                ipAddress = ipAddress,
                success = success,
                errorMessage = errorMessage
            )
        )
    }

    suspend fun recordLogin(
        userId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        userEmail: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<AuditEvent> = record(
        action = AuditAction.LOGIN,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        userEmail = userEmail,
        resourceType = AuditResourceTypes.AUTH,
        resourceId = userId,
        summary = "Inicio de sesión",
        metadata = metadata
    )

    suspend fun recordLogout(
        userId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        userEmail: String = ""
    ): Result<AuditEvent> = record(
        action = AuditAction.LOGOUT,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        userEmail = userEmail,
        resourceType = AuditResourceTypes.AUTH,
        resourceId = userId,
        summary = "Cierre de sesión"
    )

    suspend fun recordCreate(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<AuditEvent> = record(
        action = AuditAction.CREATE,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        resourceType = resourceType,
        resourceId = resourceId,
        resourceLabel = resourceLabel,
        metadata = metadata
    )

    suspend fun recordUpdate(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<AuditEvent> = record(
        action = AuditAction.UPDATE,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        resourceType = resourceType,
        resourceId = resourceId,
        resourceLabel = resourceLabel,
        metadata = metadata
    )

    suspend fun recordDelete(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<AuditEvent> = record(
        action = AuditAction.DELETE,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        resourceType = resourceType,
        resourceId = resourceId,
        resourceLabel = resourceLabel,
        metadata = metadata
    )

    suspend fun recordDownload(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = null,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<AuditEvent> = record(
        action = AuditAction.DOWNLOAD,
        userId = userId,
        companyId = companyId,
        userDisplayName = userDisplayName,
        resourceType = resourceType,
        resourceId = resourceId,
        resourceLabel = resourceLabel,
        metadata = metadata
    )

    suspend fun listEvents(
        companyId: String,
        action: AuditAction? = null,
        userId: String? = null,
        resourceType: String? = null,
        limit: Long = 100
    ): Result<List<AuditEvent>> {
        return try {
            val cid = if (companyId == PLATFORM_AUDIT_COMPANY) {
                PLATFORM_AUDIT_COMPANY
            } else {
                TenantIsolationGuard.requireCompanyId(companyId)
            }
            var query: Query = if (cid == PLATFORM_AUDIT_COMPANY) {
                firestore.collection("platform")
                    .document("audit")
                    .collection(TenantCollections.AUDIT_LOGS)
            } else {
                collection(cid)
            }
            // Prefer simple query + in-memory filter to avoid composite index requirements in foundation.
            val snap = query.limit(limit * 3).get().await()
            var events = snap.documents.mapNotNull { it.toObject(AuditEvent::class.java) }
            if (action != null) events = events.filter { it.action == action }
            if (!userId.isNullOrBlank()) events = events.filter { it.userId == userId }
            if (!resourceType.isNullOrBlank()) events = events.filter { it.resourceType == resourceType }
            Result.success(
                events.sortedByDescending { it.occurredAt.seconds }.take(limit.toInt())
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvent(companyId: String, eventId: String): Result<AuditEvent?> {
        return try {
            val cid = resolveCompanyId(companyId)
            val snap = if (cid == PLATFORM_AUDIT_COMPANY) {
                firestore.collection("platform")
                    .document("audit")
                    .collection(TenantCollections.AUDIT_LOGS)
                    .document(eventId)
                    .get()
                    .await()
            } else {
                document(cid, eventId).get().await()
            }
            Result.success(snap.toObject(AuditEvent::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun defaultSummary(action: AuditAction, resourceType: String, label: String): String {
        val target = label.ifBlank { resourceType }
        return when (action) {
            AuditAction.LOGIN -> "Inicio de sesión"
            AuditAction.LOGOUT -> "Cierre de sesión"
            AuditAction.CREATE -> "Creación: $target"
            AuditAction.UPDATE -> "Edición: $target"
            AuditAction.DELETE -> "Eliminación: $target"
            AuditAction.DOWNLOAD -> "Descarga: $target"
            AuditAction.OTHER -> "Acción: $target"
        }
    }

    private fun newId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(20)}"
}
