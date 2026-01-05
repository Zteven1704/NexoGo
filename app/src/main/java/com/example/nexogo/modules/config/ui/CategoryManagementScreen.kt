package com.example.nexogo.modules.config.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.inventory.model.Category
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firebaseRepository = remember { FirebaseRepository() }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar categorías
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val result = firebaseRepository.getCollection("categories")
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                categories = dataList.mapNotNull { data ->
                    try {
                        Category(
                            id = data["id"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            isActive = data["isActive"] as? Boolean ?: true,
                            productCount = (data["productCount"] as? Number)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                error = "Error cargando categorías: ${result.exceptionOrNull()?.message}"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Gestión de Categorías",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar categoría"
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
                            imageVector = Icons.Default.Info,
                            contentDescription = "Información",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gestión de Categorías",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las categorías se usan para organizar los productos del inventario. Puedes crear, editar y eliminar categorías según tus necesidades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de categorías
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando categorías...")
                    }
                }
            } else if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (categories.isEmpty()) {
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
                            imageVector = Icons.Default.Category,
                            contentDescription = "Sin categorías",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay categorías creadas",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Crear primera categoría")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onEdit = { 
                                selectedCategory = category
                                showEditDialog = true
                            },
                            onDelete = { 
                                categoryToDelete = category
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo para agregar categoría
    if (showAddDialog) {
        CategoryDialog(
            category = null,
            onSave = { name, description ->
                coroutineScope.launch {
                    try {
                        val categoryData = mapOf(
                            "id" to name.lowercase().replace(" ", "_"),
                            "name" to name,
                            "description" to description,
                            "isActive" to true,
                            "productCount" to 0
                        )
                        val result = firebaseRepository.createDocument("categories", name.lowercase().replace(" ", "_"), categoryData)
                        if (result.isSuccess) {
                            showAddDialog = false
                            // Recargar categorías
                            val result = firebaseRepository.getCollection("categories")
                            if (result.isSuccess) {
                                val dataList = result.getOrNull() ?: emptyList()
                                categories = dataList.mapNotNull { data ->
                                    try {
                                        Category(
                                            id = data["id"] as? String ?: "",
                                            name = data["name"] as? String ?: "",
                                            description = data["description"] as? String ?: "",
                                            isActive = data["isActive"] as? Boolean ?: true,
                                            productCount = (data["productCount"] as? Number)?.toInt() ?: 0
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        } else {
                            error = "Error creando categoría: ${result.exceptionOrNull()?.message}"
                        }
                    } catch (e: Exception) {
                        error = "Error: ${e.message}"
                    }
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }
    
    // Diálogo para editar categoría
    if (showEditDialog && selectedCategory != null) {
        CategoryDialog(
            category = selectedCategory,
            onSave = { name, description ->
                coroutineScope.launch {
                    try {
                        val updateData = mapOf(
                            "name" to name,
                            "description" to description
                        )
                        val result = firebaseRepository.updateDocument("categories", selectedCategory!!.id, updateData)
                        if (result.isSuccess) {
                            showEditDialog = false
                            selectedCategory = null
                            // Recargar categorías
                            val result = firebaseRepository.getCollection("categories")
                            if (result.isSuccess) {
                                val dataList = result.getOrNull() ?: emptyList()
                                categories = dataList.mapNotNull { data ->
                                    try {
                                        Category(
                                            id = data["id"] as? String ?: "",
                                            name = data["name"] as? String ?: "",
                                            description = data["description"] as? String ?: "",
                                            isActive = data["isActive"] as? Boolean ?: true,
                                            productCount = (data["productCount"] as? Number)?.toInt() ?: 0
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                        } else {
                            error = "Error actualizando categoría: ${result.exceptionOrNull()?.message}"
                        }
                    } catch (e: Exception) {
                        error = "Error: ${e.message}"
                    }
                }
            },
            onDismiss = { 
                showEditDialog = false
                selectedCategory = null
            }
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog && categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                categoryToDelete = null
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
                Text("¿Estás seguro de que quieres eliminar la categoría \"${categoryToDelete?.name}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val result = firebaseRepository.deleteDocument("categories", categoryToDelete!!.id)
                                if (result.isSuccess) {
                                    showDeleteDialog = false
                                    categoryToDelete = null
                                    // Recargar categorías
                                    val result = firebaseRepository.getCollection("categories")
                                    if (result.isSuccess) {
                                        val dataList = result.getOrNull() ?: emptyList()
                                        categories = dataList.mapNotNull { data ->
                                            try {
                                                Category(
                                                    id = data["id"] as? String ?: "",
                                                    name = data["name"] as? String ?: "",
                                                    description = data["description"] as? String ?: "",
                                                    isActive = data["isActive"] as? Boolean ?: true,
                                                    productCount = (data["productCount"] as? Number)?.toInt() ?: 0
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }
                                } else {
                                    error = "Error eliminando categoría: ${result.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                error = "Error: ${e.message}"
                            }
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
                        showDeleteDialog = false
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: Category,
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
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (category.description.isNotEmpty()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Productos: ${category.productCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
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

@Composable
fun CategoryDialog(
    category: Category?,
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
