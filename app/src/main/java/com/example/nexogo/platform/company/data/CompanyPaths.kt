package com.example.nexogo.platform.company.data

/**
 * Canonical Firestore and Storage paths for Company tenancy.
 * Does not alter legacy root collections.
 */
object CompanyPaths {
    const val COMPANIES = "companies"
    const val COMPANY_INDEX = "company_index"
    const val USERS = "users"

    const val SETTINGS = "settings"
    const val SETTINGS_MAIN = "main"
    const val MEMBERSHIPS = "memberships"
    const val USER_COMPANY_MEMBERSHIPS = "company_memberships"
    /** Root collection: pending/accepted invites discoverable by invitee email. */
    const val COMPANY_INVITES = "company_invites"

    /** Storage root prefix: companies/{companyId}/... */
    const val STORAGE_ROOT = "companies"

    fun companyDoc(companyId: String): String = "$COMPANIES/$companyId"

    fun settingsDoc(companyId: String): String =
        "$COMPANIES/$companyId/$SETTINGS/$SETTINGS_MAIN"

    fun membershipDoc(companyId: String, userId: String): String =
        "$COMPANIES/$companyId/$MEMBERSHIPS/$userId"

    fun indexDoc(companyId: String): String = "$COMPANY_INDEX/$companyId"

    fun userCompanyMembershipDoc(userId: String, companyId: String): String =
        "$USERS/$userId/$USER_COMPANY_MEMBERSHIPS/$companyId"

    fun storagePrefix(companyId: String): String = "$STORAGE_ROOT/$companyId"

    fun brandingLogoPath(companyId: String, fileName: String = "logo.png"): String =
        "${storagePrefix(companyId)}/branding/$fileName"
}

/**
 * Mirror under users/{uid}/company_memberships/{companyId}
 * for listing a user's companies without collection-group queries.
 */
data class UserCompanyRef(
    val companyId: String = "",
    val companyName: String = "",
    val status: String = "",
    val roleCodes: List<String> = emptyList(),
    val isDefault: Boolean = false
)

