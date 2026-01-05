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
import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.ui.components.ProductCard
import com.example.nexogo.modules.inventory.utils.InventoryUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    products: List<Product>,
    categories: List<com.example.nexogo.modules.inventory.model.Category>,
    isLoading: Boolean,
    error: String?,
    searchQuery: String,
    selectedCategoryId: String?,
    showLowStockOnly: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onLowStockFilter: (Boolean) -> Unit,
    onProductEdit: (Product) -> Unit,
    onProductDelete: (Product) -> Unit,
    onProductImageClick: (Product) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateBack: () -> Unit,
    canEdit: Boolean = true,
    canDelete: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    
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
                    if (canEdit) {
                        IconButton(onClick = onNavigateToCreate) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar producto"
                            )
                        }
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
            InventoryStatsCard(
                products = products,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filtros y búsqueda
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("Buscar productos") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Filtros
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Selector de categoría
                        CategorySelector(
                            categories = categories,
                            selectedCategoryId = selectedCategoryId,
                            onCategorySelected = onCategorySelected,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Filtro de stock bajo
                        FilterChip(
                            selected = showLowStockOnly,
                            onClick = { onLowStockFilter(!showLowStockOnly) },
                            label = { Text("Stock Bajo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Stock bajo"
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de productos
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
                        Text("Cargando inventario...")
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
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (products.isEmpty()) {
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
                        if (canEdit) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onNavigateToCreate) {
                                Text("Agregar primer producto")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onEdit = onProductEdit,
                            onDelete = { 
                                productToDelete = product
                                showDeleteDialog = true
                            },
                            onImageClick = onProductImageClick,
                            canEdit = canEdit,
                            canDelete = canDelete
                        )
                    }
                }
            }
        }
        
        // Diálogo de confirmación de eliminación
        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false
                    productToDelete = null
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
                    Text("¿Estás seguro de que quieres eliminar el producto \"${productToDelete?.name}\"? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            productToDelete?.let { onProductDelete(it) }
                            showDeleteDialog = false
                            productToDelete = null
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
                            productToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun InventoryStatsCard(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    val stats = InventoryUtils.getInventoryStats(products)
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen del Inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Productos",
                    value = stats.totalProducts.toString(),
                    icon = Icons.Default.Inventory
                )
                
                StatItem(
                    label = "Valor Total",
                    value = InventoryUtils.formatPriceCOP(stats.totalValue),
                    icon = Icons.Default.AttachMoney
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Stock Bajo",
                    value = stats.lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    isWarning = stats.lowStockCount > 0
                )
                
                StatItem(
                    label = "Sin Stock",
                    value = stats.outOfStockCount.toString(),
                    icon = Icons.Default.Error,
                    isError = stats.outOfStockCount > 0
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isWarning: Boolean = false,
    isError: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = when {
                isError -> MaterialTheme.colorScheme.error
                isWarning -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
