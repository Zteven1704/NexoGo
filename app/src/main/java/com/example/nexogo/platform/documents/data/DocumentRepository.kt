package com.example.nexogo.platform.documents.data

import com.example.nexogo.platform.documents.model.Document
import com.example.nexogo.platform.documents.model.DocumentCategory
import com.example.nexogo.platform.documents.model.DocumentEntityType
import com.example.nexogo.platform.documents.model.DocumentFileFamily
import com.example.nexogo.platform.documents.model.DocumentFolder
import com.example.nexogo.platform.documents.model.DocumentLink
import com.example.nexogo.platform.documents.model.DocumentMimeTypes
import com.example.nexogo.platform.documents.model.DocumentRelation
import com.example.nexogo.platform.documents.model.DocumentStatus
import com.example.nexogo.platform.documents.model.DocumentTag
import com.example.nexogo.platform.documents.model.DocumentVersion
import com.example.nexogo.platform.documents.model.DocumentVersionStatus
import com.example.nexogo.platform.documents.model.FolderContextType
import com.example.nexogo.platform.documents.model.FolderStatus
import com.example.nexogo.platform.documents.storage.DocumentStoragePaths
import com.example.nexogo.platform.documents.storage.DocumentUploadDescriptor
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.data.TenantAwareRepository
import com.example.nexogo.platform.tenant.data.TenantCollections
import com.example.nexogo.platform.tenant.guard.TenantIsolationGuard
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Document Management repository (metadata + links + taxonomy).
 * Storage bytes: callers upload to [DocumentStoragePaths] then register via [registerUpload] / [addVersion].
 * No AI / OCR in v1.
 */
class DocumentRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : TenantAwareRepository(firestore) {

    override val collectionName: String = TenantCollections.DOCUMENTS

    private fun requireTenant(companyId: String?): String =
        TenantIsolationGuard.requireCompanyId(companyId ?: TenantContext.companyId)

    private fun versionsCol(companyId: String, documentId: String) =
        document(companyId, documentId).collection(TenantCollections.DOCUMENT_VERSIONS)

    private fun linksCol(companyId: String) =
        tenantFirestore.collection(scope(companyId), TenantCollections.DOCUMENT_LINKS)

    private fun foldersCol(companyId: String) =
        tenantFirestore.collection(scope(companyId), TenantCollections.DOCUMENT_FOLDERS)

    private fun tagsCol(companyId: String) =
        tenantFirestore.collection(scope(companyId), TenantCollections.DOCUMENT_TAGS)

    private fun categoriesCol(companyId: String) =
        tenantFirestore.collection(scope(companyId), TenantCollections.DOCUMENT_CATEGORIES)

    // region Documents

