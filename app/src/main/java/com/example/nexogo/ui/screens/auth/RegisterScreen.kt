package com.example.nexogo.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nexogo.R
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { 
        FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) 
    }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.USER) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var scope = rememberCoroutineScope()
    
    fun registerUser() {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Todos los campos son requeridos"
            return
        }
        
        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }
        
        if (password.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        
        if (!email.contains("@")) {
            errorMessage = "Email inválido"
            return
        }
        
        isLoading = true
        errorMessage = null
        successMessage = null
        
        scope.launch {
            try {
                println("DEBUG: RegisterScreen - Llamando a authRepository.registerUser...")
                authRepository.registerUser(email, password, name, selectedRole, phone, whatsapp)
                    .onSuccess { user ->
                        println("DEBUG: RegisterScreen - Registro exitoso: ${user.name}")
                        successMessage = "Tu cuenta ha sido registrada. Debes esperar la aprobación del administrador antes de iniciar sesión."
                        // Limpiar campos
                        name = ""
                        email = ""
                        password = ""
                        confirmPassword = ""
                        phone = ""
                        whatsapp = ""
                        selectedRole = UserRole.USER
                        // Redirigir al login inmediatamente
                        println("DEBUG: RegisterScreen - Navegando a login...")
                        onNavigateToLogin()
                    }
                    .onFailure { exception ->
                        println("DEBUG: RegisterScreen - Error en registro: ${exception.message}")
                        errorMessage = when {
                            exception.message?.contains("email-already-in-use") == true -> 
                                "Este email ya está registrado. Intenta iniciar sesión."
                            exception.message?.contains("weak-password") == true -> 
                                "La contraseña es muy débil. Usa al menos 6 caracteres."
                            exception.message?.contains("invalid-email") == true -> 
                                "Email inválido."
                            else -> "Error al registrar usuario. Intenta nuevamente."
                        }
                        println("DEBUG: RegisterScreen - Estableciendo errorMessage: $errorMessage")
                        println("DEBUG: RegisterScreen - Estableciendo isLoading = false")
                        isLoading = false
                    }
            } catch (e: Exception) {
                println("DEBUG: RegisterScreen - Excepción en scope.launch: ${e.message}")
                errorMessage = "Error inesperado: ${e.message}"
                isLoading = false
            }
            // Solo establecer isLoading = false si no se ha establecido ya en los bloques anteriores
            if (isLoading) {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = {
                    println("DEBUG: RegisterScreen - Botón Volver presionado")
                    onNavigateToLogin()
                },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }
        
        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "NexoGo Logo",
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "Registro de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Crea tu cuenta en NexoGo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = "Nombre")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono (opcional)") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = "Teléfono")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // WhatsApp field
        OutlinedTextField(
            value = whatsapp,
            onValueChange = { whatsapp = it },
            label = { Text("WhatsApp (opcional)") },
            leadingIcon = {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Role selection
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = getRoleDisplayName(selectedRole),
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol") },
                leadingIcon = {
                    Icon(Icons.Default.Work, contentDescription = "Rol")
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                UserRole.values().forEach { role ->
                    DropdownMenuItem(
                        text = { Text(getRoleDisplayName(role)) },
                        onClick = {
                            selectedRole = role
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Contraseña")
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirmar contraseña")
            },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Register button
        Button(
            onClick = { registerUser() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Registrarse")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Login button
        TextButton(
            onClick = {
                println("DEBUG: RegisterScreen - Botón Inicia sesión presionado")
                onNavigateToLogin()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
        
        // Error message
        errorMessage?.let { error ->
            println("DEBUG: RegisterScreen - Mostrando error en UI: $error")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Success message
        successMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun getRoleDisplayName(role: UserRole): String {
    return when (role) {
        UserRole.ADMIN -> "Administrador"
        UserRole.VET -> "Médico Veterinario"
        UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        UserRole.USER -> "Usuario"
    }
}