package com.example.nexogo.platform.role.model

import com.google.firebase.Timestamp

/**
 * Platform IAM models (ROLE FOUNDATION).
 * Additive — does not replace legacy [com.example.nexogo.core.models.UserRole].
 */

/**
 * Atomic permission: `{domain}.{resource}.{action}`.
 * Catalog: permissions_catalog/{key}
 */
data class Permission(
    val key: String = "",
    val domain: String = "",
    val resource: String = "",
    val action: String = "",
    val description: String = "",
    val isPlatform: Boolean = false,
    val module: String = ""
)

/**
 * Named set of permissions.
 * System roles: platform_roles/{code}
 * Custom roles: companies/{companyId}/roles/{roleId}
 */
data class Role(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    /** Null / blank = platform system role (not tenant-scoped). */
    val companyId: String? = null,
    val permissionKeys: List<String> = emptyList(),
    /** Optional inheritance hint for custom roles (e.g. EMPLOYEE). */
    val baseTemplate: String? = null,
    val moduleAccess: Map<String, Boolean> = emptyMap(),
    val isSystem: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)

/**
 * Explicit IAM assignment of roles to a user within a company.
 * Path: companies/{companyId}/role_assignments/{userId}
 *
 * Complements [com.example.nexogo.platform.company.model.CompanyMembership.roleCodes]
 * without replacing membership documents.
 */
data class UserRoleAssignment(
    val id: String = "",
    val userId: String = "",
    val companyId: String = "",
    val roleCodes: List<String> = emptyList(),
    val customRoleIds: List<String> = emptyList(),
    val status: AssignmentStatus = AssignmentStatus.ACTIVE,
    val assignedAt: Timestamp = Timestamp.now(),
    val assignedBy: String = "",
    val updatedAt: Timestamp = Timestamp.now(),
    val notes: String = ""
)

enum class AssignmentStatus {
    ACTIVE,
    SUSPENDED,
    REVOKED
}

/**
 * Base platform role codes (fixed catalog).
 * Same string values as [com.example.nexogo.platform.company.model.CompanyRoleCodes].
 */
object BaseRoleCodes {
    const val SUPER_ADMIN = "SUPER_ADMIN"
    const val ADMIN = "ADMIN"
    const val MANAGER = "MANAGER"
    const val EMPLOYEE = "EMPLOYEE"
    const val CLIENT = "CLIENT"

    val ALL: List<String> = listOf(
        SUPER_ADMIN,
        ADMIN,
        MANAGER,
        EMPLOYEE,
        CLIENT
    )

    fun isBase(code: String): Boolean = ALL.contains(code)
}

/**
 * Compiled effective permission set for a role in a company (or platform).
 * Path: role_permissions/{companyId}_{roleCode}
 */
data class RolePermissionsDoc(
    val id: String = "",
    val companyId: String = "",
    val roleCode: String = "",
    val roleId: String = "",
    val permissionKeys: List<String> = emptyList(),
    val updatedAt: Timestamp = Timestamp.now()
)

object PlatformRoleIds {
    const val PLATFORM_COMPANY_SENTINEL = "_platform"
}
