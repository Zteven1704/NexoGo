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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.company.ui.LocalCompanySession
import com.example.nexogo.platform.role.data.RoleRepository
import com.example.nexogo.platform.role.engine.PermissionModule
import com.example.nexogo.platform.role.model.Permission
import com.example.nexogo.platform.role.nav.NavPermissionFactory
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import kotlinx.coroutines.launch

/**
 * Shows permission catalog + effective matrix for the current membership (PermissionEngine).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformPermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { RoleRepository() }
    val company = LocalActiveCompany.current
    val companySession = LocalCompanySession.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var catalog by remember { mutableStateOf<List<Permission>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val engine = remember(currentUser?.role, companySession.membership?.roleCodes) {
        NavPermissionFactory.forHub(currentUser?.role, companySession.membership)
    }
    val roleCodes = companySession.membership?.roleCodes.orEmpty()
        .ifEmpty { listOf("(legacy bridge)") }

    LaunchedEffect(Unit) {
        scope.launch {
            loading = true
            catalog = repo.getPermissionCatalog().getOrElse { emptyList() }
                .sortedBy { it.key }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Permisos")
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
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tu acceso efectivo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Roles: ${roleCodes.joinToString()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    PermissionModule.ALL.forEach { module ->
                        val actions = engine.allowedActions(module)
                        if (actions.isNotEmpty() || engine.canEnter(module)) {
                            Text(
                                "${module.key}: ${
                                    if (actions.isEmpty()) "entrar"
                                    else actions.joinToString { it.key }
                                }",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (engine.isSuperAdmin()) {
                        Text(
                            "SUPER_ADMIN: acceso total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Catálogo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (loading) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(catalog, key = { it.key }) { perm ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(perm.key, style = MaterialTheme.typography.titleSmall)
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        listOf(perm.domain, perm.resource, perm.action)
                                            .filter { it.isNotBlank() }
                                            .joinToString(" · ")
                                            .ifBlank { perm.module },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (perm.description.isNotBlank()) {
                                    Text(perm.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
