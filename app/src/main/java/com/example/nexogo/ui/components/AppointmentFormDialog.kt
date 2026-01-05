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
fun AppointmentFormDialog(
    isOpen: Boolean,
    pets: List<SimplePet>,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isOpen) {
        var selectedPet by remember { mutableStateOf<SimplePet?>(null) }
        var selectedDate by remember { mutableStateOf(Date()) }
        var selectedTime by remember { mutableStateOf("09:00") }
        var reason by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf(AppointmentStatus.SCHEDULED) }
        
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        var showPetDropdown by remember { mutableStateOf(false) }
        var showStatusDropdown by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nueva Cita") },
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
                            value = selectedPet?.name ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Mascota") },
                            leadingIcon = { Icon(Icons.Default.Pets, contentDescription = null) },
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
                                    text = { Text(pet.name) },
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
                        onValueChange = { },
                        label = { Text("Fecha") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )
                    
                    // Time selection
                    ExposedDropdownMenuBox(
                        expanded = showTimePicker,
                        onExpandedChange = { showTimePicker = !showTimePicker }
                    ) {
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Hora") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimePicker) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = showTimePicker,
                            onDismissRequest = { showTimePicker = false }
                        ) {
                            getTimeSlots().forEach { time ->
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
                    
                    // Reason
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo de la cita") },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Status selection
                    ExposedDropdownMenuBox(
                        expanded = showStatusDropdown,
                        onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                    ) {
                        OutlinedTextField(
                            value = getStatusDisplayName(selectedStatus),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Estado") },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
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
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPet != null) {
                            val combinedDateTime = combineDateAndTime(selectedDate, selectedTime)
                            val appointment = Appointment(
                                patientId = selectedPet!!.id,
                                patientName = selectedPet!!.name,
                                vetId = "current_vet",
                                dateTime = com.google.firebase.Timestamp(combinedDateTime),
                                reason = reason,
                                notes = "${selectedPet!!.name} - ${reason}",
                                status = selectedStatus
                            )
                            onSave(appointment)
                        }
                    },
                    enabled = selectedPet != null && reason.isNotBlank()
                ) {
                    Text("Agregar Cita")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
        
        // Date picker
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

@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    
    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val years = (currentYear..currentYear + 2).toList()
    val months = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    
    // Calculate days in selected month
    val daysInMonth = Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val days = (1..daysInMonth).toList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Fecha") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Selecciona una fecha:", style = MaterialTheme.typography.bodyMedium)
                
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Año:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (selectedYear > currentYear) selectedYear-- 
                            },
                            enabled = selectedYear > currentYear
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Año anterior")
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { 
                                if (selectedYear < currentYear + 2) selectedYear++ 
                            },
                            enabled = selectedYear < currentYear + 2
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Año siguiente")
                        }
                    }
                }
                
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mes:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (selectedMonth > 0) {
                                    selectedMonth--
                                } else {
                                    selectedMonth = 11
                                    if (selectedYear > currentYear) selectedYear--
                                }
                            },
                            enabled = !(selectedYear == currentYear && selectedMonth == 0)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior")
                        }
                        Text(
                            text = months[selectedMonth],
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(
                            onClick = { 
                                if (selectedMonth < 11) {
                                    selectedMonth++
                                } else {
                                    selectedMonth = 0
                                    if (selectedYear < currentYear + 2) selectedYear++
                                }
                            },
                            enabled = !(selectedYear == currentYear + 2 && selectedMonth == 11)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente")
                        }
                    }
                }
                
                // Day selector
                Text("Día:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(days) { day ->
                        val isSelected = day == selectedDay
                        val isToday = day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                     selectedMonth == Calendar.getInstance().get(Calendar.MONTH) &&
                                     selectedYear == Calendar.getInstance().get(Calendar.YEAR)
                        
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    },
                                    CircleShape
                                )
                                .clickable { selectedDay = day },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Quick date buttons
                Divider()
                Text("Fechas rápidas:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { 
                            val today = Calendar.getInstance()
                            selectedYear = today.get(Calendar.YEAR)
                            selectedMonth = today.get(Calendar.MONTH)
                            selectedDay = today.get(Calendar.DAY_OF_MONTH)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hoy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { 
                            val tomorrow = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_MONTH, 1)
                            }
                            selectedYear = tomorrow.get(Calendar.YEAR)
                            selectedMonth = tomorrow.get(Calendar.MONTH)
                            selectedDay = tomorrow.get(Calendar.DAY_OF_MONTH)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mañana")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }.time
                    onDateSelected(selectedDate)
                }
            ) {
                Text("Seleccionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

private fun getTimeSlots(): List<String> {
    return listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30"
    )
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
            val hour = timeParts[0].toIntOrNull() ?: 9
            val minute = timeParts[1].toIntOrNull() ?: 0
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
    } catch (e: Exception) {
        // Keep default time if parsing fails
    }
    
    return calendar.time
}
