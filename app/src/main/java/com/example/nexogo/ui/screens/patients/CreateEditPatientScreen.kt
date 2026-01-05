package com.example.nexogo.ui.screens.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.model.SimplePet
import com.example.nexogo.viewmodel.PatientViewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditPatientScreen(
    patientId: String? = null, // null para crear, no null para editar
    onNavigateBack: () -> Unit,
    patientViewModel: PatientViewModel = PatientViewModel.getInstance(LocalContext.current)
) {
    val isEditing = patientId != null
    val patients by patientViewModel.patients.collectAsStateWithLifecycle()
    
    // Obtener el paciente a editar
    val patientToEdit = if (isEditing && patientId != null) {
        patients.find { it.id == patientId }
    } else null
    
    // Datos del dueño - inicializar con datos del paciente si se está editando
    var ownerName by remember { mutableStateOf(patientToEdit?.name ?: "") }
    var ownerPhone by remember { mutableStateOf(patientToEdit?.phone ?: "") }
    var ownerEmail by remember { mutableStateOf(patientToEdit?.email ?: "") }
    var ownerAddress by remember { mutableStateOf(patientToEdit?.address ?: "") }
    
    // Datos de la mascota actual
    var petName by remember { mutableStateOf("") }
    var petSpecies by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("") }
    
    // Lista de mascotas - inicializar con mascotas del paciente si se está editando
    var pets by remember { mutableStateOf<List<SimplePet>>(patientToEdit?.pets ?: emptyList()) }
    
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showSpeciesDropdown by remember { mutableStateOf(false) }
    var showGenderDropdown by remember { mutableStateOf(false) }
    var editingPetIndex by remember { mutableStateOf<Int?>(null) }
    
    val species = listOf("Perro", "Gato", "Conejo", "Hamster", "Pájaro", "Tortuga", "Otro")
    val genders = listOf("Macho", "Hembra", "Desconocido")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Paciente" else "Nuevo Paciente") },
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
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = "Paciente",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isEditing) "✏️ Editar Paciente" else "➕ Nuevo Paciente",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Datos del dueño y sus mascotas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Datos del dueño
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
                    Text(
                        text = "👤 Datos del Dueño",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ownerAddress,
                        onValueChange = { ownerAddress = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            // Mascotas
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
                            text = "🐾 Mascotas",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Button(
                            onClick = { 
                                // Limpiar campos y marcar como nueva mascota
                                petName = ""
                                petSpecies = ""
                                petBreed = ""
                                petAge = ""
                                petGender = ""
                                editingPetIndex = null
                                showAddPetDialog = true 
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar mascota")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (pets.isNotEmpty()) {
                        pets.forEachIndexed { index, pet ->
                            PetCard(
                                pet = pet,
                                onEdit = {
                                    // Cargar datos de la mascota para editar
                                    petName = pet.name
                                    petSpecies = pet.species
                                    petBreed = pet.breed
                                    petAge = pet.age.toString()
                                    petGender = "Desconocido" // Valor por defecto
                                    showAddPetDialog = true
                                    // Marcar que estamos editando una mascota existente
                                    editingPetIndex = index
                                },
                                onDelete = {
                                    pets = pets.filterIndexed { i, _ -> i != index }
                                }
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "No hay mascotas registradas. Haz clic en 'Agregar' para añadir una.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Botón de guardar
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
                            if (ownerName.isNotBlank() && ownerPhone.isNotBlank()) {
                                val patient = SimplePatient(
                                    id = if (isEditing) patientId!! else "patient_${System.currentTimeMillis()}",
                                    name = ownerName,
                                    phone = ownerPhone,
                                    email = ownerEmail,
                                    address = ownerAddress,
                                    pets = pets
                                )
                                
                                if (isEditing) {
                                    patientViewModel.updatePatient(patient)
                                } else {
                                    patientViewModel.addPatient(patient)
                                }
                                
                                showSuccessDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = ownerName.isNotBlank() && ownerPhone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = if (isEditing) "💾 Actualizar Paciente" else "💾 Guardar Paciente",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (ownerName.isBlank() || ownerPhone.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nombre y teléfono son obligatorios",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar mascota
    if (showAddPetDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddPetDialog = false
                // Limpiar campos
                petName = ""
                petSpecies = ""
                petBreed = ""
                petAge = ""
                petGender = ""
                editingPetIndex = null
            },
            title = { Text(if (editingPetIndex != null) "Editar Mascota" else "Agregar Mascota") },
            text = {
                Column {
                    OutlinedTextField(
                        value = petName,
                        onValueChange = { petName = it },
                        label = { Text("Nombre de la mascota") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector de especie
                    ExposedDropdownMenuBox(
                        expanded = showSpeciesDropdown,
                        onExpandedChange = { showSpeciesDropdown = !showSpeciesDropdown }
                    ) {
                        OutlinedTextField(
                            value = petSpecies,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSpeciesDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Especie") },
                            singleLine = true
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showSpeciesDropdown,
                            onDismissRequest = { showSpeciesDropdown = false }
                        ) {
                            species.forEach { species ->
                                DropdownMenuItem(
                                    text = { Text(species) },
                                    onClick = {
                                        petSpecies = species
                                        showSpeciesDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = petBreed,
                        onValueChange = { petBreed = it },
                        label = { Text("Raza") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = petAge,
                        onValueChange = { petAge = it },
                        label = { Text("Edad (años)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector de género
                    ExposedDropdownMenuBox(
                        expanded = showGenderDropdown,
                        onExpandedChange = { showGenderDropdown = !showGenderDropdown }
                    ) {
                        OutlinedTextField(
                            value = petGender,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGenderDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Género") },
                            singleLine = true
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showGenderDropdown,
                            onDismissRequest = { showGenderDropdown = false }
                        ) {
                            genders.forEach { gender ->
                                DropdownMenuItem(
                                    text = { Text(gender) },
                                    onClick = {
                                        petGender = gender
                                        showGenderDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (petName.isNotBlank() && petSpecies.isNotBlank()) {
                            val newPet = SimplePet(
                                id = if (editingPetIndex != null) pets[editingPetIndex!!].id else "pet_${System.currentTimeMillis()}",
                                name = petName,
                                species = petSpecies,
                                breed = petBreed,
                                age = petAge.toIntOrNull() ?: 0
                            )
                            
                            if (editingPetIndex != null) {
                                // Editar mascota existente
                                val updatedPets = pets.toMutableList()
                                updatedPets[editingPetIndex!!] = newPet
                                pets = updatedPets
                            } else {
                                // Agregar nueva mascota
                                pets = pets + newPet
                            }
                            
                            // Limpiar campos
                            petName = ""
                            petSpecies = ""
                            petBreed = ""
                            petAge = ""
                            petGender = ""
                            editingPetIndex = null
                            
                            showAddPetDialog = false
                        }
                    }
                ) {
                    Text(if (editingPetIndex != null) "Actualizar" else "Agregar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddPetDialog = false
                        // Limpiar campos
                        petName = ""
                        petSpecies = ""
                        petBreed = ""
                        petAge = ""
                        petGender = ""
                        editingPetIndex = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text(if (isEditing) "Paciente Actualizado" else "Paciente Guardado") },
            text = { Text("El paciente ha sido ${if (isEditing) "actualizado" else "registrado"} exitosamente.") },
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

@Composable
private fun PetCard(
    pet: SimplePet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${pet.species} - ${pet.breed} (${pet.age} años)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar mascota",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar mascota",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
