package com.example.nexogo.platform.role.compat

import com.example.nexogo.core.models.UserRole
import com.example.nexogo.platform.role.model.BaseRoleCodes

/**
 * Maps legacy vet [UserRole] → platform [BaseRoleCodes] without modifying or deleting legacy enums.
 * UI and auth continue using [UserRole]; platform IAM can read the mapped code when needed.
 */
object LegacyRoleBridge {

    fun toBaseRoleCode(legacy: UserRole): String = when (legacy) {
        UserRole.ADMIN -> BaseRoleCodes.ADMIN
        UserRole.VET -> BaseRoleCodes.MANAGER
        UserRole.VET_ASSISTANT -> BaseRoleCodes.EMPLOYEE
        UserRole.USER -> BaseRoleCodes.CLIENT
    }

    fun toLegacyRoles(baseCode: String): List<UserRole> = when (baseCode) {
        BaseRoleCodes.SUPER_ADMIN -> listOf(UserRole.ADMIN)
        BaseRoleCodes.ADMIN -> listOf(UserRole.ADMIN)
        BaseRoleCodes.MANAGER -> listOf(UserRole.VET)
        BaseRoleCodes.EMPLOYEE -> listOf(UserRole.VET_ASSISTANT)
        BaseRoleCodes.CLIENT -> listOf(UserRole.USER)
        else -> emptyList()
    }

    fun displayHint(baseCode: String): String = when (baseCode) {
        BaseRoleCodes.SUPER_ADMIN -> "Super administrador"
        BaseRoleCodes.ADMIN -> "Administrador"
        BaseRoleCodes.MANAGER -> "Manager / Veterinario (legacy VET)"
        BaseRoleCodes.EMPLOYEE -> "Empleado / Auxiliar (legacy VET_ASSISTANT)"
        BaseRoleCodes.CLIENT -> "Cliente (legacy USER)"
        else -> baseCode
    }
}
