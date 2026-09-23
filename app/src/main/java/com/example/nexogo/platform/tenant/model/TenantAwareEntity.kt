package com.example.nexogo.platform.tenant.model

import com.google.firebase.Timestamp

/**
 * Contract for every future business entity owned by a [companyId].
 * Legacy modules (patients, citas, …) are NOT migrated yet — new modules must implement this.
 */
interface TenantAwareEntity {
    val id: String
    val companyId: String
    val createdAt: Timestamp
    val updatedAt: Timestamp
}

/**
 * Minimal stamp applied when creating/updating tenant-scoped documents.
 */
data class TenantStamp(
    val companyId: String,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    init {
        require(companyId.isNotBlank()) { "TenantStamp.companyId must not be blank" }
    }
}

/**
 * Marker for documents that live under companies/{companyId}/… and also denormalize companyId.
 */
interface HierarchicalTenantDocument : TenantAwareEntity
