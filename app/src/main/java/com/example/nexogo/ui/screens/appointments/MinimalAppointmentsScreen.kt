package com.example.nexogo.ui.screens.appointments

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalAppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit = {}
) {
    println("DEBUG: MinimalAppointmentsScreen - Iniciando composición")
    
    // Variables de estado locales simples
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var appointments by remember { mutableStateOf<List<AppointmentData>>(emptyList()) }
    
    println("DEBUG: MinimalAppointmentsScreen - Estado inicial: isLoading=$isLoading, message='$message', appointments=${appointments.size}")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Citas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
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
            Text(
                text = "📅 Gestión de Citas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Indicador de estado
            if (isLoading) {
                CircularProgressIndicator()
                Text("Cargando...")
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Calendario visual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
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
                        appointments = appointments,
                        onDateSelected = { date ->
                            selectedDate = date
                            println("DEBUG: MinimalAppointmentsScreen - Fecha seleccionada: $date")
                        }
                    )
                }
            }
            
            // Citas del día seleccionado
            val dayAppointments = appointments.filter { appointment ->
                val appointmentDate = appointment.date
                val selectedDateStr = "${selectedDate.date}/${selectedDate.month + 1}/${selectedDate.year + 1900}"
                val appointmentDateStr = "${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}"
                appointmentDateStr == selectedDateStr
            }
            
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
                                println("DEBUG: MinimalAppointmentsScreen - Editar cita: ${appointment.id}")
                            },
                            onDelete = { 
                                println("DEBUG: MinimalAppointmentsScreen - Eliminar cita: ${appointment.id}")
                                appointments = appointments.filter { it.id != appointment.id }
                            }
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        println("DEBUG: MinimalAppointmentsScreen - Botón Nueva Cita presionado")
                        onNavigateToCreateAppointment()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nueva Cita")
                }
                
                Button(
                    onClick = {
                        println("DEBUG: MinimalAppointmentsScreen - Botón Cargar presionado")
                        isLoading = true
                        message = "Cargando citas..."
                        
                        // Simular carga de citas
                        appointments = listOf(
                            AppointmentData(
                                id = "1",
                                petName = "Max",
                                ownerName = "María González",
                                date = Date(2024, 0, 15), // 15 de enero
                                time = "10:00",
                                reason = "Consulta general",
                                status = "Programada"
                            ),
                            AppointmentData(
                                id = "2",
                                petName = "Luna",
                                ownerName = "Juan Pérez",
                                date = Date(2024, 0, 15), // 15 de enero
                                time = "14:30",
                                reason = "Vacunación",
                                status = "Programada"
                            ),
                            AppointmentData(
                                id = "3",
                                petName = "Bella",
                                ownerName = "Ana López",
                                date = Date(2024, 0, 16), // 16 de enero
                                time = "09:00",
                                reason = "Revisión",
                                status = "Confirmada"
                            )
                        )
                        
                        isLoading = false
                        message = "Citas cargadas exitosamente"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cargar Citas")
                }
            }
            
            Button(
                onClick = {
                    println("DEBUG: MinimalAppointmentsScreen - Botón Volver presionado")
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
    
    println("DEBUG: MinimalAppointmentsScreen - Composición completada")
}

// Datos de cita simple
data class AppointmentData(
    val id: String,
    val petName: String,
    val ownerName: String,
    val date: Date,
    val time: String,
    val reason: String,
    val status: String
)

// Tarjeta de cita
@Composable
fun AppointmentCard(
    appointment: AppointmentData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    text = appointment.petName,
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
                text = "Hora: ${appointment.time}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Motivo: ${appointment.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estado: ${appointment.status}",
                style = MaterialTheme.typography.bodySmall,
                color = when (appointment.status) {
                    "Programada" -> MaterialTheme.colorScheme.primary
                    "Confirmada" -> Color.Green
                    "Cancelada" -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// Calendario simple
@Composable
fun SimpleCalendarGrid(
    selectedDate: Date,
    appointments: List<AppointmentData>,
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
                            val appointmentDate = appointment.date
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
