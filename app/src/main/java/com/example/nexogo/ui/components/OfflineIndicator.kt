package com.example.nexogo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.network.NetworkStatus

/**
 * Indicador visual del estado de conexión
 * Muestra un banner cuando la app está offline
 */
@Composable
fun OfflineIndicator(
    modifier: Modifier = Modifier
) {
    val isOnline by NetworkStatus.isOnline.collectAsState()
    
    if (!isOnline) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Modo offline — Los cambios se sincronizarán cuando vuelva la conexión",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Indicador compacto del estado de conexión (solo icono)
 */
@Composable
fun OfflineIndicatorCompact(
    modifier: Modifier = Modifier
) {
    val isOnline by NetworkStatus.isOnline.collectAsState()
    
    if (!isOnline) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = "Offline",
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier.size(20.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = "Online",
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier.size(20.dp)
        )
    }
}

/**
 * Badge que muestra el estado de sincronización pendiente
 */
@Composable
fun PendingSyncBadge(
    hasPendingWrites: Boolean,
    modifier: Modifier = Modifier
) {
    if (hasPendingWrites) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Text(
                text = "Pendiente de sincronizar",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}




