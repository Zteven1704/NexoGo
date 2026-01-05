package com.example.nexogo.modules.history.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nexogo.core.FirebaseRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun AttachmentUploader(
    recordId: String,
    onUploadComplete: (String) -> Unit,
    onUploadError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var attachments by remember { mutableStateOf<List<AttachmentItem>>(emptyList()) }
    
    val firebaseRepository = remember { FirebaseRepository() }
    
    // Selector de archivos
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            isUploading = true
            uploadProgress = 0f
            
            GlobalScope.launch {
                try {
                    val fileName = "attachment_${System.currentTimeMillis()}"
                    val storagePath = "medical_records/$recordId/files/$fileName"
                    
                    val result = firebaseRepository.uploadFile(storagePath, fileUri)
                    if (result.isSuccess) {
                        val downloadUrl = result.getOrNull() ?: ""
                        val newAttachment = AttachmentItem(
                            name = fileName,
                            url = downloadUrl,
                            type = getFileType(fileName)
                        )
                        attachments = attachments + newAttachment
                        onUploadComplete(downloadUrl)
                    } else {
                        onUploadError("Error subiendo archivo: ${result.exceptionOrNull()?.message}")
                    }
                    isUploading = false
                } catch (e: Exception) {
                    onUploadError("Error: ${e.message}")
                    isUploading = false
                }
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Adjuntos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botón para seleccionar archivos
        Button(
            onClick = { filePickerLauncher.launch("*/*") },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isUploading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subiendo...")
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Adjuntar archivo"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjuntar Archivo")
            }
        }
        
        if (isUploading) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = uploadProgress,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Lista de adjuntos
        if (attachments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachments) { attachment ->
                    AttachmentCard(
                        attachment = attachment,
                        onRemove = {
                            attachments = attachments.filter { it != attachment }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentCard(
    attachment: AttachmentItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Preview del archivo
            when (attachment.type) {
                AttachmentType.IMAGE -> {
                    AsyncImage(
                        model = attachment.url,
                        contentDescription = attachment.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                AttachmentType.PDF -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = "Archivo",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = attachment.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

data class AttachmentItem(
    val name: String,
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE, PDF, DOCUMENT, OTHER
}

private fun getFileType(fileName: String): AttachmentType {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp" -> AttachmentType.IMAGE
        "pdf" -> AttachmentType.PDF
        "doc", "docx", "txt", "rtf" -> AttachmentType.DOCUMENT
        else -> AttachmentType.OTHER
    }
}

