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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
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
import com.example.nexogo.modules.appointments.AppointmentViewModel
import com.example.nexogo.core.firebase.DateFilterDiagnostics
import com.example.nexogo.model.Appointment
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit = {}
) {
    println("DEBUG: AppointmentsScreen - Iniciando composición")
    
    // Usar AppointmentViewModel del módulo appointments
    val appointmentViewModel = remember { AppointmentViewModel() }
    println("DEBUG: AppointmentsScreen - AppointmentViewModel obtenido")

    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    val message by appointmentViewModel.message.collectAsStateWithLifecycle()
    
    println("DEBUG: AppointmentsScreen - Appointments obtenido: ${appointments.size}")
    println("DEBUG: AppointmentsScreen - IsLoading: $isLoading")
    println("DEBUG: AppointmentsScreen - Message: $message")
    
    // Cargar citas al inicializar
    LaunchedEffect(Unit) {
        println("DEBUG: AppointmentsScreen - Cargando citas...")
        appointmentViewModel.loadAppointments()
    }
    
    // Estados para diálogos
    var showEditScreen by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    
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
                onClick = onNavigateToCreateAppointment,
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Gestión de Citas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Indicador de carga
            if (isLoading) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando citas...")
                    }
                }
            }
            
            // Mostrar mensaje si existe
            if (message.isNotEmpty()) {
                item {
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
            }
            
            // Mostrar número de citas
            item {
                Text(
                    text = "Citas encontradas: ${appointments.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Lista de citas
            if (appointments.isEmpty() && !isLoading) {
                item {
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
                                text = "📅 No hay citas programadas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Las citas aparecerán aquí cuando se programen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(appointments) { appointment ->
                    AppointmentCard(
                        appointment = appointment,
                        onEdit = { appointmentToEdit = appointment; showEditScreen = true },
                        onDelete = { appointmentToDelete = appointment; showDeleteDialog = true }
                    )
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta cita?") },
            confirmButton = {
                Button(
                    onClick = {
                        appointmentToDelete?.let { appointment ->
                            appointmentViewModel.deleteAppointment(appointment.id)
                        }
                        showDeleteDialog = false
                        appointmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = appointment.patientName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Fecha: ${appointment.dateTime.toDate()}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (appointment.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notas: ${appointment.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Estado: ${appointment.status.name}",
                style = MaterialTheme.typography.bodySmall,
                color = when (appointment.status) {
                    com.example.nexogo.model.AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
                    com.example.nexogo.model.AppointmentStatus.COMPLETED -> Color.Green
                    com.example.nexogo.model.AppointmentStatus.CANCELLED -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}