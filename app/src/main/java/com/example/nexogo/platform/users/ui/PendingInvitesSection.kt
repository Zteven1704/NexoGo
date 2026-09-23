package com.example.nexogo.platform.users.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.users.viewmodel.PendingInvitesViewModel
import com.example.nexogo.viewmodel.PersistentAuthViewModel

/**
 * Shows pending company invites for the signed-in email (join without CreateCompanyFlow).
 */
@Composable
fun PendingInvitesSection(
    onJoined: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { CompanySessionManager.getInstance(context) }
    val vm = remember { PendingInvitesViewModel(sessionManager = sessionManager) }
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser?.email) {
        val email = currentUser?.email
        if (!email.isNullOrBlank()) vm.load(email)
    }

    if (ui.invites.isEmpty() && ui.error == null) return

    Column(
        Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Invitaciones a empresas", style = MaterialTheme.typography.titleMedium)
        ui.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        ui.message?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        ui.invites.forEach { invite ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(invite.companyName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Rol: ${invite.roleCodes.joinToString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val user = currentUser ?: return@Button
                            vm.accept(
                                invite = invite,
                                userId = user.id,
                                email = user.email,
                                displayName = user.name,
                                onBound = onJoined
                            )
                        },
                        enabled = !ui.isLoading
                    ) { Text("Aceptar e ingresar") }
                }
            }
        }
    }
}
