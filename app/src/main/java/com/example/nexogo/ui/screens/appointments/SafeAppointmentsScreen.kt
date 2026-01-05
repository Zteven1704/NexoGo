package com.example.nexogo.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.SafeFirebaseAppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeAppointmentsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateAppointment: () -> Unit = {}
) {
    println("DEBUG: SafeAppointmentsScreen - Iniciando composición")
    
    // Usar ViewModel seguro
    val appointmentViewModel = remember { SafeFirebaseAppointmentViewModel() }
    println("DEBUG: SafeAppointmentsScreen - ViewModel obtenido")

    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    val message by appointmentViewModel.message.collectAsStateWithLifecycle()
    
    println("DEBUG: SafeAppointmentsScreen - Appointments obtenido: ${appointments.size}")
    println("DEBUG: SafeAppointmentsScreen - IsLoading: $isLoading")
    println("DEBUG: SafeAppointmentsScreen - Message: $message")
    
    // Cargar citas al inicializar
    LaunchedEffect(Unit) {
        println("DEBUG: SafeAppointmentsScreen - Cargando citas...")
        appointmentViewModel.loadAppointments()
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var appointmentToDelete by remember { mutableStateOf<com.example.nexogo.model.Appointment?>(null) }
    
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📅 Gestión de Citas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando citas...")
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Mostrar mensaje si existe
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
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Mostrar número de citas
            Text(
                text = "Citas encontradas: ${appointments.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de citas
            if (appointments.isEmpty() && !isLoading) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appointments) { appointment ->
                        SafeAppointmentCard(
                            appointment = appointment,
                            onEdit = { /* TODO: Implementar edición */ },
                            onDelete = { appointmentToDelete = appointment; showDeleteDialog = true }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón para nueva cita
            Button(
                onClick = onNavigateToCreateAppointment,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nueva Cita")
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
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
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        appointmentToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SafeAppointmentCard(
    appointment: com.example.nexogo.model.Appointment,
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
        }
    }
}

