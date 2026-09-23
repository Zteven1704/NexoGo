package com.example.nexogo.platform.tenant.guard

import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.example.nexogo.platform.tenant.scope.CompanyScope
import com.example.nexogo.platform.tenant.storage.TenantStoragePaths

/**
 * Central deny-by-default checks for tenant isolation.
 */
object TenantIsolationGuard {

    fun requireCompanyId(companyId: String?): String {
        require(!companyId.isNullOrBlank()) { "companyId is required for tenant-scoped operations" }
        return companyId
    }

    fun assertEntityInScope(entity: TenantAwareEntity, scope: CompanyScope) {
        scope.assertOwns(entity)
    }

    fun assertSameTenant(leftCompanyId: String, rightCompanyId: String) {
        require(leftCompanyId.isNotBlank() && leftCompanyId == rightCompanyId) {
            "Cross-tenant reference denied: $leftCompanyId vs $rightCompanyId"
        }
    }

    fun assertStorageInTenant(storagePath: String, companyId: String) {
        TenantStoragePaths.assertBelongsToCompany(storagePath, companyId)
    }
}
