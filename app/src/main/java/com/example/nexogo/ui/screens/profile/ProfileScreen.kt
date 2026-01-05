package com.example.nexogo.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.viewmodel.ProfileViewModel
import com.example.nexogo.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    
    // Debug logs
    LaunchedEffect(user, state, isLoading) {
        println("DEBUG: ProfileScreen - Estado actual:")
        println("  - user: ${user?.name} (${user?.email})")
        println("  - state: $state")
        println("  - isLoading: $isLoading")
        println("  - message: $message")
    }
    
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    // Create a temporary file for camera capture
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Check if there are pending changes
    val hasChanges = remember(name, phone, whatsapp, selectedImageUri, user) {
        val originalName = user?.name ?: ""
        val originalPhone = user?.phone ?: ""
        val originalWhatsapp = user?.whatsapp ?: ""
        
        val hasNameChange = name != originalName
        val hasPhoneChange = phone != originalPhone
        val hasWhatsappChange = whatsapp != originalWhatsapp
        // Cambio de imagen: si hay una imagen seleccionada, es un cambio
        val hasImageChange = selectedImageUri != null
        
        val changes = hasNameChange || hasPhoneChange || hasWhatsappChange || hasImageChange
        
        println("DEBUG: ProfileScreen - Detección de cambios:")
        println("  - user: ${user?.name}")
        println("  - Nombre: '$name' vs '$originalName' = $hasNameChange")
        println("  - Teléfono: '$phone' vs '$originalPhone' = $hasPhoneChange")
        println("  - WhatsApp: '$whatsapp' vs '$originalWhatsapp' = $hasWhatsappChange")
        println("  - Imagen seleccionada: '$selectedImageUri'")
        println("  - Cambio de imagen: $hasImageChange (selectedImageUri != null)")
        println("  - Hay cambios: $changes")
        
        changes
    }
    
    // Launcher for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            println("DEBUG: ProfileScreen - Imagen de cámara seleccionada: $selectedImageUri")
        }
    }
    
    // Launcher for gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            println("DEBUG: ProfileScreen - Imagen de galería seleccionada: $selectedImageUri")
        }
    }
    
    // Initialize fields when user changes
    LaunchedEffect(user) {
        user?.let { userData ->
            println("DEBUG: ProfileScreen - Inicializando campos con usuario: ${userData.name}")
            name = userData.name
            phone = userData.phone
            whatsapp = userData.whatsapp
            println("DEBUG: ProfileScreen - Campos inicializados: name='$name', phone='$phone', whatsapp='$whatsapp'")
        }
    }
    
    // Clear messages after a delay and exit editing mode on success
    LaunchedEffect(message) {
        message?.let { msg ->
            if (msg.contains("actualizado exitosamente")) {
                isEditing = false
                // Clear selected image after successful save
                selectedImageUri = null
                println("DEBUG: ProfileScreen - Imagen seleccionada reseteada después de guardar")
            }
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
    
    // Handle state changes
    LaunchedEffect(state) {
        when (state) {
            is ProfileState.Success -> {
                if (message?.contains("actualizado exitosamente") == true) {
                    isEditing = false
                    selectedImageUri = null
                }
            }
            is ProfileState.Error -> {
                // Error handling is done in the UI
            }
            ProfileState.Loading -> {
                // Loading state is handled in the UI
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isEditing) {
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    println("DEBUG: ProfileScreen - Guardando perfil con imagen: $selectedImageUri")
                                    viewModel.updateUserProfile(name, phone, whatsapp, selectedImageUri)
                                }
                            },
                            enabled = run {
                                val nameValid = name.isNotBlank()
                                val phoneValid = phone.isNotBlank()
                                val notLoading = !isLoading
                                val hasChangesValue = hasChanges
                                
                                println("DEBUG: ProfileScreen - Estado del botón Guardar:")
                                println("  - name.isNotBlank(): $nameValid ('$name')")
                                println("  - phone.isNotBlank(): $phoneValid ('$phone')")
                                println("  - !isLoading: $notLoading ($isLoading)")
                                println("  - hasChanges: $hasChangesValue")
                                println("  - Botón habilitado: ${nameValid && phoneValid && notLoading && hasChangesValue}")
                                
                                nameValid && phoneValid && notLoading && hasChangesValue
                            }
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Guardar")
                        }
                        
                        // Indicador visual de cambios pendientes
                        if (hasChanges) {
                            Text(
                                text = "• Hay cambios pendientes",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        
                        TextButton(
                            onClick = {
                                isEditing = false
                                // Reset fields
                                user?.let { userData ->
                                    name = userData.name
                                    phone = userData.phone
                                    whatsapp = userData.whatsapp
                                }
                                // Clear selected image
                                selectedImageUri = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile picture
            Card(
                modifier = Modifier.size(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val imageToShow = selectedImageUri ?: user?.profileImageUrl
                    if (imageToShow != null && imageToShow.toString().isNotEmpty()) {
                        // Show actual profile image
                        AsyncImage(
                            model = imageToShow,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        println("DEBUG: ProfileScreen - Mostrando imagen: $imageToShow")
                    } else {
                        // Show placeholder
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        println("DEBUG: ProfileScreen - Mostrando placeholder, imagen: $imageToShow")
                    }
                    
                    // Add photo button overlay
                    if (isEditing) {
                        FloatingActionButton(
                            onClick = {
                                showImagePicker = true
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar foto",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User role badge
            user?.let { userData ->
                AssistChip(
                    onClick = { },
                    label = { Text(getRoleDisplayName(userData.role)) },
                    leadingIcon = {
                        Icon(
                            when (userData.role) {
                                com.example.nexogo.core.models.UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                com.example.nexogo.core.models.UserRole.VET -> Icons.Default.MedicalServices
                                com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> Icons.Default.HealthAndSafety
                                com.example.nexogo.core.models.UserRole.USER -> Icons.Default.Person
                            },
                            contentDescription = null
                        )
                    }
                )
                
                if (userData.isProfessional && !userData.isApproved) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text("Pendiente de aprobación") },
                        leadingIcon = {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null)
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            labelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando perfil...")
                    }
                }
            }
            
            // No user data message
            if (!isLoading && user == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No se pudo cargar el perfil",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (val currentState = state) {
                                is ProfileState.Error -> currentState.message
                                else -> "Error desconocido"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadUserProfile() }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            
            // Profile form
            user?.let { userData ->
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email field (read-only)
                OutlinedTextField(
                    value = userData.email,
                    onValueChange = { },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // WhatsApp field
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEditing,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Error message
                when (val currentState = state) {
                    is ProfileState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = currentState.message,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    else -> {}
                }
                
                // Success message
                message?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Account info
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información de la cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha de registro:")
                            Text(
                                text = "Hace ${getDaysAgo(userData.createdAt)} días",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estado:")
                            Text(
                                text = if (userData.isActive) "Activo" else "Inactivo",
                                color = if (userData.isActive) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                        
                        if (userData.isProfessional) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Aprobación:")
                                Text(
                                    text = if (userData.isApproved) "Aprobado" else "Pendiente",
                                    color = if (userData.isApproved) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Image picker dialog
    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Seleccionar foto de perfil") },
            text = { Text("¿Cómo quieres agregar tu foto de perfil?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImagePicker = false
                        // Launch camera
                        val tempFile = java.io.File(context.cacheDir, "temp_photo.jpg")
                        val uri = Uri.fromFile(tempFile)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                ) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImagePicker = false
                        // Launch gallery
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Galería")
                }
            }
        )
    }
}

private fun getRoleDisplayName(role: com.example.nexogo.core.models.UserRole?): String {
    return when (role) {
        com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
        com.example.nexogo.core.models.UserRole.VET -> "Médico Veterinario"
        com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        com.example.nexogo.core.models.UserRole.USER -> "Usuario"
        null -> "Usuario"
    }
}

private fun getDaysAgo(timestamp: com.google.firebase.Timestamp): Long {
    val now = System.currentTimeMillis()
    val created = timestamp.seconds * 1000
    return (now - created) / (1000 * 60 * 60 * 24)
}
