package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.sales.data.SalesRepository
import com.example.nexogo.modules.sales.model.Service
import com.example.nexogo.modules.sales.model.ServiceCategory
import com.example.nexogo.modules.sales.viewmodel.SalesViewModel
import com.example.nexogo.modules.sales.utils.SalesUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firebaseRepository = remember { FirebaseRepository() }
    val salesRepository = remember { SalesRepository(firebaseRepository) }
    val viewModel = remember { SalesViewModel(salesRepository) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditServiceDialog by remember { mutableStateOf(false) }
    var showDeleteServiceDialog by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var serviceToDelete by remember { mutableStateOf<Service?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadServices()
        viewModel.loadServiceCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Gestión de Servicios",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Agregar categoría"
                        )
                    }
                    IconButton(onClick = { showAddServiceDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar servicio"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Información
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Información",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Servicios Veterinarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestiona los servicios que ofreces como radiografías, exámenes, eutanasia, cirugías, etc. Organízalos por categorías para facilitar su búsqueda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de servicios
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando servicios...")
                    }
                }
            } else if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (uiState.services.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "Sin servicios",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay servicios registrados",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddServiceDialog = true }) {
                            Text("Crear primer servicio")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.services) { service ->
                        ServiceCard(
                            service = service,
                            onEdit = { 
                                selectedService = service
                                showEditServiceDialog = true
                            },
                            onDelete = { 
                                serviceToDelete = service
                                showDeleteServiceDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar servicio
    if (showAddServiceDialog) {
        ServiceDialog(
            service = null,
            categories = uiState.serviceCategories,
            onSave = { name, description, category, price, duration ->
                coroutineScope.launch {
                    val service = Service(
                        name = name,
                        description = description,
                        category = category,
                        price = price,
                        duration = duration,
                        createdBy = "current_user" // TODO: Obtener del usuario actual
                    )
                    viewModel.createService(service)
                    showAddServiceDialog = false
                }
            },
            onDismiss = { showAddServiceDialog = false }
        )
    }
    
    // Diálogo para editar servicio
    if (showEditServiceDialog && selectedService != null) {
        ServiceDialog(
            service = selectedService,
            categories = uiState.serviceCategories,
            onSave = { name, description, category, price, duration ->
                coroutineScope.launch {
                    val updatedService = selectedService!!.copy(
                        name = name,
                        description = description,
                        category = category,
                        price = price,
                        duration = duration
                    )
                    viewModel.updateService(selectedService!!.id, updatedService)
                    showEditServiceDialog = false
                    selectedService = null
                }
            },
            onDismiss = { 
                showEditServiceDialog = false
                selectedService = null
            }
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteServiceDialog && serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteServiceDialog = false
                serviceToDelete = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Advertencia",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmar Eliminación",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar el servicio \"${serviceToDelete?.name}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteService(serviceToDelete!!.id)
                            showDeleteServiceDialog = false
                            serviceToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteServiceDialog = false
                        serviceToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para agregar categoría
    if (showAddCategoryDialog) {
        ServiceCategoryDialog(
            category = null,
            onSave = { name, description ->
                coroutineScope.launch {
                    val category = ServiceCategory(
                        name = name,
                        description = description
                    )
                    viewModel.createServiceCategory(category)
                    showAddCategoryDialog = false
                }
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
}

@Composable
fun ServiceCard(
    service: Service,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (service.description.isNotEmpty()) {
                    Text(
                        text = service.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Categoría: ${service.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Precio: ${SalesUtils.formatPriceCOP(service.price)} • Duración: ${service.duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDialog(
    service: Service?,
    categories: List<ServiceCategory>,
    onSave: (String, String, String, Double, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(service?.category ?: "") }
    var price by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(service?.duration?.toString() ?: "") }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    val validationErrors = remember(name, description, selectedCategory, price, duration) {
        mutableMapOf<String, String>().apply {
            if (name.isBlank()) this["name"] = "El nombre es obligatorio"
            if (description.isBlank()) this["description"] = "La descripción es obligatoria"
            if (selectedCategory.isBlank()) this["category"] = "Debe seleccionar una categoría"
            if (price.isBlank() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
                this["price"] = "El precio debe ser mayor a 0"
            }
            if (duration.isBlank() || duration.toIntOrNull() == null || duration.toInt() <= 0) {
                this["duration"] = "La duración debe ser mayor a 0"
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (service != null) "Editar Servicio" else "Nuevo Servicio",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del servicio *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && validationErrors.containsKey("name"),
                    supportingText = if (showValidationErrors && validationErrors.containsKey("name")) {
                        { Text(validationErrors["name"]!!) }
                    } else null
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = showValidationErrors && validationErrors.containsKey("description"),
                    supportingText = if (showValidationErrors && validationErrors.containsKey("description")) {
                        { Text(validationErrors["description"]!!) }
                    } else null
                )
                
                // Selector de categoría
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = showValidationErrors && validationErrors.containsKey("category"),
                        supportingText = if (showValidationErrors && validationErrors.containsKey("category")) {
                            { Text(validationErrors["category"]!!) }
                        } else null
                    )
                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = { }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category.name }
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && validationErrors.containsKey("price"),
                        supportingText = if (showValidationErrors && validationErrors.containsKey("price")) {
                            { Text(validationErrors["price"]!!) }
                        } else null
                    )
                    
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duración (min) *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && validationErrors.containsKey("duration"),
                        supportingText = if (showValidationErrors && validationErrors.containsKey("duration")) {
                            { Text(validationErrors["duration"]!!) }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validationErrors.isEmpty()) {
                        onSave(name, description, selectedCategory, price.toDouble(), duration.toInt())
                    } else {
                        showValidationErrors = true
                    }
                }
            ) {
                Text(if (service != null) "Actualizar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ServiceCategoryDialog(
    category: ServiceCategory?,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    val validationErrors = remember(name, description) {
        mutableMapOf<String, String>().apply {
            if (name.isBlank()) this["name"] = "El nombre es obligatorio"
            if (description.isBlank()) this["description"] = "La descripción es obligatoria"
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (category != null) "Editar Categoría" else "Nueva Categoría",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && validationErrors.containsKey("name"),
                    supportingText = if (showValidationErrors && validationErrors.containsKey("name")) {
                        { Text(validationErrors["name"]!!) }
                    } else null
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = showValidationErrors && validationErrors.containsKey("description"),
                    supportingText = if (showValidationErrors && validationErrors.containsKey("description")) {
                        { Text(validationErrors["description"]!!) }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validationErrors.isEmpty()) {
                        onSave(name, description)
                    } else {
                        showValidationErrors = true
                    }
                }
            ) {
                Text(if (category != null) "Actualizar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
