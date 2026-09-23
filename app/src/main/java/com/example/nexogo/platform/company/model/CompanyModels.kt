package com.example.nexogo.platform.company.model

import com.google.firebase.Timestamp

/** Alias requested by platform integration (same as [Company]). */
typealias CompanyModel = Company

/**
 * Tenant root for NexoGo SaaS.
 * Paths: companies/{companyId}
 *
 * New platform package — does not replace legacy auth/users yet.
 */
data class Company(
    val id: String = "",
    val name: String = "",
    val legalName: String = "",
    val taxId: String = "",
    val status: CompanyStatus = CompanyStatus.DRAFT,
    val planId: String = CompanyPlans.FREE,
    val industryPacks: List<String> = emptyList(),
    val primaryContactEmail: String = "",
    val phone: String = "",
    val address: CompanyAddress = CompanyAddress(),
    val branding: CompanyBranding = CompanyBranding(),
    val limits: CompanyLimits = CompanyLimits(),
    val flags: CompanyFlags = CompanyFlags(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val deletedAt: Timestamp? = null
)

enum class CompanyStatus {
    DRAFT,
    ACTIVE,
    SUSPENDED,
    DELETED
}

object CompanyPlans {
    const val FREE = "free"
    const val PRO = "pro"
    const val BUSINESS = "business"
    const val ENTERPRISE = "enterprise"
}

object IndustryPacks {
    const val VETERINARY = "veterinary"
    const val CLINIC = "clinic"
    const val WAREHOUSE = "warehouse"
    const val BUSINESS_B2B = "business_b2b"
    const val WORKSHOP = "workshop"
    const val CONSULTING = "consulting"
}

data class CompanyAddress(
    val street: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "",
    val postalCode: String = ""
)

data class CompanyBranding(
    val logoDocumentId: String = "",
    val logoStoragePath: String = "",
    val primaryColor: String = "",
    val locale: String = "es",
    val timezone: String = "America/Bogota"
)

data class CompanyLimits(
    val maxUsers: Int = 5,
    val maxStorageMb: Int = 512,
    val maxAiRequestsMonth: Int = 50,
    val maxClients: Int = 100,
    val maxCustomRoles: Int = 3
)

data class CompanyFlags(
    val aiEnabled: Boolean = false,
    val documentsEnabled: Boolean = true,
    val crmEnabled: Boolean = false
)

/**
 * Lightweight platform index: company_index/{companyId}
 */
data class CompanyIndex(
    val companyId: String = "",
    val name: String = "",
    val status: CompanyStatus = CompanyStatus.DRAFT,
    val planId: String = CompanyPlans.FREE,
    val industryPacks: List<String> = emptyList(),
    val ownerUserId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

/**
 * companies/{companyId}/settings/main
 */
data class CompanySettings(
    val companyId: String = "",
    val paymentMethods: List<String> = listOf("CASH", "CARD", "TRANSFER"),
    val featureToggles: Map<String, Boolean> = emptyMap(),
    val documentMaxUploadMb: Int = 25,
    val documentRetentionDaysDeleted: Int = 30,
    val requireMemberApproval: Boolean = true,
    val autoApproveClientRole: Boolean = false,
    val aiMonthlyRequestQuota: Int = 50,
    val updatedAt: Timestamp = Timestamp.now(),
    val updatedBy: String = ""
)

/**
 * companies/{companyId}/memberships/{userId}
 */
data class CompanyMembership(
    val userId: String = "",
    val companyId: String = "",
    val roleCodes: List<String> = emptyList(),
    val customRoleIds: List<String> = emptyList(),
    val status: MembershipStatus = MembershipStatus.PENDING,
    val scopeType: MembershipScopeType = MembershipScopeType.COMPANY,
    val branchIds: List<String> = emptyList(),
    val linkedClientId: String? = null,
    val isDefault: Boolean = false,
    val title: String = "",
    val email: String = "",
    val displayName: String = "",
    val invitedBy: String = "",
    val approvedBy: String = "",
    /** Set on invite accept so firestore.rules CASO 2 can verify company_invites/{inviteId}. */
    val inviteId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val lastAccessAt: Timestamp? = null
)

enum class MembershipStatus {
    INVITED,
    PENDING,
    ACTIVE,
    SUSPENDED,
    REVOKED
}

enum class MembershipScopeType {
    COMPANY,
    BRANCH,
    TEAM,
    ASSIGNED,
    OWN
}

/**
 * Base role codes for a company membership (platform IAM).
 * Distinct from legacy vet UserRole in core.models.
 */
object CompanyRoleCodes {
    const val SUPER_ADMIN = "SUPER_ADMIN"
    const val ADMIN = "ADMIN"
    const val MANAGER = "MANAGER"
    const val EMPLOYEE = "EMPLOYEE"
    const val CLIENT = "CLIENT"
}
