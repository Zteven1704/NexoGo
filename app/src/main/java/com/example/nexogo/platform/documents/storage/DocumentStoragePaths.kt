package com.example.nexogo.platform.documents.storage

import com.example.nexogo.platform.tenant.storage.TenantStoragePaths

/**
 * Firebase Storage path builders for Document Management v1.
 *
 * Layout:
 *   companies/{companyId}/documents/{documentId}/v{n}/{safeFileName}
 *   companies/{companyId}/documents/{documentId}/thumb/{safeFileName}
 *   companies/{companyId}/tmp/{uploadId}/{safeFileName}
 *
 * Actual putFile/getDownloadUrl is deferred to UI / upload service — metadata is registered via repository.
 */
object DocumentStoragePaths {

    private const val MAX_FILE_NAME = 120

    fun versionObjectPath(
        companyId: String,
        documentId: String,
        versionNumber: Int,
        fileName: String
    ): String {
        require(companyId.isNotBlank() && documentId.isNotBlank()) { "companyId and documentId required" }
        require(versionNumber > 0) { "versionNumber must be > 0" }
        val safe = sanitizeFileName(fileName)
        return "${TenantStoragePaths.prefix(companyId)}/documents/$documentId/v$versionNumber/$safe"
    }

    fun thumbnailPath(
        companyId: String,
        documentId: String,
        fileName: String = "thumb.jpg"
    ): String =
        "${TenantStoragePaths.prefix(companyId)}/documents/$documentId/thumb/${sanitizeFileName(fileName)}"

    fun draftUploadPath(
        companyId: String,
        uploadId: String,
        fileName: String
    ): String =
        TenantStoragePaths.tempUpload(companyId, uploadId, sanitizeFileName(fileName))

    fun sanitizeFileName(fileName: String): String {
        val trimmed = fileName.trim().ifBlank { "file" }
        val cleaned = trimmed
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), "_")
        return cleaned.take(MAX_FILE_NAME)
    }

    fun assertInTenant(storagePath: String, companyId: String) {
        TenantStoragePaths.assertBelongsToCompany(storagePath, companyId)
    }
}

/**
 * Descriptor used when registering metadata after (or before) a Storage upload.
 */
data class DocumentUploadDescriptor(
    val companyId: String,
    val fileName: String,
    val mimeType: String,
    val size: Long,
    val storagePath: String,
    val checksum: String = "",
    val createdBy: String = "",
    val folderId: String? = null,
    val categoryId: String? = null,
    val tagIds: List<String> = emptyList(),
    val displayName: String = "",
    val changeNote: String = "",
    /** When set (from [DocumentRepository.prepareUploadPath]), reuses the same id. */
    val documentId: String = ""
)
