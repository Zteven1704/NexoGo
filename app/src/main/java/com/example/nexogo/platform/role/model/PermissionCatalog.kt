package com.example.nexogo.platform.role.model

import com.example.nexogo.platform.role.engine.EnginePermissionKeys

/**
 * Canonical permission keys and default bundles per base role.
 * Source of truth aligned with ROLE_SYSTEM.md §5.
 * Engine keys (`{module}.{action}`) live in [EnginePermissionKeys] and are seeded here.
 */
object PermissionKeys {

    // clients
    const val CLIENTS_CLIENT_READ = "clients.client.read"
    const val CLIENTS_CLIENT_CREATE = "clients.client.create"
    const val CLIENTS_CLIENT_UPDATE = "clients.client.update"
    const val CLIENTS_CLIENT_DELETE = "clients.client.delete"
    const val CLIENTS_CONTACT_MANAGE = "clients.contact.manage"
    const val CLIENTS_CLIENT_READ_OWN = "clients.client.read_own"

    // records
    const val RECORDS_RECORD_READ = "records.record.read"
    const val RECORDS_RECORD_CREATE = "records.record.create"
    const val RECORDS_RECORD_UPDATE = "records.record.update"
    const val RECORDS_RECORD_FINALIZE = "records.record.finalize"
    const val RECORDS_RECORD_DELETE = "records.record.delete"
    const val RECORDS_RECORD_EXPORT = "records.record.export"
    const val RECORDS_RECORD_READ_OWN = "records.record.read_own"

    // agenda
    const val AGENDA_APPOINTMENT_READ = "agenda.appointment.read"
    const val AGENDA_APPOINTMENT_CREATE = "agenda.appointment.create"
    const val AGENDA_APPOINTMENT_UPDATE = "agenda.appointment.update"
    const val AGENDA_APPOINTMENT_DELETE = "agenda.appointment.delete"
    const val AGENDA_APPOINTMENT_READ_OWN = "agenda.appointment.read_own"
    const val AGENDA_SERVICE_MANAGE = "agenda.service.manage"

    // inventory
    const val INVENTORY_PRODUCT_READ = "inventory.product.read"
    const val INVENTORY_PRODUCT_MANAGE = "inventory.product.manage"
    const val INVENTORY_STOCK_ADJUST = "inventory.stock.adjust"
    const val INVENTORY_MOVEMENT_READ = "inventory.movement.read"

    // sales
    const val SALES_SALE_READ = "sales.sale.read"
    const val SALES_SALE_CREATE = "sales.sale.create"
    const val SALES_SALE_UPDATE = "sales.sale.update"
    const val SALES_SALE_CHARGE = "sales.sale.charge"
    const val SALES_SALE_REFUND = "sales.sale.refund"
    const val SALES_SALE_READ_OWN = "sales.sale.read_own"
    const val SALES_SALE_EXPORT = "sales.sale.export"

    // chat
    const val CHAT_CONVERSATION_READ = "chat.conversation.read"
    const val CHAT_CONVERSATION_CREATE = "chat.conversation.create"
    const val CHAT_MESSAGE_SEND = "chat.message.send"
    const val CHAT_CONVERSATION_MODERATE = "chat.conversation.moderate"
    const val CHAT_BOT_MANAGE = "chat.bot.manage"

    // documents
    const val DOCUMENTS_FILE_READ = "documents.file.read"
    const val DOCUMENTS_FILE_UPLOAD = "documents.file.upload"
    const val DOCUMENTS_FILE_DELETE = "documents.file.delete"
    const val DOCUMENTS_FILE_READ_OWN = "documents.file.read_own"

    // tasks (Permission Engine module)
    const val TASKS_TASK_READ = "tasks.task.read"
    const val TASKS_TASK_CREATE = "tasks.task.create"
    const val TASKS_TASK_UPDATE = "tasks.task.update"
    const val TASKS_TASK_DELETE = "tasks.task.delete"
    const val TASKS_TASK_EXPORT = "tasks.task.export"
    const val TASKS_TASK_APPROVE = "tasks.task.approve"
    const val TASKS_TASK_READ_OWN = "tasks.task.read_own"

