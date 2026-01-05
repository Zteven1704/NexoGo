package com.example.nexogo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDialog(
    appointment: Appointment?,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        var patientName by remember { mutableStateOf(appointment?.notes?.split(" - ")?.getOrNull(1) ?: "") }
        var petName by remember { mutableStateOf(appointment?.notes?.split(" - ")?.firstOrNull() ?: "") }
        var reason by remember { mutableStateOf(appointment?.reason ?: "") }
        var notes by remember { mutableStateOf(appointment?.notes ?: "") }
        var selectedDate by remember { mutableStateOf(appointment?.dateTime?.toDate() ?: Date()) }
        var selectedTime by remember { mutableStateOf(formatTime(appointment?.dateTime?.toDate())) }
        var selectedStatus by remember { mutableStateOf(appointment?.status ?: AppointmentStatus.SCHEDULED) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Text(if (appointment == null) "Nueva Cita" else "Editar Cita")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Patient name
                    OutlinedTextField(
                        value = patientName,
                        onValueChange = { patientName = it },
                        label = { Text("Nombre del Dueño") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Pet name
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Nombre de la Mascota") },
                        leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Reason
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo de la cita") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Date picker
                    OutlinedTextField(
                        value = formatDate(selectedDate),
                        onValueChange = { },
                        label = { Text("Fecha") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Implement date picker */ }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        }
                    )
                    
                    // Time picker
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { selectedTime = it },
                        label = { Text("Hora (HH:MM)") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("14:30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    // Status dropdown
                    ExposedDropdownMenuBox(
                        expanded = false,
                        onExpandedChange = { }
                    ) {
                        OutlinedTextField(
                            value = getStatusDisplayName(selectedStatus),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Estado") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = false,
                            onDismissRequest = { }
                        ) {
                            AppointmentStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(getStatusDisplayName(status)) },
                                    onClick = { selectedStatus = status }
                                )
                            }
                        }
                    }
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas") },
                        leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val combinedDateTime = combineDateAndTime(selectedDate, selectedTime)
                        val newAppointment = (appointment ?: Appointment()).copy(
                            reason = reason,
                            notes = "$petName - $patientName",
                            dateTime = com.google.firebase.Timestamp(combinedDateTime),
                            status = selectedStatus
                        )
                        onSave(newAppointment)
                    },
                    enabled = patientName.isNotBlank() && petName.isNotBlank()
                ) {
                    Text(if (appointment == null) "Agregar" else "Guardar")
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

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

private fun formatTime(date: Date?): String {
    return date?.let {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormat.format(it)
    } ?: ""
}

private fun getStatusDisplayName(status: AppointmentStatus): String {
    return when (status) {
        AppointmentStatus.SCHEDULED -> "Programada"
        AppointmentStatus.CONFIRMED -> "Confirmada"
        AppointmentStatus.COMPLETED -> "Completada"
        AppointmentStatus.CANCELLED -> "Cancelada"
        AppointmentStatus.RESCHEDULED -> "Reprogramada"
        AppointmentStatus.NO_SHOW -> "No se presentó"
        AppointmentStatus.IN_PROGRESS -> "En progreso"
    }
}

private fun combineDateAndTime(date: Date, timeString: String): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    
    try {
        val timeParts = timeString.split(":")
        if (timeParts.size == 2) {
            val hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
    } catch (e: Exception) {
        // Keep original time if parsing fails
    }
    
    return calendar.time
}
