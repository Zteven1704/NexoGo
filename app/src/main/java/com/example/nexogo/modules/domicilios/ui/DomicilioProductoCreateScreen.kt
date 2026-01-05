package com.example.nexogo.modules.domicilios.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.nexogo.model.DatosProducto
import com.example.nexogo.model.ProductoDomicilio
import com.example.nexogo.modules.domicilios.DomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Pantalla para crear solicitud de domicilio de productos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomicilioProductoCreateScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val viewModel = remember { DomicilioViewModel(DomicilioRepository()) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var nombreUsuario by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    var listaProductos by remember { mutableStateOf<List<ProductoDomicilio>>(emptyList()) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    
    // Cargar dirección del perfil si existe
    LaunchedEffect(Unit) {
        // TODO: Cargar dirección del perfil del usuario desde Firestore
    }
    
    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        successMessage?.let {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Domicilio de Productos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = nombreUsuario,
                        onValueChange = { nombreUsuario = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )
                    
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección de entrega") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        placeholder = { Text("Dirección completa donde se entregará") }
                    )
                }
            }
            
            // Lista de productos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Productos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAddProductDialog = true },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar producto")
                        }
                    }
                    
                    if (listaProductos.isEmpty()) {
                        Text(
                            text = "No hay productos agregados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        listaProductos.forEachIndexed { index, producto ->
                            ProductoItemCard(
                                producto = producto,
                                onDelete = {
                                    listaProductos = listaProductos.filterIndexed { i, _ -> i != index }
                                }
                            )
                        }
                    }
                }
            }
            
            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !isLoading,
                placeholder = { Text("Notas adicionales sobre la solicitud...") }
            )
            
            // Foto adjunta (opcional)
            FotoSection(
                fotoUri = fotoUri,
                onFotoChange = { fotoUri = it },
                enabled = !isLoading
            )
            
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = {
                    if (validarFormularioProducto(nombreUsuario, telefono, direccion, listaProductos)) {
                        val datosProducto = DatosProducto(
                            listaProductos = listaProductos,
                            observaciones = observaciones
                        )
                        viewModel.createDomicilioProducto(
                            nombreUsuario = nombreUsuario,
                            telefono = telefono,
                            direccion = direccion,
                            datosProducto = datosProducto,
                            fotoUri = fotoUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && validarFormularioProducto(nombreUsuario, telefono, direccion, listaProductos)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Solicitud")
                }
            }
        }
    }
    
    // Dialog para agregar producto
    if (showAddProductDialog) {
        AddProductDialog(
            onDismiss = { showAddProductDialog = false },
            onAdd = { producto ->
                listaProductos = listaProductos + producto
                showAddProductDialog = false
            }
        )
    }
}

@Composable
fun ProductoItemCard(
    producto: ProductoDomicilio,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre.ifBlank { "Producto sin nombre" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Cantidad: ${producto.cantidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (producto.observaciones.isNotBlank()) {
                    Text(
                        text = producto.observaciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (ProductoDomicilio) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("1") }
    var observaciones by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { if (it.all { char -> char.isDigit() }) cantidad = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && cantidad.isNotBlank()) {
                        onAdd(
                            ProductoDomicilio(
                                nombre = nombre,
                                cantidad = cantidad.toIntOrNull() ?: 1,
                                observaciones = observaciones
                            )
                        )
                    }
                },
                enabled = nombre.isNotBlank() && cantidad.isNotBlank()
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

fun validarFormularioProducto(
    nombreUsuario: String,
    telefono: String,
    direccion: String,
    listaProductos: List<ProductoDomicilio>
): Boolean {
    return nombreUsuario.isNotBlank() &&
            telefono.isNotBlank() &&
            direccion.isNotBlank() &&
            listaProductos.isNotEmpty()
}




