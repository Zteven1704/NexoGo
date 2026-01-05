package com.example.nexogo.modules.history.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Log

/**
 * Versión ultra-simplificada que solo muestra texto
 */
@Composable
fun UltraSimpleHistoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Log para debug
    LaunchedEffect(Unit) {
        Log.d("NEXOGO_DEBUG", "UltraSimpleHistoryScreen cargada exitosamente")
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Historial Clínico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Funcionando correctamente",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onNavigateBack) {
                Text("Volver")
            }
        }
    }
}
