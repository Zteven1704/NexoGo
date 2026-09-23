package com.example.nexogo.platform.role.data

import com.example.nexogo.platform.role.model.AssignmentStatus
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.model.BaseRolePermissionBundles
import com.example.nexogo.platform.role.model.Permission
import com.example.nexogo.platform.role.model.PermissionKeys
import com.example.nexogo.platform.role.model.PlatformRoleIds
import com.example.nexogo.platform.role.model.Role
import com.example.nexogo.platform.role.model.RolePermissionsDoc
import com.example.nexogo.platform.role.model.UserRoleAssignment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Firestore access for ROLE FOUNDATION.
 * Additive only — does not modify legacy auth / UserRole storage.
 */
class RoleRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun catalogCollection() =
        firestore.collection(RolePaths.PERMISSIONS_CATALOG)

    private fun platformRolesCollection() =
        firestore.collection(RolePaths.PLATFORM_ROLES)

    private fun rolePermissionsCollection() =
        firestore.collection(RolePaths.ROLE_PERMISSIONS)

    private fun companyRoles(companyId: String) =
        firestore.collection(RolePaths.companyRolesCollection(companyId))

    private fun assignments(companyId: String) =
        firestore.collection(RolePaths.assignmentsCollection(companyId))

    // region Seed / catalog

    /**
     * Writes permission catalog + five system roles + compiled role_permissions.
     * Idempotent (merge). Safe to call from bootstrap; not wired to UI yet.
     */
    suspend fun seedPlatformFoundation(): Result<Unit> {
        return try {
            val now = Timestamp.now()
            val batch = firestore.batch()

            PermissionKeys.catalog().forEach { permission ->
                batch.set(
                    catalogCollection().document(permission.key),
                    permission,
                    SetOptions.merge()
                )
            }

            BaseRolePermissionBundles.systemRoles().forEach { role ->
                batch.set(platformRolesCollection().document(role.code), role, SetOptions.merge())
                val compiledId = RolePaths.rolePermissionsDocId(null, role.code)
                batch.set(
                    rolePermissionsCollection().document(compiledId),
                    RolePermissionsDoc(
                        id = compiledId,
                        companyId = PlatformRoleIds.PLATFORM_COMPANY_SENTINEL,
                        roleCode = role.code,
                        roleId = role.id,
                        permissionKeys = role.permissionKeys,
                        updatedAt = now
                    ),
                    SetOptions.merge()
                )
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPermissionCatalog(): Result<List<Permission>> {
        return try {
            val snap = catalogCollection().get().await()
            val remote = snap.documents.mapNotNull { it.toObject(Permission::class.java) }
            if (remote.isEmpty()) {
                Result.success(PermissionKeys.catalog())
            } else {
                Result.success(remote)
            }
        } catch (e: Exception) {
            Result.success(PermissionKeys.catalog())
        }
    }

    // endregion

    // region Roles

    suspend fun getSystemRole(code: String): Result<Role?> {
        return try {
            if (!BaseRoleCodes.isBase(code)) {
                return Result.success(null)
            }
            val remote = platformRolesCollection().document(code).get().await()
                .toObject(Role::class.java)
            Result.success(remote ?: BaseRolePermissionBundles.systemRoles().find { it.code == code })
        } catch (e: Exception) {
            Result.success(BaseRolePermissionBundles.systemRoles().find { it.code == code })
        }
    }

    suspend fun listSystemRoles(): Result<List<Role>> {
        return try {
            val snap = platformRolesCollection().get().await()
            val remote = snap.documents.mapNotNull { it.toObject(Role::class.java) }
            if (remote.isEmpty()) {
                Result.success(BaseRolePermissionBundles.systemRoles())
            } else {
                Result.success(remote)
            }
        } catch (e: Exception) {
            Result.success(BaseRolePermissionBundles.systemRoles())
        }
    }

    suspend fun createCustomRole(
        companyId: String,
        code: String,
        name: String,
        permissionKeys: List<String>,
        createdBy: String,
        baseTemplate: String? = BaseRoleCodes.EMPLOYEE,
        moduleAccess: Map<String, Boolean> = emptyMap(),
        description: String = ""
    ): Result<Role> {
        return try {
            require(companyId.isNotBlank()) { "companyId required" }
            require(code.isNotBlank()) { "code required" }
            require(permissionKeys.none { it.startsWith("platform.") }) {
                "Custom roles cannot grant platform.* permissions"
            }

            val roleId = "role_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val now = Timestamp.now()
            val role = Role(
                id = roleId,
                code = code.trim().lowercase(),
                name = name.trim(),
                description = description,
                companyId = companyId,
                permissionKeys = permissionKeys.distinct().sorted(),
                baseTemplate = baseTemplate,
                moduleAccess = moduleAccess,
                isSystem = false,
                isActive = true,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy
            )

            val batch = firestore.batch()
            batch.set(companyRoles(companyId).document(roleId), role)
            val compiledId = RolePaths.rolePermissionsDocId(companyId, role.code)
            batch.set(
                rolePermissionsCollection().document(compiledId),
                RolePermissionsDoc(
                    id = compiledId,
                    companyId = companyId,
                    roleCode = role.code,
                    roleId = roleId,
                    permissionKeys = role.permissionKeys,
                    updatedAt = now
                )
            )
            batch.commit().await()
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomRole(companyId: String, roleId: String): Result<Role?> {
        return try {
            val role = companyRoles(companyId).document(roleId).get().await()
                .toObject(Role::class.java)
            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listCustomRoles(companyId: String): Result<List<Role>> {
        return try {
            val snap = companyRoles(companyId).get().await()
            Result.success(snap.documents.mapNotNull { it.toObject(Role::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomRole(role: Role): Result<Role> {
        return try {
            require(!role.isSystem) { "System roles are immutable via this API" }
            require(!role.companyId.isNullOrBlank()) { "companyId required" }
            require(role.permissionKeys.none { it.startsWith("platform.") }) {
                "Custom roles cannot grant platform.* permissions"
            }
            val updated = role.copy(updatedAt = Timestamp.now())
            val batch = firestore.batch()
            batch.set(companyRoles(updated.companyId!!).document(updated.id), updated, SetOptions.merge())
            val compiledId = RolePaths.rolePermissionsDocId(updated.companyId, updated.code)
            batch.set(
                rolePermissionsCollection().document(compiledId),
                RolePermissionsDoc(
                    id = compiledId,
                    companyId = updated.companyId,
                    roleCode = updated.code,
                    roleId = updated.id,
                    permissionKeys = updated.permissionKeys,
                    updatedAt = updated.updatedAt
                ),
                SetOptions.merge()
            )
            batch.commit().await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Assignments

    suspend fun upsertAssignment(assignment: UserRoleAssignment): Result<UserRoleAssignment> {
        return try {
            require(assignment.companyId.isNotBlank() && assignment.userId.isNotBlank())
            val now = Timestamp.now()
            val doc = assignment.copy(
                id = assignment.id.ifBlank { assignment.userId },
                updatedAt = now,
                assignedAt = if (assignment.assignedAt.seconds == 0L) now else assignment.assignedAt
            )
            assignments(doc.companyId).document(doc.userId).set(doc, SetOptions.merge()).await()
            Result.success(doc)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAssignment(companyId: String, userId: String): Result<UserRoleAssignment?> {
        return try {
            val snap = assignments(companyId).document(userId).get().await()
            Result.success(snap.toObject(UserRoleAssignment::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listAssignments(companyId: String): Result<List<UserRoleAssignment>> {
        return try {
            val snap = assignments(companyId).get().await()
            Result.success(snap.documents.mapNotNull { it.toObject(UserRoleAssignment::class.java) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun revokeAssignment(companyId: String, userId: String): Result<Unit> {
        return try {
            assignments(companyId).document(userId).set(
                mapOf(
                    "status" to AssignmentStatus.REVOKED.name,
                    "updatedAt" to Timestamp.now()
                ),
                SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    // region Effective permissions

    /**
     * Resolves effective permission keys for role codes (+ optional custom role ids).
     * Uses in-memory base bundles when Firestore is unavailable (offline-safe foundation).
     */
    suspend fun resolveEffectivePermissions(
        companyId: String?,
        roleCodes: List<String>,
        customRoleIds: List<String> = emptyList()
    ): Result<Set<String>> {
        return try {
            val effective = linkedSetOf<String>()

            roleCodes.distinct().forEach { code ->
                if (BaseRoleCodes.isBase(code)) {
                    effective += BaseRolePermissionBundles.forRole(code)
                }
                val compiledId = RolePaths.rolePermissionsDocId(
                    if (BaseRoleCodes.isBase(code)) null else companyId,
                    code
                )
                val remote = rolePermissionsCollection().document(compiledId).get().await()
                    .toObject(RolePermissionsDoc::class.java)
                if (remote != null) {
                    effective += remote.permissionKeys
                }
            }

            if (!companyId.isNullOrBlank()) {
                customRoleIds.distinct().forEach { roleId ->
                    val custom = companyRoles(companyId).document(roleId).get().await()
                        .toObject(Role::class.java)
                    if (custom != null && custom.isActive) {
                        effective += custom.permissionKeys
                    }
                }
            }

            Result.success(effective)
        } catch (e: Exception) {
            // Fail soft: still return in-memory base bundles so callers can authorize offline.
            val fallback = roleCodes
                .filter { BaseRoleCodes.isBase(it) }
                .flatMap { BaseRolePermissionBundles.forRole(it) }
                .toSet()
            if (fallback.isNotEmpty()) Result.success(fallback) else Result.failure(e)
        }
    }

    suspend fun resolveFromAssignment(assignment: UserRoleAssignment): Result<Set<String>> {
        if (assignment.status != AssignmentStatus.ACTIVE) {
            return Result.success(emptySet())
        }
        return resolveEffectivePermissions(
            companyId = assignment.companyId,
            roleCodes = assignment.roleCodes,
            customRoleIds = assignment.customRoleIds
        )
    }

    // endregion
}
