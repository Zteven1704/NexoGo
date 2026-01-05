package com.example.nexogo.modules.history.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nexogo.modules.history.models.Attachment
import com.example.nexogo.modules.history.models.AttachmentType

/**
 * Componente para subir archivos adjuntos
 */
@Composable
fun FileUploader(
    onFileSelected: (Uri, String, AttachmentType) -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Adjuntar Archivos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isUploading) {
                UploadProgress(
                    progress = uploadProgress,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                UploadButtons(
                    onFileSelected = onFileSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Botones para seleccionar diferentes tipos de archivos
 */
@Composable
private fun UploadButtons(
    onFileSelected: (Uri, String, AttachmentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón para imágenes
        UploadButton(
            icon = Icons.Default.Image,
            label = "Imagen",
            onClick = { /* TODO: Implementar selección de imagen */ }
        )
        
        // Botón para documentos
        UploadButton(
            icon = Icons.Default.Description,
            label = "Documento",
            onClick = { /* TODO: Implementar selección de documento */ }
        )
        
        // Botón para cámara
        UploadButton(
            icon = Icons.Default.CameraAlt,
            label = "Cámara",
            onClick = { /* TODO: Implementar captura de cámara */ }
        )
    }
}

/**
 * Botón individual para subir archivos
 */
@Composable
private fun UploadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Indicador de progreso de subida
 */
@Composable
private fun UploadProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Subiendo archivo...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Lista de archivos adjuntos
 */
@Composable
fun AttachmentList(
    attachments: List<Attachment>,
    onRemove: (Attachment) -> Unit,
    onDownload: (Attachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Archivos Adjuntos (${attachments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onRemove = { onRemove(attachment) },
                    onDownload = { onDownload(attachment) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Item individual de archivo adjunto
 */
@Composable
private fun AttachmentItem(
    attachment: Attachment,
    onRemove: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono según tipo de archivo
            Icon(
                imageVector = getAttachmentIcon(attachment.type),
                contentDescription = attachment.type.name,
                tint = getAttachmentColor(attachment.type),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del archivo
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                
                Text(
                    text = formatFileSize(attachment.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botones de acción
            Row {
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Obtiene el icono según el tipo de archivo
 */
private fun getAttachmentIcon(type: AttachmentType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AttachmentType.IMAGE -> Icons.Default.Image
        AttachmentType.PDF -> Icons.Default.PictureAsPdf
        AttachmentType.DOCUMENT -> Icons.Default.Description
        AttachmentType.VIDEO -> Icons.Default.VideoFile
        AttachmentType.AUDIO -> Icons.Default.AudioFile
    }
}

/**
 * Obtiene el color según el tipo de archivo
 */
private fun getAttachmentColor(type: AttachmentType): Color {
    return when (type) {
        AttachmentType.IMAGE -> Color(0xFF4CAF50)
        AttachmentType.PDF -> Color(0xFFF44336)
        AttachmentType.DOCUMENT -> Color(0xFF2196F3)
        AttachmentType.VIDEO -> Color(0xFF9C27B0)
        AttachmentType.AUDIO -> Color(0xFFFF9800)
    }
}

/**
 * Formatea el tamaño del archivo
 */
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}
