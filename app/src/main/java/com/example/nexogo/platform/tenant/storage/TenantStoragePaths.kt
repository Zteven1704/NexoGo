package com.example.nexogo.platform.tenant.storage

import com.example.nexogo.platform.company.data.CompanyPaths
import com.example.nexogo.platform.tenant.scope.CompanyScope

/**
 * Storage path builders for tenant-isolated files.
 * Root: companies/{companyId}/…
 * Does not migrate legacy Storage layouts.
 */
object TenantStoragePaths {

    fun prefix(scope: CompanyScope): String =
        CompanyPaths.storagePrefix(scope.companyId)

    fun prefix(companyId: String): String =
        CompanyPaths.storagePrefix(companyId)

    fun documents(companyId: String, documentId: String, fileName: String): String =
        "${prefix(companyId)}/documents/$documentId/$fileName"

    fun records(companyId: String, recordId: String, fileName: String): String =
        "${prefix(companyId)}/records/$recordId/$fileName"

    fun clients(companyId: String, clientId: String, fileName: String): String =
        "${prefix(companyId)}/clients/$clientId/$fileName"

    fun chat(companyId: String, conversationId: String, fileName: String): String =
        "${prefix(companyId)}/chat/$conversationId/$fileName"

    fun sales(companyId: String, saleId: String, fileName: String): String =
        "${prefix(companyId)}/sales/$saleId/$fileName"

    fun branding(companyId: String, fileName: String = "logo.png"): String =
        CompanyPaths.brandingLogoPath(companyId, fileName)

    fun tempUpload(companyId: String, uploadId: String, fileName: String): String =
        "${prefix(companyId)}/tmp/$uploadId/$fileName"

    /**
     * Validates that a storage object path belongs to the given company.
     */
    fun belongsToCompany(storagePath: String, companyId: String): Boolean {
        if (companyId.isBlank() || storagePath.isBlank()) return false
        val expected = "${CompanyPaths.STORAGE_ROOT}/$companyId/"
        return storagePath == CompanyPaths.storagePrefix(companyId) ||
            storagePath.startsWith(expected)
    }

    fun assertBelongsToCompany(storagePath: String, companyId: String) {
        require(belongsToCompany(storagePath, companyId)) {
            "Storage path '$storagePath' is outside company '$companyId'"
        }
    }
}
