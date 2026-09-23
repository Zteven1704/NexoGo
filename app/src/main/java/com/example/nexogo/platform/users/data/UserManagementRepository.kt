package com.example.nexogo.platform.users.data

import com.example.nexogo.platform.company.data.CompanyPaths
import com.example.nexogo.platform.company.data.CompanyRepository
import com.example.nexogo.platform.company.data.UserCompanyRef
import com.example.nexogo.platform.company.model.CompanyInvite
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.CompanyRoleCodes
import com.example.nexogo.platform.company.model.InviteStatus
import com.example.nexogo.platform.company.model.MembershipStatus
import com.example.nexogo.platform.role.data.RoleRepository
import com.example.nexogo.platform.role.model.AssignmentStatus
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.model.UserRoleAssignment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * User Management v1 — invite / activate / deactivate / role / revoke within a company.
 */
class UserManagementRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val companyRepository: CompanyRepository = CompanyRepository(),
    private val roleRepository: RoleRepository = RoleRepository()
) {

    private fun invitesCol() = firestore.collection(CompanyPaths.COMPANY_INVITES)

    private fun membershipsCol(companyId: String) =
        firestore.collection(CompanyPaths.COMPANIES)
            .document(companyId)
            .collection(CompanyPaths.MEMBERSHIPS)

    suspend fun listMembers(companyId: String): Result<List<CompanyMembership>> {
        return try {
            require(companyId.isNotBlank())
            val snap = membershipsCol(companyId).get().await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(CompanyMembership::class.java)?.copy(userId = doc.id)
            }.sortedWith(
                compareBy<CompanyMembership> { it.status != MembershipStatus.ACTIVE }
                    .thenBy { it.displayName.ifBlank { it.email }.lowercase() }
            )
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listCompanyInvites(companyId: String): Result<List<CompanyInvite>> {
        return try {
            val snap = invitesCol()
                .whereEqualTo("companyId", companyId)
                .get()
                .await()
            val list = snap.documents.mapNotNull { doc ->
                doc.toObject(CompanyInvite::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listPendingInvitesForEmail(email: String): Result<List<CompanyInvite>> {
        return try {
            val normalized = email.trim().lowercase()
            if (normalized.isBlank()) return Result.success(emptyList())
            val snap = invitesCol()
                .whereEqualTo("email", normalized)
                .whereEqualTo("status", InviteStatus.PENDING.name)
                .get()
                .await()
            Result.success(
                snap.documents.mapNotNull { doc ->
                    doc.toObject(CompanyInvite::class.java)?.copy(id = doc.id)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a PENDING invite discoverable by Auth email.
     * Does not require the invitee to exist yet.
     */
    suspend fun inviteUser(
        companyId: String,
        companyName: String,
        email: String,
        roleCode: String,
        invitedBy: String,
        invitedByName: String = "",
        displayName: String = ""
    ): Result<CompanyInvite> {
        return try {
            val normalized = email.trim().lowercase()
            require(normalized.contains("@")) { "Email inválido" }
            require(companyId.isNotBlank())
            val role = roleCode.ifBlank { CompanyRoleCodes.EMPLOYEE }
            require(BaseRoleCodes.isBase(role) || role in listOf(
                CompanyRoleCodes.ADMIN,
                CompanyRoleCodes.MANAGER,
                CompanyRoleCodes.EMPLOYEE,
                CompanyRoleCodes.CLIENT
            )) { "Rol no válido" }

            val existingPending = listCompanyInvites(companyId).getOrElse { emptyList() }
                .any {
                    it.email == normalized && it.status == InviteStatus.PENDING
                }
            if (existingPending) {
                return Result.failure(IllegalStateException("Ya existe una invitación pendiente para $normalized"))
            }

            val id = "inv_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val now = Timestamp.now()
            val invite = CompanyInvite(
                id = id,
                companyId = companyId,
                companyName = companyName,
                email = normalized,
                displayName = displayName.trim(),
                roleCodes = listOf(role),
                status = InviteStatus.PENDING,
                invitedBy = invitedBy,
                invitedByName = invitedByName,
                createdAt = now,
                updatedAt = now
            )
            invitesCol().document(id).set(invite).await()
            Result.success(invite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Invitee accepts: creates ACTIVE membership + own mirror + assignment; marks invite ACCEPTED.
     */
    suspend fun acceptInvite(
        invite: CompanyInvite,
        userId: String,
        userEmail: String,
        displayName: String
    ): Result<CompanyMembership> {
        return try {
            require(invite.status == InviteStatus.PENDING) { "Invitación no pendiente" }
            require(userId.isNotBlank())
            val email = userEmail.trim().lowercase()
            require(email == invite.email) { "Esta invitación no corresponde a tu email" }

            val existing = companyRepository.getMembership(invite.companyId, userId).getOrNull()
            if (existing != null && existing.status == MembershipStatus.REVOKED) {
                // Reactivate path via membership update below
            } else if (existing != null && existing.status == MembershipStatus.ACTIVE) {
                markInviteAccepted(invite.id, userId)
                return Result.success(existing)
            }

            val now = Timestamp.now()
            val membership = CompanyMembership(
                userId = userId,
                companyId = invite.companyId,
                roleCodes = invite.roleCodes.ifEmpty { listOf(CompanyRoleCodes.EMPLOYEE) },
                status = MembershipStatus.ACTIVE,
                isDefault = true,
                title = "",
                email = email,
                displayName = displayName.ifBlank { invite.displayName }.ifBlank { email },
                invitedBy = invite.invitedBy,
                approvedBy = userId,
                inviteId = invite.id,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
            membershipsCol(invite.companyId).document(userId).set(membership).await()

            firestore.collection(CompanyPaths.USERS)
                .document(userId)
                .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                .document(invite.companyId)
                .set(
                    UserCompanyRef(
                        companyId = invite.companyId,
                        companyName = invite.companyName,
                        status = MembershipStatus.ACTIVE.name,
                        roleCodes = membership.roleCodes,
                        isDefault = true
                    )
                )
                .await()

            roleRepository.upsertAssignment(
                UserRoleAssignment(
                    id = userId,
                    userId = userId,
                    companyId = invite.companyId,
                    roleCodes = membership.roleCodes,
                    status = AssignmentStatus.ACTIVE,
                    assignedBy = invite.invitedBy.ifBlank { userId }
                )
            )

            markInviteAccepted(invite.id, userId)
            Result.success(membership)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun markInviteAccepted(inviteId: String, userId: String) {
        invitesCol().document(inviteId).update(
            mapOf(
                "status" to InviteStatus.ACCEPTED.name,
                "acceptedByUserId" to userId,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    suspend fun revokeInvite(inviteId: String): Result<Unit> {
        return try {
            invitesCol().document(inviteId).update(
                mapOf(
                    "status" to InviteStatus.REVOKED.name,
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateMember(companyId: String, userId: String, approvedBy: String): Result<Unit> =
        setMemberStatus(companyId, userId, MembershipStatus.ACTIVE, approvedBy)

    suspend fun deactivateMember(companyId: String, userId: String, updatedBy: String): Result<Unit> =
        setMemberStatus(companyId, userId, MembershipStatus.SUSPENDED, updatedBy)

    /** Soft-remove access (REVOKED). Does not delete the membership doc. */
    suspend fun removeAccess(companyId: String, userId: String, updatedBy: String): Result<Unit> =
        setMemberStatus(companyId, userId, MembershipStatus.REVOKED, updatedBy)

    suspend fun changeRole(
        companyId: String,
        userId: String,
        roleCode: String,
        updatedBy: String
    ): Result<Unit> {
        return try {
            require(BaseRoleCodes.isBase(roleCode)) { "Rol no válido: $roleCode" }
            val existing = companyRepository.getMembership(companyId, userId).getOrNull()
                ?: return Result.failure(IllegalStateException("Miembro no encontrado"))
            if (existing.status == MembershipStatus.REVOKED) {
                return Result.failure(IllegalStateException("Usuario sin acceso; reactiva primero"))
            }
            val updated = existing.copy(
                roleCodes = listOf(roleCode),
                updatedAt = Timestamp.now(),
                approvedBy = updatedBy.ifBlank { existing.approvedBy }
            )
            membershipsCol(companyId).document(userId).set(updated).await()
            // Best-effort mirror (only works if updating self; admin skip)
            runCatching {
                firestore.collection(CompanyPaths.USERS)
                    .document(userId)
                    .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                    .document(companyId)
                    .update(
                        mapOf(
                            "roleCodes" to updated.roleCodes,
                            "status" to updated.status.name
                        )
                    )
                    .await()
            }
            roleRepository.upsertAssignment(
                UserRoleAssignment(
                    id = userId,
                    userId = userId,
                    companyId = companyId,
                    roleCodes = updated.roleCodes,
                    status = if (updated.status == MembershipStatus.ACTIVE) {
                        AssignmentStatus.ACTIVE
                    } else {
                        AssignmentStatus.SUSPENDED
                    },
                    assignedBy = updatedBy
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun setMemberStatus(
        companyId: String,
        userId: String,
        status: MembershipStatus,
        actorId: String
    ): Result<Unit> {
        return try {
            val existing = companyRepository.getMembership(companyId, userId).getOrNull()
                ?: return Result.failure(IllegalStateException("Miembro no encontrado"))
            if (existing.userId == actorId && status != MembershipStatus.ACTIVE) {
                // Allow self-revoke later; for now prevent admin locking self out accidentally in UI
            }
            val updated = existing.copy(
                status = status,
                updatedAt = Timestamp.now(),
                approvedBy = if (status == MembershipStatus.ACTIVE) actorId else existing.approvedBy
            )
            membershipsCol(companyId).document(userId).set(updated).await()
            runCatching {
                firestore.collection(CompanyPaths.USERS)
                    .document(userId)
                    .collection(CompanyPaths.USER_COMPANY_MEMBERSHIPS)
                    .document(companyId)
                    .update(mapOf("status" to status.name))
                    .await()
            }
            val assignmentStatus = when (status) {
                MembershipStatus.ACTIVE -> AssignmentStatus.ACTIVE
                MembershipStatus.REVOKED -> AssignmentStatus.REVOKED
                else -> AssignmentStatus.SUSPENDED
            }
            roleRepository.upsertAssignment(
                UserRoleAssignment(
                    id = userId,
                    userId = userId,
                    companyId = companyId,
                    roleCodes = updated.roleCodes.ifEmpty { listOf(BaseRoleCodes.EMPLOYEE) },
                    status = assignmentStatus,
                    assignedBy = actorId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        val ASSIGNABLE_ROLES: List<String> = listOf(
            BaseRoleCodes.ADMIN,
            BaseRoleCodes.MANAGER,
            BaseRoleCodes.EMPLOYEE,
            BaseRoleCodes.CLIENT
        )
    }
}
