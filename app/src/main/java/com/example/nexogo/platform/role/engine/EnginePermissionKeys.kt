package com.example.nexogo.platform.role.engine

/**
 * Canonical `{module}.{action}` keys for the Permission Engine.
 * Seeded alongside legacy `PermissionKeys` catalog.
 */
object EnginePermissionKeys {

    // clients
    const val CLIENTS_VIEW = "clients.view"
    const val CLIENTS_CREATE = "clients.create"
    const val CLIENTS_EDIT = "clients.edit"
    const val CLIENTS_DELETE = "clients.delete"
    const val CLIENTS_EXPORT = "clients.export"
    const val CLIENTS_APPROVE = "clients.approve"

    // documents
    const val DOCUMENTS_VIEW = "documents.view"
    const val DOCUMENTS_CREATE = "documents.create"
    const val DOCUMENTS_EDIT = "documents.edit"
    const val DOCUMENTS_DELETE = "documents.delete"
    const val DOCUMENTS_EXPORT = "documents.export"
    const val DOCUMENTS_APPROVE = "documents.approve"

    // sales
    const val SALES_VIEW = "sales.view"
    const val SALES_CREATE = "sales.create"
    const val SALES_EDIT = "sales.edit"
    const val SALES_DELETE = "sales.delete"
    const val SALES_EXPORT = "sales.export"
    const val SALES_APPROVE = "sales.approve"

    // inventory
    const val INVENTORY_VIEW = "inventory.view"
    const val INVENTORY_CREATE = "inventory.create"
    const val INVENTORY_EDIT = "inventory.edit"
    const val INVENTORY_DELETE = "inventory.delete"
    const val INVENTORY_EXPORT = "inventory.export"
    const val INVENTORY_APPROVE = "inventory.approve"

    // chat
    const val CHAT_VIEW = "chat.view"
    const val CHAT_CREATE = "chat.create"
    const val CHAT_EDIT = "chat.edit"
    const val CHAT_DELETE = "chat.delete"
    const val CHAT_EXPORT = "chat.export"
    const val CHAT_APPROVE = "chat.approve"

    // tasks
    const val TASKS_VIEW = "tasks.view"
    const val TASKS_CREATE = "tasks.create"
    const val TASKS_EDIT = "tasks.edit"
    const val TASKS_DELETE = "tasks.delete"
    const val TASKS_EXPORT = "tasks.export"
    const val TASKS_APPROVE = "tasks.approve"

    fun allKeys(): List<String> = PermissionModule.ALL.flatMap { module ->
        PermissionAction.ALL.map { action -> EnginePermission(module, action).key }
    }

    fun forModule(module: PermissionModule): List<String> =
        PermissionAction.ALL.map { EnginePermission(module, it).key }

    fun key(module: PermissionModule, action: PermissionAction): String =
        EnginePermission(module, action).key
}
