package com.example.nexogo.platform.role.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.company.ui.LocalCompanySession
import com.example.nexogo.platform.role.data.RoleRepository
import com.example.nexogo.platform.role.model.Role
import com.example.nexogo.platform.role.nav.NavPermissionFactory
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * Lists platform system roles + company custom roles (RoleRepository).
 * Tenant-scoped via [LocalActiveCompany]; seed is admin-only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformRolesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { RoleRepository() }
    val company = LocalActiveCompany.current
    val companySession = LocalCompanySession.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var systemRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var customRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var seeding by remember { mutableStateOf(false) }

    val isAdmin = remember(currentUser?.role, companySession.membership?.roleCodes) {
        NavPermissionFactory.showAdmin(
            NavPermissionFactory.forHub(currentUser?.role, companySession.membership)
        )
    }

    fun reload() {
        scope.launch {
            loading = true
            message = null
            systemRoles = repo.listSystemRoles().getOrElse { emptyList() }
            val cid = company?.id
            customRoles = if (!cid.isNullOrBlank()) {
                repo.listCustomRoles(cid).getOrElse { emptyList() }
            } else {
                emptyList()
            }
            loading = false
        }
    }

    LaunchedEffect(company?.id) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Roles")
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
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isAdmin) {
                Button(
                    onClick = {
                        scope.launch {
                            seeding = true
                            message = null
                            val result = repo.seedPlatformFoundation()
                            seeding = false
                            message = if (result.isSuccess) {
                                "Catálogo y roles base sembrados."
                            } else {
                                result.exceptionOrNull()?.message ?: "Error al sembrar"
                            }
                            reload()
                        }
                    },
                    enabled = !seeding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (seeding) "Sembrando…" else "Sembrar roles y permisos base")
                }
                Spacer(Modifier.height(8.dp))
            }

            message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if (loading) {
                BoxCentered { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text("Roles del sistema", style = MaterialTheme.typography.titleMedium)
                    }
                    items(systemRoles, key = { it.code.ifBlank { it.id } }) { role ->
                        RoleCard(role, system = true)
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("Roles de la empresa", style = MaterialTheme.typography.titleMedium)
                    }
                    if (customRoles.isEmpty()) {
                        item {
                            Text(
                                "Sin roles personalizados. Usa roles del sistema en Usuarios.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(customRoles, key = { it.id }) { role ->
                            RoleCard(role, system = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleCard(role: Role, system: Boolean) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(role.name.ifBlank { role.code }, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (system) "Sistema" else "Empresa",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (role.code.isNotBlank()) {
                Text(role.code, style = MaterialTheme.typography.labelMedium)
            }
            if (role.description.isNotBlank()) {
                Text(role.description, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${role.permissionKeys.size} permisos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BoxCentered(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}
