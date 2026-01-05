package com.example.nexogo.modules.inventory.ui

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
import com.example.nexogo.modules.inventory.data.InventoryRepository
import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.model.Category
import com.example.nexogo.modules.inventory.viewmodel.InventoryViewModel
import com.example.nexogo.modules.inventory.utils.InventoryUtils
import com.example.nexogo.modules.inventory.ui.components.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val firebaseRepository = remember { FirebaseRepository() }
    val inventoryRepository = remember { InventoryRepository(firebaseRepository) }
    val viewModel = remember { InventoryViewModel(inventoryRepository) }
    
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
        viewModel.loadCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Inventario",
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
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Gestionar categorías"
                        )
                    }
                    IconButton(onClick = onNavigateToCreate) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar producto"
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
            // Estadísticas del inventario
            if (uiState.products.isNotEmpty()) {
                InventoryStatsCard(
                    products = uiState.products,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Lista de productos
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
                        Text("Cargando inventario...")
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
            } else if (uiState.products.isEmpty()) {
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Sin productos",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay productos en el inventario",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToCreate) {
                            Text("Agregar primer producto")
                        }
                    }
                }
            } else {
                // Lista de productos
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { product -> 
                                android.util.Log.d("NEXOGO_INVENTORY_MAIN", "Editando producto: ${product.id}")
                                onNavigateToEdit(product.id) 
                            },
                            onDelete = { /* TODO: Eliminar producto */ },
                            onImageClick = { /* TODO: Ver imagen */ },
                            canEdit = true,
                            canDelete = true
                        )
                    }
                }
            }
        }
    }
}

