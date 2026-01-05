package com.example.nexogo.ui.screens.sales

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
import com.example.nexogo.model.Sale
import com.example.nexogo.model.PaymentStatus
import com.example.nexogo.viewmodel.FirebaseSalesViewModel
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateSale: () -> Unit,
    onNavigateToSaleDetail: (String) -> Unit,
    onNavigateToReports: () -> Unit,
    salesViewModel: FirebaseSalesViewModel = FirebaseSalesViewModel()
) {
    val sales by salesViewModel.sales.collectAsStateWithLifecycle()
    val isLoading by salesViewModel.isLoading.collectAsStateWithLifecycle()
    val message by salesViewModel.message.collectAsStateWithLifecycle()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Clear message when it changes
    LaunchedEffect(message) {
        message?.let {
            salesViewModel.clearMessage()
        }
    }
    
    val filteredSales = remember(sales, selectedFilter, searchQuery) {
        var filtered = sales
        
        when (selectedFilter) {
            "Pagadas" -> filtered = filtered.filter { it.paymentStatus == PaymentStatus.PAID }
            "Pendientes" -> filtered = filtered.filter { it.paymentStatus == PaymentStatus.PENDING }
            "Anuladas" -> filtered = filtered.filter { it.paymentStatus == PaymentStatus.CANCELLED }
        }
        
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { sale ->
                sale.saleNumber.contains(searchQuery, ignoreCase = true) ||
                sale.clientName.contains(searchQuery, ignoreCase = true) ||
                sale.professionalName.contains(searchQuery, ignoreCase = true)
            }
        }
        
        filtered.sortedByDescending { it.date }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas y Servicios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { onNavigateToReports() }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Reportes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreateSale() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva venta")
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
            // Search bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por número, cliente o profesional...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                }
            }
            
            // Summary cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                SummaryCard(
                    title = "Total Ventas",
                    value = "$${String.format("%.2f", sales.sumOf { it.total })}",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                
                SummaryCard(
                    title = "Pendientes",
                    value = sales.count { it.paymentStatus == PaymentStatus.PENDING }.toString(),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                }
            }
            
            // Sales list
            if (filteredSales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = "Sin ventas",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay ventas registradas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Toca el botón + para registrar la primera venta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    }
                }
            } else {
                items(filteredSales) { sale ->
                    SaleCard(
                        sale = sale,
                        onClick = { onNavigateToSaleDetail(sale.id) }
                    )
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtrar ventas") },
            text = {
                Column {
                    listOf("Todas", "Pagadas", "Pendientes", "Anuladas").forEach { filter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(filter)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) {
                    Text("Aplicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SaleCard(
    sale: Sale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Venta #${sale.saleNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sale.clientName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Dr. ${sale.professionalName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.2f", sale.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PaymentStatusChip(status = sale.paymentStatus)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(sale.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${sale.items.size} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sale.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusChip(status: PaymentStatus) {
    val (text, color) = when (status) {
                PaymentStatus.PAID -> "Pagado" to MaterialTheme.colorScheme.primary
                PaymentStatus.PENDING -> "Pendiente" to MaterialTheme.colorScheme.secondary
                PaymentStatus.CANCELLED -> "Anulado" to MaterialTheme.colorScheme.error
                PaymentStatus.REFUNDED -> "Reembolsado" to MaterialTheme.colorScheme.tertiary
                PaymentStatus.PARTIAL -> "Parcial" to MaterialTheme.colorScheme.secondary
    }
    
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
