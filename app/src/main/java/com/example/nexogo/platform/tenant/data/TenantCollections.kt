package com.example.nexogo.platform.tenant.data

/**
 * Future business collection names under companies/{companyId}/…
 * Infrastructure catalog only — legacy root collections are unchanged.
 */
object TenantCollections {
    const val CLIENTS = "clients"
    const val CONTACTS = "contacts"
    const val RECORDS = "records"
    const val RECORD_TEMPLATES = "record_templates"
    const val APPOINTMENTS = "appointments"
    const val SERVICES = "services"
    const val STAFF_SCHEDULES = "staff_schedules"
    const val PRODUCTS = "products"
    const val STOCK_MOVEMENTS = "stock_movements"
    const val CATEGORIES = "categories"
    const val SALES = "sales"
    const val CONVERSATIONS = "conversations"
    const val MESSAGES = "messages"
    const val CHAT_CHANNELS = "chat_channels"
    const val CHAT_GROUPS = "chat_groups"
    const val DOCUMENTS = "documents"
    const val DOCUMENT_VERSIONS = "versions"
    const val DOCUMENT_LINKS = "document_links"
    const val DOCUMENT_FOLDERS = "folders"
    const val DOCUMENT_TAGS = "document_tags"
    const val DOCUMENT_CATEGORIES = "document_categories"
    const val OPPORTUNITIES = "opportunities"
    const val CRM_ACTIVITIES = "crm_activities"
    const val PIPELINE_STAGES = "pipeline_stages"
    const val LEADS = "leads"
    const val CUSTOMER_JOURNEYS = "customer_journeys"
    const val FOLLOW_UPS = "follow_ups"
    const val DASHBOARD_LAYOUTS = "dashboard_layouts"
    const val DASHBOARD_SNAPSHOTS = "dashboard_snapshots"
    const val TASKS = "tasks"
    const val TASK_COMMENTS = "comments"
    const val AI_JOBS = "ai_jobs"
    const val AI_ANALYSES = "ai_analyses"
    const val DOCUMENT_SUMMARIES = "document_summaries"
    const val EXTRACTED_ENTITIES = "extracted_entities"
    const val AUDIT_LOGS = "audit_logs"
    const val USAGE_EVENTS = "usage_events"
    const val USAGE_METRICS = "usage_metrics"
    const val USAGE_ACTIVE_USERS = "usage_active_users"
    const val NOTIFICATIONS = "notifications"
    const val ROLES = "roles"
    const val ROLE_ASSIGNMENTS = "role_assignments"
    const val MEMBERSHIPS = "memberships"
    const val SETTINGS = "settings"

    /** All known tenant-scoped collection segments (for docs / validation). */
    val ALL: List<String> = listOf(
        CLIENTS, CONTACTS, RECORDS, RECORD_TEMPLATES,
        APPOINTMENTS, SERVICES, STAFF_SCHEDULES,
        PRODUCTS, STOCK_MOVEMENTS, CATEGORIES, SALES,
        CONVERSATIONS, CHAT_CHANNELS, CHAT_GROUPS, DOCUMENTS, DOCUMENT_LINKS, DOCUMENT_FOLDERS,
        DOCUMENT_TAGS, DOCUMENT_CATEGORIES,
        OPPORTUNITIES, CRM_ACTIVITIES, PIPELINE_STAGES, LEADS, CUSTOMER_JOURNEYS, FOLLOW_UPS,
        DASHBOARD_LAYOUTS, DASHBOARD_SNAPSHOTS, TASKS,
        AI_JOBS, AI_ANALYSES, DOCUMENT_SUMMARIES, EXTRACTED_ENTITIES,
        AUDIT_LOGS, USAGE_EVENTS, USAGE_METRICS, USAGE_ACTIVE_USERS, NOTIFICATIONS,
        ROLES, ROLE_ASSIGNMENTS, MEMBERSHIPS, SETTINGS
    )
}
