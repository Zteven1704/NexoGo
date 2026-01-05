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
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.model.SimplePet
import com.example.nexogo.viewmodel.AuthViewModel
import com.example.nexogo.viewmodel.PatientViewModel
import com.example.nexogo.viewmodel.FirebaseAppointmentViewModel
import androidx.compose.ui.platform.LocalContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAppointmentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPatients: () -> Unit = {},
    authViewModel: AuthViewModel = AuthViewModel.getInstance(),
    patientViewModel: PatientViewModel = PatientViewModel.getInstance(LocalContext.current),
    appointmentViewModel: FirebaseAppointmentViewModel = FirebaseAppointmentViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val patients by patientViewModel.patients.collectAsStateWithLifecycle()
    
    // Obtener todas las mascotas de forma reactiva
    val allPets = remember(patients) {
        patients.flatMap { patient ->
            patient.pets.map { pet ->
                pet.copy(name = "${pet.name} (${patient.name})")
            }
        }
    }
    
    var selectedPet by remember { mutableStateOf<SimplePet?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showPetDropdown by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showMinutePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Cita") },
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
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅 Programar Nueva Cita",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete los datos para programar la cita",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Selector de mascota
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "🐾 Seleccionar Mascota",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (allPets.isEmpty()) {
                        // Mostrar mensaje cuando no hay mascotas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⚠️ No hay mascotas registradas",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Primero debes registrar pacientes y sus mascotas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = onNavigateToPatients,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Ir a Gestión de Pacientes")
                                }
                            }
                        }
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = showPetDropdown,
                            onExpandedChange = { showPetDropdown = !showPetDropdown }
                        ) {
                            OutlinedTextField(
                                value = selectedPet?.name ?: "Seleccionar mascota",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPetDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Mascota") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showPetDropdown,
                                onDismissRequest = { showPetDropdown = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                allPets.forEach { pet ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(
                                                    text = pet.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${pet.species} - ${pet.breed}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
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
                }
            }
            
            // Selector de fecha
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📅 Fecha de la Cita",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Fecha actual mostrada
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Fecha seleccionada: ${selectedDate.date}/${selectedDate.month + 1}/${selectedDate.year + 1900}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selectores de fecha en columnas para mejor responsividad
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Primera fila: Día y Mes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Selector de día
                            ExposedDropdownMenuBox(
                                expanded = showDayPicker,
                                onExpandedChange = { showDayPicker = !showDayPicker }
                            ) {
                                OutlinedTextField(
                                    value = selectedDate.date.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDayPicker) },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .menuAnchor(),
                                    label = { Text("Día") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showDayPicker,
                                    onDismissRequest = { showDayPicker = false },
                                    modifier = Modifier
                                        .heightIn(max = 300.dp)
                                        .widthIn(min = 100.dp)
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
                            
                            // Selector de mes
                            ExposedDropdownMenuBox(
                                expanded = showMonthPicker,
                                onExpandedChange = { showMonthPicker = !showMonthPicker }
                            ) {
                                OutlinedTextField(
                                    value = (selectedDate.month + 1).toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthPicker) },
                                    modifier = Modifier
                                        .width(100.dp)
                                        .menuAnchor(),
                                    label = { Text("Mes") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showMonthPicker,
                                    onDismissRequest = { showMonthPicker = false },
                                    modifier = Modifier
                                        .heightIn(max = 300.dp)
                                        .widthIn(min = 120.dp)
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
                        }
                        
                        // Segunda fila: Año
                        ExposedDropdownMenuBox(
                            expanded = showYearPicker,
                            onExpandedChange = { showYearPicker = !showYearPicker }
                        ) {
                            OutlinedTextField(
                                value = (selectedDate.year + 1900).toString(),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showYearPicker) },
                                modifier = Modifier
                                    .width(120.dp)
                                    .menuAnchor(),
                                label = { Text("Año") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showYearPicker,
                                onDismissRequest = { showYearPicker = false },
                                modifier = Modifier
                                    .heightIn(max = 300.dp)
                                    .widthIn(min = 120.dp)
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
                }
            }
            
            // Selector de hora
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "⏰ Hora de la Cita",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Hora actual mostrada
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Hora seleccionada: ${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Selector de hora - más compacto
                        ExposedDropdownMenuBox(
                            expanded = showTimePicker,
                            onExpandedChange = { showTimePicker = !showTimePicker }
                        ) {
                            OutlinedTextField(
                                value = String.format("%02d", selectedHour),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimePicker) },
                                modifier = Modifier
                                    .weight(1f)
                                    .menuAnchor(),
                                label = { Text("Hora") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showTimePicker,
                                onDismissRequest = { showTimePicker = false },
                                modifier = Modifier.heightIn(max = 150.dp)
                            ) {
                                (8..18).forEach { hour ->
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
                        
                        // Selector de minutos - más compacto
                        ExposedDropdownMenuBox(
                            expanded = showMinutePicker,
                            onExpandedChange = { showMinutePicker = !showMinutePicker }
                        ) {
                            OutlinedTextField(
                                value = String.format("%02d", selectedMinute),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMinutePicker) },
                                modifier = Modifier
                                    .weight(1f)
                                    .menuAnchor(),
                                label = { Text("Minutos") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = showMinutePicker,
                                onDismissRequest = { showMinutePicker = false },
                                modifier = Modifier.heightIn(max = 150.dp)
                            ) {
                                listOf(0, 15, 30, 45).forEach { minute ->
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
            }
            
            // Notas adicionales
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📝 Notas Adicionales",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Descripción de la cita") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            
            // Botón de programar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { 
                            if (selectedPet != null) {
                                val calendar = Calendar.getInstance()
                                calendar.time = selectedDate
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                                calendar.set(Calendar.MINUTE, selectedMinute)
                                
                                val appointment = com.example.nexogo.model.Appointment(
                                    id = UUID.randomUUID().toString(), // Generar ID único
                                    patientId = "patient_${selectedPet!!.id}",
                                    patientName = selectedPet!!.name,
                                    ownerId = currentUser?.id ?: "owner_1",
                                    ownerName = currentUser?.name ?: "Dueño",
                                    vetId = currentUser?.id ?: "vet_1",
                                    vetName = currentUser?.name ?: "Veterinario",
                                    dateTime = com.google.firebase.Timestamp(calendar.time),
                                    notes = notes,
                                    reason = "Consulta general",
                                    status = com.example.nexogo.model.AppointmentStatus.SCHEDULED,
                                    createdAt = com.google.firebase.Timestamp.now(),
                                    updatedAt = com.google.firebase.Timestamp.now(),
                                    createdBy = currentUser?.id ?: "vet_1"
                                )
                                
                                // Log para debugging
                                println("DEBUG: Creando cita con ID: ${appointment.id}")
                                println("DEBUG: Mascota: ${appointment.patientName}")
                                println("DEBUG: Fecha: ${appointment.dateTime}")
                                
                                appointmentViewModel.saveAppointment(appointment)
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedPet != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (selectedPet != null) "✅ Programar Cita" else "⚠️ Selecciona una mascota",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (selectedPet == null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Debes seleccionar una mascota para continuar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Cita Programada") },
            text = { Text("La cita ha sido programada exitosamente.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}