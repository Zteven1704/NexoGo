package com.example.nexogo.platform.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.company.model.CompanyInvite
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.InviteStatus
import com.example.nexogo.platform.company.model.MembershipStatus
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.role.engine.PermissionEngine
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.nav.NavPermissionFactory
import com.example.nexogo.platform.users.data.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserManagementUiState(
    val companyId: String? = null,
    val companyName: String = "",
    val members: List<CompanyMembership> = emptyList(),
    val invites: List<CompanyInvite> = emptyList(),
    val canManage: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class UserManagementViewModel(
    private val repository: UserManagementRepository = UserManagementRepository(),
    private val sessionManager: CompanySessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(UserManagementUiState())
    val uiState: StateFlow<UserManagementUiState> = _ui.asStateFlow()

    fun bind(
        companyId: String,
        companyName: String,
        permissionEngine: PermissionEngine
    ) {
        val canManage = NavPermissionFactory.showAdmin(permissionEngine)
        _ui.update {
            it.copy(
                companyId = companyId,
                companyName = companyName,
                canManage = canManage,
                error = if (!canManage) "Se requiere rol ADMIN para gestionar usuarios" else null
            )
        }
        if (canManage) refresh()
    }

    fun refresh() {
        val cid = _ui.value.companyId ?: sessionManager.activeCompanyId
        if (cid.isNullOrBlank()) {
            _ui.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val members = repository.listMembers(cid)
            val invites = repository.listCompanyInvites(cid)
            if (members.isFailure) {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = members.exceptionOrNull()?.message ?: "Error al listar miembros"
                    )
                }
                return@launch
            }
            _ui.update {
                it.copy(
                    isLoading = false,
                    members = members.getOrElse { emptyList() },
                    invites = invites.getOrElse { emptyList() }
                        .filter { inv -> inv.status == InviteStatus.PENDING },
                    message = "Equipo actualizado",
                    companyId = cid
                )
            }
        }
    }

    fun invite(
        email: String,
        roleCode: String,
        displayName: String,
        invitedBy: String,
        invitedByName: String
    ) {
        if (!_ui.value.canManage) {
            _ui.update { it.copy(error = "Sin permiso de administración") }
            return
        }
        val cid = _ui.value.companyId ?: return
        val name = _ui.value.companyName
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null, message = null) }
            val result = repository.inviteUser(
                companyId = cid,
                companyName = name,
                email = email,
                roleCode = roleCode.ifBlank { BaseRoleCodes.EMPLOYEE },
                invitedBy = invitedBy,
                invitedByName = invitedByName,
                displayName = displayName
            )
            if (result.isSuccess) {
                _ui.update {
                    it.copy(isLoading = false, message = "Invitación enviada a ${email.trim()}")
                }
                refresh()
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al invitar"
                    )
                }
            }
        }
    }

    fun activate(userId: String, actorId: String) = mutateStatus(userId) {
        repository.activateMember(_ui.value.companyId!!, userId, actorId)
    }

    fun deactivate(userId: String, actorId: String) = mutateStatus(userId) {
        repository.deactivateMember(_ui.value.companyId!!, userId, actorId)
    }

    fun removeAccess(userId: String, actorId: String) = mutateStatus(userId) {
        repository.removeAccess(_ui.value.companyId!!, userId, actorId)
    }

    fun changeRole(userId: String, roleCode: String, actorId: String) {
        if (!_ui.value.canManage) return
        val cid = _ui.value.companyId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = repository.changeRole(cid, userId, roleCode, actorId)
            if (result.isSuccess) {
                _ui.update { it.copy(isLoading = false, message = "Rol actualizado") }
                refresh()
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al cambiar rol"
                    )
                }
            }
        }
    }

    fun revokeInvite(inviteId: String) {
        if (!_ui.value.canManage) return
        viewModelScope.launch {
            repository.revokeInvite(inviteId)
            refresh()
        }
    }

    private fun mutateStatus(userId: String, block: suspend () -> Result<Unit>) {
        if (!_ui.value.canManage) return
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = block()
            if (result.isSuccess) {
                _ui.update { it.copy(isLoading = false, message = "Estado actualizado") }
                refresh()
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error"
                    )
                }
            }
        }
    }

    fun statusLabel(status: MembershipStatus): String = when (status) {
        MembershipStatus.ACTIVE -> "Activo"
        MembershipStatus.SUSPENDED -> "Desactivado"
        MembershipStatus.INVITED -> "Invitado"
        MembershipStatus.PENDING -> "Pendiente"
        MembershipStatus.REVOKED -> "Sin acceso"
    }
}
