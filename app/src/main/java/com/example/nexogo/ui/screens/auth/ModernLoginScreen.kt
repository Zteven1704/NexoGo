package com.example.nexogo.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.modules.auth.AuthViewModel
import com.example.nexogo.modules.auth.AuthState

/**
 * Pantalla de login moderna con Material 3 y pestañas
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModernLoginScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    
    // Estados del formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(com.example.nexogo.core.models.UserRole.USER) }
    var showPassword by remember { mutableStateOf(false) }
    var showRoleDropdown by remember { mutableStateOf(false) }
    
    // Navegación automática cuando se autentica
    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            onNavigateToDashboard()
        }
    }
    
    // Gradiente de fondo
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD), // Azul muy claro
            Color(0xFFBBDEFB), // Azul claro
            Color(0xFF90CAF9)  // Azul medio
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo y título
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐾",
                        fontSize = 60.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "NexoGo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            
            Text(
                text = "Sistema de Gestión Veterinaria",
                fontSize = 16.sp,
                color = Color(0xFF424242),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pestañas de Login/Registro
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Pestañas
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TabButton(
                            text = "Iniciar Sesión",
                            isSelected = isLoginMode,
                            onClick = { viewModel.toggleLoginMode() },
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        TabButton(
                            text = "Crear Cuenta",
                            isSelected = !isLoginMode,
                            onClick = { viewModel.toggleLoginMode() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Formulario con animación
                    AnimatedContent(
                        targetState = isLoginMode,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (isLoginMode) -it else it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { if (isLoginMode) it else -it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        label = "form_transition"
                    ) { loginMode ->
                        if (loginMode) {
                            LoginForm(
                                email = email,
                                password = password,
                                showPassword = showPassword,
                                isLoading = isLoading,
                                onEmailChange = { email = it },
                                onPasswordChange = { password = it },
                                onShowPasswordToggle = { showPassword = !showPassword },
                                onLoginClick = {
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.login(email, password)
                                    }
                                },
                                onForgotPasswordClick = {
                                    if (email.isNotBlank()) {
                                        viewModel.resetPassword(email)
                                    }
                                }
                            )
                        } else {
                            RegisterForm(
                                email = email,
                                password = password,
                                name = name,
                                phone = phone,
                                selectedRole = selectedRole,
                                showPassword = showPassword,
                                showRoleDropdown = showRoleDropdown,
                                isLoading = isLoading,
                                onEmailChange = { email = it },
                                onPasswordChange = { password = it },
                                onNameChange = { name = it },
                                onPhoneChange = { phone = it },
                                onRoleChange = { selectedRole = it },
                                onShowPasswordToggle = { showPassword = !showPassword },
                                onShowRoleDropdownToggle = { showRoleDropdown = !showRoleDropdown },
                                onRegisterClick = {
                                    if (email.isNotBlank() && password.isNotBlank() && name.isNotBlank()) {
                                        viewModel.register(email, password, name, selectedRole, phone.takeIf { it.isNotBlank() })
                                    }
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mensajes de estado
                    if (message.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E8)
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFF2E7D32),
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Text(
                                text = errorMessage!!,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFFC62828),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información adicional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿Necesitas ayuda?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Contacta con el administrador del sistema",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1565C0) else Color(0xFFE0E0E0)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF424242),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    showPassword: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShowPasswordToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(Icons.Rounded.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Botón de "¿Olvidaste tu contraseña?"
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("¿Olvidaste tu contraseña?")
        }
        
        // Botón de login
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Iniciar Sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    email: String,
    password: String,
    name: String,
    phone: String,
    selectedRole: com.example.nexogo.core.models.UserRole,
    showPassword: Boolean,
    showRoleDropdown: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRoleChange: (com.example.nexogo.core.models.UserRole) -> Unit,
    onShowPasswordToggle: () -> Unit,
    onShowRoleDropdownToggle: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Campo de nombre
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nombre completo") },
            leadingIcon = {
                Icon(Icons.Rounded.Person, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Campo de email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(Icons.Rounded.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Campo de teléfono
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono (opcional)") },
            leadingIcon = {
                Icon(Icons.Rounded.Phone, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Campo de rol
        ExposedDropdownMenuBox(
            expanded = showRoleDropdown,
            onExpandedChange = { onShowRoleDropdownToggle() },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = when (selectedRole) {
                    com.example.nexogo.core.models.UserRole.USER -> "Paciente"
                    com.example.nexogo.core.models.UserRole.VET -> "Veterinario"
                    com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
                    com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
                    com.example.nexogo.core.models.UserRole.VET -> "Veterinario Pendiente"
                    com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Pendiente"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol") },
                leadingIcon = {
                    Icon(Icons.Rounded.Work, contentDescription = null)
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRoleDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !isLoading
            )
            
            ExposedDropdownMenu(
                expanded = showRoleDropdown,
                onDismissRequest = onShowRoleDropdownToggle
            ) {
                com.example.nexogo.core.models.UserRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (role) {
                                    com.example.nexogo.core.models.UserRole.USER -> "Paciente"
                                    com.example.nexogo.core.models.UserRole.VET -> "Veterinario"
                                    com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
                                    com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
                                    com.example.nexogo.core.models.UserRole.VET -> "Veterinario Pendiente"
                                    com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Pendiente"
                                }
                            )
                        },
                        onClick = {
                            onRoleChange(role)
                            onShowRoleDropdownToggle()
                        }
                    )
                }
            }
        }
        
        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = onShowPasswordToggle) {
                    Icon(
                        if (showPassword) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        // Botón de registro
        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && name.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "Crear Cuenta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
