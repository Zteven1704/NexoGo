package com.example.nexogo.platform.role.check

import com.example.nexogo.platform.role.model.AssignmentStatus
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.model.BaseRolePermissionBundles
import com.example.nexogo.platform.role.model.PermissionKeys
import com.example.nexogo.platform.role.model.UserRoleAssignment

/**
 * Pure permission evaluation for ROLE FOUNDATION.
 * Not connected to UI yet — call sites can inject effective keys later.
 */
class PermissionChecker(
    private val effectivePermissions: Set<String> = emptySet(),
    private val moduleAccess: Map<String, Boolean> = emptyMap(),
    private val roleCodes: List<String> = emptyList()
) {

    fun hasPermission(key: String): Boolean {
        if (key.isBlank()) return false
        if (effectivePermissions.contains(key)) return true
        // manage implies CRUD for same resource prefix when catalog uses .manage
        val manageKey = manageEquivalent(key)
        return manageKey != null && effectivePermissions.contains(manageKey)
    }

    fun hasAny(vararg keys: String): Boolean = keys.any { hasPermission(it) }

    fun hasAll(vararg keys: String): Boolean = keys.all { hasPermission(it) }

    fun hasRole(code: String): Boolean = roleCodes.contains(code)

    fun isSuperAdmin(): Boolean = hasRole(BaseRoleCodes.SUPER_ADMIN)

    fun canEnterModule(module: String): Boolean {
        val override = moduleAccess[module]
        if (override == false) return false
        if (override == true) return true
        return when (module) {
            "clients" -> hasAny(
                PermissionKeys.CLIENTS_CLIENT_READ,
                PermissionKeys.CLIENTS_CLIENT_READ_OWN,
                PermissionKeys.CLIENTS_CLIENT_CREATE,
                "clients.view",
                "clients.create"
            )
            "records" -> hasAny(
                PermissionKeys.RECORDS_RECORD_READ,
                PermissionKeys.RECORDS_RECORD_READ_OWN,
                PermissionKeys.RECORDS_RECORD_CREATE
            )
            "agenda" -> hasAny(
                PermissionKeys.AGENDA_APPOINTMENT_READ,
                PermissionKeys.AGENDA_APPOINTMENT_READ_OWN,
                PermissionKeys.AGENDA_APPOINTMENT_CREATE
            )
            "inventory" -> hasAny(
                PermissionKeys.INVENTORY_PRODUCT_READ,
                PermissionKeys.INVENTORY_PRODUCT_MANAGE,
                "inventory.view",
                "inventory.create"
            )
            "sales" -> hasAny(
                PermissionKeys.SALES_SALE_READ,
                PermissionKeys.SALES_SALE_READ_OWN,
                PermissionKeys.SALES_SALE_CREATE,
                "sales.view",
                "sales.create"
            )
            "chat" -> hasAny(
                PermissionKeys.CHAT_CONVERSATION_READ,
                PermissionKeys.CHAT_CONVERSATION_CREATE,
                "chat.view",
                "chat.create"
            )
            "documents" -> hasAny(
                PermissionKeys.DOCUMENTS_FILE_READ,
                PermissionKeys.DOCUMENTS_FILE_READ_OWN,
                PermissionKeys.DOCUMENTS_FILE_UPLOAD,
                "documents.view",
                "documents.create"
            )
            "tasks" -> hasAny(
                PermissionKeys.TASKS_TASK_READ,
                PermissionKeys.TASKS_TASK_READ_OWN,
                PermissionKeys.TASKS_TASK_CREATE,
                "tasks.view",
                "tasks.create"
            )
            "crm" -> hasAny(
                PermissionKeys.CRM_OPPORTUNITY_READ,
                PermissionKeys.CRM_OPPORTUNITY_MANAGE
            )
            "dashboard" -> hasAny(
                PermissionKeys.DASHBOARD_KPIS_READ,
                PermissionKeys.DASHBOARD_KPIS_READ_OWN
            )
            "settings" -> hasAny(
                PermissionKeys.SETTINGS_ORG_READ,
                PermissionKeys.SETTINGS_ORG_UPDATE,
                PermissionKeys.SETTINGS_MEMBERS_MANAGE
            )
            "platform" -> hasAny(
                PermissionKeys.PLATFORM_ORG_READ_ALL,
                PermissionKeys.PLATFORM_ORG_CREATE
            )
            else -> false
        }
    }

    fun snapshot(): Set<String> = effectivePermissions.toSet()

    companion object {
        fun fromRoleCodes(roleCodes: List<String>): PermissionChecker {
            val perms = roleCodes
                .filter { BaseRoleCodes.isBase(it) }
                .flatMap { BaseRolePermissionBundles.forRole(it) }
                .toSet()
            return PermissionChecker(
                effectivePermissions = perms,
                roleCodes = roleCodes
            )
        }

        fun fromAssignment(
            assignment: UserRoleAssignment?,
            extraPermissions: Set<String> = emptySet()
        ): PermissionChecker {
            if (assignment == null || assignment.status != AssignmentStatus.ACTIVE) {
                return PermissionChecker()
            }
            val base = assignment.roleCodes
                .filter { BaseRoleCodes.isBase(it) }
                .flatMap { BaseRolePermissionBundles.forRole(it) }
                .toSet()
            return PermissionChecker(
                effectivePermissions = base + extraPermissions,
                roleCodes = assignment.roleCodes
            )
        }

        fun fromEffective(
            permissions: Set<String>,
            roleCodes: List<String> = emptyList(),
            moduleAccess: Map<String, Boolean> = emptyMap()
        ): PermissionChecker = PermissionChecker(
            effectivePermissions = permissions,
            moduleAccess = moduleAccess,
            roleCodes = roleCodes
        )

        private fun manageEquivalent(key: String): String? {
            val parts = key.split(".")
            if (parts.size != 3) return null
            val (domain, resource, action) = parts
            if (action == "manage") return null
            if (action !in setOf("read", "create", "update", "delete")) return null
            return "$domain.$resource.manage"
        }
    }
}
