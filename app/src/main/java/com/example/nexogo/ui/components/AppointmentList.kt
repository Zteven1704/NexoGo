package com.example.nexogo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppointmentList(
    appointments: List<Appointment>,
    onEditAppointment: (Appointment) -> Unit,
    onDeleteAppointment: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (appointments.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No hay citas programadas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appointments) { appointment ->
                AppointmentCard(
                    appointment = appointment,
                    onEdit = { onEditAppointment(appointment) },
                    onDelete = { onDeleteAppointment(appointment) }
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appointment.notes.split(" - ").firstOrNull() ?: "Sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dueño: ${appointment.notes.split(" - ").getOrNull(1) ?: "Sin dueño"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar cita",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar cita",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatAppointmentTime(appointment.dateTime.toDate()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                AppointmentStatusChip(status = appointment.status)
            }
            
            if (appointment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = appointment.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppointmentStatusChip(status: AppointmentStatus) {
    val (text, color) = when (status) {
        AppointmentStatus.SCHEDULED -> "Programada" to MaterialTheme.colorScheme.tertiary
        AppointmentStatus.CONFIRMED -> "Confirmada" to MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> "Completada" to Color(0xFF4CAF50)
        AppointmentStatus.CANCELLED -> "Cancelada" to MaterialTheme.colorScheme.outline
        AppointmentStatus.RESCHEDULED -> "Reprogramada" to MaterialTheme.colorScheme.secondary
        AppointmentStatus.NO_SHOW -> "No se presentó" to MaterialTheme.colorScheme.error
        AppointmentStatus.IN_PROGRESS -> "En progreso" to MaterialTheme.colorScheme.primaryContainer
    }
    
    AssistChip(
        onClick = { },
        label = { 
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        )
    )
}

private fun formatAppointmentTime(date: Date?): String {
    return date?.let {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.format(it)
    } ?: "Sin hora"
}
