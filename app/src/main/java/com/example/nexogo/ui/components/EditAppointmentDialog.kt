package com.example.nexogo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import com.example.nexogo.model.SimplePet
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentDialog(
    isOpen: Boolean,
    appointment: Appointment?,
    pets: List<SimplePet>,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen && appointment != null) {
        var selectedPet by remember { mutableStateOf<SimplePet?>(null) }
        var selectedDate by remember { mutableStateOf(appointment.dateTime.toDate()) }
        var selectedTime by remember { mutableStateOf(formatTime(appointment.dateTime.toDate())) }
        var reason by remember { mutableStateOf(appointment.reason) }
        var notes by remember { mutableStateOf(appointment.notes) }
        var selectedStatus by remember { mutableStateOf(appointment.status) }
        
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var showPetDropdown by remember { mutableStateOf(false) }
        var showStatusDropdown by remember { mutableStateOf(false) }
        
        // Initialize selected pet
        LaunchedEffect(appointment) {
            selectedPet = pets.find { it.name == appointment.patientName }
        }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar Cita") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pet selection
                    ExposedDropdownMenuBox(
                        expanded = showPetDropdown,
                        onExpandedChange = { showPetDropdown = !showPetDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedPet?.let { "${it.name}" } ?: "Seleccionar mascota",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mascota") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showPetDropdown,
                            onDismissRequest = { showPetDropdown = false }
                        ) {
                            pets.forEach { pet ->
                                DropdownMenuItem(
                                    text = { Text("${pet.name}") },
                                    onClick = {
                                        selectedPet = pet
                                        showPetDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Date selection
                    OutlinedTextField(
                        value = formatDate(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Time selection
                    ExposedDropdownMenuBox(
                        expanded = showTimePicker,
                        onExpandedChange = { showTimePicker = !showTimePicker }
                    ) {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimePicker) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showTimePicker,
                            onDismissRequest = { showTimePicker = false }
                        ) {
                            generateTimeSlots().forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time) },
                                    onClick = {
                                        selectedTime = time
                                        showTimePicker = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Status selection
                    ExposedDropdownMenuBox(
                        expanded = showStatusDropdown,
                        onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                    ) {
                        OutlinedTextField(
                            value = getStatusDisplayName(selectedStatus),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Estado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showStatusDropdown,
                            onDismissRequest = { showStatusDropdown = false }
                        ) {
                            AppointmentStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(getStatusDisplayName(status)) },
                                    onClick = {
                                        selectedStatus = status
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Reason field
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo de la cita") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Notes field
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPet != null) {
                            val updatedAppointment = appointment.copy(
                                patientName = selectedPet!!.name,
                                patientId = selectedPet!!.id, // Using pet id as patient id for simplicity
                                dateTime = createTimestampFromDateAndTime(selectedDate, selectedTime),
                                reason = reason,
                                notes = notes,
                                status = selectedStatus
                            )
                            println("DEBUG: EditDialog - Cita original ID: ${appointment.id}")
                            println("DEBUG: EditDialog - Cita actualizada ID: ${updatedAppointment.id}")
                            println("DEBUG: EditDialog - Notas originales: ${appointment.notes}")
                            println("DEBUG: EditDialog - Notas nuevas: ${updatedAppointment.notes}")
                            println("DEBUG: EditDialog - Fecha original: ${appointment.dateTime}")
                            println("DEBUG: EditDialog - Fecha nueva: ${updatedAppointment.dateTime}")
                            onSave(updatedAppointment)
                        }
                    },
                    enabled = selectedPet != null
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
        
        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDateSelected = { date ->
                    selectedDate = date
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

private fun formatTime(date: Date): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(date)
}

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(date)
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

private fun generateTimeSlots(): List<String> {
    val timeSlots = mutableListOf<String>()
    for (hour in 8..18) {
        for (minute in listOf(0, 30)) {
            timeSlots.add(String.format("%02d:%02d", hour, minute))
        }
    }
    return timeSlots
}

private fun createTimestampFromDateAndTime(date: Date, time: String): com.google.firebase.Timestamp {
    val calendar = Calendar.getInstance()
    calendar.time = date
    
    val (hour, minute) = time.split(":").map { it.toInt() }
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    return com.google.firebase.Timestamp(calendar.time)
}
