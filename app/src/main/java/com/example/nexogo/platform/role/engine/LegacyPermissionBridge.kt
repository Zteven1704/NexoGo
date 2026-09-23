package com.example.nexogo.platform.role.engine

import com.example.nexogo.platform.role.model.PermissionKeys

/**
 * Maps legacy `{domain}.{resource}.{action}` keys onto engine module×action grants.
 * Enables PermissionEngine to evaluate mixed catalogs without migrating call sites yet.
 */
object LegacyPermissionBridge {

    /**
     * Expands a set of permission keys (legacy and/or engine) into an engine matrix.
     */
    fun toMatrix(keys: Collection<String>): PermissionMatrix {
        val grants = mutableMapOf<PermissionModule, MutableSet<PermissionAction>>()

        fun grant(module: PermissionModule, action: PermissionAction) {
            grants.getOrPut(module) { mutableSetOf() }.add(action)
        }

        keys.forEach { key ->
            EnginePermission.parse(key)?.let {
                grant(it.module, it.action)
                return@forEach
            }
            mapLegacyKey(key).forEach { (module, action) -> grant(module, action) }
        }

        return PermissionMatrix(grants.mapValues { it.value.toSet() })
    }

    /**
     * Reverse: engine grant → representative legacy keys (for checkers that still use catalog).
     */
    fun toLegacyKeys(module: PermissionModule, action: PermissionAction): Set<String> =
        when (module) {
            PermissionModule.CLIENTS -> when (action) {
                PermissionAction.VIEW -> setOf(
                    PermissionKeys.CLIENTS_CLIENT_READ,
                    PermissionKeys.CLIENTS_CLIENT_READ_OWN
                )
                PermissionAction.CREATE -> setOf(PermissionKeys.CLIENTS_CLIENT_CREATE)
                PermissionAction.EDIT -> setOf(
                    PermissionKeys.CLIENTS_CLIENT_UPDATE,
                    PermissionKeys.CLIENTS_CONTACT_MANAGE
                )
                PermissionAction.DELETE -> setOf(PermissionKeys.CLIENTS_CLIENT_DELETE)
                PermissionAction.EXPORT -> setOf(PermissionKeys.CLIENTS_CLIENT_READ)
                PermissionAction.APPROVE -> setOf(PermissionKeys.CLIENTS_CLIENT_UPDATE)
            }
            PermissionModule.DOCUMENTS -> when (action) {
                PermissionAction.VIEW -> setOf(
                    PermissionKeys.DOCUMENTS_FILE_READ,
                    PermissionKeys.DOCUMENTS_FILE_READ_OWN
                )
                PermissionAction.CREATE -> setOf(PermissionKeys.DOCUMENTS_FILE_UPLOAD)
                PermissionAction.EDIT -> setOf(PermissionKeys.DOCUMENTS_FILE_UPLOAD)
                PermissionAction.DELETE -> setOf(PermissionKeys.DOCUMENTS_FILE_DELETE)
                PermissionAction.EXPORT -> setOf(PermissionKeys.DOCUMENTS_FILE_READ)
                PermissionAction.APPROVE -> setOf(PermissionKeys.DOCUMENTS_FILE_UPLOAD)
            }
            PermissionModule.SALES -> when (action) {
                PermissionAction.VIEW -> setOf(
                    PermissionKeys.SALES_SALE_READ,
                    PermissionKeys.SALES_SALE_READ_OWN
                )
                PermissionAction.CREATE -> setOf(PermissionKeys.SALES_SALE_CREATE)
                PermissionAction.EDIT -> setOf(PermissionKeys.SALES_SALE_UPDATE)
                PermissionAction.DELETE -> setOf(PermissionKeys.SALES_SALE_REFUND)
                PermissionAction.EXPORT -> setOf(PermissionKeys.SALES_SALE_EXPORT)
                PermissionAction.APPROVE -> setOf(
                    PermissionKeys.SALES_SALE_CHARGE,
                    PermissionKeys.SALES_SALE_REFUND
                )
            }
            PermissionModule.INVENTORY -> when (action) {
                PermissionAction.VIEW -> setOf(
                    PermissionKeys.INVENTORY_PRODUCT_READ,
                    PermissionKeys.INVENTORY_MOVEMENT_READ
                )
                PermissionAction.CREATE -> setOf(PermissionKeys.INVENTORY_PRODUCT_MANAGE)
                PermissionAction.EDIT -> setOf(
                    PermissionKeys.INVENTORY_PRODUCT_MANAGE,
                    PermissionKeys.INVENTORY_STOCK_ADJUST
                )
                PermissionAction.DELETE -> setOf(PermissionKeys.INVENTORY_PRODUCT_MANAGE)
                PermissionAction.EXPORT -> setOf(PermissionKeys.INVENTORY_PRODUCT_READ)
                PermissionAction.APPROVE -> setOf(PermissionKeys.INVENTORY_STOCK_ADJUST)
            }
            PermissionModule.CHAT -> when (action) {
                PermissionAction.VIEW -> setOf(PermissionKeys.CHAT_CONVERSATION_READ)
                PermissionAction.CREATE -> setOf(
                    PermissionKeys.CHAT_CONVERSATION_CREATE,
                    PermissionKeys.CHAT_MESSAGE_SEND
                )
                PermissionAction.EDIT -> setOf(PermissionKeys.CHAT_CONVERSATION_MODERATE)
                PermissionAction.DELETE -> setOf(PermissionKeys.CHAT_CONVERSATION_MODERATE)
                PermissionAction.EXPORT -> setOf(PermissionKeys.CHAT_CONVERSATION_READ)
                PermissionAction.APPROVE -> setOf(PermissionKeys.CHAT_BOT_MANAGE)
            }
            PermissionModule.TASKS -> when (action) {
                PermissionAction.VIEW -> setOf(
                    PermissionKeys.TASKS_TASK_READ,
                    PermissionKeys.TASKS_TASK_READ_OWN
                )
                PermissionAction.CREATE -> setOf(PermissionKeys.TASKS_TASK_CREATE)
                PermissionAction.EDIT -> setOf(PermissionKeys.TASKS_TASK_UPDATE)
                PermissionAction.DELETE -> setOf(PermissionKeys.TASKS_TASK_DELETE)
                PermissionAction.EXPORT -> setOf(PermissionKeys.TASKS_TASK_EXPORT)
                PermissionAction.APPROVE -> setOf(PermissionKeys.TASKS_TASK_APPROVE)
            }
        }

