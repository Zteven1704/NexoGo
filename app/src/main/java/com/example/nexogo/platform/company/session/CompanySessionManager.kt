package com.example.nexogo.platform.company.session

import android.content.Context
import android.util.Log
import com.example.nexogo.platform.company.data.CompanyRepository
import com.example.nexogo.platform.company.model.Company
import com.example.nexogo.platform.company.model.CompanyLimits
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.CompanyPlans
import com.example.nexogo.platform.company.model.IndustryPacks
import com.example.nexogo.platform.company.model.MembershipStatus
import com.example.nexogo.platform.company.onboarding.OnboardingDraft
import com.example.nexogo.platform.company.onboarding.OnboardingPlanCatalog
import com.example.nexogo.platform.role.data.RoleRepository
import com.example.nexogo.platform.role.model.AssignmentStatus
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.model.UserRoleAssignment
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.UsageAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the active [Company] for the signed-in user.
 * S1 Spine: hub requires [CompanySessionState.hasActiveCompany] (ACTIVE membership).
 * Onboarding: no silent auto-create — [completeOnboarding] creates company from wizard.
 */
class CompanySessionManager private constructor(
    context: Context,
    private val repository: CompanyRepository = CompanyRepository(),
    private val roleRepository: RoleRepository = RoleRepository()
) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private val _session = MutableStateFlow(CompanySessionState())
    val session: StateFlow<CompanySessionState> = _session.asStateFlow()

    val activeCompanyId: String?
        get() = _session.value.companyId

    val activeCompany: Company?
        get() = _session.value.company

    /**
     * Restores last / default ACTIVE membership. Does **not** auto-create a company
     * (use [completeOnboarding] / CreateCompanyFlow). Optional [autoCreateIfMissing]
     * kept for legacy/tests only.
     */
    suspend fun ensureSessionForUser(
        userId: String,
        displayName: String,
        autoCreateIfMissing: Boolean = false
    ): CompanySessionState {
        if (userId.isBlank()) {
            clearSession()
            return _session.value
        }
        return mutex.withLock {
            try {
                val lastId = prefs.getString(keyLastCompany(userId), null)
                val refs = repository.listUserCompanyRefs(userId).getOrElse { emptyList() }

                val preferredId = when {
                    !lastId.isNullOrBlank() && refs.any { it.companyId == lastId } -> lastId
                    refs.any { it.isDefault } -> refs.first { it.isDefault }.companyId
                    refs.isNotEmpty() -> refs.first().companyId
                    else -> null
                }

                if (!preferredId.isNullOrBlank()) {
                    val bound = bindCompany(userId, preferredId)
                    if (bound.hasActiveCompany) {
                        return@withLock bound
                    }
                    // Do not fall through to create another tenant when bind fails.
                    Log.w(TAG, "Preferred company bind failed: ${bound.errorMessage}")
                }

                if (autoCreateIfMissing) {
                    val companyName = buildDefaultCompanyName(displayName)
                    val created = repository.createCompany(
                        name = companyName,
                        createdByUserId = userId,
                        industryPacks = listOf(IndustryPacks.VETERINARY),
                        planId = CompanyPlans.FREE
                    ).getOrThrow()
                    return@withLock bindCompany(userId, created.id)
                }

                // Needs onboarding — ready without company
                val waiting = CompanySessionState(isReady = true, errorMessage = null)
                _session.value = waiting
                TenantContext.clear()
                waiting
            } catch (e: Exception) {
                Log.e(TAG, "ensureSessionForUser failed: ${e.message}")
                val failed = CompanySessionState(
                    isReady = true,
                    errorMessage = e.message ?: "No se pudo establecer la empresa"
                )
                _session.value = failed
                TenantContext.clear()
                failed
            }
        }
    }

    /**
     * Commits [OnboardingDraft]: creates company + ADMIN membership + binds session.
     */
    suspend fun completeOnboarding(
        userId: String,
        draft: OnboardingDraft
    ): Result<CompanySessionState> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("Usuario requerido"))
        }
        if (!draft.canProceedCompany || !draft.canProceedAdmin) {
            return Result.failure(IllegalArgumentException("Completa empresa y administrador"))
        }
        return mutex.withLock {
            try {
                val limits = OnboardingPlanCatalog.limitsFor(draft.planId)
                val created = repository.createCompany(
                    name = draft.companyName.trim(),
                    createdByUserId = userId,
                    industryPacks = listOf(draft.industryPack).filter { it.isNotBlank() },
                    planId = draft.planId.ifBlank { CompanyPlans.FREE },
                    legalName = draft.legalName,
                    primaryContactEmail = draft.primaryContactEmail,
                    limits = limits,
                    adminTitle = draft.adminTitle
                ).getOrThrow()
                val bound = bindCompany(userId, created.id)
                if (!bound.hasActiveCompany) {
                    Result.failure(
                        IllegalStateException(bound.errorMessage ?: "No se pudo activar la empresa")
                    )
                } else {
                    Result.success(bound)
                }
            } catch (e: Exception) {
                Log.e(TAG, "completeOnboarding failed: ${e.message}")
                Result.failure(e)
            }
        }
    }

    suspend fun setActiveCompany(userId: String, companyId: String): CompanySessionState {
        return mutex.withLock {
            try {
                bindCompany(userId, companyId)
            } catch (e: Exception) {
                Log.w(TAG, "setActiveCompany failed: ${e.message}")
                _session.value.copy(errorMessage = e.message, isReady = true).also {
                    _session.value = it
                }
            }
        }
    }

    fun clearSession() {
        _session.value = CompanySessionState(isReady = true)
        TenantContext.clear()
    }

    private suspend fun bindCompany(userId: String, companyId: String): CompanySessionState {
        val company = repository.getCompany(companyId).getOrNull()
        if (company == null) {
            return failReady("Company not found: $companyId")
        }

        val membership = repository.getMembership(companyId, userId).getOrNull()
        if (membership == null || membership.status != MembershipStatus.ACTIVE) {
            return failReady("Membership ACTIVE required for company $companyId")
        }

        prefs.edit().putString(keyLastCompany(userId), companyId).apply()

        bootstrapRoles(userId, companyId, membership)

        val state = CompanySessionState(
            companyId = company.id,
            company = company,
            membership = membership,
            isReady = true,
            errorMessage = null
        )
        _session.value = state
        TenantContext.bind(companyId = company.id, userId = userId)
        UsageAnalytics.activeUser(userId = userId, companyId = company.id)
        Log.d(TAG, "Active company=${company.id} name=${company.name}")
        return state
    }

    private suspend fun bootstrapRoles(
        userId: String,
        companyId: String,
        membership: CompanyMembership
    ) {
        roleRepository.seedPlatformFoundation().onFailure {
            Log.w(TAG, "seedPlatformFoundation: ${it.message}")
        }
        val roleCodes = membership.roleCodes
            .filter { it.isNotBlank() }
            .ifEmpty { listOf(BaseRoleCodes.ADMIN) }
        roleRepository.upsertAssignment(
            UserRoleAssignment(
                id = userId,
                userId = userId,
                companyId = companyId,
                roleCodes = roleCodes,
                status = AssignmentStatus.ACTIVE,
                assignedBy = userId
            )
        ).onFailure {
            Log.w(TAG, "upsertAssignment: ${it.message}")
        }
    }

    private fun failReady(message: String): CompanySessionState {
        val failed = CompanySessionState(isReady = true, errorMessage = message)
        _session.value = failed
        TenantContext.clear()
        return failed
    }

    private fun buildDefaultCompanyName(displayName: String): String {
        val trimmed = displayName.trim()
        return if (trimmed.isEmpty() || trimmed.equals("Usuario", ignoreCase = true)) {
            "Mi Empresa NexoGo"
        } else {
            "Empresa de $trimmed"
        }
    }

    companion object {
        private const val TAG = "CompanySession"
        private const val PREFS_NAME = "nexogo_company_session"
        private fun keyLastCompany(userId: String) = "last_company_$userId"

        @Volatile
        private var INSTANCE: CompanySessionManager? = null

        fun getInstance(context: Context): CompanySessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CompanySessionManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
