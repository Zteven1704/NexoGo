package com.example.nexogo.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.modules.appointments.AppointmentViewModelImproved
import com.example.nexogo.model.Appointment
import com.example.nexogo.ui.components.BeautifulCalendar
import com.example.nexogo.ui.components.BeautifulAppointmentCard
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifulAppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit = {},
    onNavigateToEditAppointment: (String) -> Unit = {}
) {
    Log.d("NEXOGO_BEAUTIFUL", "🚀 BeautifulAppointmentsScreen - Iniciando composición")
    
    val appointmentViewModel = remember { AppointmentViewModelImproved() }
    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    
    // Debug: Log de citas cargadas
    LaunchedEffect(appointments) {
        Log.d("NEXOGO_BEAUTIFUL", "📋 Citas cargadas: ${appointments.size}")
        appointments.forEach { appointment ->
            Log.d("NEXOGO_BEAUTIFUL", "📋 Cita ID: ${appointment.id}, Mascota: ${appointment.patientName}")
        }
    }
    val message by appointmentViewModel.message.collectAsStateWithLifecycle()
    
    var selectedDate by remember { mutableStateOf(Date()) }
    var showCalendar by remember { mutableStateOf(true) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var showEditScreen by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    
    // Cargar citas al inicializar
    LaunchedEffect(Unit) {
        try {
            Log.d("NEXOGO_BEAUTIFUL", "🔄 Cargando citas...")
            appointmentViewModel.loadAppointments()
        } catch (e: Exception) {
            Log.e("NEXOGO_BEAUTIFUL", "❌ Error cargando citas: ${e.message}", e)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Citas",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCalendar = !showCalendar }
                    ) {
                        Icon(
                            if (showCalendar) Icons.Default.List else Icons.Default.CalendarToday,
                            contentDescription = if (showCalendar) "Ver lista" else "Ver calendario"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("NEXOGO_BEAUTIFUL", "➕ Botón Nueva Cita presionado")
                    onNavigateToCreateAppointment()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Cita")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mostrar mensaje si existe
            if (message.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
            }
            
            // Mostrar indicador de carga
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            if (showCalendar) {
                // Vista de calendario
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Calendario
                        BeautifulCalendar(
                            appointments = appointments,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                selectedDate = date
                                Log.d("NEXOGO_BEAUTIFUL", "📅 Fecha seleccionada: $date")
                            }
                        )
                    }
                }
                
                // Citas para la fecha seleccionada
                val appointmentsForDate = appointmentViewModel.getAppointmentsForDate(selectedDate)
                Log.d("NEXOGO_BEAUTIFUL", "📅 Citas para fecha seleccionada: ${appointmentsForDate.size}")
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Citas del ${formatDate(selectedDate)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (appointmentsForDate.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No hay citas programadas para este día",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Mostrar citas del día seleccionado
                items(appointmentsForDate) { appointment ->
                    BeautifulAppointmentCard(
                        appointment = appointment,
                        onEdit = { 
                            Log.d("NEXOGO_BEAUTIFUL", "🔧 Botón editar presionado para cita: ${appointment.id}")
                            onNavigateToEditAppointment(appointment.id)
                        },
                        onDelete = { 
                            appointmentToDelete = appointment
                            showDeleteDialog = true
                        },
                        onStatusChange = { status ->
                            appointmentViewModel.updateAppointmentStatus(appointment.id, status)
                        }
                    )
                }
            } else {
                // Vista de lista
                // Header con estadísticas
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatCard(
                                title = "Total",
                                value = appointments.size.toString(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatCard(
                                title = "Programadas",
                                value = appointments.count { it.status.name == "SCHEDULED" }.toString(),
                                color = Color(0xFF2196F3)
                            )
                            StatCard(
                                title = "Confirmadas",
                                value = appointments.count { it.status.name == "CONFIRMED" }.toString(),
                                color = Color(0xFF4CAF50)
                            )
                            StatCard(
                                title = "Completadas",
                                value = appointments.count { it.status.name == "COMPLETED" }.toString(),
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
                
                // Lista de todas las citas
                if (appointments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay citas programadas",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Toca el botón + para crear tu primera cita",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(appointments) { appointment ->
                        BeautifulAppointmentCard(
                            appointment = appointment,
                            onEdit = { 
                                Log.d("NEXOGO_BEAUTIFUL", "🔧 Botón editar presionado para cita: ${appointment.id}")
                                onNavigateToEditAppointment(appointment.id)
                            },
                            onDelete = { 
                                appointmentToDelete = appointment
                                showDeleteDialog = true
                            },
                            onStatusChange = { status ->
                                appointmentViewModel.updateAppointmentStatus(appointment.id, status)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Cita") },
            text = { Text("¿Estás seguro de que quieres eliminar esta cita?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appointmentViewModel.deleteAppointment(appointmentToDelete!!.id)
                        showDeleteDialog = false
                        appointmentToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(date: Date): String {
    val dateFormat = java.text.SimpleDateFormat("dd 'de' MMMM 'de' yyyy", java.util.Locale.getDefault())
    return dateFormat.format(date)
}
