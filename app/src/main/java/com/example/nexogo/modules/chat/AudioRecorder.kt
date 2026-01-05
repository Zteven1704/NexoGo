package com.example.nexogo.modules.chat

import android.media.MediaRecorder
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

/**
 * Componente para grabación de notas de voz
 */
@Composable
fun AudioRecorder(
    onRecordingComplete: (Uri, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var outputFile by remember { mutableStateOf<File?>(null) }
    
    val context = LocalContext.current
    
    // Animación para el botón de grabación
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Timer para la duración de grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingDuration++
            }
        } else {
            recordingDuration = 0
        }
    }
    
    // Función para iniciar grabación
    fun startRecording() {
        try {
            outputFile = File.createTempFile("voice_note_", ".m4a", context.cacheDir)
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    
    // Función para detener grabación
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    val uri = Uri.fromFile(file)
                    val fileName = "nota_voz_${System.currentTimeMillis()}.m4a"
                    val fileSize = file.length()
                    onRecordingComplete(uri, fileName, fileSize)
                }
            }
            
            isRecording = false
            recordingDuration = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Función para cancelar grabación
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            outputFile?.delete()
            outputFile = null
            
            isRecording = false
            recordingDuration = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    if (isRecording) {
        // UI durante la grabación
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicador de grabación
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Text(
                        text = "Grabando...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                // Duración
                Text(
                    text = formatDuration(recordingDuration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                // Botones de acción
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón cancelar
                    IconButton(
                        onClick = { cancelRecording() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    // Botón detener
                    IconButton(
                        onClick = { stopRecording() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Detener",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    } else {
        // Botón para iniciar grabación
        IconButton(
            onClick = { startRecording() },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Grabar nota de voz",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Formatear duración en segundos a formato MM:SS
 */
private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

/**
 * Componente para mostrar la duración de una nota de voz
 */
@Composable
fun VoiceNoteDuration(
    duration: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Reproducir",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = formatDuration(duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Componente para mostrar el estado de grabación
 */
@Composable
fun RecordingIndicator(
    isRecording: Boolean,
    duration: Int,
    modifier: Modifier = Modifier
) {
    if (isRecording) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Indicador visual
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                
                Text(
                    text = "Grabando ${formatDuration(duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

