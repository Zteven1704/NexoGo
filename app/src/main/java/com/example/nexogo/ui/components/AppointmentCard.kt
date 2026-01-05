package com.example.nexogo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.Appointment
import com.example.nexogo.viewmodel.PatientViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    patientViewModel: PatientViewModel,
    modifier: Modifier = Modifier
) {
    val pets by patientViewModel.patients.collectAsStateWithLifecycle()
    val allPets = patientViewModel.getAllPets()
    
    // Usar el nombre de la mascota directamente del appointment
    val petName = appointment.patientName.ifEmpty { "Mascota no especificada" }
    
    // Formatear la fecha y hora
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val appointmentDate = appointment.dateTime.toDate()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con nombre de la mascota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Botones de acción
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar cita",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar cita",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Información de la cita
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fecha: ${dateFormat.format(appointmentDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Hora: ${timeFormat.format(appointmentDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Estado de la cita
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (appointment.status) {
                        com.example.nexogo.model.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.tertiaryContainer
                        com.example.nexogo.model.AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                        com.example.nexogo.model.AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
                        com.example.nexogo.model.AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (appointment.status) {
                            com.example.nexogo.model.AppointmentStatus.SCHEDULED -> "Programada"
                            com.example.nexogo.model.AppointmentStatus.CONFIRMED -> "Confirmada"
                            com.example.nexogo.model.AppointmentStatus.COMPLETED -> "Completada"
                            com.example.nexogo.model.AppointmentStatus.CANCELLED -> "Cancelada"
                            else -> "Desconocido"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (appointment.status) {
                            com.example.nexogo.model.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.onTertiaryContainer
                            com.example.nexogo.model.AppointmentStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                            com.example.nexogo.model.AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.onSecondaryContainer
                            com.example.nexogo.model.AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // Notas de la cita
            if (appointment.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notas: ${appointment.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Razón de la cita
            if (appointment.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Motivo: ${appointment.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