    // crm
    const val CRM_OPPORTUNITY_READ = "crm.opportunity.read"
    const val CRM_OPPORTUNITY_MANAGE = "crm.opportunity.manage"
    const val CRM_ACTIVITY_MANAGE = "crm.activity.manage"
    const val CRM_PIPELINE_MANAGE = "crm.pipeline.manage"

    // dashboard
    const val DASHBOARD_KPIS_READ = "dashboard.kpis.read"
    const val DASHBOARD_KPIS_READ_OWN = "dashboard.kpis.read_own"
    const val DASHBOARD_LAYOUT_UPDATE = "dashboard.layout.update"

    // settings
    const val SETTINGS_ORG_READ = "settings.org.read"
    const val SETTINGS_ORG_UPDATE = "settings.org.update"
    const val SETTINGS_MEMBERS_MANAGE = "settings.members.manage"
    const val SETTINGS_ROLES_MANAGE = "settings.roles.manage"
    const val SETTINGS_CATEGORIES_MANAGE = "settings.categories.manage"
    const val SETTINGS_PACKS_MANAGE = "settings.packs.manage"

    // platform
    const val PLATFORM_ORG_CREATE = "platform.organization.create"
    const val PLATFORM_ORG_SUSPEND = "platform.organization.suspend"
    const val PLATFORM_ORG_READ_ALL = "platform.organization.read_all"
    const val PLATFORM_USER_IMPERSONATE = "platform.user.impersonate"
    const val PLATFORM_BILLING_MANAGE = "platform.billing.manage"

    /** Full catalog for seed / metadata (legacy + engine keys). */
    fun allKeys(): List<String> = listOf(
        CLIENTS_CLIENT_READ, CLIENTS_CLIENT_CREATE, CLIENTS_CLIENT_UPDATE, CLIENTS_CLIENT_DELETE,
        CLIENTS_CONTACT_MANAGE, CLIENTS_CLIENT_READ_OWN,
        RECORDS_RECORD_READ, RECORDS_RECORD_CREATE, RECORDS_RECORD_UPDATE, RECORDS_RECORD_FINALIZE,
        RECORDS_RECORD_DELETE, RECORDS_RECORD_EXPORT, RECORDS_RECORD_READ_OWN,
        AGENDA_APPOINTMENT_READ, AGENDA_APPOINTMENT_CREATE, AGENDA_APPOINTMENT_UPDATE,
        AGENDA_APPOINTMENT_DELETE, AGENDA_APPOINTMENT_READ_OWN, AGENDA_SERVICE_MANAGE,
        INVENTORY_PRODUCT_READ, INVENTORY_PRODUCT_MANAGE, INVENTORY_STOCK_ADJUST, INVENTORY_MOVEMENT_READ,
        SALES_SALE_READ, SALES_SALE_CREATE, SALES_SALE_UPDATE, SALES_SALE_CHARGE, SALES_SALE_REFUND,
        SALES_SALE_READ_OWN, SALES_SALE_EXPORT,
        CHAT_CONVERSATION_READ, CHAT_CONVERSATION_CREATE, CHAT_MESSAGE_SEND,
        CHAT_CONVERSATION_MODERATE, CHAT_BOT_MANAGE,
        DOCUMENTS_FILE_READ, DOCUMENTS_FILE_UPLOAD, DOCUMENTS_FILE_DELETE, DOCUMENTS_FILE_READ_OWN,
        TASKS_TASK_READ, TASKS_TASK_CREATE, TASKS_TASK_UPDATE, TASKS_TASK_DELETE,
        TASKS_TASK_EXPORT, TASKS_TASK_APPROVE, TASKS_TASK_READ_OWN,
        CRM_OPPORTUNITY_READ, CRM_OPPORTUNITY_MANAGE, CRM_ACTIVITY_MANAGE, CRM_PIPELINE_MANAGE,
        DASHBOARD_KPIS_READ, DASHBOARD_KPIS_READ_OWN, DASHBOARD_LAYOUT_UPDATE,
        SETTINGS_ORG_READ, SETTINGS_ORG_UPDATE, SETTINGS_MEMBERS_MANAGE, SETTINGS_ROLES_MANAGE,
        SETTINGS_CATEGORIES_MANAGE, SETTINGS_PACKS_MANAGE,
        PLATFORM_ORG_CREATE, PLATFORM_ORG_SUSPEND, PLATFORM_ORG_READ_ALL,
        PLATFORM_USER_IMPERSONATE, PLATFORM_BILLING_MANAGE
    ) + EnginePermissionKeys.allKeys()

