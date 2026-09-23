package com.example.nexogo.platform.documents.model

import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.google.firebase.Timestamp

/** Alias requested by Document Management module v1. */
typealias DocumentModel = Document

/**
 * Logical document head (current version metadata).
 * Bytes live in Firebase Storage at [storagePath].
 * Path: companies/{companyId}/documents/{documentId}
 */
data class Document(
    override val id: String = "",
    override val companyId: String = "",
    val name: String = "",
    val originalFileName: String = "",
    val fileFamily: DocumentFileFamily = DocumentFileFamily.OTHER,
    val mimeType: String = "",
    val size: Long = 0L,
    val checksum: String = "",
    val folderId: String? = null,
    val categoryId: String? = null,
    val tagIds: List<String> = emptyList(),
    val status: DocumentStatus = DocumentStatus.ACTIVE,
    val visibility: DocumentVisibility = DocumentVisibility.ORG_STAFF,
    val currentVersionId: String? = null,
    val currentVersionNumber: Int = 0,
    /** Firebase Storage object path for current version. */
    val storagePath: String = "",
    val thumbnailPath: String = "",
    val downloadUrl: String = "",
    val source: DocumentSource = DocumentSource.UPLOAD,
    val generator: String = "",
    val searchTokens: List<String> = emptyList(),
    val notes: String = "",
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val updatedBy: String = "",
    val archivedAt: Timestamp? = null
) : TenantAwareEntity

enum class DocumentFileFamily {
    PDF,
    WORD,
    EXCEL,
    IMAGE,
    OTHER
}

enum class DocumentStatus {
    ACTIVE,
    ARCHIVED,
    DELETED
}

enum class DocumentVisibility {
    ORG_STAFF,
    CLIENT_SHARED,
    PRIVATE
}

enum class DocumentSource {
    UPLOAD,
    GENERATED,
    IMPORT,
    CHAT
}

/**
 * Version history entry.
 * Path: companies/{companyId}/documents/{documentId}/versions/{versionId}
 */
data class DocumentVersion(
    override val id: String = "",
    override val companyId: String = "",
    val documentId: String = "",
    val versionNumber: Int = 1,
    val storagePath: String = "",
    val mimeType: String = "",
    val size: Long = 0L,
    val checksum: String = "",
    val changeNote: String = "",
    val isCurrent: Boolean = true,
    val status: DocumentVersionStatus = DocumentVersionStatus.ACTIVE,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

enum class DocumentVersionStatus {
    ACTIVE,
    SUPERSEDED,
    RESTORED_FROM
}

/**
 * Taxonomy category for library organization (distinct from flexible tags).
 * Path: companies/{companyId}/document_categories/{categoryId}
 */
data class DocumentCategory(
    override val id: String = "",
    override val companyId: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val parentId: String? = null,
    val color: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

/**
 * Folder tree node (optionally anchored to Client / Record / Employee / …).
 * Path: companies/{companyId}/folders/{folderId}
 */
data class DocumentFolder(
    override val id: String = "",
    override val companyId: String = "",
    val parentId: String? = null,
    val name: String = "",
    val path: String = "",
    val contextType: FolderContextType = FolderContextType.LIBRARY,
    val contextId: String? = null,
    val status: FolderStatus = FolderStatus.ACTIVE,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

enum class FolderContextType {
    ORG,
    LIBRARY,
    CLIENT,
    RECORD,
    SALE,
    EMPLOYEE
}

enum class FolderStatus {
    ACTIVE,
    ARCHIVED
}

/**
 * Flexible multi-tag label per company.
 * Path: companies/{companyId}/document_tags/{tagId}
 */
data class DocumentTag(
    override val id: String = "",
    override val companyId: String = "",
    val name: String = "",
    val color: String = "",
    val categoryHint: TagCategoryHint = TagCategoryHint.GENERAL,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

enum class TagCategoryHint {
    LEGAL,
    CLINICAL,
    FINANCE,
    HR,
    GENERAL
}

/**
 * Link document ↔ business entity without duplicating bytes.
 * Path: companies/{companyId}/document_links/{linkId}
 */
data class DocumentLink(
    override val id: String = "",
    override val companyId: String = "",
    val documentId: String = "",
    val entityType: DocumentEntityType = DocumentEntityType.CLIENT,
    val entityId: String = "",
    val relation: DocumentRelation = DocumentRelation.ATTACHMENT,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

enum class DocumentEntityType {
    COMPANY,
    CLIENT,
    RECORD,
    EMPLOYEE,
    SALE
}

enum class DocumentRelation {
    PRIMARY,
    ATTACHMENT,
    EVIDENCE,
    CONTRACT,
    IDENTITY,
    OTHER
}

/**
 * MIME / extension helpers for supported families (PDF, Word, Excel, images).
 */
object DocumentMimeTypes {
    val PDF: Set<String> = setOf("application/pdf")
    val WORD: Set<String> = setOf(
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )
    val EXCEL: Set<String> = setOf(
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/csv"
    )
    val IMAGE: Set<String> = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
        "image/heic",
        "image/heif"
    )

    fun familyFromMime(mimeType: String): DocumentFileFamily {
        val mime = mimeType.trim().lowercase()
        return when {
            mime in PDF || mime == "application/pdf" -> DocumentFileFamily.PDF
            mime in WORD -> DocumentFileFamily.WORD
            mime in EXCEL -> DocumentFileFamily.EXCEL
            mime in IMAGE || mime.startsWith("image/") -> DocumentFileFamily.IMAGE
            else -> DocumentFileFamily.OTHER
        }
    }

    fun familyFromFileName(fileName: String): DocumentFileFamily {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> DocumentFileFamily.PDF
            "doc", "docx" -> DocumentFileFamily.WORD
            "xls", "xlsx", "csv" -> DocumentFileFamily.EXCEL
            "jpg", "jpeg", "png", "webp", "gif", "heic", "heif" -> DocumentFileFamily.IMAGE
            else -> DocumentFileFamily.OTHER
        }
    }

    fun isSupported(mimeType: String, fileName: String = ""): Boolean {
        val family = if (mimeType.isNotBlank()) familyFromMime(mimeType) else familyFromFileName(fileName)
        return family != DocumentFileFamily.OTHER || fileName.isBlank()
    }

    fun buildSearchTokens(name: String, originalFileName: String = ""): List<String> {
        return listOf(name, originalFileName)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
