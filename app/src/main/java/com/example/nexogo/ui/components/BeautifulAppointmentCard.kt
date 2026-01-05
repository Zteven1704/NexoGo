package com.example.nexogo.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BeautifulAppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (AppointmentStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(appointment.status).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con nombre de la mascota y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Mascota",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = appointment.patientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información de la cita
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fecha y hora
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Fecha y Hora",
                    value = formatDateTime(appointment.dateTime.toDate()),
                    iconColor = MaterialTheme.colorScheme.primary
                )
                
                // Dueño
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Dueño",
                    value = appointment.ownerName,
                    iconColor = MaterialTheme.colorScheme.secondary
                )
                
                // Veterinario
                InfoRow(
                    icon = Icons.Default.MedicalServices,
                    label = "Veterinario",
                    value = appointment.vetName,
                    iconColor = MaterialTheme.colorScheme.tertiary
                )
                
                // Motivo
                InfoRow(
                    icon = Icons.Default.Info,
                    label = "Motivo",
                    value = appointment.reason,
                    iconColor = MaterialTheme.colorScheme.outline
                )
                
                // Notas (si existen)
                if (appointment.notes.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Default.Notes,
                        label = "Notas",
                        value = appointment.notes,
                        iconColor = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estado y botón de cambio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estado actual
                StatusChip(
                    status = appointment.status,
                    onClick = {
                        val newStatus = getNextStatus(appointment.status)
                        onStatusChange(newStatus)
                    }
                )
                
                // Duración
                Text(
                    text = "${appointment.duration} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusChip(
    status: AppointmentStatus,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = getStatusColor(status),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(status).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getStatusIcon(status),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = getStatusColor(status)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = getStatusText(status),
                fontWeight = FontWeight.Medium,
                color = getStatusColor(status)
            )
        }
    }
}

private fun getStatusColor(status: AppointmentStatus): Color {
    return when (status) {
        AppointmentStatus.SCHEDULED -> Color(0xFF2196F3) // Azul
        AppointmentStatus.CONFIRMED -> Color(0xFF4CAF50) // Verde
        AppointmentStatus.COMPLETED -> Color(0xFF9C27B0) // Púrpura
        AppointmentStatus.CANCELLED -> Color(0xFFF44336) // Rojo
        else -> Color(0xFF757575) // Gris para estados no definidos
    }
}

private fun getStatusText(status: AppointmentStatus): String {
    return when (status) {
        AppointmentStatus.SCHEDULED -> "Programada"
        AppointmentStatus.CONFIRMED -> "Confirmada"
        AppointmentStatus.COMPLETED -> "Completada"
        AppointmentStatus.CANCELLED -> "Cancelada"
        else -> "Desconocido"
    }
}

private fun getStatusIcon(status: AppointmentStatus): ImageVector {
    return when (status) {
        AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
        AppointmentStatus.CONFIRMED -> Icons.Default.CheckCircle
        AppointmentStatus.COMPLETED -> Icons.Default.Done
        AppointmentStatus.CANCELLED -> Icons.Default.Cancel
        else -> Icons.Default.Help
    }
}

private fun getNextStatus(currentStatus: AppointmentStatus): AppointmentStatus {
    return when (currentStatus) {
        AppointmentStatus.SCHEDULED -> AppointmentStatus.CONFIRMED
        AppointmentStatus.CONFIRMED -> AppointmentStatus.COMPLETED
        AppointmentStatus.COMPLETED -> AppointmentStatus.SCHEDULED
        AppointmentStatus.CANCELLED -> AppointmentStatus.SCHEDULED
        else -> AppointmentStatus.SCHEDULED
    }
}

private fun formatDateTime(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale.getDefault())
    return dateFormat.format(date)
}
