package com.example.nexogo.platform.company.model

import com.google.firebase.Timestamp

/**
 * Root path: company_invites/{inviteId}
 * Discoverable by invitee Auth email without prior membership.
 */
data class CompanyInvite(
    val id: String = "",
    val companyId: String = "",
    val companyName: String = "",
    val email: String = "",
    val displayName: String = "",
    val roleCodes: List<String> = listOf(CompanyRoleCodes.EMPLOYEE),
    val status: InviteStatus = InviteStatus.PENDING,
    val invitedBy: String = "",
    val invitedByName: String = "",
    val acceptedByUserId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    REVOKED,
    EXPIRED
}
