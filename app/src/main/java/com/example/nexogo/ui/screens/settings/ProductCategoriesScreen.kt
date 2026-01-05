package com.example.nexogo.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.viewmodel.ProductCategoryViewModel
import com.example.nexogo.viewmodel.ProductCategory
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCategoriesScreen(
    onNavigateBack: () -> Unit,
    categoryViewModel: ProductCategoryViewModel = ProductCategoryViewModel.getInstance(LocalContext.current)
) {
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val isLoading by categoryViewModel.isLoading.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<ProductCategory?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías de Productos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header info
            item {
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
                                Icons.Default.Category,
                                contentDescription = "Categorías",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Gestión de Categorías",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Administra las categorías de productos del inventario. Puedes crear, editar, eliminar y activar/desactivar categorías.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Categories list
            if (categories.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = "Sin categorías",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay categorías creadas",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Toca el botón + para agregar la primera categoría",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = {
                            selectedCategory = category
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedCategory = category
                            showDeleteDialog = true
                        },
                        onToggleStatus = {
                            categoryViewModel.toggleCategoryStatus(category.id)
                        }
                    )
                }
            }
        }
    }
    
    // Add category dialog
    if (showAddDialog) {
        AddEditCategoryDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, description ->
                categoryViewModel.addCategory(name, description)
                showAddDialog = false
            }
        )
    }
    
    // Edit category dialog
    if (showEditDialog && selectedCategory != null) {
        AddEditCategoryDialog(
            category = selectedCategory,
            onDismiss = { 
                showEditDialog = false
                selectedCategory = null
            },
            onSave = { name, description ->
                selectedCategory?.let { category ->
                    categoryViewModel.updateCategory(category.id, name, description)
                }
                showEditDialog = false
                selectedCategory = null
            }
        )
    }
    
    // Delete category dialog
    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedCategory = null
            },
            title = { Text("Eliminar Categoría") },
            text = { Text("¿Estás seguro de que quieres eliminar la categoría \"${selectedCategory?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCategory?.let { category ->
                            categoryViewModel.deleteCategory(category.id)
                        }
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    category: ProductCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
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
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = category.isActive,
                        onCheckedChange = { onToggleStatus() }
                    )
                    
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
}

@Composable
private fun AddEditCategoryDialog(
    category: ProductCategory?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Agregar Categoría" else "Editar Categoría") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, description) },
                enabled = name.isNotEmpty()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
