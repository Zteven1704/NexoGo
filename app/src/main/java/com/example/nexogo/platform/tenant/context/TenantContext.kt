package com.example.nexogo.platform.tenant.context

import com.example.nexogo.platform.company.session.CompanySessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Active company for the current app session / repository operations.
 * Infrastructure only — not wired to Compose screens beyond existing CompanyProvider.
 */
data class TenantContextSnapshot(
    val companyId: String? = null,
    val userId: String? = null,
    val isReady: Boolean = false
) {
    val hasTenant: Boolean
        get() = !companyId.isNullOrBlank()

    fun requireCompanyId(): String {
        val id = companyId
        require(!id.isNullOrBlank()) {
            "No active company in TenantContext — bind Company session first"
        }
        return id
    }
}

/**
 * Process-wide holder for the active tenant.
 * Updated from [CompanySessionState] when available; safe defaults keep legacy flow running.
 */
object TenantContext {
    private val _snapshot = MutableStateFlow(TenantContextSnapshot())
    val snapshot: StateFlow<TenantContextSnapshot> = _snapshot.asStateFlow()

    val current: TenantContextSnapshot
        get() = _snapshot.value

    val companyId: String?
        get() = _snapshot.value.companyId

    fun bind(companyId: String?, userId: String? = _snapshot.value.userId) {
        _snapshot.value = TenantContextSnapshot(
            companyId = companyId?.takeIf { it.isNotBlank() },
            userId = userId?.takeIf { it.isNotBlank() },
            isReady = true
        )
    }

    fun bindFromSession(session: CompanySessionState, userId: String? = null) {
        bind(
            companyId = session.companyId,
            userId = userId ?: _snapshot.value.userId
        )
    }

    fun clear() {
        _snapshot.value = TenantContextSnapshot(isReady = true)
    }

    fun requireCompanyId(): String = current.requireCompanyId()
}