    fun parse(key: String): Permission {
        val parts = key.split(".")
        val domain = parts.getOrNull(0).orEmpty()
        // Engine keys: module.action (2 parts). Legacy: domain.resource.action (3+).
        val resource = if (parts.size == 2) "module" else parts.getOrNull(1).orEmpty()
        val action = if (parts.size == 2) parts.getOrNull(1).orEmpty() else parts.getOrNull(2).orEmpty()
        return Permission(
            key = key,
            domain = domain,
            resource = resource,
            action = action,
            description = key,
            isPlatform = domain == "platform",
            module = domain
        )
    }

    fun catalog(): List<Permission> = allKeys().distinct().map { parse(it) }
}

/**
 * Default permission bundles for base roles (ROLE_SYSTEM §5.4).
 */
object BaseRolePermissionBundles {

    private val PLATFORM: Set<String> = setOf(
        PermissionKeys.PLATFORM_ORG_CREATE,
        PermissionKeys.PLATFORM_ORG_SUSPEND,
        PermissionKeys.PLATFORM_ORG_READ_ALL,
        PermissionKeys.PLATFORM_USER_IMPERSONATE,
        PermissionKeys.PLATFORM_BILLING_MANAGE
    )

    private val SETTINGS_FULL: Set<String> = setOf(
        PermissionKeys.SETTINGS_ORG_READ,
        PermissionKeys.SETTINGS_ORG_UPDATE,
        PermissionKeys.SETTINGS_MEMBERS_MANAGE,
        PermissionKeys.SETTINGS_ROLES_MANAGE,
        PermissionKeys.SETTINGS_CATEGORIES_MANAGE,
        PermissionKeys.SETTINGS_PACKS_MANAGE
    )

    private val ENGINE_FULL: Set<String> = EnginePermissionKeys.allKeys().toSet()