    private fun mapLegacyKey(key: String): List<Pair<PermissionModule, PermissionAction>> {
        return when (key) {
            // clients
            PermissionKeys.CLIENTS_CLIENT_READ,
            PermissionKeys.CLIENTS_CLIENT_READ_OWN -> listOf(
                PermissionModule.CLIENTS to PermissionAction.VIEW
            )
            PermissionKeys.CLIENTS_CLIENT_CREATE -> listOf(
                PermissionModule.CLIENTS to PermissionAction.CREATE
            )
            PermissionKeys.CLIENTS_CLIENT_UPDATE,
            PermissionKeys.CLIENTS_CONTACT_MANAGE -> listOf(
                PermissionModule.CLIENTS to PermissionAction.EDIT
            )
            PermissionKeys.CLIENTS_CLIENT_DELETE -> listOf(
                PermissionModule.CLIENTS to PermissionAction.DELETE
            )

            // documents
            PermissionKeys.DOCUMENTS_FILE_READ,
            PermissionKeys.DOCUMENTS_FILE_READ_OWN -> listOf(
                PermissionModule.DOCUMENTS to PermissionAction.VIEW
            )
            PermissionKeys.DOCUMENTS_FILE_UPLOAD -> listOf(
                PermissionModule.DOCUMENTS to PermissionAction.CREATE,
                PermissionModule.DOCUMENTS to PermissionAction.EDIT
            )
            PermissionKeys.DOCUMENTS_FILE_DELETE -> listOf(
                PermissionModule.DOCUMENTS to PermissionAction.DELETE
            )

            // sales
            PermissionKeys.SALES_SALE_READ,
            PermissionKeys.SALES_SALE_READ_OWN -> listOf(
                PermissionModule.SALES to PermissionAction.VIEW
            )
            PermissionKeys.SALES_SALE_CREATE -> listOf(
                PermissionModule.SALES to PermissionAction.CREATE
            )
            PermissionKeys.SALES_SALE_UPDATE -> listOf(
                PermissionModule.SALES to PermissionAction.EDIT
            )
            PermissionKeys.SALES_SALE_REFUND -> listOf(
                PermissionModule.SALES to PermissionAction.DELETE,
                PermissionModule.SALES to PermissionAction.APPROVE
            )
            PermissionKeys.SALES_SALE_EXPORT -> listOf(
                PermissionModule.SALES to PermissionAction.EXPORT
            )
            PermissionKeys.SALES_SALE_CHARGE -> listOf(
                PermissionModule.SALES to PermissionAction.APPROVE
            )

            // inventory
            PermissionKeys.INVENTORY_PRODUCT_READ,
            PermissionKeys.INVENTORY_MOVEMENT_READ -> listOf(
                PermissionModule.INVENTORY to PermissionAction.VIEW
            )
            PermissionKeys.INVENTORY_PRODUCT_MANAGE -> listOf(
                PermissionModule.INVENTORY to PermissionAction.CREATE,
                PermissionModule.INVENTORY to PermissionAction.EDIT,
                PermissionModule.INVENTORY to PermissionAction.DELETE
            )
            PermissionKeys.INVENTORY_STOCK_ADJUST -> listOf(
                PermissionModule.INVENTORY to PermissionAction.EDIT,
                PermissionModule.INVENTORY to PermissionAction.APPROVE
            )

            // chat
            PermissionKeys.CHAT_CONVERSATION_READ -> listOf(
                PermissionModule.CHAT to PermissionAction.VIEW
            )
            PermissionKeys.CHAT_CONVERSATION_CREATE,
            PermissionKeys.CHAT_MESSAGE_SEND -> listOf(
                PermissionModule.CHAT to PermissionAction.CREATE
            )
            PermissionKeys.CHAT_CONVERSATION_MODERATE -> listOf(
                PermissionModule.CHAT to PermissionAction.EDIT,
                PermissionModule.CHAT to PermissionAction.DELETE
            )
            PermissionKeys.CHAT_BOT_MANAGE -> listOf(
                PermissionModule.CHAT to PermissionAction.APPROVE
            )

            // tasks (legacy-style keys)
            PermissionKeys.TASKS_TASK_READ,
            PermissionKeys.TASKS_TASK_READ_OWN -> listOf(
                PermissionModule.TASKS to PermissionAction.VIEW
            )
            PermissionKeys.TASKS_TASK_CREATE -> listOf(
                PermissionModule.TASKS to PermissionAction.CREATE
            )
            PermissionKeys.TASKS_TASK_UPDATE -> listOf(
                PermissionModule.TASKS to PermissionAction.EDIT
            )
            PermissionKeys.TASKS_TASK_DELETE -> listOf(
                PermissionModule.TASKS to PermissionAction.DELETE
            )
            PermissionKeys.TASKS_TASK_EXPORT -> listOf(
                PermissionModule.TASKS to PermissionAction.EXPORT
            )
            PermissionKeys.TASKS_TASK_APPROVE -> listOf(
                PermissionModule.TASKS to PermissionAction.APPROVE
            )

            else -> emptyList()
        }
    }
}
