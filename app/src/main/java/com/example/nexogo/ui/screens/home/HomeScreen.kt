package com.example.nexogo.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.company.ui.LocalCompanySession
import com.example.nexogo.platform.role.engine.PermissionEngine
import com.example.nexogo.platform.role.nav.NavPermissionFactory
import com.example.nexogo.platform.users.ui.PendingInvitesSection
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Primary hub — platform modules only (Clients, Records, Documents, Users, Roles, Permissions).
 * Legacy routes remain in NavGraph for Settings/deep-link; not linked here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlatformClients: () -> Unit = {},
    onNavigateToPlatformRecords: () -> Unit = {},
    onNavigateToPlatformDocuments: () -> Unit = {},
    onNavigateToPlatformUsers: () -> Unit = {},
    onNavigateToPlatformRoles: () -> Unit = {},
    onNavigateToPlatformPermissions: () -> Unit = {},
    onNavigateToCompanyOnboarding: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: PersistentAuthViewModel = PersistentAuthViewModel.getInstance(LocalContext.current)
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeCompany = LocalActiveCompany.current
    val companySession = LocalCompanySession.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val permissionEngine = remember(currentUser?.role, companySession.membership?.roleCodes) {
        NavPermissionFactory.forHub(currentUser?.role, companySession.membership)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshUserFromDataStore()
    }

    if (!companySession.isReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    if (!companySession.hasActiveCompany) {
        CompanySessionRequiredPane(
            userEmail = currentUser?.email,
            message = companySession.errorMessage
                ?: "Crea tu empresa para empezar a usar NexoGo.",
            onLogout = {
                viewModel.logout()
                onLogout()
            },
            onRetry = {
                currentUser?.let { viewModel.setCurrentUser(it) }
            },
            onCreateCompany = onNavigateToCompanyOnboarding
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentUser = currentUser,
                companyName = activeCompany?.name,
                permissionEngine = permissionEngine,
                scope = scope,
                drawerState = drawerState,
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onNavigateToPlatformClients = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformClients()
                },
                onNavigateToPlatformRecords = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformRecords()
                },
                onNavigateToPlatformDocuments = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformDocuments()
                },
                onNavigateToPlatformUsers = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformUsers()
                },
                onNavigateToPlatformRoles = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformRoles()
                },
                onNavigateToPlatformPermissions = {
                    scope.launch { drawerState.close() }
                    onNavigateToPlatformPermissions()
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                    onLogout()
                }
            )
        },
        gesturesEnabled = true
    ) {
        HomeContent(
            currentUser = currentUser,
            companyName = activeCompany?.name,
            permissionEngine = permissionEngine,
            onOpenDrawer = { scope.launch { drawerState.open() } },
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToPlatformClients = onNavigateToPlatformClients,
            onNavigateToPlatformRecords = onNavigateToPlatformRecords,
            onNavigateToPlatformDocuments = onNavigateToPlatformDocuments,
            onNavigateToPlatformUsers = onNavigateToPlatformUsers,
            onNavigateToPlatformRoles = onNavigateToPlatformRoles,
            onNavigateToPlatformPermissions = onNavigateToPlatformPermissions
        )
    }
}

@Composable
private fun CompanySessionRequiredPane(
    userEmail: String?,
    message: String,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    onCreateCompany: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Empresa requerida", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onCreateCompany, modifier = Modifier.fillMaxWidth()) {
                Text("Crear empresa")
            }
            userEmail?.takeIf { it.isNotBlank() }?.let {
                PendingInvitesSection(onJoined = onRetry)
            }
            OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                Text("Reintentar")
            }
            TextButtonish(onClick = onLogout)
        }
    }
}

