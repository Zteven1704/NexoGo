package com.example.nexogo.platform.role.engine

import com.example.nexogo.platform.role.check.PermissionChecker
import com.example.nexogo.platform.role.model.AssignmentStatus
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.model.BaseRolePermissionBundles
import com.example.nexogo.platform.role.model.UserRoleAssignment

/**
 * Permission Engine — evaluates view/create/edit/delete/export/approve per module.
 *
 * Modules: clients, documents, sales, inventory, chat, tasks.
 * Accepts mixed legacy + engine keys; builds a [PermissionMatrix] for fast checks.
 */
class PermissionEngine(
    private val matrix: PermissionMatrix = PermissionMatrix.EMPTY,
    private val roleCodes: List<String> = emptyList(),
    private val moduleAccess: Map<String, Boolean> = emptyMap()
) {

    fun can(module: PermissionModule, action: PermissionAction): Boolean {
        if (moduleAccess[module.key] == false) return false
        if (isSuperAdmin()) return true
        return matrix.allows(module, action)
    }

    fun can(moduleKey: String, actionKey: String): Boolean {
        val module = PermissionModule.fromKey(moduleKey) ?: return false
        val action = PermissionAction.fromKey(actionKey) ?: return false
        return can(module, action)
    }

    fun canView(module: PermissionModule): Boolean = can(module, PermissionAction.VIEW)
    fun canCreate(module: PermissionModule): Boolean = can(module, PermissionAction.CREATE)
    fun canEdit(module: PermissionModule): Boolean = can(module, PermissionAction.EDIT)
    fun canDelete(module: PermissionModule): Boolean = can(module, PermissionAction.DELETE)
    fun canExport(module: PermissionModule): Boolean = can(module, PermissionAction.EXPORT)
    fun canApprove(module: PermissionModule): Boolean = can(module, PermissionAction.APPROVE)

    fun canAny(module: PermissionModule, vararg actions: PermissionAction): Boolean =
        actions.any { can(module, it) }

    fun canAll(module: PermissionModule, vararg actions: PermissionAction): Boolean =
        actions.all { can(module, it) }

    fun canEnter(module: PermissionModule): Boolean {
        if (moduleAccess[module.key] == false) return false
        if (moduleAccess[module.key] == true) return true
        return canView(module) || canCreate(module)
    }

    fun decide(module: PermissionModule, action: PermissionAction): PermissionDecision {
        val allowed = can(module, action)
        val engineKey = EnginePermission(module, action).key
        val reason = when {
            moduleAccess[module.key] == false -> "moduleAccess denied"
            isSuperAdmin() -> "SUPER_ADMIN bypass"
            allowed -> "granted by matrix"
            else -> "missing ${module.key}.${action.key}"
        }
        return PermissionDecision(
            module = module,
            action = action,
            allowed = allowed,
            reason = reason,
            matchedKeys = if (allowed) listOf(engineKey) else emptyList()
        )
    }

    fun matrix(): PermissionMatrix = matrix

    fun allowedActions(module: PermissionModule): Set<PermissionAction> {
        if (moduleAccess[module.key] == false) return emptySet()
        if (isSuperAdmin()) return PermissionAction.ALL.toSet()
        return matrix.actionsFor(module)
    }

    fun enabledModules(): Set<PermissionModule> {
        if (isSuperAdmin()) return PermissionModule.ALL.toSet()
        return PermissionModule.ALL.filter { canEnter(it) }.toSet()
    }

    fun engineKeys(): Set<String> = matrix.toEngineKeys()

    fun hasRole(code: String): Boolean = roleCodes.contains(code)

    fun isSuperAdmin(): Boolean = hasRole(BaseRoleCodes.SUPER_ADMIN)

    fun toChecker(): PermissionChecker = PermissionChecker.fromEffective(
        permissions = matrix.toEngineKeys() + expandToLegacySnapshot(),
        roleCodes = roleCodes,
        moduleAccess = moduleAccess
    )

    private fun expandToLegacySnapshot(): Set<String> = buildSet {
        matrix.grants.forEach { (module, actions) ->
            actions.forEach { action ->
                addAll(LegacyPermissionBridge.toLegacyKeys(module, action))
            }
        }
    }

    companion object {

        fun fromKeys(
            keys: Collection<String>,
            roleCodes: List<String> = emptyList(),
            moduleAccess: Map<String, Boolean> = emptyMap()
        ): PermissionEngine = PermissionEngine(
            matrix = LegacyPermissionBridge.toMatrix(keys),
            roleCodes = roleCodes,
            moduleAccess = moduleAccess
        )

        fun fromMatrix(
            matrix: PermissionMatrix,
            roleCodes: List<String> = emptyList(),
            moduleAccess: Map<String, Boolean> = emptyMap()
        ): PermissionEngine = PermissionEngine(matrix, roleCodes, moduleAccess)

        fun fromRoleCodes(
            roleCodes: List<String>,
            moduleAccess: Map<String, Boolean> = emptyMap()
        ): PermissionEngine {
            val keys = roleCodes
                .filter { BaseRoleCodes.isBase(it) }
                .flatMap { BaseRolePermissionBundles.forRole(it) }
            return fromKeys(keys, roleCodes, moduleAccess)
        }

        fun fromAssignment(
            assignment: UserRoleAssignment?,
            extraKeys: Set<String> = emptySet(),
            moduleAccess: Map<String, Boolean> = emptyMap()
        ): PermissionEngine {
            if (assignment == null || assignment.status != AssignmentStatus.ACTIVE) {
                return PermissionEngine()
            }
            val base = assignment.roleCodes
                .filter { BaseRoleCodes.isBase(it) }
                .flatMap { BaseRolePermissionBundles.forRole(it) }
            return fromKeys(
                keys = base + extraKeys,
                roleCodes = assignment.roleCodes,
                moduleAccess = moduleAccess
            )
        }

        fun fromChecker(checker: PermissionChecker): PermissionEngine =
            fromKeys(checker.snapshot(), roleCodes = emptyList())

        /** Default matrices for documentation / UI role editors. */
        fun defaultMatrixForRole(roleCode: String): PermissionMatrix =
            LegacyPermissionBridge.toMatrix(BaseRolePermissionBundles.forRole(roleCode))
    }
}
