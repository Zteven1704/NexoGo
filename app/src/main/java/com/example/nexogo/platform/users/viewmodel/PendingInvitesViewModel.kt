package com.example.nexogo.platform.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.company.model.CompanyInvite
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.users.data.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingInvitesUiState(
    val invites: List<CompanyInvite> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/**
 * Loads / accepts invites for the signed-in user's email (no prior membership required).
 */
class PendingInvitesViewModel(
    private val repository: UserManagementRepository = UserManagementRepository(),
    private val sessionManager: CompanySessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(PendingInvitesUiState())
    val uiState: StateFlow<PendingInvitesUiState> = _ui.asStateFlow()

    fun load(email: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = repository.listPendingInvitesForEmail(email)
            if (result.isSuccess) {
                _ui.update {
                    it.copy(isLoading = false, invites = result.getOrThrow())
                }
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message,
                        invites = emptyList()
                    )
                }
            }
        }
    }

    fun accept(
        invite: CompanyInvite,
        userId: String,
        email: String,
        displayName: String,
        onBound: () -> Unit
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = repository.acceptInvite(invite, userId, email, displayName)
            if (result.isSuccess) {
                sessionManager.setActiveCompany(userId, invite.companyId)
                _ui.update {
                    it.copy(
                        isLoading = false,
                        message = "Te uniste a ${invite.companyName}",
                        invites = it.invites.filter { i -> i.id != invite.id }
                    )
                }
                onBound()
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "No se pudo aceptar"
                    )
                }
            }
        }
    }
}
