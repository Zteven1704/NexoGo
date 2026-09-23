package com.example.nexogo.platform.role.engine

/**
 * Permission Engine vocabulary — module × action matrix.
 * Additive layer on top of ROLE FOUNDATION (`PermissionKeys` / `PermissionChecker`).
 */

/**
 * Canonical CRUD + workflow actions exposed by the engine.
 */
enum class PermissionAction {
    VIEW,
    CREATE,
    EDIT,
    DELETE,
    EXPORT,
    APPROVE;

    val key: String get() = name.lowercase()

    companion object {
        fun fromKey(raw: String): PermissionAction? =
            entries.find { it.key.equals(raw, ignoreCase = true) }

        val ALL: List<PermissionAction> = entries.toList()
    }
}

/**
 * Business modules covered by the Permission Engine.
 */
enum class PermissionModule {
    CLIENTS,
    DOCUMENTS,
    SALES,
    INVENTORY,
    CHAT,
    TASKS;

    val key: String get() = name.lowercase()

    companion object {
        fun fromKey(raw: String): PermissionModule? =
            entries.find { it.key.equals(raw, ignoreCase = true) }

        val ALL: List<PermissionModule> = entries.toList()
    }
}

/**
 * Atomic engine permission: `{module}.{action}` e.g. `clients.view`.
 */
data class EnginePermission(
    val module: PermissionModule,
    val action: PermissionAction
) {
    val key: String get() = "${module.key}.${action.key}"

    companion object {
        fun parse(key: String): EnginePermission? {
            val parts = key.split(".")
            if (parts.size != 2) return null
            val module = PermissionModule.fromKey(parts[0]) ?: return null
            val action = PermissionAction.fromKey(parts[1]) ?: return null
            return EnginePermission(module, action)
        }
    }
}

/**
 * Full allow/deny matrix for one principal (user / role).
 */
data class PermissionMatrix(
    val grants: Map<PermissionModule, Set<PermissionAction>> = emptyMap()
) {
    fun allows(module: PermissionModule, action: PermissionAction): Boolean =
        grants[module]?.contains(action) == true

    fun actionsFor(module: PermissionModule): Set<PermissionAction> =
        grants[module].orEmpty()

    fun enabledModules(): Set<PermissionModule> =
        grants.filterValues { it.isNotEmpty() }.keys

    fun toEngineKeys(): Set<String> = buildSet {
        grants.forEach { (module, actions) ->
            actions.forEach { action ->
                add(EnginePermission(module, action).key)
            }
        }
    }

    companion object {
        val EMPTY = PermissionMatrix()

        fun fromEngineKeys(keys: Collection<String>): PermissionMatrix {
            val map = mutableMapOf<PermissionModule, MutableSet<PermissionAction>>()
            keys.forEach { key ->
                val parsed = EnginePermission.parse(key) ?: return@forEach
                map.getOrPut(parsed.module) { mutableSetOf() }.add(parsed.action)
            }
            return PermissionMatrix(map.mapValues { it.value.toSet() })
        }

        fun fullAccess(): PermissionMatrix {
            val all = PermissionAction.ALL.toSet()
            return PermissionMatrix(PermissionModule.ALL.associateWith { all })
        }
    }
}

/**
 * Decision result for diagnostics / audit.
 */
data class PermissionDecision(
    val module: PermissionModule,
    val action: PermissionAction,
    val allowed: Boolean,
    val reason: String = "",
    val matchedKeys: List<String> = emptyList()
)
