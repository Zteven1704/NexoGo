package com.example.nexogo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.model.ClinicalAttachment
import com.example.nexogo.model.ClinicalAttachmentType
import java.util.*

@Composable
fun AttachmentDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ClinicalAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        println("DEBUG: AttachmentDialog isVisible = true")
        var fileName by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(ClinicalAttachmentType.IMAGE) }
        
        // Resetear campos cuando se abre el diálogo
        LaunchedEffect(isVisible) {
            if (isVisible) {
                println("DEBUG: Reseteando campos del diálogo")
                fileName = ""
                description = ""
                selectedType = ClinicalAttachmentType.IMAGE
            }
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Agregar Archivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tipo de archivo - Simplificado
                    Text(
                        text = "Tipo de archivo:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Botones simples para seleccionar tipo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedType = ClinicalAttachmentType.IMAGE },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == ClinicalAttachmentType.IMAGE) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Imagen")
                        }
                        
                        Button(
                            onClick = { selectedType = ClinicalAttachmentType.DOCUMENT },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == ClinicalAttachmentType.DOCUMENT) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Doc")
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedType = ClinicalAttachmentType.VIDEO },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == ClinicalAttachmentType.VIDEO) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Video")
                        }
                        
                        Button(
                            onClick = { selectedType = ClinicalAttachmentType.AUDIO },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedType == ClinicalAttachmentType.AUDIO) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audio")
                        }
                    }
                    
                    // Nombre del archivo
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Nombre del archivo") },
                        placeholder = { Text("Ej: radiografia_torax.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true
                    )
                    
                    // Descripción
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción (opcional)") },
                        placeholder = { Text("Ej: Radiografía de tórax - vista lateral") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        enabled = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            val attachment = ClinicalAttachment(
                                id = "att_${System.currentTimeMillis()}",
                                fileName = fileName,
                                fileType = selectedType,
                                fileUrl = "", // En una implementación real, aquí iría la URL del archivo subido
                                description = description,
                                uploadedBy = "current_user", // En una implementación real, usar el usuario actual
                                uploadedAt = Date()
                            )
                            onConfirm(attachment)
                        }
                    },
                    enabled = fileName.isNotBlank()
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