    /**
     * Registers a new document after bytes were (or will be) placed at [DocumentUploadDescriptor.storagePath].
     */
    suspend fun registerUpload(descriptor: DocumentUploadDescriptor): Result<Document> {
        return try {
            val companyId = requireTenant(descriptor.companyId)
            DocumentStoragePaths.assertInTenant(descriptor.storagePath, companyId)
            require(descriptor.fileName.isNotBlank()) { "fileName required" }

            val family = DocumentMimeTypes.familyFromMime(descriptor.mimeType).let {
                if (it == DocumentFileFamily.OTHER) {
                    DocumentMimeTypes.familyFromFileName(descriptor.fileName)
                } else it
            }
            require(family != DocumentFileFamily.OTHER) {
                "Unsupported file type — allowed: PDF, Word, Excel, images"
            }

            val now = Timestamp.now()
            val documentId = descriptor.documentId.ifBlank { newId("doc") }
            val versionId = newId("ver")
            val displayName = descriptor.displayName.ifBlank {
                descriptor.fileName.substringBeforeLast('.').ifBlank { descriptor.fileName }
            }

            val version = DocumentVersion(
                id = versionId,
                companyId = companyId,
                documentId = documentId,
                versionNumber = 1,
                storagePath = descriptor.storagePath,
                mimeType = descriptor.mimeType,
                size = descriptor.size,
                checksum = descriptor.checksum,
                changeNote = descriptor.changeNote.ifBlank { "Initial upload" },
                isCurrent = true,
                status = DocumentVersionStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
                createdBy = descriptor.createdBy
            )

            val doc = Document(
                id = documentId,
                companyId = companyId,
                name = displayName,
                originalFileName = descriptor.fileName,
                fileFamily = family,
                mimeType = descriptor.mimeType,
                size = descriptor.size,
                checksum = descriptor.checksum,
                folderId = descriptor.folderId,
                categoryId = descriptor.categoryId,
                tagIds = descriptor.tagIds,
                status = DocumentStatus.ACTIVE,
                currentVersionId = versionId,
                currentVersionNumber = 1,
                storagePath = descriptor.storagePath,
                searchTokens = DocumentMimeTypes.buildSearchTokens(displayName, descriptor.fileName),
                createdAt = now,
                updatedAt = now,
                createdBy = descriptor.createdBy,
                updatedBy = descriptor.createdBy
            )

            val batch = firestore.batch()
            batch.set(document(companyId, documentId), doc)
            batch.set(versionsCol(companyId, documentId).document(versionId), version)
            batch.commit().await()
            Result.success(doc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Builds the canonical Storage path for a new document upload (version 1) before putFile.
     */
    fun prepareUploadPath(companyId: String, fileName: String, documentId: String = newId("doc")): PreparedUploadPath {
        val cid = requireTenant(companyId)
        return PreparedUploadPath(
            documentId = documentId,
            storagePath = DocumentStoragePaths.versionObjectPath(cid, documentId, 1, fileName)
        )
    }

    /**
     * S2: uploads bytes to tenant Storage then registers Firestore metadata.
     * Optionally links the document to a client or record.
     */
    suspend fun uploadAndRegister(
        companyId: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        createdBy: String,
        displayName: String = "",
        linkEntityType: DocumentEntityType? = null,
        linkEntityId: String? = null
    ): Result<Document> {
        return try {
            val cid = requireTenant(companyId)
            require(bytes.isNotEmpty()) { "file bytes required" }
            val prepared = prepareUploadPath(cid, fileName)
            val ref = storage.reference.child(prepared.storagePath)
            ref.putBytes(bytes).await()
            val registered = registerUpload(
                DocumentUploadDescriptor(
                    companyId = cid,
                    fileName = fileName,
                    mimeType = mimeType.ifBlank { "application/octet-stream" },
                    size = bytes.size.toLong(),
                    storagePath = prepared.storagePath,
                    createdBy = createdBy,
                    displayName = displayName,
                    documentId = prepared.documentId
                )
            ).getOrThrow()

            if (linkEntityType != null && !linkEntityId.isNullOrBlank()) {
                linkDocument(
                    companyId = cid,
                    documentId = registered.id,
                    entityType = linkEntityType,
                    entityId = linkEntityId,
                    relation = DocumentRelation.ATTACHMENT,
                    createdBy = createdBy
                )
            }
            Result.success(registered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addVersion(
        companyId: String,
        documentId: String,
        storagePath: String,
        mimeType: String,
        size: Long,
        createdBy: String,
        checksum: String = "",
        changeNote: String = ""
    ): Result<Document> {
        return try {
            val cid = requireTenant(companyId)
            DocumentStoragePaths.assertInTenant(storagePath, cid)
            val existing = getDocument(cid, documentId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Document not found"))

            val nextNumber = existing.currentVersionNumber + 1
            val now = Timestamp.now()
            val versionId = newId("ver")

            val batch = firestore.batch()
            existing.currentVersionId?.let { prevId ->
                batch.set(
                    versionsCol(cid, documentId).document(prevId),
                    mapOf(
                        "isCurrent" to false,
                        "status" to DocumentVersionStatus.SUPERSEDED.name,
                        "updatedAt" to now
                    ),
                    SetOptions.merge()
                )
            }

            val version = DocumentVersion(
                id = versionId,
                companyId = cid,
                documentId = documentId,
                versionNumber = nextNumber,
                storagePath = storagePath,
                mimeType = mimeType.ifBlank { existing.mimeType },
                size = size,
                checksum = checksum,
                changeNote = changeNote,
                isCurrent = true,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )
            batch.set(versionsCol(cid, documentId).document(versionId), version)

            val updated = existing.copy(
                mimeType = version.mimeType,
                size = size,
                checksum = checksum,
                currentVersionId = versionId,
                currentVersionNumber = nextNumber,
                storagePath = storagePath,
                fileFamily = DocumentMimeTypes.familyFromMime(version.mimeType).let {
                    if (it == DocumentFileFamily.OTHER) existing.fileFamily else it
                },
                updatedAt = now,
                updatedBy = createdBy
            )
            batch.set(document(cid, documentId), updated, SetOptions.merge())
            batch.commit().await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocument(companyId: String, documentId: String): Result<Document?> {
        return try {
            val cid = requireTenant(companyId)
            Result.success(document(cid, documentId).get().await().toObject(Document::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDocuments(
        companyId: String,
        folderId: String? = null,
        categoryId: String? = null,
        fileFamily: DocumentFileFamily? = null,
        includeArchived: Boolean = false
    ): Result<List<Document>> {
        return try {
            val cid = requireTenant(companyId)
            var query: Query = listQuery(cid)
            if (!folderId.isNullOrBlank()) {
                query = query.whereEqualTo("folderId", folderId)
            }
            if (!categoryId.isNullOrBlank()) {
                query = query.whereEqualTo("categoryId", categoryId)
            }
            if (fileFamily != null) {
                query = query.whereEqualTo("fileFamily", fileFamily.name)
            }
            val snap = query.get().await()
            val docs = snap.documents.mapNotNull { it.toObject(Document::class.java) }
                .filter {
                    when {
                        includeArchived -> it.status != DocumentStatus.DELETED
                        else -> it.status == DocumentStatus.ACTIVE
                    }
                }
                .sortedByDescending { it.updatedAt.seconds }
            Result.success(docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchDocuments(companyId: String, queryText: String): Result<List<Document>> {
        return try {
            val cid = requireTenant(companyId)
            val needle = queryText.trim().lowercase()
            if (needle.isBlank()) return listDocuments(cid)
            val all = listDocuments(cid).getOrThrow()
            Result.success(
                all.filter {
                    it.searchTokens.any { t -> t.contains(needle) } ||
                        it.name.lowercase().contains(needle) ||
                        it.originalFileName.lowercase().contains(needle)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDocumentMeta(document: Document): Result<Document> {
        return try {
            val cid = requireTenant(document.companyId)
            require(document.id.isNotBlank())
            val toSave = document.copy(
                companyId = cid,
                updatedAt = Timestamp.now(),
                searchTokens = DocumentMimeTypes.buildSearchTokens(document.name, document.originalFileName)
            )
            setEntity(toSave, merge = true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveDocument(companyId: String, documentId: String, updatedBy: String = ""): Result<Document> {
        return try {
            val existing = getDocument(companyId, documentId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Document not found"))
            updateDocumentMeta(
                existing.copy(
                    status = DocumentStatus.ARCHIVED,
                    archivedAt = Timestamp.now(),
                    updatedBy = updatedBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listVersions(companyId: String, documentId: String): Result<List<DocumentVersion>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = versionsCol(cid, documentId).get().await()
            Result.success(
                snap.documents.mapNotNull { it.toObject(DocumentVersion::class.java) }
                    .sortedByDescending { it.versionNumber }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Links (Company / Client / Record / Employee)

    suspend fun linkDocument(
        companyId: String,
        documentId: String,
        entityType: DocumentEntityType,
        entityId: String,
        relation: DocumentRelation = DocumentRelation.ATTACHMENT,
        createdBy: String = ""
    ): Result<DocumentLink> {
        return try {
            val cid = requireTenant(companyId)
            require(documentId.isNotBlank() && entityId.isNotBlank())
            getDocument(cid, documentId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Document not found"))

            val now = Timestamp.now()
            val link = DocumentLink(
                id = newId("lnk"),
                companyId = cid,
                documentId = documentId,
                entityType = entityType,
                entityId = entityId,
                relation = relation,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )
            linksCol(cid).document(link.id).set(link).await()
            Result.success(link)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listLinksForEntity(
        companyId: String,
        entityType: DocumentEntityType,
        entityId: String
    ): Result<List<DocumentLink>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = linksCol(cid)
                .whereEqualTo("entityType", entityType.name)
                .whereEqualTo("entityId", entityId)
                .get()
                .await()
            Result.success(snap.documents.mapNotNull { it.toObject(DocumentLink::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listLinksForDocument(companyId: String, documentId: String): Result<List<DocumentLink>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = linksCol(cid).whereEqualTo("documentId", documentId).get().await()
            Result.success(snap.documents.mapNotNull { it.toObject(DocumentLink::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlink(companyId: String, linkId: String): Result<Unit> {
        return try {
            val cid = requireTenant(companyId)
            linksCol(cid).document(linkId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Categories

    suspend fun createCategory(category: DocumentCategory): Result<DocumentCategory> {
        return try {
            val cid = requireTenant(category.companyId)
            require(category.name.isNotBlank())
            val now = Timestamp.now()
            val toSave = category.copy(
                id = category.id.ifBlank { newId("cat") },
                companyId = cid,
                code = category.code.ifBlank { category.name.trim().lowercase().replace(" ", "_") },
                createdAt = now,
                updatedAt = now
            )
            categoriesCol(cid).document(toSave.id).set(toSave).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listCategories(companyId: String): Result<List<DocumentCategory>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = categoriesCol(cid).get().await()
            Result.success(
                snap.documents.mapNotNull { it.toObject(DocumentCategory::class.java) }
                    .filter { it.isActive }
                    .sortedBy { it.sortOrder }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: DocumentCategory): Result<DocumentCategory> {
        return try {
            val cid = requireTenant(category.companyId)
            require(category.id.isNotBlank())
            val toSave = category.copy(companyId = cid, updatedAt = Timestamp.now())
            categoriesCol(cid).document(toSave.id).set(toSave, SetOptions.merge()).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Folders

    suspend fun createFolder(folder: DocumentFolder): Result<DocumentFolder> {
        return try {
            val cid = requireTenant(folder.companyId)
            require(folder.name.isNotBlank())
            val now = Timestamp.now()
            val id = folder.id.ifBlank { newId("fld") }
            val parentPath = if (!folder.parentId.isNullOrBlank()) {
                foldersCol(cid).document(folder.parentId).get().await()
                    .toObject(DocumentFolder::class.java)?.path.orEmpty()
            } else ""
            val path = if (parentPath.isBlank()) "/${folder.name}/" else "$parentPath${folder.name}/"
            val toSave = folder.copy(
                id = id,
                companyId = cid,
                path = folder.path.ifBlank { path },
                createdAt = now,
                updatedAt = now
            )
            foldersCol(cid).document(id).set(toSave).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listFolders(
        companyId: String,
        parentId: String? = null,
        contextType: FolderContextType? = null
    ): Result<List<DocumentFolder>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = foldersCol(cid).get().await()
            var list = snap.documents.mapNotNull { it.toObject(DocumentFolder::class.java) }
                .filter { it.status == FolderStatus.ACTIVE }
            list = when {
                parentId == null -> list
                parentId.isEmpty() -> list.filter { it.parentId.isNullOrBlank() }
                else -> list.filter { it.parentId == parentId }
            }
            if (contextType != null) {
                list = list.filter { it.contextType == contextType }
            }
            Result.success(list.sortedBy { it.name.lowercase() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureContextFolder(
        companyId: String,
        contextType: FolderContextType,
        contextId: String,
        name: String,
        createdBy: String = ""
    ): Result<DocumentFolder> {
        return try {
            val cid = requireTenant(companyId)
            val existing = listFolders(cid, contextType = contextType).getOrThrow()
                .firstOrNull { it.contextId == contextId }
            if (existing != null) return Result.success(existing)
            createFolder(
                DocumentFolder(
                    companyId = cid,
                    name = name,
                    contextType = contextType,
                    contextId = contextId,
                    createdBy = createdBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Tags

    suspend fun createTag(tag: DocumentTag): Result<DocumentTag> {
        return try {
            val cid = requireTenant(tag.companyId)
            require(tag.name.isNotBlank())
            val now = Timestamp.now()
            val toSave = tag.copy(
                id = tag.id.ifBlank { newId("tag") },
                companyId = cid,
                createdAt = now,
                updatedAt = now
            )
            tagsCol(cid).document(toSave.id).set(toSave).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listTags(companyId: String): Result<List<DocumentTag>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = tagsCol(cid).get().await()
            Result.success(
                snap.documents.mapNotNull { it.toObject(DocumentTag::class.java) }
                    .sortedBy { it.name.lowercase() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTag(tag: DocumentTag): Result<DocumentTag> {
        return try {
            val cid = requireTenant(tag.companyId)
            require(tag.id.isNotBlank())
            val toSave = tag.copy(companyId = cid, updatedAt = Timestamp.now())
            tagsCol(cid).document(toSave.id).set(toSave, SetOptions.merge()).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    fun newId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(20)}"
}

data class PreparedUploadPath(
    val documentId: String,
    val storagePath: String
)