    private val BUSINESS_FULL: Set<String> = setOf(
        PermissionKeys.CLIENTS_CLIENT_READ,
        PermissionKeys.CLIENTS_CLIENT_CREATE,
        PermissionKeys.CLIENTS_CLIENT_UPDATE,
        PermissionKeys.CLIENTS_CLIENT_DELETE,
        PermissionKeys.CLIENTS_CONTACT_MANAGE,
        PermissionKeys.RECORDS_RECORD_READ,
        PermissionKeys.RECORDS_RECORD_CREATE,
        PermissionKeys.RECORDS_RECORD_UPDATE,
        PermissionKeys.RECORDS_RECORD_FINALIZE,
        PermissionKeys.RECORDS_RECORD_DELETE,
        PermissionKeys.RECORDS_RECORD_EXPORT,
        PermissionKeys.AGENDA_APPOINTMENT_READ,
        PermissionKeys.AGENDA_APPOINTMENT_CREATE,
        PermissionKeys.AGENDA_APPOINTMENT_UPDATE,
        PermissionKeys.AGENDA_APPOINTMENT_DELETE,
        PermissionKeys.AGENDA_SERVICE_MANAGE,
        PermissionKeys.INVENTORY_PRODUCT_READ,
        PermissionKeys.INVENTORY_PRODUCT_MANAGE,
        PermissionKeys.INVENTORY_STOCK_ADJUST,
        PermissionKeys.INVENTORY_MOVEMENT_READ,
        PermissionKeys.SALES_SALE_READ,
        PermissionKeys.SALES_SALE_CREATE,
        PermissionKeys.SALES_SALE_UPDATE,
        PermissionKeys.SALES_SALE_CHARGE,
        PermissionKeys.SALES_SALE_REFUND,
        PermissionKeys.SALES_SALE_EXPORT,
        PermissionKeys.CHAT_CONVERSATION_READ,
        PermissionKeys.CHAT_CONVERSATION_CREATE,
        PermissionKeys.CHAT_MESSAGE_SEND,
        PermissionKeys.CHAT_CONVERSATION_MODERATE,
        PermissionKeys.CHAT_BOT_MANAGE,
        PermissionKeys.DOCUMENTS_FILE_READ,
        PermissionKeys.DOCUMENTS_FILE_UPLOAD,
        PermissionKeys.DOCUMENTS_FILE_DELETE,
        PermissionKeys.TASKS_TASK_READ,
        PermissionKeys.TASKS_TASK_CREATE,
        PermissionKeys.TASKS_TASK_UPDATE,
        PermissionKeys.TASKS_TASK_DELETE,
        PermissionKeys.TASKS_TASK_EXPORT,
        PermissionKeys.TASKS_TASK_APPROVE,
        PermissionKeys.CRM_OPPORTUNITY_READ,
        PermissionKeys.CRM_OPPORTUNITY_MANAGE,
        PermissionKeys.CRM_ACTIVITY_MANAGE,
        PermissionKeys.CRM_PIPELINE_MANAGE,
        PermissionKeys.DASHBOARD_KPIS_READ,
        PermissionKeys.DASHBOARD_LAYOUT_UPDATE
    ) + ENGINE_FULL

    private val MANAGER: Set<String> = BUSINESS_FULL + setOf(
        PermissionKeys.SETTINGS_ORG_READ,
        PermissionKeys.SETTINGS_CATEGORIES_MANAGE
    )

    private val ENGINE_EMPLOYEE: Set<String> = setOf(
        EnginePermissionKeys.CLIENTS_VIEW, EnginePermissionKeys.CLIENTS_CREATE, EnginePermissionKeys.CLIENTS_EDIT,
        EnginePermissionKeys.DOCUMENTS_VIEW, EnginePermissionKeys.DOCUMENTS_CREATE, EnginePermissionKeys.DOCUMENTS_EDIT,
        EnginePermissionKeys.SALES_VIEW, EnginePermissionKeys.SALES_CREATE, EnginePermissionKeys.SALES_EDIT,
        EnginePermissionKeys.INVENTORY_VIEW,
        EnginePermissionKeys.CHAT_VIEW, EnginePermissionKeys.CHAT_CREATE,
        EnginePermissionKeys.TASKS_VIEW, EnginePermissionKeys.TASKS_CREATE, EnginePermissionKeys.TASKS_EDIT
    )

    private val ENGINE_CLIENT: Set<String> = setOf(
        EnginePermissionKeys.CLIENTS_VIEW,
        EnginePermissionKeys.DOCUMENTS_VIEW,
        EnginePermissionKeys.SALES_VIEW,
        EnginePermissionKeys.CHAT_VIEW, EnginePermissionKeys.CHAT_CREATE,
        EnginePermissionKeys.TASKS_VIEW
    )

