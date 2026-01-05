package com.example.nexogo.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.modules.auth.AuthViewModel
import com.example.nexogo.modules.auth.AuthState

/**
 * LoginScreen ultra simplificado para evitar crashes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraSimpleLoginScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }
    
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Navegación automática cuando se autentica
    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            onNavigateToDashboard()
        }
    }
    
    // Fondo con gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo y título
            Text(
                text = "🐾 NexoGo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            
            Text(
                text = "Clínica Veterinaria",
                fontSize = 16.sp,
                color = Color(0xFF424242)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Card principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Toggle Login/Register
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isLoginMode = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginMode) Color(0xFF1565C0) else Color(0xFFE0E0E0)
                            )
                        ) {
                            Text(
                                text = "Iniciar Sesión",
                                color = if (isLoginMode) Color.White else Color(0xFF424242)
                            )
                        }
                        
                        Button(
                            onClick = { isLoginMode = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLoginMode) Color(0xFF1565C0) else Color(0xFFE0E0E0)
                            )
                        ) {
                            Text(
                                text = "Registrarse",
                                color = if (!isLoginMode) Color.White else Color(0xFF424242)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Campos del formulario
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    // Botón principal
                    Button(
                        onClick = {
                            if (isLoginMode) {
                                viewModel.login(email, password)
                            } else {
                                // Para registro simple, usar datos por defecto
                                viewModel.register(
                                    email = email,
                                    password = password,
                                    name = "Usuario Test",
                                    role = com.example.nexogo.core.models.UserRole.USER
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (isLoginMode) "Iniciar Sesión" else "Registrarse",
                                fontSize = 16.sp
                            )
                        }
                    }
                    
                    // Mensajes
                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp
                        )
                    }
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de prueba rápida
            // Botón de acceso rápido removido para forzar validación real
        }
    }
}
