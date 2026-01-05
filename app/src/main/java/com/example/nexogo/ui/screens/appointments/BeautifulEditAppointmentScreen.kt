package com.example.nexogo.ui.screens.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.SimplePet
import com.example.nexogo.modules.appointments.AppointmentViewModelImproved
import androidx.compose.ui.platform.LocalContext
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifulEditAppointmentScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit
) {
    val appointmentViewModel = remember { AppointmentViewModelImproved() }
    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    
    // Buscar la cita por ID
    val appointment = remember(appointmentId, appointments) {
        appointments.find { it.id == appointmentId }
    }
    
    if (appointment == null) {
        // Pantalla de carga o error
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cargando cita...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Cita no encontrada",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: $appointmentId",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }
    
    // Pantalla de edición principal
    BeautifulEditAppointmentContent(
        appointment = appointment,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeautifulEditAppointmentContent(
    appointment: Appointment,
    onNavigateBack: () -> Unit
) {
    var selectedPet by remember { mutableStateOf<SimplePet?>(null) }
    var selectedDate by remember { mutableStateOf(appointment.dateTime.toDate()) }
    var selectedHour by remember { mutableStateOf(appointment.dateTime.toDate().hours) }
    var selectedMinute by remember { mutableStateOf(appointment.dateTime.toDate().minutes) }
    var reason by remember { mutableStateOf(appointment.reason) }
    var notes by remember { mutableStateOf(appointment.notes) }
    var selectedStatus by remember { mutableStateOf(appointment.status) }
    
    var showPetDropdown by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMinutePicker by remember { mutableStateOf(false) }
    var showStatusPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    
    val appointmentViewModel = remember { AppointmentViewModelImproved() }
    
    // Lista de mascotas de ejemplo
    val pets = remember {
        listOf(
            SimplePet("1", "Max", "Perro", "Golden Retriever"),
            SimplePet("2", "Luna", "Gato", "Persa"),
            SimplePet("3", "Rocky", "Perro", "Bulldog"),
            SimplePet("4", "Bella", "Gato", "Siamés"),
            SimplePet("5", "Charlie", "Perro", "Labrador")
        )
    }
    
    // Inicializar mascota seleccionada
    LaunchedEffect(appointment) {
        selectedPet = pets.find { it.name == appointment.patientName }
    }
    
    // Función para manejar el guardado
    fun handleSave() {
        if (isSaving) return
        
        isSaving = true
        saveError = null
        
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
        calendar.set(Calendar.MINUTE, selectedMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val updatedAppointment = appointment.copy(
            patientName = selectedPet?.name ?: appointment.patientName,
            patientId = selectedPet?.id ?: appointment.patientId,
            dateTime = com.google.firebase.Timestamp(calendar.time),
            reason = reason,
            notes = notes,
            status = selectedStatus,
            updatedAt = com.google.firebase.Timestamp.now()
        )
        
        kotlinx.coroutines.GlobalScope.launch {
            try {
                appointmentViewModel.updateAppointment(updatedAppointment)
                withContext(Dispatchers.Main) {
                    onNavigateBack()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    saveError = "Error al guardar: ${e.message}"
                    isSaving = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Editar Cita",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header con información de la cita
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Editando Cita",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Mascota",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = appointment.patientName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Fecha Original",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                    .format(appointment.dateTime.toDate()),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Mostrar error si existe
            if (saveError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Error al guardar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = saveError!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { 
                                saveError = null
                                handleSave()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            
            // Formulario de edición
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Título del formulario
                    Text(
                        text = "Información de la Cita",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Selector de mascota
                    Column {
                        Text(
                            text = "Mascota",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = showPetDropdown,
                            onExpandedChange = { showPetDropdown = !showPetDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedPet?.let { "${it.name} - ${it.species}" } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Seleccionar mascota") },
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showPetDropdown,
                                onDismissRequest = { showPetDropdown = false }
                            ) {
                                pets.forEach { pet ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    if (pet.species == "Perro") Icons.Default.Pets else Icons.Default.Pets,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text("${pet.name} - ${pet.species}")
                                            }
                                        },
                                        onClick = {
                                            selectedPet = pet
                                            showPetDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Selector de fecha y hora
                    Column {
                        Text(
                            text = "Fecha y Hora",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Fecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Día
                            ExposedDropdownMenuBox(
                                expanded = showDayPicker,
                                onExpandedChange = { showDayPicker = !showDayPicker },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedDate.date.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Día") },
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDayPicker) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showDayPicker,
                                    onDismissRequest = { showDayPicker = false }
                                ) {
                                    (1..31).forEach { day ->
                                        DropdownMenuItem(
                                            text = { Text(day.toString()) },
                                            onClick = {
                                                val calendar = Calendar.getInstance()
                                                calendar.time = selectedDate
                                                calendar.set(Calendar.DAY_OF_MONTH, day)
                                                selectedDate = calendar.time
                                                showDayPicker = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Mes
                            ExposedDropdownMenuBox(
                                expanded = showMonthPicker,
                                onExpandedChange = { showMonthPicker = !showMonthPicker },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = (selectedDate.month + 1).toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Mes") },
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthPicker) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showMonthPicker,
                                    onDismissRequest = { showMonthPicker = false }
                                ) {
                                    (1..12).forEach { month ->
                                        DropdownMenuItem(
                                            text = { Text(month.toString()) },
                                            onClick = {
                                                val calendar = Calendar.getInstance()
                                                calendar.time = selectedDate
                                                calendar.set(Calendar.MONTH, month - 1)
                                                selectedDate = calendar.time
                                                showMonthPicker = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Año
                            ExposedDropdownMenuBox(
                                expanded = showYearPicker,
                                onExpandedChange = { showYearPicker = !showYearPicker },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = (selectedDate.year + 1900).toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Año") },
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearPicker) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showYearPicker,
                                    onDismissRequest = { showYearPicker = false }
                                ) {
                                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                    (currentYear..currentYear + 2).forEach { year ->
                                        DropdownMenuItem(
                                            text = { Text(year.toString()) },
                                            onClick = {
                                                val calendar = Calendar.getInstance()
                                                calendar.time = selectedDate
                                                calendar.set(Calendar.YEAR, year)
                                                selectedDate = calendar.time
                                                showYearPicker = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Hora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = showTimePicker,
                                onExpandedChange = { showTimePicker = !showTimePicker },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = String.format("%02d", selectedHour),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Hora") },
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimePicker) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showTimePicker,
                                    onDismissRequest = { showTimePicker = false }
                                ) {
                                    (0..23).forEach { hour ->
                                        DropdownMenuItem(
                                            text = { Text(String.format("%02d", hour)) },
                                            onClick = {
                                                selectedHour = hour
                                                showTimePicker = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            ExposedDropdownMenuBox(
                                expanded = showMinutePicker,
                                onExpandedChange = { showMinutePicker = !showMinutePicker },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = String.format("%02d", selectedMinute),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Minutos") },
                                    trailingIcon = { 
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMinutePicker) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showMinutePicker,
                                    onDismissRequest = { showMinutePicker = false }
                                ) {
                                    (0..59 step 15).forEach { minute ->
                                        DropdownMenuItem(
                                            text = { Text(String.format("%02d", minute)) },
                                            onClick = {
                                                selectedMinute = minute
                                                showMinutePicker = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Campo de motivo
                    Column {
                        Text(
                            text = "Motivo de la Cita",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Motivo de la cita") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    // Campo de notas
                    Column {
                        Text(
                            text = "Notas Adicionales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notas adicionales") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    // Selector de estado
                    Column {
                        Text(
                            text = "Estado de la Cita",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = showStatusPicker,
                            onExpandedChange = { showStatusPicker = !showStatusPicker }
                        ) {
                            OutlinedTextField(
                                value = when (selectedStatus) {
                                    com.example.nexogo.model.AppointmentStatus.SCHEDULED -> "Programada"
                                    com.example.nexogo.model.AppointmentStatus.CONFIRMED -> "Confirmada"
                                    com.example.nexogo.model.AppointmentStatus.COMPLETED -> "Completada"
                                    com.example.nexogo.model.AppointmentStatus.CANCELLED -> "Cancelada"
                                    else -> "Programada"
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Estado") },
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusPicker) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showStatusPicker,
                                onDismissRequest = { showStatusPicker = false }
                            ) {
                                listOf(
                                    com.example.nexogo.model.AppointmentStatus.SCHEDULED to "Programada",
                                    com.example.nexogo.model.AppointmentStatus.CONFIRMED to "Confirmada",
                                    com.example.nexogo.model.AppointmentStatus.COMPLETED to "Completada",
                                    com.example.nexogo.model.AppointmentStatus.CANCELLED to "Cancelada"
                                ).forEach { (status, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedStatus = status
                                            showStatusPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text("Guardando...")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Guardar Cambios")
                        }
                    }
                }
            }
        }
    }
}

