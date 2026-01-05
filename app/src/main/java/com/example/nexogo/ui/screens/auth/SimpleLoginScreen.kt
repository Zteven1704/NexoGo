package com.example.nexogo.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nexogo.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.first

/**
 * Pantalla de login simplificada sin dependencias complejas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleLoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val authRepository = remember { 
        FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) 
    }
    
    // Crear usuario administrador si no existe
    LaunchedEffect(Unit) {
        try {
            println("DEBUG: SimpleLoginScreen - Iniciando verificación de usuario administrador...")
            
            // Verificar si el usuario admin existe en Firestore
            val existingQuery = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .whereEqualTo("correo", "admin@nexogo.com")
                .get()
                .await()
            
            if (existingQuery.documents.isEmpty()) {
                println("DEBUG: SimpleLoginScreen - Usuario admin no existe, creando...")
                
                // Crear usuario en Firebase Auth
                val authResult = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword("admin@nexogo.com", "123456")
                    .await()
                
                val uid = authResult.user?.uid
                println("DEBUG: SimpleLoginScreen - Usuario admin creado en Auth con UID: $uid")
                
                if (uid != null) {
                    // Crear documento en Firestore
                    val adminData = mapOf(
                        "uid" to uid,
                        "nombre" to "Dr. María González",
                        "correo" to "admin@nexogo.com",
                        "telefono" to "1234567890",
                        "whatsapp" to "1234567890",
                        "rol" to "ADMIN",
                        "estado" to "aprobado",
                        "isApproved" to true,
                        "fechaRegistro" to com.google.firebase.Timestamp.now()
                    )
                    
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .set(adminData)
                        .await()
                    
                    println("DEBUG: SimpleLoginScreen - Usuario admin creado en Firestore con UID: $uid")
                }
            } else {
                println("DEBUG: SimpleLoginScreen - Usuario admin ya existe en Firestore")
                val existingDoc = existingQuery.documents.first()
                val existingUid = existingDoc.getString("uid")
                println("DEBUG: SimpleLoginScreen - UID existente: $existingUid")
            }
        } catch (e: Exception) {
            println("DEBUG: SimpleLoginScreen - Error: ${e.message}")
            e.printStackTrace()
        }
    }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
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
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sistema de Gestión Veterinaria",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Formulario de login
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Campo de email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Campo de contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Mensaje de estado
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Mensaje de error
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Botón de login
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            message = ""
                            
                            // Validaciones básicas
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Por favor completa todos los campos"
                                isLoading = false
                                return@launch
                            }
                            
                            if (!email.contains("@")) {
                                errorMessage = "Por favor ingresa un email válido"
                                isLoading = false
                                return@launch
                            }
                            
                            if (password.length < 6) {
                                errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                isLoading = false
                                return@launch
                            }
                            
                       // Validación directa con Firebase Auth
                       try {
                           val authResult = FirebaseAuth.getInstance()
                               .signInWithEmailAndPassword(email, password)
                               .await()
                           
                           val user = authResult.user
                           if (user != null) {
                               println("DEBUG: SimpleLoginScreen - Login exitoso para: ${user.email}")
                               println("DEBUG: SimpleLoginScreen - UID: ${user.uid}")
                               
                               // Cargar perfil desde DataStore
                               val dataStore = com.example.nexogo.data.AppDataStore(context)
                               val savedUser = dataStore.getUser().first()
                               
                               if (savedUser != null) {
                                   println("DEBUG: SimpleLoginScreen - Perfil cargado desde DataStore: ${savedUser.name}")
                                   println("DEBUG: SimpleLoginScreen - Foto de perfil: ${savedUser.profileImageUrl}")
                                   
                                   // Actualizar el perfil con el UID de Firebase Auth
                                   val updatedUser = savedUser.copy(id = user.uid)
                                   dataStore.saveUser(updatedUser)
                                   
                                   // Actualizar el ViewModel con el perfil completo
                                   val authViewModel = com.example.nexogo.viewmodel.PersistentAuthViewModel.getInstance(context)
                                   authViewModel.setCurrentUser(updatedUser)
                                   
                                   message = "¡Bienvenido ${updatedUser.name}!"
                               } else {
                                   // Si no hay perfil guardado, crear uno básico
                                   val basicUser = com.example.nexogo.core.models.User(
                                       id = user.uid,
                                       email = user.email ?: email,
                                       name = user.displayName ?: "Usuario",
                                       phone = "",
                                       whatsapp = "",
                                       profileImageUrl = if (email == "admin@nexogo.com") {
                                           "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=150&h=150&fit=crop&crop=face"
                                       } else {
                                           "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop&crop=face"
                                       },
                                       role = if (email == "admin@nexogo.com") com.example.nexogo.core.models.UserRole.ADMIN else com.example.nexogo.core.models.UserRole.USER,
                                       isApproved = true,
                                       fcmToken = null
                                   )
                                   
                                   // Guardar el perfil básico
                                   dataStore.saveUser(basicUser)
                                   
                                   // Actualizar el ViewModel
                                   val authViewModel = com.example.nexogo.viewmodel.PersistentAuthViewModel.getInstance(context)
                                   authViewModel.setCurrentUser(basicUser)
                                   
                                   message = "¡Bienvenido ${basicUser.name}!"
                               }
                               
                               onNavigateToHome()
                           } else {
                               errorMessage = "Error al autenticar usuario"
                           }
                       } catch (e: Exception) {
                           println("DEBUG: SimpleLoginScreen - Error en login: ${e.message}")
                           errorMessage = when {
                               e.message?.contains("user-not-found") == true ->
                                   "Usuario no encontrado. Verifica tu email"
                               e.message?.contains("wrong-password") == true ->
                                   "Contraseña incorrecta"
                               e.message?.contains("invalid-email") == true ->
                                   "Formato de email inválido"
                               e.message?.contains("network") == true ->
                                   "Error de conexión. Verifica tu internet"
                               else -> e.message ?: "Error al iniciar sesión"
                           }
                       }
                            
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Iniciar Sesión")
                }
                
                // Botón de registro
                TextButton(
                    onClick = {
                        println("DEBUG: SimpleLoginScreen - Usuario hizo clic en 'No tengo cuenta'")
                        // Forzar logout antes de ir al registro para evitar redirección automática
                        // Esto se manejará en el NavGraph
                        onNavigateToRegister()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¿No tienes cuenta? Regístrate aquí")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información adicional
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Demo de NexoGo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sistema de gestión veterinaria completo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
