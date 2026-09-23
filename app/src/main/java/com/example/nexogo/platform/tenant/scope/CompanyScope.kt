package com.example.nexogo.platform.tenant.scope

import com.example.nexogo.platform.company.data.CompanyPaths
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.model.TenantAwareEntity

/**
 * Scopes all future Firestore / Storage operations to a single company.
 * Deny-by-default: blank companyId is rejected.
 */
class CompanyScope private constructor(
    val companyId: String
) {
    init {
        require(companyId.isNotBlank()) { "CompanyScope requires a non-blank companyId" }
    }

    /** companies/{companyId} */
    fun rootPath(): String = CompanyPaths.companyDoc(companyId)

    /** companies/{companyId}/{collection} */
    fun collectionPath(collection: String): String {
        require(collection.isNotBlank()) { "collection name required" }
        require(!collection.contains('/')) { "collection must be a single segment" }
        return "${rootPath()}/$collection"
    }

    /** companies/{companyId}/{collection}/{documentId} */
    fun documentPath(collection: String, documentId: String): String {
        require(documentId.isNotBlank()) { "documentId required" }
        return "${collectionPath(collection)}/$documentId"
    }

    fun assertOwns(entity: TenantAwareEntity) {
        require(entity.companyId == companyId) {
            "Tenant isolation violation: entity.companyId=${entity.companyId} scope=$companyId"
        }
    }

    fun assertOwns(entityCompanyId: String) {
        require(entityCompanyId == companyId) {
            "Tenant isolation violation: entity.companyId=$entityCompanyId scope=$companyId"
        }
    }

    fun stampCompanyId(): String = companyId

    companion object {
        fun of(companyId: String): CompanyScope = CompanyScope(companyId)

        /** Uses [TenantContext] active company. */
        fun fromContext(): CompanyScope = CompanyScope(TenantContext.requireCompanyId())

        fun fromContextOrNull(): CompanyScope? {
            val id = TenantContext.companyId
            return if (id.isNullOrBlank()) null else CompanyScope(id)
        }
    }
}
