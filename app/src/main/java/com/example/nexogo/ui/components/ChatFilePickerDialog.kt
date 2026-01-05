package com.example.nexogo.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nexogo.model.MessageType

@Composable
fun ChatFilePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onFileSelected: (String, String, String) -> Unit // fileName, fileType, fileUri
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Seleccionar Archivo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Elige el tipo de archivo que quieres adjuntar",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // File type selection buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Image picker
                        val imagePicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                onFileSelected(
                                    "Imagen_${System.currentTimeMillis()}.jpg",
                                    MessageType.IMAGE.name,
                                    it.toString()
                                )
                                onDismiss()
                            }
                        }
                        
                        FileTypeButton(
                            icon = Icons.Default.Image,
                            title = "Imagen",
                            description = "JPG, PNG, GIF",
                            onClick = {
                                imagePicker.launch("image/*")
                            }
                        )
                        
                        // Video picker
                        val videoPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                onFileSelected(
                                    "Video_${System.currentTimeMillis()}.mp4",
                                    MessageType.VIDEO.name,
                                    it.toString()
                                )
                                onDismiss()
                            }
                        }
                        
                        FileTypeButton(
                            icon = Icons.Default.VideoFile,
                            title = "Video",
                            description = "MP4, AVI, MOV",
                            onClick = {
                                videoPicker.launch("video/*")
                            }
                        )
                        
                        // Audio picker
                        val audioPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                onFileSelected(
                                    "Audio_${System.currentTimeMillis()}.mp3",
                                    MessageType.AUDIO.name,
                                    it.toString()
                                )
                                onDismiss()
                            }
                        }
                        
                        FileTypeButton(
                            icon = Icons.Default.AudioFile,
                            title = "Audio",
                            description = "MP3, WAV, AAC",
                            onClick = {
                                audioPicker.launch("audio/*")
                            }
                        )
                        
                        // Document picker
                        val documentPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri ->
                            uri?.let {
                                onFileSelected(
                                    "Documento_${System.currentTimeMillis()}.pdf",
                                    MessageType.DOCUMENT.name,
                                    it.toString()
                                )
                                onDismiss()
                            }
                        }
                        
                        FileTypeButton(
                            icon = Icons.Default.Description,
                            title = "Documento",
                            description = "PDF, DOC, TXT",
                            onClick = {
                                documentPicker.launch("*/*")
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTypeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Seleccionar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
