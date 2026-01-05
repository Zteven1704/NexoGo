package com.example.nexogo.modules.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.models.AttachmentType

/**
 * Componente para seleccionar archivos multimedia
 */
@Composable
fun MediaPicker(
    onMediaSelected: (Uri, AttachmentType, String?, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar imágenes
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onMediaSelected(it, AttachmentType.IMAGE, null, 0)
        }
    }
    
    // Launcher para seleccionar videos
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onMediaSelected(it, AttachmentType.VIDEO, null, 0)
        }
    }
    
    // Launcher para seleccionar documentos
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onMediaSelected(it, AttachmentType.DOCUMENT, null, 0)
        }
    }
    
    // Launcher para seleccionar audio
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onMediaSelected(it, AttachmentType.AUDIO, null, 0)
        }
    }
    
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Seleccionar archivo") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MediaOption(
                        icon = Icons.Default.Photo,
                        title = "Imagen",
                        description = "JPG, PNG",
                        onClick = {
                            imagePicker.launch("image/*")
                            showPicker = false
                        }
                    )
                    
                    MediaOption(
                        icon = Icons.Default.Videocam,
                        title = "Video",
                        description = "MP4, AVI",
                        onClick = {
                            videoPicker.launch("video/*")
                            showPicker = false
                        }
                    )
                    
                    MediaOption(
                        icon = Icons.Default.AudioFile,
                        title = "Audio",
                        description = "MP3, M4A",
                        onClick = {
                            audioPicker.launch("audio/*")
                            showPicker = false
                        }
                    )
                    
                    MediaOption(
                        icon = Icons.Default.Description,
                        title = "Documento",
                        description = "PDF, DOC, TXT",
                        onClick = {
                            documentPicker.launch("*/*")
                            showPicker = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Botón para abrir el selector
    IconButton(
        onClick = { showPicker = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.AttachFile,
            contentDescription = "Adjuntar archivo",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MediaOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Componente para mostrar el progreso de carga
 */
@Composable
fun UploadProgressIndicator(
    isUploading: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (isUploading) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Subiendo archivo...",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Componente para mostrar previsualización de archivos
 */
@Composable
fun MediaPreview(
    mediaType: AttachmentType,
    mediaUrl: String?,
    mediaName: String?,
    modifier: Modifier = Modifier
) {
    when (mediaType) {
        AttachmentType.IMAGE -> {
            // Previsualización de imagen
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Imagen",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📷 Imagen",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        AttachmentType.VIDEO -> {
            // Previsualización de video
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Video",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎥 Video",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        AttachmentType.AUDIO -> {
            // Previsualización de audio
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = "Audio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎵 Audio",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        AttachmentType.DOCUMENT, AttachmentType.PDF -> {
            // Previsualización de documento
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Documento",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📄 ${mediaName ?: "Documento"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        else -> {
            // Previsualización genérica
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Archivo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📎 Archivo",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
