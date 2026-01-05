package com.example.nexogo.modules.inventory.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.inventory.data.InventoryRepository
import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.model.Category
import com.example.nexogo.modules.inventory.viewmodel.InventoryViewModel
import com.example.nexogo.modules.inventory.utils.InventoryUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryEditScreen(
    productId: String? = null,
    onNavigateBack: () -> Unit,
    onProductSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firebaseRepository = remember { FirebaseRepository() }
    val inventoryRepository = remember { InventoryRepository(firebaseRepository) }
    val viewModel = remember { InventoryViewModel(inventoryRepository) }
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estados del formulario
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf("") }
    var unitPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showValidationErrors by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingProduct by remember { mutableStateOf(false) }
    
    // Cargar categorías
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }
    
    // Cargar producto si se está editando
    LaunchedEffect(productId) {
        android.util.Log.d("NEXOGO_INVENTORY_EDIT", "ProductId recibido: $productId")
        productId?.let { id ->
            android.util.Log.d("NEXOGO_INVENTORY_EDIT", "Cargando producto con ID: $id")
            isLoadingProduct = true
            viewModel.loadProduct(id)
        }
    }
    
    // Actualizar campos cuando se carga el producto
    LaunchedEffect(uiState.currentProduct) {
        uiState.currentProduct?.let { product ->
            android.util.Log.d("NEXOGO_INVENTORY_EDIT", "Producto cargado: ${product.name}")
            name = product.name
            description = product.description
            categoryId = product.categoryId
            unitPrice = product.unitPrice.toString()
            quantity = product.quantity.toString()
            isLoadingProduct = false
        }
    }
    
    // Selector de imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            imageUri = it
            coroutineScope.launch {
                viewModel.uploadProductImage(productId ?: "temp", it)
            }
        }
    }
    
    // Validación
    val validationErrors = remember(name, description, categoryId, unitPrice, quantity) {
        mutableMapOf<String, String>().apply {
            if (name.isBlank()) this["name"] = "El nombre es obligatorio"
            if (description.isBlank()) this["description"] = "La descripción es obligatoria"
            if (categoryId.isBlank()) this["categoryId"] = "Debe seleccionar una categoría"
            if (unitPrice.isBlank() || unitPrice.toDoubleOrNull() == null || unitPrice.toDouble() <= 0) {
                this["unitPrice"] = "El precio debe ser mayor a 0"
            }
            if (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() < 0) {
                this["quantity"] = "La cantidad no puede ser negativa"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (productId != null) "Editar Producto" else "Nuevo Producto",
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
            // Indicador de carga si está cargando el producto
            if (isLoadingProduct && productId != null) {
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
                            text = "Cargando datos del producto...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Información básica
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del producto *") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showValidationErrors && validationErrors.containsKey("name"),
                        supportingText = if (showValidationErrors && validationErrors.containsKey("name")) {
                            { Text(validationErrors["name"]!!) }
                        } else null
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    CategorySelector(
                        categories = uiState.categories,
                        selectedCategoryId = categoryId,
                        onCategorySelected = { categoryId = it ?: "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Categoría *",
                        placeholder = "Seleccionar categoría"
                    )
                }
            }
            
            // Precio y cantidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Precio y Stock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unitPrice,
                            onValueChange = { unitPrice = it },
                            label = { Text("Precio unitario *") },
                            modifier = Modifier.weight(1f),
                            isError = showValidationErrors && validationErrors.containsKey("unitPrice"),
                            supportingText = if (showValidationErrors && validationErrors.containsKey("unitPrice")) {
                                { Text(validationErrors["unitPrice"]!!) }
                            } else null
                        )
                        
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Cantidad *") },
                            modifier = Modifier.weight(1f),
                            isError = showValidationErrors && validationErrors.containsKey("quantity"),
                            supportingText = if (showValidationErrors && validationErrors.containsKey("quantity")) {
                                { Text(validationErrors["quantity"]!!) }
                            } else null
                        )
                    }
                }
            }
            
            // Imagen del producto
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Imagen del Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Preview de imagen
                    if (imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Botón para cambiar imagen
                            IconButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Cambiar imagen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // Botón para seleccionar imagen
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Agregar imagen"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Imagen")
                        }
                    }
                    
                    if (uiState.isUploading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = uiState.uploadProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Subiendo imagen...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            isSaving = true
                            val newProduct = Product(
                                id = productId ?: "",
                                name = name,
                                description = description,
                                categoryId = categoryId,
                                categoryName = uiState.categories.find { it.id == categoryId }?.name ?: "",
                                unitPrice = unitPrice.toDouble(),
                                quantity = quantity.toInt(),
                                imageUrl = "" // Se actualizará después de subir la imagen
                            )
                            
                            coroutineScope.launch {
                                if (productId != null) {
                                    viewModel.updateProduct(productId, newProduct)
                                } else {
                                    viewModel.createProduct(newProduct)
                                }
                                isSaving = false
                                onProductSaved()
                            }
                        } else {
                            showValidationErrors = true
                        }
                    },
                    enabled = !isSaving && !uiState.isUploading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSaving) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardando...")
                        }
                    } else {
                        Text(if (productId != null) "Actualizar" else "Crear")
                    }
                }
            }
            
            // Mostrar error si existe
            if (uiState.error != null) {
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
            }
        }
    }
}