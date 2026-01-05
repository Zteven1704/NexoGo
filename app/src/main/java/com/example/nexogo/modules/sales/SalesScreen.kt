package com.example.nexogo.modules.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexogo.core.models.PaymentMethod
import com.example.nexogo.core.models.PaymentStatus
import java.util.*

/**
 * Pantalla principal de ventas y servicios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateSale: () -> Unit,
    viewModel: SalesViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    
    val sales by viewModel.filteredSales.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    // Actualizar búsqueda cuando cambie el query
    LaunchedEffect(searchQuery) {
        viewModel.searchSales(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💰 Ventas y Servicios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onNavigateToCreateSale) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateSale,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de búsqueda
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar ventas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            // Indicador de carga
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Estadísticas
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${sales.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ventas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$${String.format("%.0f", viewModel.getTotalSales())}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total Vendido",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${sales.count { it.paymentStatus == PaymentStatus.PAID }}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pagadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Lista de ventas
            if (sales.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💰 No hay ventas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registra la primera venta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sales) { sale ->
                        SaleCard(
                            sale = sale,
                            onEdit = { 
                                // TODO: Implementar edición
                            },
                            onDelete = { 
                                viewModel.deleteSale(sale.id)
                            },
                            onUpdatePaymentStatus = { status ->
                                viewModel.updatePaymentStatus(sale.id, status)
                            },
                            onDownloadInvoice = {
                                // TODO: Implementar descarga de factura
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de venta individual
 */
@Composable
fun SaleCard(
    sale: com.example.nexogo.core.models.Sale,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdatePaymentStatus: (PaymentStatus) -> Unit,
    onDownloadInvoice: () -> Unit
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
                Text(
                    text = "Venta #${sale.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onDownloadInvoice) {
                        Icon(Icons.Default.Download, contentDescription = "Descargar Factura")
                    }
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
                text = "Paciente: ${sale.patientName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Dueño: ${sale.ownerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Fecha: ${sale.createdAt.toDate().date}/${sale.createdAt.toDate().month + 1}/${sale.createdAt.toDate().year + 1900}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Items de la venta
            if (sale.items.isNotEmpty()) {
                Text(
                    text = "Productos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                sale.items.take(3).forEach { item ->
                    Text(
                        text = "• ${item.productName} x${item.quantity} = $${String.format("%.2f", item.totalPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sale.items.size > 3) {
                    Text(
                        text = "... y ${sale.items.size - 3} más",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Servicios
            if (sale.services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Servicios:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                sale.services.take(3).forEach { service ->
                    Text(
                        text = "• ${service.name} = $${String.format("%.2f", service.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sale.services.size > 3) {
                    Text(
                        text = "... y ${sale.services.size - 3} más",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${String.format("%.2f", sale.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Estado de pago
                FilterChip(
                    onClick = { 
                        val newStatus = when (sale.paymentStatus) {
                            PaymentStatus.PENDING -> PaymentStatus.PAID
                            PaymentStatus.PAID -> PaymentStatus.CANCELLED
                            PaymentStatus.CANCELLED -> PaymentStatus.PENDING
                            else -> PaymentStatus.PENDING
                        }
                        onUpdatePaymentStatus(newStatus)
                    },
                    label = { Text(sale.paymentStatus.name) },
                    selected = false,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = when (sale.paymentStatus) {
                            PaymentStatus.PENDING -> MaterialTheme.colorScheme.primaryContainer
                            PaymentStatus.PAID -> Color.Green.copy(alpha = 0.2f)
                            PaymentStatus.CANCELLED -> Color.Red.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Método: ${sale.paymentMethod.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Pantalla de creación de venta
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleScreen(
    patientId: String,
    patientName: String,
    ownerId: String,
    ownerName: String,
    onNavigateBack: () -> Unit,
    onSaleSaved: () -> Unit,
    viewModel: SalesViewModel = viewModel()
) {
    var selectedProducts by remember { mutableStateOf<List<com.example.nexogo.core.models.Product>>(emptyList()) }
    var selectedServices by remember { mutableStateOf<List<String>>(emptyList()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    
    val availableServices = listOf(
        "Consulta General",
        "Vacunación",
        "Cirugía",
        "Esterilización",
        "Eutanasia",
        "Examen de Laboratorio"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta") },
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
            // Información del paciente
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información del Paciente",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Paciente: $patientName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Dueño: $ownerName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Selección de productos
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Productos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (products.isEmpty()) {
                        Text(
                            text = "No hay productos disponibles",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        products.forEach { product ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", product.unitPrice)} - Stock: ${product.quantity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Checkbox(
                                    checked = selectedProducts.contains(product),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedProducts = selectedProducts + product
                                        } else {
                                            selectedProducts = selectedProducts.filter { it.id != product.id }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Selección de servicios
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Servicios",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    availableServices.forEach { service ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = service,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Checkbox(
                                checked = selectedServices.contains(service),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedServices = selectedServices + service
                                    } else {
                                        selectedServices = selectedServices.filter { it != service }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Método de pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = paymentMethod.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Método de Pago") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            PaymentMethod.values().forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method.name) },
                                    onClick = {
                                        paymentMethod = method
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Notas
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Notas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
            
            // Resumen de la venta
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Resumen de la Venta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val totalProducts = selectedProducts.sumOf { it.unitPrice }
                    val totalServices = selectedServices.size * 50.0 // Precio fijo por servicio
                    val totalAmount = totalProducts + totalServices
                    
                    Text(
                        text = "Productos: $${String.format("%.2f", totalProducts)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Servicios: $${String.format("%.2f", totalServices)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Total: $${String.format("%.2f", totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Mensaje de estado
            if (message.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        color = if (message.contains("Error")) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Botón de guardar
            Button(
                onClick = {
                    if (selectedProducts.isNotEmpty() || selectedServices.isNotEmpty()) {
                        val saleItems = selectedProducts.map { product ->
                            com.example.nexogo.core.models.SaleItem(
                                productId = product.id,
                                productName = product.name,
                                quantity = 1,
                                unitPrice = product.unitPrice,
                                totalPrice = product.unitPrice
                            )
                        }
                        
                        val services = selectedServices.map { service ->
                            com.example.nexogo.core.models.VeterinaryService(
                                serviceId = com.example.nexogo.core.FirebaseRepository().generateId(),
                                name = service,
                                description = "Servicio veterinario",
                                price = 50.0
                            )
                        }
                        
                        viewModel.createSale(
                            patientId = patientId,
                            patientName = patientName,
                            ownerId = ownerId,
                            ownerName = ownerName,
                            items = saleItems,
                            services = services,
                            paymentMethod = paymentMethod,
                            notes = notes,
                            createdBy = "current_user" // TODO: Obtener del usuario actual
                        )
                        
                        onSaleSaved()
                    }
                },
                enabled = !isLoading && (selectedProducts.isNotEmpty() || selectedServices.isNotEmpty()),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Crear Venta")
            }
        }
    }
}
