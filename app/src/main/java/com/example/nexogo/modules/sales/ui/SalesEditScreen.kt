package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.sales.data.SalesRepository
import com.example.nexogo.modules.sales.model.*
import com.example.nexogo.modules.sales.viewmodel.SalesViewModel
import com.example.nexogo.modules.sales.utils.SalesUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesEditScreen(
    saleId: String? = null,
    onNavigateBack: () -> Unit,
    onSaleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firebaseRepository = remember { FirebaseRepository() }
    val salesRepository = remember { SalesRepository(firebaseRepository) }
    val viewModel = remember { SalesViewModel(salesRepository) }
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estados del formulario
    var clientName by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingSale by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SaleItem?>(null) }
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SaleItem?>(null) }
    
    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }
    
    // Cargar venta si se está editando
    LaunchedEffect(saleId) {
        android.util.Log.d("NEXOGO_SALES_EDIT", "SaleId recibido: $saleId")
        saleId?.let { id ->
            android.util.Log.d("NEXOGO_SALES_EDIT", "Cargando venta con ID: $id")
            isLoadingSale = true
            viewModel.loadSale(id)
        }
    }
    
    // Actualizar campos cuando se carga la venta
    LaunchedEffect(uiState.currentSale) {
        uiState.currentSale?.let { sale ->
            android.util.Log.d("NEXOGO_SALES_EDIT", "Venta cargada: ${sale.saleNumber}")
            clientName = sale.clientName
            clientPhone = sale.clientPhone
            clientEmail = sale.clientEmail
            paymentMethod = sale.paymentMethod
            notes = sale.notes
            saleItems = sale.items
            isLoadingSale = false
        }
    }
    
    // Validación
    val validationErrors = remember(clientName, saleItems) {
        mutableMapOf<String, String>().apply {
            if (clientName.isBlank()) this["clientName"] = "El nombre del cliente es obligatorio"
            if (saleItems.isEmpty()) this["items"] = "Debe agregar al menos un item"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (saleId != null) "Editar Venta" else "Nueva Venta",
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de carga si está cargando la venta
            if (isLoadingSale && saleId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Cargando datos de la venta...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Información del cliente
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información del Cliente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Nombre del cliente *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showValidationErrors && validationErrors.containsKey("clientName"),
                        supportingText = if (showValidationErrors && validationErrors.containsKey("clientName")) {
                            { Text(validationErrors["clientName"]!!) }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = clientEmail,
                        onValueChange = { clientEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Items de la venta
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
                            text = "Items de la Venta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Button(
                            onClick = { showAddItemDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar item",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (saleItems.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Sin items",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay items agregados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            saleItems.forEach { item ->
                                SaleItemCard(
                                    item = item,
                                    onEdit = { 
                                        selectedItem = item
                                        showEditItemDialog = true
                                    },
                                    onDelete = { 
                                        itemToDelete = item
                                        showDeleteItemDialog = true
                                    }
                                )
                            }
                        }
                    }
                    
                    if (showValidationErrors && validationErrors.containsKey("items")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationErrors["items"]!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Información de pago
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Método de pago
                    Text(
                        text = "Método de Pago",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.values().forEach { method ->
                            FilterChip(
                                onClick = { paymentMethod = method },
                                label = { Text(SalesUtils.getPaymentMethodText(method)) },
                                selected = paymentMethod == method,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notas
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notas adicionales") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
            
            // Resumen de la venta
            if (saleItems.isNotEmpty()) {
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
                            text = "Resumen de la Venta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val subtotal = saleItems.sumOf { it.totalPrice }
                        val tax = subtotal * 0.19 // 19% IVA
                        val total = subtotal + tax
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:")
                            Text(SalesUtils.formatPriceCOP(subtotal))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IVA (19%):")
                            Text(SalesUtils.formatPriceCOP(tax))
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = SalesUtils.formatPriceCOP(total),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        if (validationErrors.isEmpty()) {
                            coroutineScope.launch {
                                try {
                                    isSaving = true
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "=== INICIANDO GUARDADO DE VENTA ===")
                                    
                                    // Calcular totales
                                    val subtotal = saleItems.sumOf { it.totalPrice }
                                    val tax = subtotal * 0.19 // 19% IVA
                                    val total = subtotal + tax
                                    
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Subtotal: $subtotal")
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Tax: $tax")
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Total: $total")
                                    
                                    // Generar número de venta único
                                    val saleNumber = "V-${System.currentTimeMillis()}"
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Número de venta: $saleNumber")
                                    
                                    // Crear objeto Sale
                                    val sale = Sale(
                                        id = saleId ?: UUID.randomUUID().toString(),
                                        saleNumber = saleNumber,
                                        clientId = "", // TODO: Obtener ID del cliente si existe
                                        clientName = clientName,
                                        clientPhone = clientPhone,
                                        clientEmail = clientEmail,
                                        items = saleItems,
                                        subtotal = subtotal,
                                        tax = tax,
                                        total = total,
                                        paymentMethod = paymentMethod,
                                        status = SaleStatus.COMPLETED,
                                        notes = notes,
                                        createdBy = "", // TODO: Obtener ID del usuario actual
                                        createdByName = "", // TODO: Obtener nombre del usuario actual
                                        createdAt = if (saleId != null) uiState.currentSale?.createdAt ?: com.google.firebase.Timestamp.now() else com.google.firebase.Timestamp.now(),
                                        updatedAt = com.google.firebase.Timestamp.now()
                                    )
                                    
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Objeto Sale creado: ${sale.saleNumber}")
                                    
                                    // Guardar en Firebase
                                    if (saleId != null) {
                                        android.util.Log.d("NEXOGO_SALES_SAVE", "Actualizando venta existente...")
                                        viewModel.updateSale(saleId, sale)
                                    } else {
                                        android.util.Log.d("NEXOGO_SALES_SAVE", "Creando nueva venta...")
                                        viewModel.createSale(sale)
                                    }
                                    
                                    android.util.Log.d("NEXOGO_SALES_SAVE", "Venta guardada exitosamente")
                                    isSaving = false
                                    onSaleSaved()
                                    
                                } catch (e: Exception) {
                                    android.util.Log.e("NEXOGO_SALES_SAVE", "Error guardando venta: ${e.message}", e)
                                    isSaving = false
                                }
                            }
                        } else {
                            showValidationErrors = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (saleId != null) "Actualizar" else "Guardar")
                }
            }
        }
    }
    
    // Diálogo para agregar item - VERSIÓN COMPLETAMENTE PRIMITIVA
    if (showAddItemDialog) {
        CompletelyPrimitiveDialog(
            onSave = { itemName, description, quantity, unitPrice, itemType ->
                android.util.Log.d("NEXOGO_SALES", "=== INICIO onSave COMPLETAMENTE PRIMITIVO ===")
                try {
                    android.util.Log.d("NEXOGO_SALES", "Datos recibidos:")
                    android.util.Log.d("NEXOGO_SALES", "itemName: '$itemName'")
                    android.util.Log.d("NEXOGO_SALES", "description: '$description'")
                    android.util.Log.d("NEXOGO_SALES", "quantity: $quantity")
                    android.util.Log.d("NEXOGO_SALES", "unitPrice: $unitPrice")
                    android.util.Log.d("NEXOGO_SALES", "itemType: $itemType")
                    
                    // Crear el item de forma segura usando solo tipos primitivos
                    val itemId = UUID.randomUUID().toString()
                    android.util.Log.d("NEXOGO_SALES", "ID generado: $itemId")
                    
                    val totalPrice = quantity * unitPrice
                    android.util.Log.d("NEXOGO_SALES", "Total calculado: $totalPrice")
                    
                    // Crear el item paso a paso para evitar problemas
                    android.util.Log.d("NEXOGO_SALES", "Creando SaleItem paso a paso...")
                    
                    // Intentar crear el SaleItem de forma segura
                    var newItem: SaleItem? = null
                    try {
                        android.util.Log.d("NEXOGO_SALES", "Paso 1: Creando SaleItem con parámetros básicos...")
                        newItem = SaleItem(
                            id = itemId,
                            type = itemType, // Usar el tipo seleccionado
                            itemName = itemName,
                            description = description,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            totalPrice = totalPrice
                        )
                        android.util.Log.d("NEXOGO_SALES", "Paso 1: SaleItem creado exitosamente")
                    } catch (e: Exception) {
                        android.util.Log.e("NEXOGO_SALES", "Paso 1: Error creando SaleItem: ${e.message}", e)
                        android.util.Log.e("NEXOGO_SALES", "Paso 1: Stack trace: ${e.stackTraceToString()}")
                    }
                    
                    if (newItem != null) {
                        android.util.Log.d("NEXOGO_SALES", "SaleItem creado exitosamente")
                        
                        // Actualizar la lista de forma segura
                        android.util.Log.d("NEXOGO_SALES", "Paso 2: Actualizando lista...")
                        val currentItems = saleItems.toMutableList()
                        currentItems.add(newItem)
                        saleItems = currentItems
                        
                        android.util.Log.d("NEXOGO_SALES", "Paso 2: Lista actualizada, nuevo tamaño: ${saleItems.size}")
                        
                        // Cerrar el diálogo
                        android.util.Log.d("NEXOGO_SALES", "Paso 3: Cerrando diálogo...")
                        showAddItemDialog = false
                        android.util.Log.d("NEXOGO_SALES", "Paso 3: Diálogo cerrado")
                        android.util.Log.d("NEXOGO_SALES", "=== FIN onSave EXITOSO ===")
                    } else {
                        android.util.Log.e("NEXOGO_SALES", "No se pudo crear el SaleItem")
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("NEXOGO_SALES", "ERROR en onSave: ${e.message}", e)
                    android.util.Log.e("NEXOGO_SALES", "Stack trace: ${e.stackTraceToString()}")
                }
            },
            onDismiss = { 
                android.util.Log.d("NEXOGO_SALES", "Diálogo cancelado por usuario")
                showAddItemDialog = false 
            }
        )
    }
    
    // Diálogo para editar item
    if (showEditItemDialog && selectedItem != null) {
        AddItemDialog(
            item = selectedItem,
            services = uiState.services,
            onSave = { itemName, description, quantity, unitPrice, itemType ->
                val updatedItem = selectedItem!!.copy(
                    type = itemType,
                    itemName = itemName,
                    description = description,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    totalPrice = quantity * unitPrice
                )
                saleItems = saleItems.map { if (it.id == selectedItem!!.id) updatedItem else it }
                showEditItemDialog = false
                selectedItem = null
            },
            onDismiss = { 
                showEditItemDialog = false
                selectedItem = null
            }
        )
    }
    
    // Diálogo de confirmación de eliminación de item
    if (showDeleteItemDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteItemDialog = false
                itemToDelete = null
            },
            title = {
                Text("Eliminar Item")
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar este item de la venta?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        saleItems = saleItems.filter { it.id != itemToDelete!!.id }
                        showDeleteItemDialog = false
                        itemToDelete = null
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
                        showDeleteItemDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SaleItemCard(
    item: SaleItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = if (item.type == SaleItemType.PRODUCT) "Producto" else "Servicio",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = false,
                        modifier = Modifier.height(24.dp)
                    )
                }
                Text(
                    text = "${item.quantity} x ${SalesUtils.formatPriceCOP(item.unitPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: ${SalesUtils.formatPriceCOP(item.totalPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    item: SaleItem? = null,
    services: List<Service>,
    onSave: (String, String, Int, Double, SaleItemType) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf(item?.itemName ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var unitPrice by remember { mutableStateOf(item?.unitPrice?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(item?.type ?: SaleItemType.PRODUCT) }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item != null) "Editar Item" else "Agregar Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del item
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nombre del item *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && itemName.isBlank(),
                    supportingText = if (showValidationErrors && itemName.isBlank()) {
                        { Text("El nombre es obligatorio") }
                    } else null
                )
                
                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = showValidationErrors && description.isBlank(),
                    supportingText = if (showValidationErrors && description.isBlank()) {
                        { Text("La descripción es obligatoria") }
                    } else null
                )
                
                // Cantidad y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() <= 0),
                        supportingText = if (showValidationErrors && (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() <= 0)) {
                            { Text("La cantidad debe ser mayor a 0") }
                        } else null
                    )
                    
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Precio unitario *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && (unitPrice.isBlank() || unitPrice.toDoubleOrNull() == null || unitPrice.toDouble() <= 0),
                        supportingText = if (showValidationErrors && (unitPrice.isBlank() || unitPrice.toDoubleOrNull() == null || unitPrice.toDouble() <= 0)) {
                            { Text("El precio debe ser mayor a 0") }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val quantityInt = quantity.toIntOrNull() ?: 0
                        val unitPriceDouble = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        if (itemName.isBlank() || description.isBlank() || quantityInt <= 0 || unitPriceDouble <= 0) {
                            showValidationErrors = true
                        } else {
                            onSave(
                                itemName,
                                description,
                                quantityInt,
                                unitPriceDouble,
                                selectedType
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NEXOGO_SALES", "Error al guardar item: ${e.message}")
                        showValidationErrors = true
                    }
                }
            ) {
                Text(if (item != null) "Actualizar" else "Agregar")
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
fun SimpleAddItemDialog(
    onSave: (String, String, Int, Double, SaleItemType) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SaleItemType.PRODUCT) }
    var showValidationErrors by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Agregar Item")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tipo de item
                Text(
                    text = "Tipo de Item",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedType = SaleItemType.PRODUCT },
                        label = { Text("Producto") },
                        selected = selectedType == SaleItemType.PRODUCT,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { selectedType = SaleItemType.SERVICE },
                        label = { Text("Servicio") },
                        selected = selectedType == SaleItemType.SERVICE,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Nombre del item
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showValidationErrors && itemName.isBlank(),
                    supportingText = if (showValidationErrors && itemName.isBlank()) {
                        { Text("El nombre es obligatorio") }
                    } else null
                )
                
                // Descripción
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = showValidationErrors && description.isBlank(),
                    supportingText = if (showValidationErrors && description.isBlank()) {
                        { Text("La descripción es obligatoria") }
                    } else null
                )
                
                // Cantidad y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() <= 0),
                        supportingText = if (showValidationErrors && (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() <= 0)) {
                            { Text("La cantidad debe ser mayor a 0") }
                        } else null
                    )
                    
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f),
                        isError = showValidationErrors && (unitPrice.isBlank() || unitPrice.toDoubleOrNull() == null || unitPrice.toDouble() <= 0),
                        supportingText = if (showValidationErrors && (unitPrice.isBlank() || unitPrice.toDoubleOrNull() == null || unitPrice.toDouble() <= 0)) {
                            { Text("El precio debe ser mayor a 0") }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        android.util.Log.d("NEXOGO_SALES", "Intentando agregar item...")
                        android.util.Log.d("NEXOGO_SALES", "itemName: '$itemName'")
                        android.util.Log.d("NEXOGO_SALES", "description: '$description'")
                        android.util.Log.d("NEXOGO_SALES", "quantity: '$quantity'")
                        android.util.Log.d("NEXOGO_SALES", "unitPrice: '$unitPrice'")
                        android.util.Log.d("NEXOGO_SALES", "selectedType: $selectedType")
                        
                        val qty = quantity.toIntOrNull() ?: 0
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        android.util.Log.d("NEXOGO_SALES", "qty parsed: $qty")
                        android.util.Log.d("NEXOGO_SALES", "price parsed: $price")
                        
                        if (itemName.isBlank()) {
                            android.util.Log.d("NEXOGO_SALES", "Error: itemName is blank")
                            showValidationErrors = true
                        } else if (description.isBlank()) {
                            android.util.Log.d("NEXOGO_SALES", "Error: description is blank")
                            showValidationErrors = true
                        } else if (qty <= 0) {
                            android.util.Log.d("NEXOGO_SALES", "Error: qty <= 0")
                            showValidationErrors = true
                        } else if (price <= 0) {
                            android.util.Log.d("NEXOGO_SALES", "Error: price <= 0")
                            showValidationErrors = true
                        } else {
                            android.util.Log.d("NEXOGO_SALES", "Validación exitosa, llamando onSave...")
                            onSave(itemName, description, qty, price, selectedType)
                            android.util.Log.d("NEXOGO_SALES", "onSave llamado exitosamente")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("NEXOGO_SALES", "Error al guardar item: ${e.message}", e)
                        showValidationErrors = true
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
