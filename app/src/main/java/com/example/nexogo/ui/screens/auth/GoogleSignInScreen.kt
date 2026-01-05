package com.example.nexogo.ui.screens.auth

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexogo.manager.FirebaseAuthManager
import com.example.nexogo.core.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pantalla de login con Google Sign-In
 */
@Composable
fun GoogleSignInScreen(
    onSignInSuccess: (User) -> Unit,
    onSignInError: (String) -> Unit
) {
    val context = LocalContext.current
    val authManager = remember(context) { FirebaseAuthManager(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "NexoGo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sistema Veterinario",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Botón de Google Sign-In
        Button(
            onClick = {
                isLoading = true
                errorMessage = ""
                signInWithGoogle(context, authManager, onSignInSuccess, onSignInError) {
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Iniciar sesión con Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mensaje de error
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Información adicional
        Text(
            text = "Al iniciar sesión, aceptas nuestros términos de servicio y política de privacidad",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Función para manejar el sign-in con Google
 */
private fun signInWithGoogle(
    context: Context,
    authManager: FirebaseAuthManager,
    onSuccess: (User) -> Unit,
    onError: (String) -> Unit,
    onComplete: () -> Unit
) {
    val googleSignInClient = authManager.googleSignInClient
    val signInIntent = googleSignInClient.signInIntent
    
    // En una implementación real, esto se manejaría con ActivityResultLauncher
    // Por ahora, simulamos el proceso
    CoroutineScope(Dispatchers.Main).launch {
        try {
            // Simular el proceso de sign-in
            // En una implementación real, aquí se manejaría el resultado del Intent
            
            // Por ahora, mostramos un mensaje de que la funcionalidad está disponible
            onError("Google Sign-In configurado correctamente. Implementar ActivityResultLauncher para funcionalidad completa.")
            onComplete()
            
        } catch (e: Exception) {
            onError("Error al iniciar sesión con Google: ${e.message}")
            onComplete()
        }
    }
}
