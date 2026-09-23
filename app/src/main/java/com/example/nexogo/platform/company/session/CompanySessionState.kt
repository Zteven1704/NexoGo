package com.example.nexogo.platform.company.session

import com.example.nexogo.platform.company.model.Company
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.MembershipStatus

/**
 * Snapshot of the active company session for UI / repos.
 */
data class CompanySessionState(
    val companyId: String? = null,
    val company: Company? = null,
    val membership: CompanyMembership? = null,
    val isReady: Boolean = false,
    val errorMessage: String? = null
) {
    val hasActiveCompany: Boolean
        get() = !companyId.isNullOrBlank() &&
            company != null &&
            membership != null &&
            membership.status == MembershipStatus.ACTIVE

    /** Ready session without ACTIVE company → show CreateCompanyFlow. */
    val needsOnboarding: Boolean
        get() = isReady && !hasActiveCompany
}
