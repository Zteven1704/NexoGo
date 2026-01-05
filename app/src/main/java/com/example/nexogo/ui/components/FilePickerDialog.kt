package com.example.nexogo.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.model.ClinicalAttachment
import com.example.nexogo.model.ClinicalAttachmentType
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ClinicalAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        val context = LocalContext.current
        var fileName by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(ClinicalAttachmentType.IMAGE) }
        var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
        var selectedFileName by remember { mutableStateOf("") }
        
        // Resetear campos cuando se abre el diálogo
        LaunchedEffect(isVisible) {
            if (isVisible) {
                println("DEBUG: Reseteando campos del FilePickerDialog")
                fileName = ""
                description = ""
                selectedType = ClinicalAttachmentType.IMAGE
                selectedFileUri = null
                selectedFileName = ""
            }
        }
        
        // Launcher para seleccionar archivos
        val filePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                selectedFileName = getFileName(context, it)
                fileName = selectedFileName
                println("DEBUG: Archivo seleccionado: $selectedFileName")
            }
        }
        
        // Launcher para seleccionar imágenes específicamente
        val imagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                selectedFileName = getFileName(context, it)
                fileName = selectedFileName
                println("DEBUG: Imagen seleccionada: $selectedFileName")
            }
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Seleccionar Archivo",
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
                    // Tipo de archivo
                    Text(
                        text = "Tipo de archivo:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Botones para seleccionar tipo
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
                    
                    // Botón para seleccionar archivo
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Seleccionar archivo",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = if (selectedFileUri != null) "Archivo seleccionado" else "Seleccionar archivo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (selectedFileName.isNotEmpty()) {
                                Text(
                                    text = selectedFileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Button(
                                onClick = {
                                    if (selectedType == ClinicalAttachmentType.IMAGE) {
                                        imagePickerLauncher.launch("image/*")
                                    } else {
                                        filePickerLauncher.launch("*/*")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Buscar en el dispositivo")
                            }
                        }
                    }
                    
                    // Nombre del archivo (editable)
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
                                fileUrl = selectedFileUri?.toString() ?: "", // URI del archivo seleccionado
                                description = description,
                                uploadedBy = "current_user",
                                uploadedAt = Date()
                            )
                            onConfirm(attachment)
                            println("DEBUG: Archivo adjunto creado: ${attachment.fileName}")
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

private fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                it.getString(nameIndex) ?: "archivo_${System.currentTimeMillis()}"
            } else {
                "archivo_${System.currentTimeMillis()}"
            }
        } else {
            "archivo_${System.currentTimeMillis()}"
        }
    } ?: "archivo_${System.currentTimeMillis()}"
}