    private val EMPLOYEE: Set<String> = setOf(
        PermissionKeys.CLIENTS_CLIENT_READ,
        PermissionKeys.CLIENTS_CLIENT_CREATE,
        PermissionKeys.CLIENTS_CLIENT_UPDATE,
        PermissionKeys.CLIENTS_CONTACT_MANAGE,
        PermissionKeys.RECORDS_RECORD_READ,
        PermissionKeys.RECORDS_RECORD_CREATE,
        PermissionKeys.RECORDS_RECORD_UPDATE,
        PermissionKeys.AGENDA_APPOINTMENT_READ,
        PermissionKeys.AGENDA_APPOINTMENT_CREATE,
        PermissionKeys.AGENDA_APPOINTMENT_UPDATE,
        PermissionKeys.AGENDA_APPOINTMENT_DELETE,
        PermissionKeys.INVENTORY_PRODUCT_READ,
        PermissionKeys.SALES_SALE_READ,
        PermissionKeys.SALES_SALE_CREATE,
        PermissionKeys.SALES_SALE_UPDATE,
        PermissionKeys.CHAT_CONVERSATION_READ,
        PermissionKeys.CHAT_CONVERSATION_CREATE,
        PermissionKeys.CHAT_MESSAGE_SEND,
        PermissionKeys.DOCUMENTS_FILE_READ,
        PermissionKeys.DOCUMENTS_FILE_UPLOAD,
        PermissionKeys.TASKS_TASK_READ,
        PermissionKeys.TASKS_TASK_CREATE,
        PermissionKeys.TASKS_TASK_UPDATE,
        PermissionKeys.DASHBOARD_KPIS_READ
    ) + ENGINE_EMPLOYEE

    private val CLIENT: Set<String> = setOf(
        PermissionKeys.CLIENTS_CLIENT_READ_OWN,
        PermissionKeys.RECORDS_RECORD_READ_OWN,
        PermissionKeys.AGENDA_APPOINTMENT_READ_OWN,
        PermissionKeys.AGENDA_APPOINTMENT_CREATE,
        PermissionKeys.SALES_SALE_READ_OWN,
        PermissionKeys.DOCUMENTS_FILE_READ_OWN,
        PermissionKeys.CHAT_CONVERSATION_READ,
        PermissionKeys.CHAT_CONVERSATION_CREATE,
        PermissionKeys.CHAT_MESSAGE_SEND,
        PermissionKeys.TASKS_TASK_READ_OWN,
        PermissionKeys.DASHBOARD_KPIS_READ_OWN
    ) + ENGINE_CLIENT

    private val ADMIN: Set<String> = BUSINESS_FULL + SETTINGS_FULL

    private val SUPER_ADMIN: Set<String> = PLATFORM + ADMIN

    fun forRole(code: String): Set<String> = when (code) {
        BaseRoleCodes.SUPER_ADMIN -> SUPER_ADMIN
        BaseRoleCodes.ADMIN -> ADMIN
        BaseRoleCodes.MANAGER -> MANAGER
        BaseRoleCodes.EMPLOYEE -> EMPLOYEE
        BaseRoleCodes.CLIENT -> CLIENT
        else -> emptySet()
    }

    fun systemRoles(): List<Role> = listOf(
        Role(
            id = BaseRoleCodes.SUPER_ADMIN,
            code = BaseRoleCodes.SUPER_ADMIN,
            name = "Super administrador",
            description = "Operación cross-tenant de la plataforma NexoGo",
            companyId = null,
            permissionKeys = SUPER_ADMIN.toList().sorted(),
            isSystem = true
        ),
        Role(
            id = BaseRoleCodes.ADMIN,
            code = BaseRoleCodes.ADMIN,
            name = "Administrador",
            description = "Control total de una empresa",
            companyId = null,
            permissionKeys = ADMIN.toList().sorted(),
            isSystem = true
        ),
        Role(
            id = BaseRoleCodes.MANAGER,
            code = BaseRoleCodes.MANAGER,
            name = "Manager",
            description = "Liderazgo operativo de módulos de negocio",
            companyId = null,
            permissionKeys = MANAGER.toList().sorted(),
            isSystem = true
        ),
        Role(
            id = BaseRoleCodes.EMPLOYEE,
            code = BaseRoleCodes.EMPLOYEE,
            name = "Empleado",
            description = "Operación diaria de staff",
            companyId = null,
            permissionKeys = EMPLOYEE.toList().sorted(),
            isSystem = true
        ),
        Role(
            id = BaseRoleCodes.CLIENT,
            code = BaseRoleCodes.CLIENT,
            name = "Cliente",
            description = "Portal de recursos propios",
            companyId = null,
            permissionKeys = CLIENT.toList().sorted(),
            isSystem = true
        )
    )
}
