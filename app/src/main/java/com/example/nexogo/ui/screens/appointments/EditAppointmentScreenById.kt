package com.example.nexogo.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppointmentScreenById(
    appointmentId: String,
    onNavigateBack: () -> Unit
) {
    android.util.Log.d("NEXOGO_EDIT", "🔧 EditAppointmentScreenById iniciada con ID: $appointmentId")
    val appointmentViewModel = remember { AppointmentViewModelImproved() }
    val appointments by appointmentViewModel.appointments.collectAsStateWithLifecycle()
    val isLoading by appointmentViewModel.isLoading.collectAsStateWithLifecycle()
    
    // Buscar la cita por ID
    val appointment = remember(appointmentId, appointments) {
        android.util.Log.d("NEXOGO_EDIT", "🔍 Buscando cita con ID: $appointmentId")
        android.util.Log.d("NEXOGO_EDIT", "🔍 Citas disponibles: ${appointments.size}")
        appointments.forEach { apt ->
            android.util.Log.d("NEXOGO_EDIT", "🔍 Cita disponible - ID: ${apt.id}, Mascota: ${apt.patientName}")
        }
        appointments.find { it.id == appointmentId }
    }
    
    if (appointment == null) {
        // Mostrar pantalla de carga o error
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cita no encontrada")
                    Text("ID buscado: $appointmentId")
                    Text("Total citas: ${appointments.size}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }
    
    android.util.Log.d("NEXOGO_EDIT", "✅ Cita encontrada: ${appointment.patientName}")
    
    // Usar la pantalla de edición existente con la cita encontrada
    EditAppointmentScreenContent(
        appointment = appointment,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAppointmentScreenContent(
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
    
    // Función para manejar el guardado
    fun handleSave() {
        if (isSaving) return
        
        isSaving = true
        saveError = null
        
        // Crear la cita actualizada
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
        
        // Actualizar la cita completa
        android.util.Log.d("NEXOGO_EDIT", "💾 Guardando cambios de la cita: ${updatedAppointment.id}")
        android.util.Log.d("NEXOGO_EDIT", "💾 Mascota: ${updatedAppointment.patientName}")
        android.util.Log.d("NEXOGO_EDIT", "💾 Fecha: ${updatedAppointment.dateTime}")
        android.util.Log.d("NEXOGO_EDIT", "💾 Motivo: ${updatedAppointment.reason}")
        android.util.Log.d("NEXOGO_EDIT", "💾 Estado: ${updatedAppointment.status}")
        
        // Usar coroutine scope del ViewModel con Dispatchers.Main para navegación
        kotlinx.coroutines.GlobalScope.launch {
            try {
                appointmentViewModel.updateAppointment(updatedAppointment)
                android.util.Log.d("NEXOGO_EDIT", "✅ Cita actualizada exitosamente")
                
                // Navegar en el hilo principal
                withContext(Dispatchers.Main) {
                    onNavigateBack()
                }
            } catch (e: Exception) {
                android.util.Log.e("NEXOGO_EDIT", "❌ Error al guardar: ${e.message}", e)
                
                // Actualizar UI en el hilo principal
                withContext(Dispatchers.Main) {
                    saveError = "Error al guardar: ${e.message}"
                    isSaving = false
                }
            }
        }
    }
    
    // Lista de mascotas de ejemplo (en una app real, esto vendría del ViewModel)
    val pets = remember {
        listOf(
            SimplePet("1", "Max", "Perro", "Golden Retriever"),
            SimplePet("2", "Luna", "Gato", "Persa"),
            SimplePet("3", "Rocky", "Perro", "Bulldog")
        )
    }
    
    // Inicializar mascota seleccionada
    LaunchedEffect(appointment) {
        selectedPet = pets.find { it.name == appointment.patientName }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Cita") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información de la cita actual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Editando Cita",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mascota: ${appointment.patientName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Fecha: ${appointment.dateTime.toDate()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Selector de mascota
            ExposedDropdownMenuBox(
                expanded = showPetDropdown,
                onExpandedChange = { showPetDropdown = !showPetDropdown }
            ) {
                OutlinedTextField(
                    value = selectedPet?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mascota") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = showPetDropdown,
                    onDismissRequest = { showPetDropdown = false }
                ) {
                    pets.forEach { pet ->
                        DropdownMenuItem(
                            text = { Text("${pet.name} - ${pet.species}") },
                            onClick = {
                                selectedPet = pet
                                showPetDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Selector de fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Día
                ExposedDropdownMenuBox(
                    expanded = showDayPicker,
                    onExpandedChange = { showDayPicker = !showDayPicker }
                ) {
                    OutlinedTextField(
                        value = selectedDate.date.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDayPicker) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
                    onExpandedChange = { showMonthPicker = !showMonthPicker }
                ) {
                    OutlinedTextField(
                        value = (selectedDate.month + 1).toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mes") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthPicker) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
                    onExpandedChange = { showYearPicker = !showYearPicker }
                ) {
                    OutlinedTextField(
                        value = (selectedDate.year + 1900).toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Año") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearPicker) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
            
            // Selector de hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = showTimePicker,
                    onExpandedChange = { showTimePicker = !showTimePicker }
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", selectedHour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimePicker) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
                    onExpandedChange = { showMinutePicker = !showMinutePicker }
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", selectedMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Minutos") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMinutePicker) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
            
            // Campo de motivo
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Motivo de la cita") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // Campo de notas
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // Selector de estado
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusPicker) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
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
            
            // Mostrar error si existe
            if (saveError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = saveError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("Guardando...")
                        }
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}
