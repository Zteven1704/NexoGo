package com.example.nexogo.platform.company.data

import com.example.nexogo.platform.company.model.Company
import com.example.nexogo.platform.company.model.CompanyIndex
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.CompanyRoleCodes
import com.example.nexogo.platform.company.model.CompanySettings
import com.example.nexogo.platform.company.model.CompanyStatus
import com.example.nexogo.platform.company.model.MembershipStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Minimal Firestore access for Company tenancy.
 * Additive only — does not modify legacy repositories.
 */
class CompanyRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun companiesCollection() =
        firestore.collection(CompanyPaths.COMPANIES)

    private fun indexCollection() =
        firestore.collection(CompanyPaths.COMPANY_INDEX)

    /**
     * Creates company root doc, default settings, optional admin membership, and platform index.
     * Onboarding may pass [legalName], [primaryContactEmail], [limits], [adminTitle].
     */
    suspend fun createCompany(
        name: String,
        createdByUserId: String,
        industryPacks: List<String> = emptyList(),
        planId: String = com.example.nexogo.platform.company.model.CompanyPlans.FREE,
        createAdminMembership: Boolean = true,
        legalName: String = "",
        primaryContactEmail: String = "",
        limits: com.example.nexogo.platform.company.model.CompanyLimits =
            com.example.nexogo.platform.company.model.CompanyLimits(),
        adminTitle: String = "Administrador"
    ): Result<Company> {
        return try {
            val companyId = "cmp_${UUID.randomUUID().toString().replace("-", "").take(20)}"
            val now = Timestamp.now()
            val company = Company(
                id = companyId,
                name = name.trim(),
                legalName = legalName.trim().ifBlank { name.trim() },
                status = CompanyStatus.ACTIVE,
                planId = planId,
                industryPacks = industryPacks,
                primaryContactEmail = primaryContactEmail.trim(),
                limits = limits,
                createdAt = now,
                updatedAt = now,
                createdBy = createdByUserId
            )

            val batch = firestore.batch()

            batch.set(companiesCollection().document(companyId), company)

            val settings = CompanySettings(
                companyId = companyId,
                aiMonthlyRequestQuota = limits.maxAiRequestsMonth,
                documentMaxUploadMb = 25,
                updatedAt = now,
                updatedBy = createdByUserId
            )
            batch.set(
                companiesCollection()
                    .document(companyId)
                    .collection(CompanyPaths.SETTINGS)
                    .document(CompanyPaths.SETTINGS_MAIN),
                settings
            )

            val index = CompanyIndex(
                companyId = companyId,
                name = company.name,
                status = company.status,
                planId = company.planId,
                industryPacks = company.industryPacks,
                ownerUserId = createdByUserId,
                createdAt = now
            )
            batch.set(indexCollection().document(companyId), index)

            if (createAdminMembership && createdByUserId.isNotBlank()) {
                val membership = CompanyMembership(
                    userId = createdByUserId,
                    companyId = companyId,
                    roleCodes = listOf(CompanyRoleCodes.ADMIN),
                    status = MembershipStatus.ACTIVE,
                    isDefault = true,
                    title = adminTitle.trim().ifBlank { "Administrador" },
                    invitedBy = createdByUserId,
                    approvedBy = createdByUserId,
                    createdAt = now,
                    updatedAt = now
                )
                batch.set(
                    companiesCollection()
                        .document(companyId)
                        .collection(CompanyPaths.MEMBERSHIPS)
                        .document(createdByUserId),
                    membership
                )
                batch.set(
                    firestore.collection(CompanyPaths.USERS)
                        .document(createdByUserId)
                        .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                        .document(companyId),
                    UserCompanyRef(
                        companyId = companyId,
                        companyName = company.name,
                        status = MembershipStatus.ACTIVE.name,
                        roleCodes = listOf(CompanyRoleCodes.ADMIN),
                        isDefault = true
                    )
                )
            }

            batch.commit().await()
            Result.success(company)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCompany(companyId: String): Result<Company?> {
        return try {
            val snap = companiesCollection().document(companyId).get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(snap.toObject(Company::class.java)?.copy(id = snap.id))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCompany(company: Company): Result<Unit> {
        return try {
            val updated = company.copy(updatedAt = Timestamp.now())
            companiesCollection().document(company.id).set(updated).await()
            indexCollection().document(company.id).set(
                CompanyIndex(
                    companyId = company.id,
                    name = updated.name,
                    status = updated.status,
                    planId = updated.planId,
                    industryPacks = updated.industryPacks,
                    ownerUserId = "",
                    createdAt = updated.createdAt
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSettings(companyId: String): Result<CompanySettings?> {
        return try {
            val snap = companiesCollection()
                .document(companyId)
                .collection(CompanyPaths.SETTINGS)
                .document(CompanyPaths.SETTINGS_MAIN)
                .get()
                .await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(snap.toObject(CompanySettings::class.java))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertSettings(settings: CompanySettings): Result<Unit> {
        return try {
            val updated = settings.copy(updatedAt = Timestamp.now())
            companiesCollection()
                .document(settings.companyId)
                .collection(CompanyPaths.SETTINGS)
                .document(CompanyPaths.SETTINGS_MAIN)
                .set(updated)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMembership(companyId: String, userId: String): Result<CompanyMembership?> {
        return try {
            val snap = companiesCollection()
                .document(companyId)
                .collection(CompanyPaths.MEMBERSHIPS)
                .document(userId)
                .get()
                .await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(
                    snap.toObject(CompanyMembership::class.java)?.copy(userId = snap.id)
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertMembership(membership: CompanyMembership): Result<Unit> {
        return try {
            val updated = membership.copy(updatedAt = Timestamp.now())
            companiesCollection()
                .document(membership.companyId)
                .collection(CompanyPaths.MEMBERSHIPS)
                .document(membership.userId)
                .set(updated)
                .await()

            val companyName = getCompany(membership.companyId).getOrNull()?.name.orEmpty()
            firestore.collection(CompanyPaths.USERS)
                .document(membership.userId)
                .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                .document(membership.companyId)
                .set(
                    UserCompanyRef(
                        companyId = membership.companyId,
                        companyName = companyName,
                        status = updated.status.name,
                        roleCodes = updated.roleCodes,
                        isDefault = updated.isDefault
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listUserCompanyRefs(userId: String): Result<List<UserCompanyRef>> {
        return try {
            if (userId.isBlank()) return Result.success(emptyList())
            val snap = firestore.collection(CompanyPaths.USERS)
                .document(userId)
                .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(UserCompanyRef::class.java)?.copy(companyId = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listIndex(limit: Long = 50): Result<List<CompanyIndex>> {
        return try {
            val snap = indexCollection()
                .orderBy("createdAt")
                .limit(limit)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(CompanyIndex::class.java)?.copy(companyId = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
