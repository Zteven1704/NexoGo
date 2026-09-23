package com.example.nexogo.platform.tenant.data

import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.example.nexogo.platform.tenant.scope.CompanyScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Base repository for future tenant-scoped modules.
 * Subclasses target a single collection under companies/{companyId}/…
 * Legacy repositories remain untouched.
 */
abstract class TenantAwareRepository(
    protected val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    protected val tenantFirestore: TenantFirestore = TenantFirestore(firestore)
) {
    /** Collection segment under the company root (e.g. [TenantCollections.CLIENTS]). */
    protected abstract val collectionName: String

    protected fun scope(companyId: String = TenantContext.requireCompanyId()): CompanyScope =
        CompanyScope.of(companyId)

    protected fun collection(companyId: String = TenantContext.requireCompanyId()) =
        tenantFirestore.collection(scope(companyId), collectionName)

    protected fun document(companyId: String, documentId: String) =
        tenantFirestore.document(scope(companyId), collectionName, documentId)

    protected fun listQuery(companyId: String = TenantContext.requireCompanyId()) =
        tenantFirestore.scopedQuery(scope(companyId), collectionName)

    /**
     * Ensures payload companyId matches the target scope before write.
     */
    protected fun <T : TenantAwareEntity> assertWritable(entity: T, companyId: String) {
        CompanyScope.of(companyId).assertOwns(entity)
    }

    protected suspend fun <T : TenantAwareEntity> setEntity(
        entity: T,
        merge: Boolean = true
    ): Result<T> {
        return try {
            assertWritable(entity, entity.companyId)
            val ref = document(entity.companyId, entity.id)
            if (merge) {
                ref.set(entity, SetOptions.merge()).await()
            } else {
                ref.set(entity).await()
            }
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected suspend fun deleteEntity(companyId: String, documentId: String): Result<Unit> {
        return try {
            require(documentId.isNotBlank())
            document(companyId, documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    protected fun touchUpdatedAt(): Timestamp = Timestamp.now()
}
