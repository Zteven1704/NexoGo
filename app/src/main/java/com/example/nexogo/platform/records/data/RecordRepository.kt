package com.example.nexogo.platform.records.data

import com.example.nexogo.platform.records.model.Record
import com.example.nexogo.platform.records.model.RecordFactories
import com.example.nexogo.platform.records.model.RecordStatus
import com.example.nexogo.platform.records.model.RecordType
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.data.TenantAwareRepository
import com.example.nexogo.platform.tenant.data.TenantCollections
import com.example.nexogo.platform.tenant.guard.TenantIsolationGuard
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Tenant-scoped Records (Expedientes) repository.
 * Path: companies/{companyId}/records/{recordId}
 * Does not use legacy clinical_records / medical_records collections.
 */
class RecordRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TenantAwareRepository(firestore) {

    override val collectionName: String = TenantCollections.RECORDS

    private fun requireTenant(companyId: String?): String =
        TenantIsolationGuard.requireCompanyId(companyId ?: TenantContext.companyId)

    suspend fun createRecord(record: Record): Result<Record> {
        return try {
            val companyId = requireTenant(record.companyId)
            require(record.clientId.isNotBlank()) { "clientId required" }
            require(record.title.isNotBlank()) { "title required" }
            val now = Timestamp.now()
            val id = record.id.ifBlank { newId("rec") }
            val toSave = record.copy(
                id = id,
                companyId = companyId,
                searchTokens = if (record.searchTokens.isEmpty()) {
                    RecordFactories.buildSearchTokens(
                        record.title,
                        record.summary,
                        record.tags,
                        record.category
                    )
                } else record.searchTokens,
                createdAt = if (record.createdAt.seconds == 0L) now else record.createdAt,
                updatedAt = now
            )
            setEntity(toSave, merge = false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecord(record: Record): Result<Record> {
        return try {
            val companyId = requireTenant(record.companyId)
            require(record.id.isNotBlank()) { "record id required" }
            require(record.clientId.isNotBlank()) { "clientId required" }
            val toSave = record.copy(
                companyId = companyId,
                updatedAt = Timestamp.now(),
                searchTokens = RecordFactories.buildSearchTokens(
                    record.title,
                    record.summary,
                    record.tags,
                    record.category
                )
            )
            setEntity(toSave, merge = true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecord(companyId: String, recordId: String): Result<Record?> {
        return try {
            val cid = requireTenant(companyId)
            val snap = document(cid, recordId).get().await()
            Result.success(snap.toObject(Record::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listRecords(
        companyId: String,
        clientId: String? = null,
        typeFilter: RecordType? = null,
        includeArchived: Boolean = false
    ): Result<List<Record>> {
        return try {
            val cid = requireTenant(companyId)
            var query: Query = listQuery(cid)
            if (!clientId.isNullOrBlank()) {
                query = query.whereEqualTo("clientId", clientId)
            }
            if (typeFilter != null) {
                query = query.whereEqualTo("recordType", typeFilter.name)
            }
            val snap = query.get().await()
            val records = snap.documents.mapNotNull { it.toObject(Record::class.java) }
                .filter { includeArchived || it.status != RecordStatus.ARCHIVED }
                .sortedByDescending { it.updatedAt.seconds }
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRecords(companyId: String, queryText: String): Result<List<Record>> {
        return try {
            val cid = requireTenant(companyId)
            val needle = queryText.trim().lowercase()
            if (needle.isBlank()) return listRecords(cid)
            val all = listRecords(cid).getOrThrow()
            Result.success(
                all.filter { record ->
                    record.searchTokens.any { it.contains(needle) } ||
                        record.title.lowercase().contains(needle) ||
                        record.summary.lowercase().contains(needle)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizeRecord(
        companyId: String,
        recordId: String,
        finalizedBy: String
    ): Result<Record> {
        return try {
            val existing = getRecord(companyId, recordId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Record not found"))
            require(existing.status == RecordStatus.DRAFT) {
                "Only DRAFT records can be finalized (current=${existing.status})"
            }
            val now = Timestamp.now()
            updateRecord(
                existing.copy(
                    status = RecordStatus.FINAL,
                    finalizedBy = finalizedBy,
                    finalizedAt = now,
                    updatedBy = finalizedBy,
                    updatedAt = now
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveRecord(
        companyId: String,
        recordId: String,
        updatedBy: String = ""
    ): Result<Record> {
        return try {
            val existing = getRecord(companyId, recordId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Record not found"))
            updateRecord(
                existing.copy(
                    status = RecordStatus.ARCHIVED,
                    archivedAt = Timestamp.now(),
                    updatedBy = updatedBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Links a Document id (Documents module) without storing file bytes.
     */
    suspend fun attachDocumentId(
        companyId: String,
        recordId: String,
        documentId: String,
        setAsPrimary: Boolean = false,
        updatedBy: String = ""
    ): Result<Record> {
        return try {
            require(documentId.isNotBlank())
            val existing = getRecord(companyId, recordId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Record not found"))
            val ids = (existing.documentIds + documentId).distinct()
            updateRecord(
                existing.copy(
                    documentIds = ids,
                    primaryDocumentId = if (setAsPrimary) documentId else existing.primaryDocumentId,
                    updatedBy = updatedBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenRecords(companyId: String, clientId: String? = null): Flow<List<Record>> = callbackFlow {
        val cid = requireTenant(companyId)
        var query: Query = listQuery(cid)
        if (!clientId.isNullOrBlank()) {
            query = query.whereEqualTo("clientId", clientId)
        }
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { it.toObject(Record::class.java) }
                ?.filter { it.status != RecordStatus.ARCHIVED }
                ?.sortedByDescending { it.updatedAt.seconds }
                .orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    private fun newId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(20)}"
}
