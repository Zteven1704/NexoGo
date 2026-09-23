package com.example.nexogo.platform.role.data

import com.example.nexogo.platform.company.data.CompanyPaths
import com.example.nexogo.platform.role.model.PlatformRoleIds

/**
 * Firestore paths for ROLE FOUNDATION.
 * Additive — does not alter legacy root collections (usuarios, citas, …).
 */
object RolePaths {
    const val PERMISSIONS_CATALOG = "permissions_catalog"
    const val PLATFORM_ROLES = "platform_roles"
    const val ROLE_PERMISSIONS = "role_permissions"
    const val ROLES = "roles"
    const val ROLE_ASSIGNMENTS = "role_assignments"

    fun permissionDoc(key: String): String = "$PERMISSIONS_CATALOG/$key"

    fun platformRoleDoc(code: String): String = "$PLATFORM_ROLES/$code"

    fun companyRoleDoc(companyId: String, roleId: String): String =
        "${CompanyPaths.COMPANIES}/$companyId/$ROLES/$roleId"

    fun companyRolesCollection(companyId: String): String =
        "${CompanyPaths.COMPANIES}/$companyId/$ROLES"

    fun assignmentDoc(companyId: String, userId: String): String =
        "${CompanyPaths.COMPANIES}/$companyId/$ROLE_ASSIGNMENTS/$userId"

    fun assignmentsCollection(companyId: String): String =
        "${CompanyPaths.COMPANIES}/$companyId/$ROLE_ASSIGNMENTS"

    fun rolePermissionsDocId(companyId: String?, roleCode: String): String {
        val tenant = companyId?.takeIf { it.isNotBlank() } ?: PlatformRoleIds.PLATFORM_COMPANY_SENTINEL
        return "${tenant}_$roleCode"
    }

    fun rolePermissionsDoc(companyId: String?, roleCode: String): String =
        "$ROLE_PERMISSIONS/${rolePermissionsDocId(companyId, roleCode)}"
}
