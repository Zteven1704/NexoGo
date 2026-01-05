package com.example.nexogo.modules.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.model.AppointmentStatus
import java.util.*

/**
 * Pantalla principal de gestión de citas con calendario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit,
    viewModel: AppointmentViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val currentDate = Date()
    val calendar = Calendar.getInstance()
    calendar.time = currentDate
    
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    
    // Obtener citas del mes actual
    val monthAppointments = viewModel.getAppointmentsForMonth(currentYear, currentMonth)
    
    // Obtener citas del día seleccionado
    val dayAppointments = viewModel.getAppointmentsForDate(selectedDate)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📅 Gestión de Citas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCreateAppointment) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Cita")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateAppointment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cita")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator()
                Text("Cargando citas...")
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Calendario mensual
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📅 Calendario de Citas",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Calendario simple
                    SimpleCalendarGrid(
                        selectedDate = selectedDate,
                        appointments = monthAppointments,
                        onDateSelected = { date ->
                            viewModel.setSelectedDate(date)
                        }
                    )
                }
            }
            
            // Citas del día seleccionado
            Text(
                text = "Citas del ${selectedDate.date}/${selectedDate.month + 1}/${selectedDate.year + 1900}: ${dayAppointments.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Lista de citas del día
            if (dayAppointments.isEmpty()) {
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
                        Text(
                            text = "📅 No hay citas programadas para este día",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Selecciona otro día o programa una nueva cita",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dayAppointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onEdit = { 
                                // TODO: Implementar edición
                            },
                            onDelete = { 
                                viewModel.deleteAppointment(appointment.id)
                            },
                            onStatusChange = { status ->
                                viewModel.updateAppointmentStatus(appointment.id, status)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de cita individual
 */
@Composable
fun AppointmentCard(
    appointment: com.example.nexogo.model.Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (AppointmentStatus) -> Unit
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
                Text(
                    text = appointment.patientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Dueño: ${appointment.ownerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Veterinario: ${appointment.vetName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Hora: ${appointment.dateTime.toDate().hours}:${appointment.dateTime.toDate().minutes.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Motivo: ${appointment.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Estado de la cita
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado: ",
                    style = MaterialTheme.typography.bodySmall
                )
                
                FilterChip(
                    onClick = { 
                        val newStatus = when (appointment.status) {
                            com.example.nexogo.model.AppointmentStatus.SCHEDULED -> com.example.nexogo.model.AppointmentStatus.CONFIRMED
                            com.example.nexogo.model.AppointmentStatus.CONFIRMED -> com.example.nexogo.model.AppointmentStatus.COMPLETED
                            com.example.nexogo.model.AppointmentStatus.COMPLETED -> com.example.nexogo.model.AppointmentStatus.SCHEDULED
                            com.example.nexogo.model.AppointmentStatus.CANCELLED -> com.example.nexogo.model.AppointmentStatus.SCHEDULED
                            else -> com.example.nexogo.model.AppointmentStatus.SCHEDULED
                        }
                        onStatusChange(newStatus)
                    },
                    label = { Text(appointment.status.name) },
                    selected = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = when (appointment.status) {
                            com.example.nexogo.model.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer
                            com.example.nexogo.model.AppointmentStatus.CONFIRMED -> Color.Green.copy(alpha = 0.2f)
                            com.example.nexogo.model.AppointmentStatus.COMPLETED -> Color.Blue.copy(alpha = 0.2f)
                            com.example.nexogo.model.AppointmentStatus.CANCELLED -> Color.Red.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
        }
    }
}

/**
 * Calendario simple
 */
@Composable
fun SimpleCalendarGrid(
    selectedDate: Date,
    appointments: List<com.example.nexogo.model.Appointment>,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = selectedDate
    
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    // Obtener el primer día del mes
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    
    // Obtener el número de días en el mes
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    Column {
        // Encabezado del calendario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${getMonthName(currentMonth)} $currentYear",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Días del mes
        var day = 1
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < firstDayOfWeek - 1) {
                        // Espacios vacíos antes del primer día del mes
                        Box(modifier = Modifier.weight(1f).height(40.dp))
                    } else if (day <= daysInMonth) {
                        val currentDay = day
                        val hasAppointment = appointments.any { appointment ->
                            val appointmentDate = appointment.dateTime.toDate()
                            appointmentDate.date == currentDay && 
                            appointmentDate.month == currentMonth && 
                            appointmentDate.year + 1900 == currentYear
                        }
                        
                        val isSelected = selectedDate.date == currentDay && 
                                       selectedDate.month == currentMonth && 
                                       selectedDate.year + 1900 == currentYear
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    val newDate = Date(currentYear - 1900, currentMonth, currentDay)
                                    onDateSelected(newDate)
                                }
                                .background(
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        hasAppointment -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    hasAppointment -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        day++
                    } else {
                        // Espacios vacíos después del último día del mes
                        Box(modifier = Modifier.weight(1f).height(40.dp))
                    }
                }
            }
        }
    }
}

fun getMonthName(month: Int): String {
    return when (month) {
        0 -> "Enero"
        1 -> "Febrero"
        2 -> "Marzo"
        3 -> "Abril"
        4 -> "Mayo"
        5 -> "Junio"
        6 -> "Julio"
        7 -> "Agosto"
        8 -> "Septiembre"
        9 -> "Octubre"
        10 -> "Noviembre"
        11 -> "Diciembre"
        else -> ""
    }
}

