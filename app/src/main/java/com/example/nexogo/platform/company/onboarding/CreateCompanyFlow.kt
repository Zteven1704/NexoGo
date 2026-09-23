package com.example.nexogo.platform.company.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.company.session.CompanySessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCompanyFlowUiState(
    val step: OnboardingStep = OnboardingStep.COMPANY_SETUP,
    val draft: OnboardingDraft = OnboardingDraft(),
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false
)

/**
 * Orchestrates CompanySetupWizard → plan → InitialAdminSetup → session bind.
 */
class CreateCompanyFlow(
    private val sessionManager: CompanySessionManager,
    private val userIdProvider: () -> String,
    private val emailProvider: () -> String = { "" },
    private val displayNameProvider: () -> String = { "" }
) : ViewModel() {

    private val _ui = MutableStateFlow(
        CreateCompanyFlowUiState(
            draft = OnboardingDraft(
                adminDisplayName = displayNameProvider().trim(),
                primaryContactEmail = emailProvider().trim()
            )
        )
    )
    val uiState: StateFlow<CreateCompanyFlowUiState> = _ui.asStateFlow()

    fun updateDraft(transform: (OnboardingDraft) -> OnboardingDraft) {
        _ui.update { it.copy(draft = transform(it.draft), error = null) }
    }

    fun setDraft(draft: OnboardingDraft) {
        _ui.update { it.copy(draft = draft, error = null) }
    }

    fun goToPlan() {
        val draft = _ui.value.draft
        if (!draft.canProceedCompany) {
            _ui.update { it.copy(error = "Ingresa un nombre de empresa (mín. 2 caracteres)") }
            return
        }
        _ui.update { it.copy(step = OnboardingStep.PLAN_SELECTION, error = null) }
    }

    fun goToAdmin() {
        _ui.update { it.copy(step = OnboardingStep.ADMIN_SETUP, error = null) }
    }

    fun back() {
        _ui.update { state ->
            val prev = when (state.step) {
                OnboardingStep.PLAN_SELECTION -> OnboardingStep.COMPANY_SETUP
                OnboardingStep.ADMIN_SETUP -> OnboardingStep.PLAN_SELECTION
                else -> state.step
            }
            state.copy(step = prev, error = null)
        }
    }

    fun selectPlan(planId: String) {
        _ui.update {
            it.copy(draft = it.draft.copy(planId = planId), error = null)
        }
    }

    fun submit() {
        val draft = _ui.value.draft
        if (!draft.canProceedAdmin) {
            _ui.update { it.copy(error = "Ingresa el nombre del administrador") }
            return
        }
        val userId = userIdProvider()
        if (userId.isBlank()) {
            _ui.update { it.copy(error = "Sesión de usuario inválida — vuelve a iniciar sesión") }
            return
        }
        viewModelScope.launch {
            _ui.update {
                it.copy(step = OnboardingStep.COMPLETING, isSubmitting = true, error = null)
            }
            val result = sessionManager.completeOnboarding(userId, draft)
            if (result.isSuccess) {
                _ui.update {
                    it.copy(
                        step = OnboardingStep.DONE,
                        isSubmitting = false,
                        completed = true
                    )
                }
            } else {
                _ui.update {
                    it.copy(
                        step = OnboardingStep.ADMIN_SETUP,
                        isSubmitting = false,
                        error = result.exceptionOrNull()?.message
                            ?: "No se pudo crear la empresa"
                    )
                }
            }
        }
    }

    companion object {
        fun create(
            context: Context,
            userId: String,
            email: String,
            displayName: String
        ): CreateCompanyFlow {
            val session = CompanySessionManager.getInstance(context)
            return CreateCompanyFlow(
                sessionManager = session,
                userIdProvider = { userId },
                emailProvider = { email },
                displayNameProvider = { displayName }
            )
        }
    }
}
