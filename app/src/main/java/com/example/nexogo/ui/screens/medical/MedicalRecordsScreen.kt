package com.example.nexogo.ui.screens.medical

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = AuthViewModel.getInstance() // Shared instance
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial Clínico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Historial Clínico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            currentUser?.let { user ->
                Text(
                    text = "¡Bienvenido, ${user.name}!",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "Rol: ${getRoleDisplayName(user.role)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Las funciones de historial clínico se implementarán aquí",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* TODO: Implement create medical record */ }
            ) {
                Text("Crear Nuevo Registro")
            }
        }
    }
}

private fun getRoleDisplayName(role: com.example.nexogo.core.models.UserRole?): String {
    return when (role) {
        com.example.nexogo.core.models.UserRole.ADMIN -> "Administrador"
        com.example.nexogo.core.models.UserRole.VET -> "Médico Veterinario"
        com.example.nexogo.core.models.UserRole.VET_ASSISTANT -> "Auxiliar Veterinario"
        com.example.nexogo.core.models.UserRole.USER -> "Usuario"
        null -> "Desconocido"
    }
}