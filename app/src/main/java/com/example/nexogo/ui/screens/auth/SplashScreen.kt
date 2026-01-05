package com.example.nexogo.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.example.nexogo.modules.auth.AuthViewModel
import com.example.nexogo.modules.auth.AuthState

/**
 * Pantalla de inicio con animaciones y verificación de autenticación
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animations")
    
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_alpha"
    )
    
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )
    
    // Gradiente animado
    val gradientColors = listOf(
        Color(0xFF4FC3F7), // Azul veterinario claro
        Color(0xFF0288D1), // Azul veterinario
        Color(0xFF01579B)  // Azul veterinario oscuro
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo principal con animaciones
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Emoji del logo
                Text(
                    text = "🐾",
                    fontSize = 120.sp,
                    modifier = Modifier.scale(logoScale)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Título principal
            Text(
                text = "NexoGo",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtítulo con animación
            Text(
                text = "Cuidamos lo que amas ❤️",
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Texto de estado
            Text(
                text = when (authState) {
                    AuthState.Loading -> "Verificando sesión..."
                    AuthState.Authenticated -> "¡Bienvenido de vuelta!"
                    AuthState.Unauthenticated -> "Iniciando aplicación..."
                    AuthState.Error -> "Error de conexión"
                    else -> "Iniciando aplicación..."
                },
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
            
            // Mostrar error si existe
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = Color(0xFFFFCDD2), // Rojo suave
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
        
        // Información de la empresa en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sistema de Gestión Veterinaria",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Versión 1.0.0",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
    
    // Lógica de navegación basada en el estado de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Loading -> {
                // Verificar estado de autenticación
                viewModel.checkAuthState()
                delay(2500) // Duración de la animación
            }
            AuthState.Authenticated -> {
                delay(1000) // Pequeña pausa para mostrar "Bienvenido"
                onNavigateToDashboard()
            }
            AuthState.Unauthenticated -> {
                delay(1000) // Pequeña pausa antes de ir al login
                onNavigateToLogin()
            }
            AuthState.Error -> {
                delay(2000) // Mostrar error por un momento
                onNavigateToLogin() // Ir al login para reintentar
            }
            else -> {
                delay(1000)
                onNavigateToLogin()
            }
        }
    }
}

/**
 * Estados de autenticación
 */
enum class AuthState {
    Loading,
    Authenticated,
    Unauthenticated,
    Error
}
