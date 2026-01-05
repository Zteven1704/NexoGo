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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.modules.appointments.AppointmentViewModelImproved
import com.example.nexogo.core.firebase.DateFilterDiagnostics
import com.example.nexogo.model.Appointment
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreenImproved(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit = {}
) {
    Log.d("NEXOGO_APPT_UI", "🚀 AppointmentsScreenImproved - Iniciando composición")
    
    // Usar AppointmentViewModelImproved
    val appointmentViewModel = remember { AppointmentViewModelImproved() }
    Log.d("NEXOGO_APPT_UI", "📱 AppointmentViewModelImproved obtenido")

    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    val message by appointmentViewModel.message.collectAsStateWithLifecycle()
    
    Log.d("NEXOGO_APPT_UI", "📊 Appointments obtenido: ${appointments.size}")
    Log.d("NEXOGO_APPT_UI", "⏳ IsLoading: $isLoading")
    Log.d("NEXOGO_APPT_UI", "💬 Message: $message")
    
    // Cargar citas al inicializar
    LaunchedEffect(Unit) {
        try {
            Log.d("NEXOGO_APPT_UI", "🔄 Cargando citas...")
            appointmentViewModel.loadAppointments()
        } catch (e: Exception) {
            Log.e("NEXOGO_APPT_UI", "❌ Error cargando citas: ${e.message}", e)
        }
    }
    
    var selectedDate by remember { mutableStateOf(Date()) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var showEditScreen by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    
    // Pantalla con todos los ViewModels agregados
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d("NEXOGO_APPT_UI", "➕ Botón Nueva Cita presionado")
                    onNavigateToCreateAppointment()
                }
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
        ) {
            // Mostrar mensaje si existe
            if (message.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) Color.Red.copy(alpha = 0.1f)
                        else Color.Green.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (message.contains("Error")) Color.Red else Color.Green
                    )
                }
            }
            
            // Mostrar indicador de carga
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Información de debug
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Debug Info",
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    )
                    Text("Total citas: ${appointments.size}")
                    Text("Cargando: $isLoading")
                    Text("Mensaje: $message")
                }
            }
            
            // Calendario simple
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calendario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mostrar citas para la fecha seleccionada
                    val appointmentsForDate = appointmentViewModel.getAppointmentsForDate(selectedDate)
                    Log.d("NEXOGO_APPT_UI", "📅 Citas para fecha seleccionada: ${appointmentsForDate.size}")
                    
                    if (appointmentsForDate.isEmpty()) {
                        Text(
                            text = "No hay citas programadas para este día",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        appointmentsForDate.forEach { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onEdit = { 
                                    appointmentToEdit = appointment
                                    showEditScreen = true
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
            
            // Lista de todas las citas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Todas las Citas (${appointments.size})",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (appointments.isEmpty()) {
                        Text(
                            text = "No hay citas programadas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn {
                            items(appointments) { appointment ->
                                AppointmentCard(
                                    appointment = appointment,
                                    onEdit = { 
                                        appointmentToEdit = appointment
                                        showEditScreen = true
                                    },
                                    onDelete = { 
                                        appointmentToDelete = appointment
                                        showDeleteDialog = true
                                    },
                                    onStatusChange = { status ->
                                        appointmentViewModel.updateAppointmentStatus(appointment.id, status)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
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
fun AppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (com.example.nexogo.model.AppointmentStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                text = "Fecha: ${appointment.dateTime.toDate()}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Dueño: ${appointment.ownerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Veterinario: ${appointment.vetName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Motivo: ${appointment.reason}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón para cambiar estado
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
                selected = false
            )
        }
    }
}
