package com.example.nexogo.platform.users.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.platform.company.model.CompanyMembership
import com.example.nexogo.platform.company.model.MembershipStatus
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.company.ui.LocalCompanySession
import com.example.nexogo.platform.role.model.BaseRoleCodes
import com.example.nexogo.platform.role.nav.NavPermissionFactory
import com.example.nexogo.platform.users.data.UserManagementRepository
import com.example.nexogo.platform.users.viewmodel.UserManagementViewModel
import com.example.nexogo.viewmodel.PersistentAuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformUserManagementScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { CompanySessionManager.getInstance(context) }
    val vm = remember { UserManagementViewModel(sessionManager = sessionManager) }
    val company = LocalActiveCompany.current
    val companySession = LocalCompanySession.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    var showInvite by remember { mutableStateOf(false) }
    var inviteEmail by remember { mutableStateOf("") }
    var inviteName by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf(BaseRoleCodes.EMPLOYEE) }

    LaunchedEffect(company?.id, companySession.membership?.roleCodes) {
        val cid = company?.id
        if (!cid.isNullOrBlank()) {
            val engine = NavPermissionFactory.forHub(
                currentUser?.role,
                companySession.membership
            )
            vm.bind(cid, company?.name.orEmpty(), engine)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Usuarios")
                        company?.name?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (ui.canManage) {
                FloatingActionButton(onClick = { showInvite = !showInvite }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Invitar")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!ui.canManage) {
                Text(
                    ui.error ?: "Solo administradores pueden gestionar usuarios.",
                    color = MaterialTheme.colorScheme.error
                )
                return@Column
            }

            if (showInvite) {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Invitar usuario", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = inviteEmail,
                            onValueChange = { inviteEmail = it },
                            label = { Text("Email *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = inviteName,
                            onValueChange = { inviteName = it },
                            label = { Text("Nombre (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text("Rol", style = MaterialTheme.typography.labelLarge)
                        UserManagementRepository.ASSIGNABLE_ROLES.forEach { role ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = inviteRole == role,
                                        onClick = { inviteRole = role },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = inviteRole == role,
                                    onClick = { inviteRole = role }
                                )
                                Text(role, Modifier.padding(start = 8.dp))
                            }
                        }
                        Button(
                            onClick = {
                                vm.invite(
                                    email = inviteEmail,
                                    roleCode = inviteRole,
                                    displayName = inviteName,
                                    invitedBy = currentUser?.id.orEmpty(),
                                    invitedByName = currentUser?.name.orEmpty()
                                )
                                inviteEmail = ""
                                inviteName = ""
                                showInvite = false
                            },
                            enabled = inviteEmail.contains("@") && !ui.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enviar invitación") }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            ui.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if (ui.isLoading && ui.members.isEmpty()) {
                BoxLoading()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ui.invites.isNotEmpty()) {
                        item {
                            Text(
                                "Invitaciones pendientes",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        items(ui.invites, key = { it.id }) { inv ->
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(inv.email, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "Rol: ${inv.roleCodes.joinToString()} · ${inv.status}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    TextButton(onClick = { vm.revokeInvite(inv.id) }) {
                                        Text("Revocar invitación")
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                    item {
                        Text("Miembros", style = MaterialTheme.typography.titleSmall)
                    }
                    items(ui.members, key = { it.userId }) { member ->
                        MemberCard(
                            member = member,
                            statusLabel = vm.statusLabel(member.status),
                            currentUserId = currentUser?.id,
                            onActivate = {
                                vm.activate(member.userId, currentUser?.id.orEmpty())
                            },
                            onDeactivate = {
                                vm.deactivate(member.userId, currentUser?.id.orEmpty())
                            },
                            onRemove = {
                                vm.removeAccess(member.userId, currentUser?.id.orEmpty())
                            },
                            onChangeRole = { role ->
                                vm.changeRole(member.userId, role, currentUser?.id.orEmpty())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxLoading() {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MemberCard(
    member: CompanyMembership,
    statusLabel: String,
    currentUserId: String?,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onRemove: () -> Unit,
    onChangeRole: (String) -> Unit
) {
    val primaryRole = member.roleCodes.firstOrNull() ?: BaseRoleCodes.EMPLOYEE
    val isSelf = member.userId == currentUserId

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                member.displayName.ifBlank { member.email.ifBlank { member.userId } },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "${member.email.ifBlank { "—" }} · $statusLabel · $primaryRole",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (member.status != MembershipStatus.REVOKED) {
                Text("Cambiar rol", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UserManagementRepository.ASSIGNABLE_ROLES.forEach { role ->
                        if (role == primaryRole) {
                            Button(onClick = {}, enabled = false) { Text(role, style = MaterialTheme.typography.labelSmall) }
                        } else {
                            OutlinedButton(onClick = { onChangeRole(role) }) {
                                Text(role, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (member.status) {
                    MembershipStatus.ACTIVE -> {
                        OutlinedButton(
                            onClick = onDeactivate,
                            enabled = !isSelf
                        ) { Text("Desactivar") }
                    }
                    MembershipStatus.SUSPENDED, MembershipStatus.PENDING, MembershipStatus.INVITED -> {
                        Button(onClick = onActivate) { Text("Activar") }
                    }
                    MembershipStatus.REVOKED -> {
                        Button(onClick = onActivate) { Text("Reactivar") }
                    }
                }
                if (member.status != MembershipStatus.REVOKED) {
                    TextButton(
                        onClick = onRemove,
                        enabled = !isSelf
                    ) { Text("Eliminar acceso") }
                }
            }
            if (isSelf) {
                Text(
                    "No puedes desactivarte ni eliminarte a ti mismo.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
