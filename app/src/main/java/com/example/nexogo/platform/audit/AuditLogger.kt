package com.example.nexogo.platform.audit

import com.example.nexogo.platform.audit.data.AuditRepository
import com.example.nexogo.platform.audit.model.AuditEvent
import com.example.nexogo.platform.tenant.context.TenantContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fire-and-forget audit facade. Never throws to callers.
 */
object AuditLogger {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var repository: AuditRepository = AuditRepository()

    fun configure(repository: AuditRepository) {
        this.repository = repository
    }

    fun login(
        userId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        userEmail: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) {
        enqueue {
            repository.recordLogin(userId, companyId, userDisplayName, userEmail, metadata)
        }
    }

    fun logout(
        userId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        userEmail: String = ""
    ) {
        enqueue {
            repository.recordLogout(userId, companyId, userDisplayName, userEmail)
        }
    }

    fun create(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) {
        enqueue {
            repository.recordCreate(
                userId, resourceType, resourceId, companyId, userDisplayName, resourceLabel, metadata
            )
        }
    }

    fun update(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) {
        enqueue {
            repository.recordUpdate(
                userId, resourceType, resourceId, companyId, userDisplayName, resourceLabel, metadata
            )
        }
    }

    fun delete(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) {
        enqueue {
            repository.recordDelete(
                userId, resourceType, resourceId, companyId, userDisplayName, resourceLabel, metadata
            )
        }
    }

    fun download(
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String? = TenantContext.companyId,
        userDisplayName: String = "",
        resourceLabel: String = "",
        metadata: Map<String, Any> = emptyMap()
    ) {
        enqueue {
            repository.recordDownload(
                userId, resourceType, resourceId, companyId, userDisplayName, resourceLabel, metadata
            )
        }
    }

    private fun enqueue(block: suspend () -> Result<AuditEvent>) {
        scope.launch {
            try {
                block()
            } catch (_: Exception) {
                // Audit must never crash the app
            }
        }
    }
}