@Composable
private fun TextButtonish(onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text("Cerrar sesión")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    currentUser: com.example.nexogo.core.models.User?,
    companyName: String? = null,
    permissionEngine: PermissionEngine,
    onOpenDrawer: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlatformClients: () -> Unit,
    onNavigateToPlatformRecords: () -> Unit,
    onNavigateToPlatformDocuments: () -> Unit,
    onNavigateToPlatformUsers: () -> Unit,
    onNavigateToPlatformRoles: () -> Unit,
    onNavigateToPlatformPermissions: () -> Unit
) {
    val actions = getQuickActions(
        permissionEngine = permissionEngine,
        onNavigateToPlatformClients = onNavigateToPlatformClients,
        onNavigateToPlatformRecords = onNavigateToPlatformRecords,
        onNavigateToPlatformDocuments = onNavigateToPlatformDocuments,
        onNavigateToPlatformUsers = onNavigateToPlatformUsers,
        onNavigateToPlatformRoles = onNavigateToPlatformRoles,
        onNavigateToPlatformPermissions = onNavigateToPlatformPermissions
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NexoGo")
                        if (!companyName.isNullOrBlank()) {
                            Text(
                                companyName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentUser?.profileImageUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = currentUser.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(96.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        currentUser?.name ?: "Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        getRoleDisplayName(currentUser?.role),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!companyName.isNullOrBlank()) {
                        Text(
                            companyName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "Módulos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actions) { action ->
                    QuickActionCard(
                        title = action.title,
                        description = action.description,
                        icon = action.icon,
                        onClick = action.onClick
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    currentUser: com.example.nexogo.core.models.User?,
    companyName: String?,
    permissionEngine: PermissionEngine,
    scope: CoroutineScope,
    drawerState: DrawerState,
    onNavigateToProfile: () -> Unit,
    onNavigateToPlatformClients: () -> Unit,
    onNavigateToPlatformRecords: () -> Unit,
    onNavigateToPlatformDocuments: () -> Unit,
    onNavigateToPlatformUsers: () -> Unit,
    onNavigateToPlatformRoles: () -> Unit,
    onNavigateToPlatformPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(Modifier.padding(16.dp)) {
            Text("NexoGo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (!companyName.isNullOrBlank()) {
                Text(companyName, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                currentUser?.name ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Inicio") },
                selected = true,
                onClick = { scope.launch { drawerState.close() } }
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Perfil") },
                selected = false,
                onClick = onNavigateToProfile
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Operación", style = MaterialTheme.typography.labelLarge)

            if (NavPermissionFactory.showClients(permissionEngine)) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Clientes") },
                    selected = false,
                    onClick = onNavigateToPlatformClients
                )
            }
            if (NavPermissionFactory.showRecords(permissionEngine)) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                    label = { Text("Expedientes") },
                    selected = false,
                    onClick = onNavigateToPlatformRecords
                )
            }
            if (NavPermissionFactory.showDocuments(permissionEngine)) {
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    label = { Text("Documentos") },
                    selected = false,
                    onClick = onNavigateToPlatformDocuments
                )
            }

            if (NavPermissionFactory.showAdmin(permissionEngine)) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Administración", style = MaterialTheme.typography.labelLarge)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Usuarios") },
                    selected = false,
                    onClick = onNavigateToPlatformUsers
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Security, contentDescription = null) },
                    label = { Text("Roles") },
                    selected = false,
                    onClick = onNavigateToPlatformRoles
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Key, contentDescription = null) },
                    label = { Text("Permisos") },
                    selected = false,
                    onClick = onNavigateToPlatformPermissions
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text("Configuración") },
                selected = false,
                onClick = onNavigateToSettings
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                label = { Text("Cerrar sesión") },
                selected = false,
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun getRoleDisplayName(role: UserRole?): String = when (role) {
    UserRole.ADMIN -> "Administrador"
    UserRole.VET -> "Médico Veterinario"
    UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
    UserRole.USER -> "Usuario"
    null -> "Usuario"
}

private data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private fun getQuickActions(
    permissionEngine: PermissionEngine,
    onNavigateToPlatformClients: () -> Unit,
    onNavigateToPlatformRecords: () -> Unit,
    onNavigateToPlatformDocuments: () -> Unit,
    onNavigateToPlatformUsers: () -> Unit,
    onNavigateToPlatformRoles: () -> Unit,
    onNavigateToPlatformPermissions: () -> Unit
): List<QuickAction> {
    val actions = mutableListOf<QuickAction>()
    if (NavPermissionFactory.showClients(permissionEngine)) {
        actions += QuickAction("Clientes", "Listar y crear clientes de la empresa", Icons.Default.People, onNavigateToPlatformClients)
    }
    if (NavPermissionFactory.showRecords(permissionEngine)) {
        actions += QuickAction("Expedientes", "Expedientes ligados a clientes", Icons.Default.FolderOpen, onNavigateToPlatformRecords)
    }
    if (NavPermissionFactory.showDocuments(permissionEngine)) {
        actions += QuickAction("Documentos", "Subir y listar archivos", Icons.Default.Description, onNavigateToPlatformDocuments)
    }
    if (NavPermissionFactory.showUsers(permissionEngine)) {
        actions += QuickAction("Usuarios", "Invitar, activar y gestionar acceso", Icons.Default.Group, onNavigateToPlatformUsers)
    }
    if (NavPermissionFactory.showRoles(permissionEngine)) {
        actions += QuickAction("Roles", "Roles del sistema y de la empresa", Icons.Default.Security, onNavigateToPlatformRoles)
    }
    if (NavPermissionFactory.showPermissions(permissionEngine)) {
        actions += QuickAction("Permisos", "Catálogo y acceso efectivo", Icons.Default.Key, onNavigateToPlatformPermissions)
    }
    return actions
}
