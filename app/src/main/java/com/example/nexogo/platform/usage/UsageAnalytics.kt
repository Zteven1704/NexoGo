package com.example.nexogo.platform.usage

import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.data.UsageRepository
import com.example.nexogo.platform.usage.model.UsageEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fire-and-forget usage analytics facade. Never throws to callers.
 */
object UsageAnalytics {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var repository: UsageRepository = UsageRepository()

    fun configure(repository: UsageRepository) {
        this.repository = repository
    }

    fun login(
        userId: String,
        companyId: String? = TenantContext.companyId,
        metadata: Map<String, Any> = emptyMap()
    ) {
        if (userId.isBlank()) return
        enqueue {
            val cid = companyId?.takeIf { it.isNotBlank() } ?: TenantContext.companyId
            if (cid.isNullOrBlank()) return@enqueue
            repository.recordEvent(
                type = UsageEventType.LOGIN,
                userId = userId,
                companyId = cid,
                resourceType = "auth",
                metadata = metadata
            )
        }
    }

    fun clientCreated(
        userId: String,
        clientId: String,
        companyId: String? = TenantContext.companyId,
        metadata: Map<String, Any> = emptyMap()
    ) {
        trackCreate(UsageEventType.CLIENT_CREATED, userId, "client", clientId, companyId, metadata)
    }

    fun recordCreated(
        userId: String,
        recordId: String,
        companyId: String? = TenantContext.companyId,
        metadata: Map<String, Any> = emptyMap()
    ) {
        trackCreate(UsageEventType.RECORD_CREATED, userId, "record", recordId, companyId, metadata)
    }

    fun documentUploaded(
        userId: String,
        documentId: String,
        companyId: String? = TenantContext.companyId,
        metadata: Map<String, Any> = emptyMap()
    ) {
        trackCreate(UsageEventType.DOCUMENT_UPLOADED, userId, "document", documentId, companyId, metadata)
    }

    fun activeUser(
        userId: String,
        companyId: String? = TenantContext.companyId
    ) {
        if (userId.isBlank()) return
        enqueue {
            val cid = companyId?.takeIf { it.isNotBlank() } ?: TenantContext.companyId
            if (cid.isNullOrBlank()) return@enqueue
            repository.markActiveUser(cid, userId)
        }
    }

    private fun trackCreate(
        type: String,
        userId: String,
        resourceType: String,
        resourceId: String,
        companyId: String?,
        metadata: Map<String, Any>
    ) {
        if (userId.isBlank() || resourceId.isBlank()) return
        enqueue {
            repository.recordEvent(
                type = type,
                userId = userId,
                companyId = companyId,
                resourceType = resourceType,
                resourceId = resourceId,
                metadata = metadata
            )
        }
    }

    private fun enqueue(block: suspend () -> Unit) {
        scope.launch {
            runCatching { block() }
        }
    }
}
