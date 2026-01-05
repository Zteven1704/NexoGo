package com.example.nexogo.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.SettingsViewModel
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.ui.components.SafeSettingsScreen
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val modifier = if (onClick != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }
    
    Surface(
        onClick = onClick ?: { },
        modifier = modifier,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) iconTint else iconTint.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) titleColor else titleColor.copy(alpha = 0.6f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) subtitleColor else subtitleColor.copy(alpha = 0.6f)
                )
            }
            
            trailing?.invoke() ?: run {
                if (onClick != null) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToProductCategories: () -> Unit,
    onNavigateToChatbotConfig: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    settingsViewModel: SettingsViewModel = SettingsViewModel.getInstance(LocalContext.current)
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val isLoading by settingsViewModel.isLoading.collectAsStateWithLifecycle()
    val message by settingsViewModel.message.collectAsStateWithLifecycle()
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var showRoleChangeDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    
    // Clear message when it changes
    LaunchedEffect(message) {
        message?.let {
            settingsViewModel.clearMessage()
        }
    }
    
    SafeSettingsScreen {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Configuración") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account section
            item {
                SettingsSection(title = "Cuenta") {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Editar perfil",
                        subtitle = "Cambiar información personal",
                        onClick = { onNavigateToProfile() }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Cambiar contraseña",
                        subtitle = "Actualizar tu contraseña",
                        onClick = { showResetPasswordDialog = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Idioma",
                        subtitle = settings.language,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
            
            // Notifications section
            item {
                SettingsSection(title = "Notificaciones") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones push",
                        subtitle = "Recibir notificaciones en el dispositivo",
                        trailing = {
                            Switch(
                                checked = settings.notificationsEnabled,
                                onCheckedChange = { settingsViewModel.updateNotifications(it) }
                            )
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = "Recordatorios de citas",
                        subtitle = "Notificaciones 24h y 1h antes",
                        enabled = settings.notificationsEnabled,
                        trailing = {
                            Switch(
                                checked = settings.appointmentReminders,
                                onCheckedChange = { settingsViewModel.updateAppointmentReminders(it) },
                                enabled = settings.notificationsEnabled
                            )
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Chat,
                        title = "Mensajes",
                        subtitle = "Notificaciones de nuevos mensajes",
                        enabled = settings.notificationsEnabled,
                        trailing = {
                            Switch(
                                checked = settings.messageNotifications,
                                onCheckedChange = { settingsViewModel.updateMessageNotifications(it) },
                                enabled = settings.notificationsEnabled
                            )
                        }
                    )
                }
            }
            
            // Security section
            item {
                SettingsSection(title = "Seguridad") {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Autenticación biométrica",
                    subtitle = "Usar huella dactilar o Face ID",
                    trailing = {
                        Switch(
                            checked = settings.biometricAuth,
                            onCheckedChange = { settingsViewModel.updateBiometricAuth(it) }
                        )
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacidad",
                    subtitle = "Configurar privacidad de datos",
                    onClick = { /* TODO: Navigate to privacy settings */ }
                )
                }
            }
            
            // Data section
            item {
                SettingsSection(title = "Datos") {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Respaldo automático",
                    subtitle = "Sincronizar datos en la nube",
                    trailing = {
                        Switch(
                            checked = settings.autoBackup,
                            onCheckedChange = { settingsViewModel.updateAutoBackup(it) }
                        )
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Gestión de almacenamiento",
                    subtitle = "Ver uso de espacio y limpiar caché",
                    onClick = { onNavigateToStorage() }
                )
                }
            }
            
            // Inventory section
            item {
                SettingsSection(title = "Inventario") {
                SettingsItem(
                    icon = Icons.Default.Category,
                    title = "Categorías de productos",
                    subtitle = "Crear, editar y eliminar categorías",
                    onClick = { onNavigateToProductCategories() }
                )
                }
            }
            
            // Chat section (only for admin and professionals)
            if (currentUser?.role == UserRole.ADMIN || currentUser?.role == UserRole.VET || currentUser?.role == UserRole.VET_ASSISTANT) {
                item {
                    SettingsSection(title = "Chat") {
                    SettingsItem(
                        icon = Icons.Default.SmartToy,
                        title = "Configuración del Chatbot",
                        subtitle = "Configurar respuestas automáticas del chatbot",
                        onClick = { onNavigateToChatbotConfig() }
                    )
                    }
                }
            }
            
            // Testing section (only for development)
            item {
                SettingsSection(title = "Pruebas") {
                SettingsItem(
                    icon = Icons.Default.SwapHoriz,
                    title = "Cambiar rol de usuario",
                    subtitle = "Cambiar rol para probar funcionalidades",
                    onClick = { showRoleChangeDialog = true }
                )
                }
            }
            
            // Support section
            item {
                SettingsSection(title = "Soporte") {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Ayuda y FAQ",
                    subtitle = "Preguntas frecuentes y soporte",
                    onClick = { onNavigateToHelp() }
                )
                
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = "Enviar comentarios",
                    subtitle = "Reportar problemas o sugerencias",
                    onClick = { showFeedbackDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de NexoGo",
                    subtitle = "Versión 1.0.0",
                    onClick = { onNavigateToAbout() }
                )
                }
            }
            
            // Logout section
            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "Cerrar sesión",
                        subtitle = "Salir de tu cuenta",
                        onClick = { showLogoutDialog = true },
                        titleColor = MaterialTheme.colorScheme.onErrorContainer,
                        subtitleColor = MaterialTheme.colorScheme.onErrorContainer,
                        iconTint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }
    }
    
    // Language selection dialog
    if (showLanguageDialog) {
        var selectedLanguage by remember { mutableStateOf(settings.language) }
        
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Seleccionar idioma") },
            text = {
                Column {
                    listOf("Español", "English").forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == language,
                                onClick = { selectedLanguage = language }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(language)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateLanguage(selectedLanguage)
                        showLanguageDialog = false
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Aplicar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Reset password dialog
    if (showResetPasswordDialog) {
        var email by remember { mutableStateOf(currentUser?.email ?: "") }
        
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("Restablecer contraseña") },
            text = {
                Column {
                    Text("Se enviará un enlace de restablecimiento a tu correo electrónico.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.resetPassword(email)
                        showResetPasswordDialog = false
                    },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Role change dialog
    if (showRoleChangeDialog) {
        AlertDialog(
            onDismissRequest = { showRoleChangeDialog = false },
            title = { Text("Cambiar rol de usuario") },
            text = {
                Column {
                    Text("Selecciona un rol para probar las funcionalidades:")
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(
                        "Paciente" to UserRole.USER,
                        "Veterinario" to UserRole.VET,
                        "Asistente Veterinario" to UserRole.VET_ASSISTANT,
                        "Administrador" to UserRole.ADMIN
                    ).forEach { (roleName: String, role: UserRole) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentUser?.role == role,
                                onClick = { 
                                    authViewModel.switchUserRole(role)
                                    showRoleChangeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(roleName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoleChangeDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    // Feedback dialog
    if (showFeedbackDialog) {
        var feedbackText by remember { mutableStateOf("") }
        var feedbackType by remember { mutableStateOf("Sugerencia") }
        
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Enviar comentarios") },
            text = {
                Column {
                    Text("Ayúdanos a mejorar NexoGo con tus comentarios:")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = { Text("Comentarios") },
                        placeholder = { Text("Escribe tus comentarios aquí...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Send feedback
                        showFeedbackDialog = false
                    },
                    enabled = feedbackText.isNotBlank()
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
